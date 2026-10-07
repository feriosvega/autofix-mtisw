import httpClient from "../http-common";

const getAll = () => httpClient.get("/api/v1/repairs/");
const get = (id) => httpClient.get(`/api/v1/repairs/${id}`);
const getByVehicle = (vehicleId) => httpClient.get(`/api/v1/repairs/vehicle/${vehicleId}`);
const create = (data) => httpClient.post("/api/v1/repairs/", data);
const update = (data) => httpClient.put("/api/v1/repairs/", data);
const calculate = (id) => httpClient.post(`/api/v1/repairs/${id}/calculate`);
const remove = (id) => httpClient.delete(`/api/v1/repairs/${id}`);

export default { getAll, get, getByVehicle, create, update, calculate, remove };
