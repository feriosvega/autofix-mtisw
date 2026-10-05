package edu.mtisw.autofixbackend.services;

import edu.mtisw.autofixbackend.entities.BonusEntity;
import edu.mtisw.autofixbackend.repositories.BonusRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BonusServiceTest {

    @Mock
    BonusRepository bonusRepository;

    @InjectMocks
    BonusService bonusService;

    private BonusEntity bonus(String brand, int quantity, long amount) {
        return new BonusEntity(null, brand, 2026, 10, quantity, amount, null);
    }

    @Test
    void whenGetBonuses_thenReturnsList() {
        when(bonusRepository.findAllByOrderByBonusYearDescBonusMonthDescBrandAsc()).thenReturn(List.of(bonus("Toyota", 5, 70000)));
        assertThat(bonusService.getBonuses()).hasSize(1);
    }

    @Test
    void whenSaveNewBonus_thenNormalizesBrand() {
        when(bonusRepository.findByBrandIgnoreCaseAndBonusYearAndBonusMonth("Toyota", 2026, 10)).thenReturn(Optional.empty());
        when(bonusRepository.save(any(BonusEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        BonusEntity saved = bonusService.saveBonus(bonus("toyota", 5, 70000));

        assertThat(saved.getBrand()).isEqualTo("Toyota");
        assertThat(saved.getQuantity()).isEqualTo(5);
    }

    @Test
    void whenSaveExistingBonus_thenReplacesValues() {
        BonusEntity existing = bonus("Ford", 2, 50000);
        existing.setId(3L);
        when(bonusRepository.findByBrandIgnoreCaseAndBonusYearAndBonusMonth("Ford", 2026, 10)).thenReturn(Optional.of(existing));
        when(bonusRepository.save(any(BonusEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        BonusEntity saved = bonusService.saveBonus(bonus("FORD", 4, 60000));

        assertThat(saved.getId()).isEqualTo(3L);
        assertThat(saved.getQuantity()).isEqualTo(4);
        assertThat(saved.getAmount()).isEqualTo(60000);
    }

    @Test
    void whenSaveBonusInvalidBrand_thenThrows() {
        assertThatThrownBy(() -> bonusService.saveBonus(bonus("Kia", 1, 1000))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bonusService.saveBonus(bonus(null, 1, 1000))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenSaveBonusInvalidValues_thenThrows() {
        BonusEntity badMonth = bonus("Toyota", 1, 1000);
        badMonth.setBonusMonth(13);
        assertThatThrownBy(() -> bonusService.saveBonus(badMonth)).isInstanceOf(IllegalArgumentException.class);
        BonusEntity badYear = bonus("Toyota", 1, 1000);
        badYear.setBonusYear(1999);
        assertThatThrownBy(() -> bonusService.saveBonus(badYear)).isInstanceOf(IllegalArgumentException.class);
        BonusEntity badMonthLow = bonus("Toyota", 1, 1000);
        badMonthLow.setBonusMonth(0);
        assertThatThrownBy(() -> bonusService.saveBonus(badMonthLow)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bonusService.saveBonus(bonus("Toyota", -1, 1000))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bonusService.saveBonus(bonus("Toyota", 1, 0))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenRegisterFromEmail_thenParsesAllBrands() {
        when(bonusRepository.findByBrandIgnoreCaseAndBonusYearAndBonusMonth(anyString(), eq(2026), eq(10))).thenReturn(Optional.empty());
        when(bonusRepository.save(any(BonusEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        String email = """
                - Toyota: 5 bonos de 70.000 pesos
                - Ford: 2 bonos de 50.000 pesos
                - Hyundai: 1 bono de 30.000 pesos
                - Honda: 7 bonos de 40.000 pesos
                """;

        List<BonusEntity> saved = bonusService.registerFromEmail(email, 2026, 10);

        assertThat(saved).hasSize(4);
        assertThat(saved.get(0).getBrand()).isEqualTo("Toyota");
        assertThat(saved.get(0).getAmount()).isEqualTo(70000);
        assertThat(saved.get(2).getQuantity()).isEqualTo(1);
    }

    @Test
    void whenRegisterFromEmptyEmail_thenThrows() {
        assertThatThrownBy(() -> bonusService.registerFromEmail("hola", 2026, 10)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bonusService.registerFromEmail(null, 2026, 10)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenDeleteExistingBonus_thenDeletes() {
        when(bonusRepository.existsById(1L)).thenReturn(true);
        assertThat(bonusService.deleteBonus(1L)).isTrue();
        verify(bonusRepository).deleteById(1L);
    }

    @Test
    void whenDeleteMissingBonus_thenThrows() {
        when(bonusRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> bonusService.deleteBonus(1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void whenConsumeAvailableBonus_thenDecrementsAndReturnsAmount() {
        BonusEntity b = bonus("Toyota", 5, 70000);
        when(bonusRepository.findByBrandIgnoreCaseAndBonusYearAndBonusMonth("Toyota", 2026, 10)).thenReturn(Optional.of(b));

        long amount = bonusService.consumeBonus("Toyota", LocalDate.of(2026, 10, 5));

        assertThat(amount).isEqualTo(70000);
        assertThat(b.getQuantity()).isEqualTo(4);
        verify(bonusRepository).save(b);
    }

    @Test
    void whenConsumeBonusNoBrandAgreement_thenThrows() {
        assertThatThrownBy(() -> bonusService.consumeBonus("Kia", LocalDate.of(2026, 10, 5)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenConsumeBonusNotRegistered_thenThrows() {
        when(bonusRepository.findByBrandIgnoreCaseAndBonusYearAndBonusMonth("Ford", 2026, 10)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bonusService.consumeBonus("Ford", LocalDate.of(2026, 10, 5)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void whenConsumeBonusExhausted_thenThrows() {
        when(bonusRepository.findByBrandIgnoreCaseAndBonusYearAndBonusMonth("Honda", 2026, 10))
                .thenReturn(Optional.of(bonus("Honda", 0, 40000)));
        assertThatThrownBy(() -> bonusService.consumeBonus("Honda", LocalDate.of(2026, 10, 5)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void whenReturnBonus_thenIncrementsQuantity() {
        BonusEntity b = bonus("Toyota", 4, 70000);
        when(bonusRepository.findByBrandIgnoreCaseAndBonusYearAndBonusMonth("Toyota", 2026, 10)).thenReturn(Optional.of(b));

        bonusService.returnBonus("Toyota", LocalDate.of(2026, 10, 20));

        assertThat(b.getQuantity()).isEqualTo(5);
    }

    @Test
    void whenIsBonusBrand_thenOnlyFourBrands() {
        assertThat(bonusService.isBonusBrand(" hyundai ")).isTrue();
        assertThat(bonusService.isBonusBrand("Kia")).isFalse();
        assertThat(bonusService.isBonusBrand(null)).isFalse();
    }
}
