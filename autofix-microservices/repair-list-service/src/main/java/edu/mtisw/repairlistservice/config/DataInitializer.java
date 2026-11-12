package edu.mtisw.repairlistservice.config;

import edu.mtisw.repairlistservice.entities.RepairTypeEntity;
import edu.mtisw.repairlistservice.repositories.RepairTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Carga la tabla de precios del enunciado la primera vez que arranca el microservicio (BD vacía).
// Nota: el enunciado muestra "130.00" para la reparación 2 Gasolina; se interpreta como 130.000.
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    RepairTypeRepository repairTypeRepository;

    @Override
    public void run(String... args) {
        if (repairTypeRepository.count() > 0) {
            return;
        }
        add("Reparaciones del Sistema de Frenos", 120_000, 120_000, 180_000, 220_000);
        add("Servicio del Sistema de Refrigeración", 130_000, 130_000, 190_000, 230_000);
        add("Reparaciones del Motor", 350_000, 450_000, 700_000, 800_000);
        add("Reparaciones de la Transmisión", 210_000, 210_000, 300_000, 300_000);
        add("Reparación del Sistema Eléctrico", 150_000, 150_000, 200_000, 250_000);
        add("Reparaciones del Sistema de Escape", 100_000, 120_000, 450_000, 0);
        add("Reparación de Neumáticos y Ruedas", 100_000, 100_000, 100_000, 100_000);
        add("Reparaciones de la Suspensión y la Dirección", 180_000, 180_000, 210_000, 250_000);
        add("Reparación del Sistema de Aire Acondicionado y Calefacción", 150_000, 150_000, 180_000, 180_000);
        add("Reparaciones del Sistema de Combustible", 130_000, 140_000, 220_000, 0);
        add("Reparación y Reemplazo del Parabrisas y Cristales", 80_000, 80_000, 80_000, 80_000);
    }

    private void add(String name, long gasoline, long diesel, long hybrid, long electric) {
        repairTypeRepository.save(new RepairTypeEntity(null, name, gasoline, diesel, hybrid, electric));
    }
}
