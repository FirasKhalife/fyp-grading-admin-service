package com.fypgrading.adminservice.service.client;

import com.fypgrading.adminservice.service.dto.NotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Primary
@FeignClient(
    name = "${services.notification-service-name}",
    path = "/api",
    fallback = NotificationFallback.class
)
public interface NotificationClient {

    @GetMapping("/notifications/")
    List<NotificationDTO> getNotifications();

}
