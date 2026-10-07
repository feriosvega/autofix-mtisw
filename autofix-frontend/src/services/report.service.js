import httpClient from "../http-common";

const get = (report, from, to) => httpClient.get(`/api/v1/reports/${report}`, { params: { from, to } });

export default { get };
