export const money = (value) =>
  value === null || value === undefined ? "-" : `$ ${Number(value).toLocaleString("es-CL")}`;

export const time = (value) => (value ? String(value).substring(0, 5) : "");

export const errorMessage = (error) =>
  error?.response?.data?.message ||
  (error?.response?.status === 403 ? "No tiene permisos para esta operación" : error?.message || "Error inesperado");

export const firstDayOfMonth = () => {
  const d = new Date();
  return new Date(d.getFullYear(), d.getMonth(), 1).toISOString().substring(0, 10);
};

export const today = () => new Date().toISOString().substring(0, 10);
