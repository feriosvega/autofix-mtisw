package edu.mtisw.autofixbackend.services;

import edu.mtisw.autofixbackend.entities.BonusEntity;
import edu.mtisw.autofixbackend.repositories.BonusRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Bonos de descuento del convenio con la concesionaria TopCar
@Service
public class BonusService {

    public static final List<String> BONUS_BRANDS = List.of("Toyota", "Ford", "Hyundai", "Honda");

    // Línea típica del correo de TopCar: "- Toyota: 5 bonos de 70.000 pesos"
    private static final Pattern EMAIL_LINE = Pattern.compile(
            "([A-Za-zÁÉÍÓÚáéíóú]+)\\s*:\\s*(\\d+)\\s*bonos?\\s*de\\s*\\$?\\s*([\\d.]+)", Pattern.CASE_INSENSITIVE);

    @Autowired
    BonusRepository bonusRepository;

    public List<BonusEntity> getBonuses() {
        return bonusRepository.findAllByOrderByBonusYearDescBonusMonthDescBrandAsc();
    }

    // Crea o reemplaza los bonos de una marca para un mes (al inicio de cada mes llega un nuevo correo)
    public BonusEntity saveBonus(BonusEntity bonus) {
        String brand = normalizeBrand(bonus.getBrand());
        if (bonus.getBonusMonth() < 1 || bonus.getBonusMonth() > 12 || bonus.getBonusYear() < 2000) {
            throw new IllegalArgumentException("Mes/año del bono inválido");
        }
        if (bonus.getQuantity() < 0 || bonus.getAmount() <= 0) {
            throw new IllegalArgumentException("Cantidad y monto del bono deben ser positivos");
        }
        BonusEntity target = bonusRepository
                .findByBrandIgnoreCaseAndBonusYearAndBonusMonth(brand, bonus.getBonusYear(), bonus.getBonusMonth())
                .orElseGet(BonusEntity::new);
        target.setBrand(brand);
        target.setBonusYear(bonus.getBonusYear());
        target.setBonusMonth(bonus.getBonusMonth());
        target.setQuantity(bonus.getQuantity());
        target.setAmount(bonus.getAmount());
        return bonusRepository.save(target);
    }

    // Permite pegar el texto del correo de TopCar y registrar todos los bonos del mes de una vez
    public List<BonusEntity> registerFromEmail(String emailText, int year, int month) {
        List<BonusEntity> saved = new ArrayList<>();
        Matcher matcher = EMAIL_LINE.matcher(emailText == null ? "" : emailText);
        while (matcher.find()) {
            BonusEntity bonus = new BonusEntity();
            bonus.setBrand(matcher.group(1));
            bonus.setQuantity(Integer.parseInt(matcher.group(2)));
            bonus.setAmount(Long.parseLong(matcher.group(3).replace(".", "")));
            bonus.setBonusYear(year);
            bonus.setBonusMonth(month);
            saved.add(saveBonus(bonus));
        }
        if (saved.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron bonos en el texto del correo");
        }
        return saved;
    }

    public boolean deleteBonus(Long id) {
        if (!bonusRepository.existsById(id)) {
            throw new EntityNotFoundException("Bono no encontrado: " + id);
        }
        bonusRepository.deleteById(id);
        return true;
    }

    public boolean isBonusBrand(String brand) {
        return brand != null && BONUS_BRANDS.stream().anyMatch(b -> b.equalsIgnoreCase(brand.trim()));
    }

    // Usa un bono de la marca para el mes de ingreso. Retorna el monto descontado.
    @Transactional
    public long consumeBonus(String brand, LocalDate date) {
        if (!isBonusBrand(brand)) {
            throw new IllegalArgumentException("La marca " + brand + " no tiene convenio de bonos");
        }
        BonusEntity bonus = bonusRepository
                .findByBrandIgnoreCaseAndBonusYearAndBonusMonth(brand.trim(), date.getYear(), date.getMonthValue())
                .orElseThrow(() -> new IllegalStateException("No hay bonos registrados para " + brand + " en el mes de ingreso"));
        if (bonus.getQuantity() <= 0) {
            throw new IllegalStateException("No quedan bonos disponibles para " + brand);
        }
        bonus.setQuantity(bonus.getQuantity() - 1);
        bonusRepository.save(bonus);
        return bonus.getAmount();
    }

    // Devuelve un bono (si se elimina la reparación o se desmarca el bono)
    @Transactional
    public void returnBonus(String brand, LocalDate date) {
        bonusRepository
                .findByBrandIgnoreCaseAndBonusYearAndBonusMonth(brand.trim(), date.getYear(), date.getMonthValue())
                .ifPresent(bonus -> {
                    bonus.setQuantity(bonus.getQuantity() + 1);
                    bonusRepository.save(bonus);
                });
    }

    private String normalizeBrand(String brand) {
        if (!isBonusBrand(brand)) {
            throw new IllegalArgumentException("Marca sin convenio de bonos. Válidas: " + BONUS_BRANDS);
        }
        String b = brand.trim().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(b.charAt(0)) + b.substring(1);
    }
}
