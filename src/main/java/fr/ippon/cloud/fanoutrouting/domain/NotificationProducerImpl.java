package fr.ippon.cloud.fanoutrouting.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

@Slf4j
public class NotificationProducerImpl implements Supplier<Flux<Message<NotificationEvent>>>, NotificationProducer {

    private Sinks.Many<Message<NotificationEvent>> sink = Sinks.many().unicast().onBackpressureBuffer();

    @Override
    public void produce(NotificationEvent event) {
        log.info("Emit event : " + event);
        Message<NotificationEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.MESSAGE_KEY, toKey(event))
                .build();
        sink.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    @Override
    public Flux<Message<NotificationEvent>> get() {
        return sink.asFlux();
    }

    private String toKey(NotificationEvent event) {
        return "key_" + event.getId();
    }
}
