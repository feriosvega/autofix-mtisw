package edu.mtisw.reportservice.services;

import edu.mtisw.reportservice.dtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private static final String REPAIRS_URL = "http://repair-service/api/v1/repairs/report-data";
    private static final String TYPES_URL = "http://repair-list-service/api/v1/repair-types/";
    private static final List<String> VEHICLE_TYPES = List.of("SEDAN", "HATCHBACK", "SUV", "PICKUP", "FURGONETA");

    @Autowired private RestTemplate restTemplate;

    public Report1Dto report1(int month, int year) {
        validateMonth(month);
        List<RepairTypeDto> types = repairTypes();
        Map<Long, Report1RowDto> rows = new LinkedHashMap<>();
        Map<String, Long> totalsCount = zeroByVehicleType();
        Map<String, Long> totalsAmount = zeroByVehicleType();
        for (RepairTypeDto type : types) rows.put(type.getId(), new Report1RowDto(type.getId(), type.getName(),
                zeroByVehicleType(), zeroByVehicleType(), 0, 0));

        for (RepairReportDataDto repair : repairData()) {
            for (RepairReportDataDto.Detail detail : repair.getDetails()) {
                LocalDate date = LocalDate.parse(detail.getRepairDate());
                if (date.getYear() != year || date.getMonthValue() != month) continue;
                Report1RowDto row = rows.computeIfAbsent(detail.getRepairType(), id ->
                        new Report1RowDto(id, detail.getRepairName(), zeroByVehicleType(), zeroByVehicleType(), 0, 0));
                String vehicleType = repair.getVehicleType();
                if (!row.getCountsByVehicleType().containsKey(vehicleType))
                    throw new IllegalArgumentException("Tipo de vehículo desconocido: " + vehicleType);
                row.getCountsByVehicleType().merge(vehicleType, 1L, Long::sum);
                row.getAmountByVehicleType().merge(vehicleType, detail.getAmount(), Long::sum);
                row.setTotalCount(row.getTotalCount() + 1);
                row.setTotalAmount(row.getTotalAmount() + detail.getAmount());
                totalsCount.merge(vehicleType, 1L, Long::sum);
                totalsAmount.merge(vehicleType, detail.getAmount(), Long::sum);
            }
        }
        long totalCount = rows.values().stream().mapToLong(Report1RowDto::getTotalCount).sum();
        long totalAmount = rows.values().stream().mapToLong(Report1RowDto::getTotalAmount).sum();
        return new Report1Dto(year, month, VEHICLE_TYPES, new ArrayList<>(rows.values()),
                totalsCount, totalsAmount, totalCount, totalAmount);
    }

    public Report2Dto report2(int month, int year) {
        validateMonth(month);
        YearMonth target = YearMonth.of(year, month);
        List<YearMonth> selected = List.of(target.minusMonths(2), target.minusMonths(1), target);
        Map<Long, String> names = repairTypes().stream()
                .collect(Collectors.toMap(RepairTypeDto::getId, RepairTypeDto::getName, (a,b) -> a, TreeMap::new));
        Map<Long, Map<YearMonth, long[]>> values = new TreeMap<>();
        names.keySet().forEach(id -> values.put(id, new LinkedHashMap<>()));

        for (RepairReportDataDto repair : repairData()) {
            for (RepairReportDataDto.Detail detail : repair.getDetails()) {
                YearMonth detailMonth = YearMonth.from(LocalDate.parse(detail.getRepairDate()));
                if (!selected.contains(detailMonth)) continue;
                values.computeIfAbsent(detail.getRepairType(), id -> new LinkedHashMap<>());
                long[] aggregate = values.get(detail.getRepairType()).computeIfAbsent(detailMonth, ignored -> new long[2]);
                aggregate[0]++;
                aggregate[1] += detail.getAmount();
                names.putIfAbsent(detail.getRepairType(), detail.getRepairName());
            }
        }

        List<MonthValueDto> months = selected.stream().map(this::monthValue).toList();
        List<Report2RowDto> rows = values.entrySet().stream().map(entry -> {
            List<MonthValueDto> rowMonths = new ArrayList<>();
            for (int index = 0; index < selected.size(); index++) {
                YearMonth monthKey = selected.get(index);
                long[] value = entry.getValue().getOrDefault(monthKey, new long[2]);
                MonthValueDto dto = monthValue(monthKey);
                dto.setQuantity(value[0]);
                dto.setAmount(value[1]);
                if (index > 0) {
                    long[] previous = entry.getValue().getOrDefault(selected.get(index - 1), new long[2]);
                    dto.setDeltaQuantity(value[0] - previous[0]);
                    dto.setDeltaAmount(value[1] - previous[1]);
                    dto.setQuantityVariationPercent(variation(previous[0], value[0]));
                    dto.setAmountVariationPercent(variation(previous[1], value[1]));
                }
                rowMonths.add(dto);
            }
            return new Report2RowDto(entry.getKey(), names.get(entry.getKey()), rowMonths);
        }).toList();
        return new Report2Dto(year, month, months, rows);
    }

    private List<RepairTypeDto> repairTypes() {
        List<RepairTypeDto> body = restTemplate.exchange(TYPES_URL, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<RepairTypeDto>>() {}).getBody();
        return body == null ? List.of() : body;
    }

    private List<RepairReportDataDto> repairData() {
        List<RepairReportDataDto> body = restTemplate.exchange(REPAIRS_URL, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<RepairReportDataDto>>() {}).getBody();
        return body == null ? List.of() : body;
    }

    private Map<String, Long> zeroByVehicleType() {
        Map<String, Long> result = new LinkedHashMap<>();
        VEHICLE_TYPES.forEach(type -> result.put(type, 0L));
        return result;
    }

    private MonthValueDto monthValue(YearMonth value) {
        String label = value.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-CL"));
        label = label.substring(0, 1).toUpperCase(Locale.forLanguageTag("es-CL")) + label.substring(1);
        MonthValueDto dto = new MonthValueDto();
        dto.setYear(value.getYear());
        dto.setMonth(value.getMonthValue());
        dto.setLabel(label + " " + value.getYear());
        return dto;
    }

    private Double variation(long previous, long current) {
        if (previous == 0) return current == 0 ? 0.0 : null;
        return (current - previous) * 100.0 / previous;
    }

    private void validateMonth(int month) {
        if (month < 1 || month > 12) throw new IllegalArgumentException("El mes debe estar entre 1 y 12");
    }
}
