package helloworld;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

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

    @Test
    public void successfulCreateOrderResponse() {

        // Arrange
        FakeOrderRepository fakeRepository =
                new FakeOrderRepository();

        App app = new App(fakeRepository);

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

        assertNotNull(
                fakeRepository.savedOrder
        );
    }

    @Test
    public void shouldReturn404WhenOrderDoesNotExist() {

        FakeOrderRepository fakeRepository =
                new FakeOrderRepository();

        App app = new App(fakeRepository);

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