import httpClient from "../http-common";
const getAll = () => httpClient.get("/api/v1/repair-types/");
const create = (data) => httpClient.post("/api/v1/repair-types/", data);
const update = (data) => httpClient.put("/api/v1/repair-types/", data);
const remove = (id) => httpClient.delete(`/api/v1/repair-types/${id}`);
export default { getAll, create, update, remove };
