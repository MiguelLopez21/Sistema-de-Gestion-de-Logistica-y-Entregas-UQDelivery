package co.edu.uniquindio.uqdelivery.controller;

import co.edu.uniquindio.uqdelivery.dto.CancelShipmentRequestDto;
import co.edu.uniquindio.uqdelivery.dto.ShipmentHistoryFilterDto;
import co.edu.uniquindio.uqdelivery.dto.ShipmentHistoryResponseDto;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/history")
    public Page<ShipmentHistoryResponseDto> getShipmentHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) EstadoEnvio status,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        ShipmentHistoryFilterDto filter = new ShipmentHistoryFilterDto(
                query, status, startDate, endDate, page, size);
        return shipmentService.getShipmentHistory(filter, authentication);
    }

    @PatchMapping("/{id}/cancel")
    public ShipmentHistoryResponseDto cancelShipment(@PathVariable Long id,
                                                     @Valid @RequestBody CancelShipmentRequestDto request,
                                                     Authentication authentication) {
        return shipmentService.cancelShipment(id, request.reason(), authentication);
    }
}
