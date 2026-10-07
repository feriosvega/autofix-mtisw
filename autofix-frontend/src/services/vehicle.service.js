import httpClient from "../http-common";

const getAll = () => httpClient.get("/api/v1/vehicles/");
const get = (id) => httpClient.get(`/api/v1/vehicles/${id}`);
const create = (data) => httpClient.post("/api/v1/vehicles/", data);
const update = (data) => httpClient.put("/api/v1/vehicles/", data);
const remove = (id) => httpClient.delete(`/api/v1/vehicles/${id}`);

export default { getAll, get, create, update, remove };
