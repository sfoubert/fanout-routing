package fr.ippon.cloud.fanoutrouting.config;

import fr.ippon.cloud.fanoutrouting.domain.NotificationProducerImpl;
import fr.ippon.cloud.fanoutrouting.domain.NotificationEvent;
import fr.ippon.cloud.fanoutrouting.domain.NotificationProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Configuration
class FanoutRoutingConfiguration {

    private static final String EVENT_1 = "event1";
    private static final String EVENT_2 = "event2";
    private static final String EVENT_3 = "event3";

    @Bean("notification-producer")
    NotificationProducer notificationProducer() {
        return new NotificationProducerImpl();
    }

    @Bean("routing-processor")
    public Function<KStream<String, NotificationEvent>, KStream<String, NotificationEvent>[]> routingProcessor() {

        Predicate<String, NotificationEvent> isEvent1 = (k, v) -> EVENT_1.equals(v.getType());
        Predicate<String, NotificationEvent> isEvent2 = (k, v) -> EVENT_2.equals(v.getType());
        Predicate<String, NotificationEvent> isEvent3 = (k, v) -> EVENT_3.equals(v.getType());
        Predicate<String, NotificationEvent> isEventUnknown = (k, v) -> !Arrays.asList(EVENT_1, EVENT_2, EVENT_3).contains(v.getType());

        return input -> input.branch(isEvent1, isEvent2, isEvent3, isEventUnknown);
    }

    @Bean
    public Consumer<NotificationEvent> event1() {
        return data -> log.info("Data received from event-1... " + data.getAction());
    }

    @Bean
    public Consumer<NotificationEvent> event2() {
        return data -> log.info("Data received from event-2... " + data.getAction());
    }

    @Bean
    public Consumer<NotificationEvent> event3() {
        return data -> log.info("Data received from event-3... " + data.getAction());
    }
}