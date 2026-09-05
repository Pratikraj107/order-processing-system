package helloworld.messaging;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaOrderEventConsumer {

    private static final String TOPIC = "order-created";
    private static final String GROUP_ID = "order-processing-consumer";

    public static void main(String[] args) {

        String bootstrapServers =
                System.getenv().getOrDefault(
                        "KAFKA_BOOTSTRAP_SERVERS",
                        "localhost:9092"
                );

        Properties props = new Properties();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                GROUP_ID
        );

        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName()
        );

        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName()
        );

        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        props.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                "false"
        );

        try (KafkaConsumer<String, String> consumer =
                     new KafkaConsumer<>(props)) {

            consumer.subscribe(
                    Collections.singletonList(TOPIC)
            );

            System.out.println(
                    "Kafka consumer started..."
            );

            System.out.println(
                    "Listening to topic: " + TOPIC
            );

            System.out.println(
                    "Consumer group: " + GROUP_ID
            );

            while (true) {

                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> record : records) {

                    System.out.println(
                            "----------------------------------------"
                    );

                    System.out.println(
                            "Order event received"
                    );

                    System.out.println(
                            "Topic: " + record.topic()
                    );

                    System.out.println(
                            "Partition: " + record.partition()
                    );

                    System.out.println(
                            "Offset: " + record.offset()
                    );

                    System.out.println(
                            "Key: " + record.key()
                    );

                    System.out.println(
                            "Event:"
                    );

                    System.out.println(
                            record.value()
                    );

                    System.out.println(
                            "----------------------------------------"
                    );
                }

                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
            }
        }
    }
}