// Las mismas 11 reparaciones y tipos definidos en el enunciado (y en el backend)
export const REPAIR_NAMES = {
  1: "Reparaciones del Sistema de Frenos",
  2: "Servicio del Sistema de Refrigeración",
  3: "Reparaciones del Motor",
  4: "Reparaciones de la Transmisión",
  5: "Reparación del Sistema Eléctrico",
  6: "Reparaciones del Sistema de Escape",
  7: "Reparación de Neumáticos y Ruedas",
  8: "Reparaciones de la Suspensión y la Dirección",
  9: "Reparación del Sistema de Aire Acondicionado y Calefacción",
  10: "Reparaciones del Sistema de Combustible",
  11: "Reparación y Reemplazo del Parabrisas y Cristales",
};

// Reparaciones no disponibles para motor eléctrico (precio 0 en la tabla)
export const NOT_FOR_ELECTRIC = [6, 10];

export const VEHICLE_TYPES = ["SEDAN", "HATCHBACK", "SUV", "PICKUP", "FURGONETA"];
export const ENGINE_TYPES = ["GASOLINA", "DIESEL", "HIBRIDO", "ELECTRICO"];
export const BONUS_BRANDS = ["Toyota", "Ford", "Hyundai", "Honda"];

export const LABELS = {
  SEDAN: "Sedán", HATCHBACK: "Hatchback", SUV: "SUV", PICKUP: "Pickup", FURGONETA: "Furgoneta",
  GASOLINA: "Gasolina", DIESEL: "Diésel", HIBRIDO: "Híbrido", ELECTRICO: "Eléctrico",
};
