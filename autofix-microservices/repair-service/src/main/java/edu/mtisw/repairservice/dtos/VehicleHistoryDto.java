package edu.mtisw.repairservice.dtos;

import edu.mtisw.repairservice.entities.RepairRecordEntity;
import edu.mtisw.repairservice.entities.VehicleInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleHistoryDto {
    private VehicleInfo vehicle;
    private List<RepairRecordEntity> repairs;
}
