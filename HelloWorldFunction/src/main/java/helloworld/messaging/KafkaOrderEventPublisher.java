package helloworld.messaging;

import helloworld.model.OrderResponse;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final boolean kafkaEnabled;

    public KafkaOrderEventPublisher() {
        this(
            System.getenv().getOrDefault(
                "KAFKA_BOOTSTRAP_SERVERS",
                "localhost:9092"
            ),
            "order-created"
        );
    }

    public KafkaOrderEventPublisher(
            String bootstrapServers,
            String topic) {

        this.kafkaEnabled =
                Boolean.parseBoolean(
                    System.getenv().getOrDefault(
                        "KAFKA_ENABLED",
                        "true"
                    )
                );

        this.topic = topic;

        if (kafkaEnabled) {

            Properties props = new Properties();

            props.put(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    bootstrapServers
            );

            props.put(
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    StringSerializer.class.getName()
            );

            props.put(
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    StringSerializer.class.getName()
            );

            this.producer = new KafkaProducer<>(props);

        } else {

            this.producer = null;
        }
    }

    @Override
    public void publishOrderCreated(OrderResponse order) {

        if (!kafkaEnabled) {

            System.out.println(
                    "Kafka publishing is disabled. " +
                    "Skipping OrderCreated event: " +
                    order.getOrderId()
            );

            return;
        }

        String event = String.format(
                """
                {
                  "orderId": "%s",
                  "customerId": "%s",
                  "productId": "%s",
                  "quantity": %d,
                  "amount": %.2f,
                  "status": "%s"
                }
                """,
                order.getOrderId(),
                order.getCustomerId(),
                order.getProductId(),
                order.getQuantity(),
                order.getAmount(),
                order.getStatus()
        );

        ProducerRecord<String, String> record =
                new ProducerRecord<>(
                        topic,
                        order.getOrderId(),
                        event
                );

        try {

            producer.send(record).get();

            System.out.println(
                    "OrderCreated event published: " +
                    order.getOrderId()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to publish OrderCreated event",
                    e
            );
        }
    }

    public void close() {

        if (producer != null) {
            producer.close();
        }
    }
}