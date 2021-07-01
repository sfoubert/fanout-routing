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

    private static final String APP_1 = "app1";
    private static final String APP_2 = "app2";
    private static final String APP_3 = "app3";

    @Bean("notification-producer")
    NotificationProducer notificationProducer() {
        return new NotificationProducerImpl();
    }

    @Bean("routing-processor")
    public Function<KStream<String, NotificationEvent>, KStream<String, NotificationEvent>[]> routingProcessor() {

        Predicate<String, NotificationEvent> isApp1 = (k, v) -> APP_1.equals(v.getApp());
        Predicate<String, NotificationEvent> isApp2 = (k, v) -> APP_2.equals(v.getApp());
        Predicate<String, NotificationEvent> isApp3 = (k, v) -> APP_3.equals(v.getApp());
        Predicate<String, NotificationEvent> isAppUnknown = (k, v) -> !Arrays.asList(APP_1, APP_2, APP_3).contains(v.getApp());

        return input -> input.branch(isApp1, isApp2, isApp3, isAppUnknown);
    }

    @Bean
    public Consumer<NotificationEvent> app1() {
        return data -> log.info("Data received from app-1... " + data.getAction());
    }

    @Bean
    public Consumer<NotificationEvent> app2() {
        return data -> log.info("Data received from app-2... " + data.getAction());
    }

    @Bean
    public Consumer<NotificationEvent> app3() {
        return data -> log.info("Data received from app-3... " + data.getAction());
    }
}