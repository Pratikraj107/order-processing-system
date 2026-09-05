package helloworld;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import helloworld.messaging.OrderEventPublisher;
import helloworld.model.OrderResponse;
import helloworld.repository.OrderRepositoryInterface;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AppTest {

    /*
     * Fake repository so this test does not connect
     * to DynamoDB.
     */
    private static class FakeOrderRepository
            implements OrderRepositoryInterface {

        private OrderResponse savedOrder;

        @Override
        public void save(OrderResponse order) {
            this.savedOrder = order;
        }

        @Override
        public OrderResponse findById(String orderId) {

            if (savedOrder != null &&
                    savedOrder.getOrderId().equals(orderId)) {

                return savedOrder;
            }

            return null;
        }
    }

    /*
     * Fake event publisher so this test does not
     * connect to Kafka.
     */
    private static class FakeOrderEventPublisher
            implements OrderEventPublisher {

        private OrderResponse publishedOrder;

        @Override
        public void publishOrderCreated(OrderResponse order) {
            this.publishedOrder = order;
        }

        public OrderResponse getPublishedOrder() {
            return publishedOrder;
        }
    }

    @Test
    public void successfulCreateOrderResponse() {

        // Arrange
        FakeOrderRepository fakeRepository =
                new FakeOrderRepository();

        FakeOrderEventPublisher fakePublisher =
                new FakeOrderEventPublisher();

        App app =
                new App(
                        fakeRepository,
                        fakePublisher
                );

        String requestBody = """
                {
                    "customerId": "C1001",
                    "productId": "P5001",
                    "quantity": 2,
                    "amount": 1499
                }
                """;

        APIGatewayProxyRequestEvent request =
                new APIGatewayProxyRequestEvent()
                        .withHttpMethod("POST")
                        .withBody(requestBody);

        // Act
        APIGatewayProxyResponseEvent result =
                app.handleRequest(request, null);

        // Assert
        assertEquals(
                201,
                result.getStatusCode().intValue()
        );

        assertEquals(
                "application/json",
                result.getHeaders().get("Content-Type")
        );

        String content = result.getBody();

        assertNotNull(content);

        assertTrue(
                content.contains("\"orderId\"")
        );

        assertTrue(
                content.contains("\"customerId\":\"C1001\"")
        );

        assertTrue(
                content.contains("\"productId\":\"P5001\"")
        );

        assertTrue(
                content.contains("\"quantity\":2")
        );

        assertTrue(
                content.contains("\"amount\":1499.0")
        );

        assertTrue(
                content.contains("\"status\":\"CREATED\"")
        );

        // Verify order was saved
        assertNotNull(
                fakeRepository.savedOrder
        );

        // Verify OrderCreated event was published
        assertNotNull(
                fakePublisher.getPublishedOrder()
        );

        assertEquals(
                fakeRepository.savedOrder.getOrderId(),
                fakePublisher
                        .getPublishedOrder()
                        .getOrderId()
        );
    }

    @Test
    public void shouldReturn404WhenOrderDoesNotExist() {

        FakeOrderRepository fakeRepository =
                new FakeOrderRepository();

        FakeOrderEventPublisher fakePublisher =
                new FakeOrderEventPublisher();

        App app =
                new App(
                        fakeRepository,
                        fakePublisher
                );

        Map<String, String> pathParameters =
                new HashMap<>();

        pathParameters.put(
                "orderId",
                "ORD-does-not-exist"
        );

        APIGatewayProxyRequestEvent request =
                new APIGatewayProxyRequestEvent()
                        .withHttpMethod("GET")
                        .withPathParameters(pathParameters);

        APIGatewayProxyResponseEvent result =
                app.handleRequest(request, null);

        assertEquals(
                404,
                result.getStatusCode().intValue()
        );

        assertTrue(
                result.getBody().contains("Order not found")
        );
    }
}