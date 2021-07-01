package fr.ippon.cloud.fanoutrouting.domain;

public interface NotificationProducer {

    void produce(NotificationEvent event);

}
