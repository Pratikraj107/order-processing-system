package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import helloworld.model.OrderRequest;
import helloworld.model.OrderResponse;
import helloworld.repository.OrderRepositoryInterface;
import helloworld.service.OrderService;

import java.util.HashMap;
import java.util.Map;

public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    /*
     * Production constructor.
     *
     * AWS Lambda uses this constructor.
     * It creates the real DynamoDB repository.
     */
    public App() {
        this.objectMapper = new ObjectMapper();
        this.orderService = new OrderService();
    }

    /*
     * Test constructor.
     *
     * Allows unit tests to inject a fake repository
     * instead of connecting to DynamoDB.
     */
    public App(OrderRepositoryInterface orderRepository) {
        this.objectMapper = new ObjectMapper();
        this.orderService = new OrderService(orderRepository);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input,
            Context context) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        try {

            if (input == null) {
                return badRequest("Request cannot be null");
            }

            String httpMethod = input.getHttpMethod();

            /*
             * POST /orders
             */
            if ("POST".equalsIgnoreCase(httpMethod)) {

                if (input.getBody() == null || input.getBody().isBlank()) {
                    return badRequest("Request body is required");
                }

                OrderRequest request =
                        objectMapper.readValue(
                                input.getBody(),
                                OrderRequest.class
                        );

                OrderResponse order =
                        orderService.createOrder(request);

                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(201)
                        .withHeaders(headers)
                        .withBody(
                                objectMapper.writeValueAsString(order)
                        );
            }

            /*
             * GET /orders/{orderId}
             */
            if ("GET".equalsIgnoreCase(httpMethod)) {

                Map<String, String> pathParameters =
                        input.getPathParameters();

                if (pathParameters == null) {
                    return badRequest("orderId is required");
                }

                String orderId = pathParameters.get("orderId");

                if (orderId == null || orderId.isBlank()) {
                    return badRequest("orderId is required");
                }

                OrderResponse order =
                        orderService.getOrder(orderId);

                if (order == null) {

                    return new APIGatewayProxyResponseEvent()
                            .withStatusCode(404)
                            .withHeaders(headers)
                            .withBody("""
                                    {
                                        "error": "Order not found"
                                    }
                                    """);
                }

                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(200)
                        .withHeaders(headers)
                        .withBody(
                                objectMapper.writeValueAsString(order)
                        );
            }

            /*
             * Any unsupported HTTP method.
             */
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(405)
                    .withHeaders(headers)
                    .withBody("""
                            {
                                "error": "Method not allowed"
                            }
                            """);

        } catch (IllegalArgumentException e) {

            return badRequest(e.getMessage());

        } catch (Exception e) {

            /*
             * Don't expose internal exception details
             * to the API client.
             */
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(headers)
                    .withBody("""
                            {
                                "error": "Internal server error"
                            }
                            """);
        }
    }

    private APIGatewayProxyResponseEvent badRequest(String message) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String safeMessage =
                message == null ? "Invalid request made" : message;

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withHeaders(headers)
                .withBody("""
                        {
                            "error": "Invalid request",
                            "message": "%s"
                        }
                        """.formatted(safeMessage));
    }
}