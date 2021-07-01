package fr.ippon.cloud.fanoutrouting.rest;

import fr.ippon.cloud.fanoutrouting.domain.NotificationProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class FanoutRoutingRest {

    @Autowired
    private NotificationProducer notificationProducer;

    @PostMapping("/notification")
    public ResponseEntity notification(@Valid @RequestBody NotificationView notificationView) {
        notificationProducer.produce(notificationView.toEvent());
        return ResponseEntity.ok("sent to " + notificationView.toEvent().getApp());
    }
}
