package helloworld.repository;

import helloworld.model.OrderResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.HashMap;
import java.util.Map;

public class OrderRepository implements OrderRepositoryInterface {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public OrderRepository() {
        this(
            DynamoDbClient.builder().build(),
            System.getenv("ORDERS_TABLE_NAME")
        );
    }

    public OrderRepository(
            DynamoDbClient dynamoDbClient,
            String tableName) {

        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
public OrderResponse findById(String orderId) {

    Map<String, AttributeValue> key = new HashMap<>();

    key.put(
            "orderId",
            AttributeValue.builder()
                    .s(orderId)
                    .build()
    );

    GetItemRequest request = GetItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .build();

    Map<String, AttributeValue> item =
            dynamoDbClient.getItem(request).item();

    if (item == null || item.isEmpty()) {
        return null;
    }

    return new OrderResponse(
            item.get("orderId").s(),
            item.get("customerId").s(),
            item.get("productId").s(),
            Integer.parseInt(item.get("quantity").n()),
            Double.parseDouble(item.get("amount").n()),
            item.get("status").s()
    );
}

    public void save(OrderResponse order) {

        Map<String, AttributeValue> item = new HashMap<>();

        item.put(
                "orderId",
                AttributeValue.builder()
                        .s(order.getOrderId())
                        .build()
        );

        item.put(
                "customerId",
                AttributeValue.builder()
                        .s(order.getCustomerId())
                        .build()
        );

        item.put(
                "productId",
                AttributeValue.builder()
                        .s(order.getProductId())
                        .build()
        );

        item.put(
                "quantity",
                AttributeValue.builder()
                        .n(String.valueOf(order.getQuantity()))
                        .build()
        );

        item.put(
                "amount",
                AttributeValue.builder()
                        .n(String.valueOf(order.getAmount()))
                        .build()
        );

        item.put(
                "status",
                AttributeValue.builder()
                        .s(order.getStatus())
                        .build()
        );

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
    }
}