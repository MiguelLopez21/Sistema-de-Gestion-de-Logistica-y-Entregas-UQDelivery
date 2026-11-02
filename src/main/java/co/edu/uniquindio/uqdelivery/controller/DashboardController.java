package co.edu.uniquindio.uqdelivery.controller;

import co.edu.uniquindio.uqdelivery.dto.AgrupacionPeriodo;
import co.edu.uniquindio.uqdelivery.dto.DashboardResponse;
import co.edu.uniquindio.uqdelivery.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse obtener(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long repartidorId,
            @RequestParam(defaultValue = "DIA") AgrupacionPeriodo agrupacion) {
        return dashboardService.obtener(desde, hasta, repartidorId, agrupacion);
    }
}
