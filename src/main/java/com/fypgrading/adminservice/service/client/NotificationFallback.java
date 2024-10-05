package com.fypgrading.adminservice.service.client;

import com.fypgrading.adminservice.service.dto.NotificationDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationFallback implements NotificationClient {

    @Override
    public List<NotificationDTO> getNotifications() {
        return List.of();
    }
}
