package edu.mtisw.autofixbackend.services;

import edu.mtisw.autofixbackend.entities.EngineType;
import edu.mtisw.autofixbackend.entities.VehicleEntity;
import edu.mtisw.autofixbackend.entities.VehicleType;
import edu.mtisw.autofixbackend.repositories.RepairRepository;
import edu.mtisw.autofixbackend.repositories.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    VehicleRepository vehicleRepository;

    @Mock
    RepairRepository repairRepository;

    @InjectMocks
    VehicleService vehicleService;

    private VehicleEntity newVehicle(String plate) {
        return new VehicleEntity(null, plate, "Toyota", "Corolla", VehicleType.SEDAN, 2015, EngineType.GASOLINA, 5);
    }

    @Test
    void whenGetVehicles_thenReturnsAll() {
        when(vehicleRepository.findAll()).thenReturn(List.of(newVehicle("ABCD12")));
        assertThat(vehicleService.getVehicles()).hasSize(1);
    }

    @Test
    void whenGetVehicleByIdExists_thenReturns() {
        VehicleEntity v = newVehicle("ABCD12");
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(v));
        assertThat(vehicleService.getVehicleById(1L)).isSameAs(v);
    }

    @Test
    void whenGetVehicleByIdMissing_thenThrowsNotFound() {
        when(vehicleRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> vehicleService.getVehicleById(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void whenGetVehicleByPlate_thenNormalizesPlate() {
        VehicleEntity v = newVehicle("ABCD12");
        when(vehicleRepository.findByPlate("ABCD12")).thenReturn(Optional.of(v));
        assertThat(vehicleService.getVehicleByPlate("abcd-12")).isSameAs(v);
    }

    @Test
    void whenGetVehicleByPlateMissing_thenThrows() {
        when(vehicleRepository.findByPlate("ZZZZ99")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> vehicleService.getVehicleByPlate("ZZZZ99")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void whenSaveValidVehicle_thenNormalizesAndSaves() {
        when(vehicleRepository.existsByPlate("ABCD12")).thenReturn(false);
        when(vehicleRepository.save(any(VehicleEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        VehicleEntity saved = vehicleService.saveVehicle(newVehicle(" abcd 12 "));

        assertThat(saved.getPlate()).isEqualTo("ABCD12");
        verify(vehicleRepository).save(saved);
    }

    @Test
    void whenSaveDuplicatedPlate_thenThrows() {
        when(vehicleRepository.existsByPlate("ABCD12")).thenReturn(true);
        assertThatThrownBy(() -> vehicleService.saveVehicle(newVehicle("ABCD12")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Ya existe");
    }

    @Test
    void whenSaveInvalidPlate_thenThrows() {
        assertThatThrownBy(() -> vehicleService.saveVehicle(newVehicle("AB1234")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Patente");
        assertThatThrownBy(() -> vehicleService.saveVehicle(newVehicle(null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenSaveWithoutBrand_thenThrows() {
        VehicleEntity v = newVehicle("ABCD12");
        v.setBrand(" ");
        assertThatThrownBy(() -> vehicleService.saveVehicle(v)).isInstanceOf(IllegalArgumentException.class);
        v.setBrand(null);
        assertThatThrownBy(() -> vehicleService.saveVehicle(v)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenSaveWithoutTypes_thenThrows() {
        VehicleEntity v = newVehicle("ABCD12");
        v.setEngineType(null);
        assertThatThrownBy(() -> vehicleService.saveVehicle(v)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenSaveInvalidYearOrSeats_thenThrows() {
        VehicleEntity v = newVehicle("ABCD12");
        v.setManufactureYear(1800);
        assertThatThrownBy(() -> vehicleService.saveVehicle(v)).isInstanceOf(IllegalArgumentException.class);
        v.setManufactureYear(2015);
        v.setSeats(0);
        assertThatThrownBy(() -> vehicleService.saveVehicle(v)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenUpdateVehicleSamePlate_thenSaves() {
        VehicleEntity current = newVehicle("ABCD12");
        current.setId(1L);
        VehicleEntity changed = newVehicle("ABCD12");
        changed.setId(1L);
        changed.setModel("Yaris");
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(current));
        when(vehicleRepository.save(changed)).thenReturn(changed);

        assertThat(vehicleService.updateVehicle(changed).getModel()).isEqualTo("Yaris");
    }

    @Test
    void whenUpdateVehicleToExistingPlate_thenThrows() {
        VehicleEntity current = newVehicle("ABCD12");
        current.setId(1L);
        VehicleEntity changed = newVehicle("WXYZ99");
        changed.setId(1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(current));
        when(vehicleRepository.existsByPlate("WXYZ99")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.updateVehicle(changed)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenDeleteVehicleWithoutRepairs_thenDeletes() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(newVehicle("ABCD12")));
        when(repairRepository.existsByVehicleId(1L)).thenReturn(false);

        assertThat(vehicleService.deleteVehicle(1L)).isTrue();
        verify(vehicleRepository).deleteById(1L);
    }

    @Test
    void whenDeleteVehicleWithRepairs_thenThrows() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(newVehicle("ABCD12")));
        when(repairRepository.existsByVehicleId(1L)).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.deleteVehicle(1L)).isInstanceOf(IllegalStateException.class);
        verify(vehicleRepository, never()).deleteById(any());
    }

    @Test
    void whenNormalizeNullPlate_thenNull() {
        assertThat(vehicleService.normalizePlate(null)).isNull();
    }
}
