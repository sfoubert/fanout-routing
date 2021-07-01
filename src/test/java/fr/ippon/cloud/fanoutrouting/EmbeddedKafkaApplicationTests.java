package fr.ippon.cloud.fanoutrouting;

import fr.ippon.cloud.fanoutrouting.domain.NotificationEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmbeddedKafkaApplicationTests {

    private static final String INPUT_TOPIC = "notification_topic";

    private static final String OUTPUT_APP1_TOPIC = "app1_topic";
    private static final String OUTPUT_APP2_TOPIC = "app2_topic";
    private static final String OUTPUT_APP3_TOPIC = "app3_topic";

    // name must be different than our app consumers
    private static final String GROUP1_NAME = "other_group1";
    private static final String GROUP2_NAME = "other_group2";
    private static final String GROUP3_NAME = "other_group3";

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true,
            OUTPUT_APP1_TOPIC, OUTPUT_APP3_TOPIC, OUTPUT_APP3_TOPIC);

    @BeforeClass
    public static void setup() {
        System.setProperty("spring.cloud.stream.kafka.binder.brokers", embeddedKafka.getEmbeddedKafka().getBrokersAsString());
    }

    @Test
    public void testSendReceiveApp1() {
        NotificationEvent event = NotificationEvent.builder()
                .id(1L)
                .app("app1")
                .action("object.created")
                .build();
        sendMessage(event, INPUT_TOPIC);

        ConsumerRecords<String, NotificationEvent> records = consumeMessage(OUTPUT_APP1_TOPIC, GROUP1_NAME);

        assertThat(records.count()).isEqualTo(1);
        assertThat(records.iterator().next().value()).isEqualTo(event);
    }

    @Test
    public void testSendReceiveApp2() {
        NotificationEvent event = NotificationEvent.builder()
                .id(2L)
                .app("app2")
                .action("object.updated")
                .build();
        sendMessage(event, INPUT_TOPIC);

        ConsumerRecords<String, NotificationEvent> records = consumeMessage(OUTPUT_APP2_TOPIC, GROUP2_NAME);

        assertThat(records.count()).isEqualTo(1);
        assertThat(records.iterator().next().value()).isEqualTo(event);
    }

    @Test
    public void testSendReceiveApp3() {
        NotificationEvent event = NotificationEvent.builder()
                .id(3L)
                .app("app3")
                .action("object.deleted")
                .build();
        sendMessage(event, INPUT_TOPIC);

        ConsumerRecords<String, NotificationEvent> records = consumeMessage(OUTPUT_APP3_TOPIC, GROUP3_NAME);

        assertThat(records.count()).isEqualTo(1);
        assertThat(records.iterator().next().value()).isEqualTo(event);
    }

    private void sendMessage(NotificationEvent event, String inputTopic) {
        Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka.getEmbeddedKafka());
        senderProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        senderProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        DefaultKafkaProducerFactory<byte[], byte[]> pf = new DefaultKafkaProducerFactory<>(senderProps);
        KafkaTemplate<byte[], byte[]> template = new KafkaTemplate<>(pf, true);
        template.setDefaultTopic(inputTopic);

        Message<NotificationEvent> message = org.springframework.integration.support.MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.MESSAGE_KEY, "myKey")
                .build();
        template.send(message);
    }

    private ConsumerRecords<String, NotificationEvent> consumeMessage(String outputTopic, String groupName) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(groupName, "false", embeddedKafka.getEmbeddedKafka());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "fr.ippon.cloud.fanoutrouting.domain");
        DefaultKafkaConsumerFactory<String, NotificationEvent> cf = new DefaultKafkaConsumerFactory<>(consumerProps);

        Consumer<String, NotificationEvent> consumer = cf.createConsumer();
        consumer.subscribe(Collections.singleton(outputTopic));
        ConsumerRecords<String, NotificationEvent> records = consumer.poll(Duration.ofSeconds(10));
        consumer.commitSync();
        return records;
    }
}
