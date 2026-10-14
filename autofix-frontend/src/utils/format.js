export const money = (value) =>
  value === null || value === undefined ? "-" : `$ ${Number(value).toLocaleString("es-CL")}`;

export const time = (value) => (value ? String(value).substring(0, 5) : "");

export const errorMessage = (error) =>
  error?.response?.data?.message ||
  (error?.response?.status === 403 ? "No tiene permisos para esta operación" : error?.message || "Error inesperado");

// Fechas en hora local (toISOString usa UTC: en Chile, después de las 21:00 daría el día siguiente)
const localDate = (d) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;

export const firstDayOfMonth = () => {
  const d = new Date();
  return localDate(new Date(d.getFullYear(), d.getMonth(), 1));
};

export const today = () => localDate(new Date());
