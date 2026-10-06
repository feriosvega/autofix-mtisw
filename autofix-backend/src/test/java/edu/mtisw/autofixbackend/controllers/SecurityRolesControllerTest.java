package edu.mtisw.autofixbackend.controllers;

import edu.mtisw.autofixbackend.config.SecurityConfig;
import edu.mtisw.autofixbackend.dtos.MatrixReport;
import edu.mtisw.autofixbackend.dtos.ReportR1;
import edu.mtisw.autofixbackend.dtos.ReportR1Row;
import edu.mtisw.autofixbackend.entities.EngineType;
import edu.mtisw.autofixbackend.entities.VehicleEntity;
import edu.mtisw.autofixbackend.entities.VehicleType;
import edu.mtisw.autofixbackend.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Verifica la autorización por roles (P5): ADMIN accede a todo, USER solo a HU1..HU4
@WebMvcTest({VehicleController.class, RepairController.class, ReportController.class, BonusController.class, InstanceController.class})
@Import(SecurityConfig.class)
class SecurityRolesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    private VehicleService vehicleService;
    @MockBean
    private RepairService repairService;
    @MockBean
    private ReportService reportService;
    @MockBean
    private BonusService bonusService;

    private static final SimpleGrantedAuthority USER = new SimpleGrantedAuthority("ROLE_USER");
    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");

    @Test
    void withoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/")).andExpect(status().isUnauthorized());
    }

    @Test
    void publicInstanceEndpoint_thenNoTokenNeeded() throws Exception {
        mockMvc.perform(get("/api/v1/public/instance")).andExpect(status().isOk());
    }

    @Test
    void userListsVehicles_thenOk() throws Exception {
        given(vehicleService.getVehicles()).willReturn(List.of(
                new VehicleEntity(1L, "ABCD12", "Toyota", "Corolla", VehicleType.SEDAN, 2015, EngineType.GASOLINA, 5)));

        mockMvc.perform(get("/api/v1/vehicles/").with(jwt().authorities(USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].plate", is("ABCD12")));
    }

    @Test
    void userRegistersVehicle_thenOk() throws Exception {
        given(vehicleService.saveVehicle(any())).willAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/v1/vehicles/").with(jwt().authorities(USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plate\":\"ABCD12\",\"brand\":\"Kia\",\"model\":\"Rio\",\"vehicleType\":\"SEDAN\"," +
                                "\"manufactureYear\":2020,\"engineType\":\"GASOLINA\",\"seats\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brand", is("Kia")));
    }

    @Test
    void userDeletesVehicle_thenForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/vehicles/1").with(jwt().authorities(USER))).andExpect(status().isForbidden());
    }

    @Test
    void adminDeletesVehicle_thenNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/vehicles/1").with(jwt().authorities(ADMIN))).andExpect(status().isNoContent());
    }

    @Test
    void userCalculatesRepair_thenOk() throws Exception {
        mockMvc.perform(post("/api/v1/repairs/1/calculate").with(jwt().authorities(USER))).andExpect(status().isOk());
    }

    @Test
    void userGetsReportR1_thenOk() throws Exception {
        given(reportService.getReportR1(any(), any())).willReturn(new ReportR1(List.of(), new ReportR1Row()));
        mockMvc.perform(get("/api/v1/reports/r1?from=2026-10-01&to=2026-10-31").with(jwt().authorities(USER)))
                .andExpect(status().isOk());
    }

    @Test
    void userGetsReportsR2R3R4_thenForbidden() throws Exception {
        for (String r : List.of("r2", "r3", "r4")) {
            mockMvc.perform(get("/api/v1/reports/" + r + "?from=2026-10-01&to=2026-10-31").with(jwt().authorities(USER)))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void adminGetsReportR2_thenOk() throws Exception {
        given(reportService.getReportR2(any(), any())).willReturn(new MatrixReport(List.of(), List.of(), Map.of(), null));
        mockMvc.perform(get("/api/v1/reports/r2?from=2026-10-01&to=2026-10-31").with(jwt().authorities(ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void userCreatesBonus_thenForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/bonuses/").with(jwt().authorities(USER))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void businessError_thenBadRequestWithMessage() throws Exception {
        given(vehicleService.saveVehicle(any())).willThrow(new IllegalArgumentException("Patente inválida"));

        mockMvc.perform(post("/api/v1/vehicles/").with(jwt().authorities(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"plate\":\"X\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Patente inválida")));
    }

    @Test
    void notFound_then404() throws Exception {
        given(repairService.getRepairById(9L)).willThrow(new jakarta.persistence.EntityNotFoundException("no existe"));
        mockMvc.perform(get("/api/v1/repairs/9").with(jwt().authorities(USER))).andExpect(status().isNotFound());
    }
}
