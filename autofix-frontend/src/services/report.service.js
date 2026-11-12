import httpClient from "../http-common";
const get = (report, month, year) => httpClient.get(`/api/v1/reports/${report}`, { params: { month, year } });
export default { get };
