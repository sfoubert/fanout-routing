package fr.ippon.cloud.fanoutrouting.rest;

import fr.ippon.cloud.fanoutrouting.domain.NotificationEvent;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
public class NotificationView {

    @NotNull
    private Long id;

    @NotBlank
    private String app;

    @NotBlank
    private String action;

    NotificationEvent toEvent(){
        return NotificationEvent.builder()
                .id(id)
                .app(app)
                .action(action)
                .build();
    }
}
