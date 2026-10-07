import httpClient from "../http-common";

const getAll = () => httpClient.get("/api/v1/bonuses/");
const create = (data) => httpClient.post("/api/v1/bonuses/", data);
const fromEmail = (text, year, month) => httpClient.post("/api/v1/bonuses/email", { text, year, month });
const remove = (id) => httpClient.delete(`/api/v1/bonuses/${id}`);

export default { getAll, create, fromEmail, remove };
