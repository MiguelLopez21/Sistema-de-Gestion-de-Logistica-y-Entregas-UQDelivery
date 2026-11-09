package co.edu.uniquindio.uqdelivery.controller;

import co.edu.uniquindio.uqdelivery.dto.NotificationResponseDto;
import co.edu.uniquindio.uqdelivery.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponseDto> getNotifications(Authentication authentication) {
        return notificationService.getNotifications(authentication.getName());
    }

    @PatchMapping("/{id}/read")
    public NotificationResponseDto markAsRead(@PathVariable Long id, Authentication authentication) {
        return notificationService.markAsRead(id, authentication.getName());
    }
}
