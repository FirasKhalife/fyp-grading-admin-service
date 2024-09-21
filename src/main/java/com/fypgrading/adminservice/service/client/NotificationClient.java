package com.fypgrading.adminservice.service.client;

import com.fypgrading.adminservice.service.dto.NotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "${services.notification-service-name}", path = "/api")
public interface NotificationClient {

    @GetMapping("/notifications/")
    List<NotificationDTO> getNotifications();

}
