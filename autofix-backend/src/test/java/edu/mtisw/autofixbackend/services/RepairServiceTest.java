package edu.mtisw.autofixbackend.services;

import edu.mtisw.autofixbackend.entities.*;
import edu.mtisw.autofixbackend.repositories.RepairRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairServiceTest {

    @Mock
    RepairRepository repairRepository;

    @Mock
    VehicleService vehicleService;

    @Mock
    BonusService bonusService;

    @Spy
    RepairCostService repairCostService = new RepairCostService();

    @InjectMocks
    RepairService repairService;

    private VehicleEntity toyota;

    @BeforeEach
    void setUp() {
        toyota = new VehicleEntity(1L, "ABCD12", "Toyota", "Corolla", VehicleType.SEDAN, 2015, EngineType.GASOLINA, 5);
    }

    private RepairEntity repair(int... types) {
        RepairEntity r = new RepairEntity();
        r.setVehicleId(1L);
        r.setEntryDate(LocalDate.of(2026, 10, 5));   // lunes
        r.setEntryTime(LocalTime.of(10, 0));
        r.setExitDate(LocalDate.of(2026, 10, 7));
        r.setExitTime(LocalTime.of(15, 0));
        r.setPickupDate(LocalDate.of(2026, 10, 9));  // 2 días de retraso
        r.setPickupTime(LocalTime.of(11, 0));
        r.setMileage(15_000);
        List<RepairDetailEntity> details = new ArrayList<>();
        for (int t : types) {
            details.add(new RepairDetailEntity(null, t, 0));
        }
        r.setDetails(details);
        return r;
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // HU3: cálculo del costo total (ejemplo completo documentado)
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @Test
    void whenCalculateCost_thenAppliesFormula() {
        RepairEntity r = repair(1, 5);   // 120.000 + 150.000 = 270.000
        when(repairRepository.countRepairsInPeriod(eq(1L), any(), any(), eq(-1L))).thenReturn(0L);

        repairService.calculateCost(r, toyota, 70_000);

        assertThat(r.getRepairsAmount()).isEqualTo(270_000);
        assertThat(r.getMileageSurcharge()).isEqualTo(18_900);    // 7%  (15.000 km, sedán)
        assertThat(r.getAgeSurcharge()).isEqualTo(24_300);        // 9%  (11 años, sedán)
        assertThat(r.getLateSurcharge()).isEqualTo(27_000);       // 10% (2 días x 5%)
        assertThat(r.getRepairsCountDiscount()).isZero();         // sin reparaciones previas
        assertThat(r.getDayDiscount()).isEqualTo(27_000);         // 10% lunes 10:00
        assertThat(r.getBonusDiscount()).isEqualTo(70_000);       // bono Toyota
        assertThat(r.getSubtotal()).isEqualTo(243_200);
        assertThat(r.getIva()).isEqualTo(46_208);                 // 19%
        assertThat(r.getTotalAmount()).isEqualTo(289_408);
        assertThat(r.getDetails().get(0).getPrice()).isEqualTo(120_000);
    }

    @Test
    void whenCalculateCostWithPreviousRepairs_thenAppliesCountDiscount() {
        RepairEntity r = repair(3);   // motor gasolina 350.000
        r.setId(7L);
        r.setEntryDate(LocalDate.of(2026, 10, 6));   // martes: sin descuento por día
        r.setExitDate(null);
        r.setExitTime(null);
        r.setPickupDate(null);
        r.setPickupTime(null);
        r.setMileage(1000);
        toyota.setManufactureYear(2024);
        when(repairRepository.countRepairsInPeriod(eq(1L), any(), any(), eq(7L))).thenReturn(3L);

        repairService.calculateCost(r, toyota, 0);

        assertThat(r.getRepairsCountDiscount()).isEqualTo(35_000);   // 10% (3 reparaciones, gasolina)
        assertThat(r.getSubtotal()).isEqualTo(315_000);
        assertThat(r.getTotalAmount()).isEqualTo(374_850);
    }

    @Test
    void whenDiscountsExceedAmount_thenSubtotalNotNegative() {
        RepairEntity r = repair(11);  // 80.000
        r.setMileage(0);
        r.setPickupDate(r.getExitDate());
        toyota.setManufactureYear(2026);
        when(repairRepository.countRepairsInPeriod(anyLong(), any(), any(), anyLong())).thenReturn(0L);

        repairService.calculateCost(r, toyota, 200_000);

        assertThat(r.getSubtotal()).isZero();
        assertThat(r.getTotalAmount()).isZero();
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // HU2: registro de reparaciones
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @Test
    void whenSaveRepairWithBonus_thenConsumesBonusAndSaves() {
        RepairEntity r = repair(1, 5, 5);   // reparación 5 repetida: se elimina el duplicado
        r.setApplyBonus(true);
        when(vehicleService.getVehicleById(1L)).thenReturn(toyota);
        when(repairRepository.countBonusesUsed(eq(1L), any(), any(), eq(-1L))).thenReturn(0L);
        when(bonusService.consumeBonus("Toyota", r.getEntryDate())).thenReturn(70_000L);
        when(repairRepository.save(any(RepairEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        RepairEntity saved = repairService.saveRepair(r);

        assertThat(saved.getDetails()).hasSize(2);
        assertThat(saved.getBonusDiscount()).isEqualTo(70_000);
        assertThat(saved.getTotalAmount()).isEqualTo(289_408);
    }

    @Test
    void whenSaveRepairSecondBonusSameMonth_thenThrows() {
        RepairEntity r = repair(1);
        r.setApplyBonus(true);
        when(vehicleService.getVehicleById(1L)).thenReturn(toyota);
        when(repairRepository.countBonusesUsed(eq(1L), any(), any(), anyLong())).thenReturn(1L);

        assertThatThrownBy(() -> repairService.saveRepair(r)).isInstanceOf(IllegalStateException.class);
        verify(bonusService, never()).consumeBonus(anyString(), any());
    }

    @Test
    void whenSaveRepairWithoutBonus_thenDoesNotConsume() {
        RepairEntity r = repair(2);
        when(vehicleService.getVehicleById(1L)).thenReturn(toyota);
        when(repairRepository.save(any(RepairEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        RepairEntity saved = repairService.saveRepair(r);

        assertThat(saved.getBonusDiscount()).isZero();
        verifyNoInteractions(bonusService);
    }

    @Test
    void whenSaveRepairInvalidData_thenThrows() {
        when(vehicleService.getVehicleById(1L)).thenReturn(toyota);

        RepairEntity noDate = repair(1);
        noDate.setEntryDate(null);
        assertThatThrownBy(() -> repairService.saveRepair(noDate)).hasMessageContaining("ingreso");

        RepairEntity noTime = repair(1);
        noTime.setEntryTime(null);
        assertThatThrownBy(() -> repairService.saveRepair(noTime)).isInstanceOf(IllegalArgumentException.class);

        RepairEntity noDetails = repair();
        assertThatThrownBy(() -> repairService.saveRepair(noDetails)).hasMessageContaining("al menos una");

        RepairEntity nullDetails = repair(1);
        nullDetails.setDetails(null);
        assertThatThrownBy(() -> repairService.saveRepair(nullDetails)).isInstanceOf(IllegalArgumentException.class);

        RepairEntity negativeKm = repair(1);
        negativeKm.setMileage(-1);
        assertThatThrownBy(() -> repairService.saveRepair(negativeKm)).hasMessageContaining("kilometraje");

        RepairEntity exitBefore = repair(1);
        exitBefore.setExitDate(LocalDate.of(2026, 10, 1));
        assertThatThrownBy(() -> repairService.saveRepair(exitBefore)).hasMessageContaining("salida");

        RepairEntity pickupBefore = repair(1);
        pickupBefore.setPickupDate(LocalDate.of(2026, 10, 6));
        assertThatThrownBy(() -> repairService.saveRepair(pickupBefore)).hasMessageContaining("retiro");

        RepairEntity pickupWithoutExit = repair(1);
        pickupWithoutExit.setExitDate(null);
        assertThatThrownBy(() -> repairService.saveRepair(pickupWithoutExit)).hasMessageContaining("retiro");
    }

    @Test
    void whenElectricVehicleExhaustRepair_thenThrows() {
        VehicleEntity tesla = new VehicleEntity(2L, "TSLA11", "Tesla", "Model 3", VehicleType.SEDAN, 2022, EngineType.ELECTRICO, 5);
        RepairEntity r = repair(6);
        r.setVehicleId(2L);
        when(vehicleService.getVehicleById(2L)).thenReturn(tesla);

        assertThatThrownBy(() -> repairService.saveRepair(r)).hasMessageContaining("no aplica");
    }

    @Test
    void whenExitWithoutTime_thenUsesMidnight() {
        RepairEntity r = repair(1);
        r.setExitTime(null);
        r.setPickupTime(null);
        when(vehicleService.getVehicleById(1L)).thenReturn(toyota);
        when(repairRepository.save(any(RepairEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(repairService.saveRepair(r).getTotalAmount()).isPositive();
    }

    @Test
    void whenUpdateRepairAddingBonus_thenConsumes() {
        RepairEntity current = repair(1);
        current.setId(5L);
        RepairEntity changed = repair(1);
        changed.setId(5L);
        changed.setApplyBonus(true);
        when(repairRepository.findById(5L)).thenReturn(Optional.of(current));
        when(vehicleService.getVehicleById(1L)).thenReturn(toyota);
        when(repairRepository.countBonusesUsed(eq(1L), any(), any(), eq(5L))).thenReturn(0L);
        when(bonusService.consumeBonus(eq("Toyota"), any())).thenReturn(70_000L);
        when(repairRepository.save(any(RepairEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(repairService.updateRepair(changed).getBonusDiscount()).isEqualTo(70_000);
    }

    @Test
    void whenUpdateRepairRemovingBonus_thenReturnsBonus() {
        RepairEntity current = repair(1);
        current.setId(5L);
        current.setBonusDiscount(70_000);
        RepairEntity changed = repair(1);
        changed.setId(5L);
        changed.setApplyBonus(false);
        when(repairRepository.findById(5L)).thenReturn(Optional.of(current));
        when(vehicleService.getVehicleById(1L)).thenReturn(toyota);
        when(repairRepository.save(any(RepairEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        RepairEntity updated = repairService.updateRepair(changed);

        assertThat(updated.getBonusDiscount()).isZero();
        verify(bonusService).returnBonus("Toyota", current.getEntryDate());
    }

    @Test
    void whenUpdateRepairKeepingBonus_thenKeepsAmount() {
        RepairEntity current = repair(1);
        current.setId(5L);
        current.setBonusDiscount(70_000);
        RepairEntity changed = repair(1);
        changed.setId(5L);
        changed.setApplyBonus(true);
        when(repairRepository.findById(5L)).thenReturn(Optional.of(current));
        when(vehicleService.getVehicleById(1L)).thenReturn(toyota);
        when(repairRepository.save(any(RepairEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(repairService.updateRepair(changed).getBonusDiscount()).isEqualTo(70_000);
        verifyNoInteractions(bonusService);
    }

    @Test
    void whenCalculateRepair_thenRecalculatesAndSaves() {
        RepairEntity current = repair(1);
        current.setId(5L);
        current.setBonusDiscount(10_000);
        when(repairRepository.findById(5L)).thenReturn(Optional.of(current));
        when(vehicleService.getVehicleById(1L)).thenReturn(toyota);
        when(repairRepository.save(any(RepairEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        RepairEntity calculated = repairService.calculateRepair(5L);

        assertThat(calculated.getRepairsAmount()).isEqualTo(120_000);
        assertThat(calculated.getBonusDiscount()).isEqualTo(10_000);
    }

    @Test
    void whenGetRepairs_thenDelegatesToRepository() {
        when(repairRepository.findAll()).thenReturn(List.of(repair(1)));
        when(repairRepository.findByVehicleIdOrderByEntryDateDesc(1L)).thenReturn(List.of(repair(1), repair(2)));

        assertThat(repairService.getRepairs()).hasSize(1);
        assertThat(repairService.getRepairsByVehicle(1L)).hasSize(2);
    }

    @Test
    void whenGetMissingRepair_thenThrows() {
        when(repairRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> repairService.getRepairById(99L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void whenDeleteRepairWithBonus_thenReturnsBonus() {
        RepairEntity current = repair(1);
        current.setId(5L);
        current.setBonusDiscount(70_000);
        when(repairRepository.findById(5L)).thenReturn(Optional.of(current));
        when(vehicleService.getVehicleById(1L)).thenReturn(toyota);

        assertThat(repairService.deleteRepair(5L)).isTrue();
        verify(bonusService).returnBonus("Toyota", current.getEntryDate());
        verify(repairRepository).deleteById(5L);
    }

    @Test
    void whenDeleteRepairWithoutBonus_thenOnlyDeletes() {
        RepairEntity current = repair(1);
        current.setId(5L);
        when(repairRepository.findById(5L)).thenReturn(Optional.of(current));

        repairService.deleteRepair(5L);

        verifyNoInteractions(bonusService);
        verify(repairRepository).deleteById(5L);
    }
}
