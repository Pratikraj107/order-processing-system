package helloworld.service;

import helloworld.messaging.KafkaOrderEventPublisher;
import helloworld.messaging.OrderEventPublisher;
import helloworld.model.OrderRequest;
import helloworld.model.OrderResponse;
import helloworld.repository.OrderRepository;
import helloworld.repository.OrderRepositoryInterface;

import java.util.UUID;

public class OrderService {

    private final OrderRepositoryInterface orderRepository;
    private final OrderEventPublisher eventPublisher;

    public OrderService() {
        this(
            new OrderRepository(),
            new KafkaOrderEventPublisher()
        );
    }

    public OrderService(OrderRepositoryInterface orderRepository) {
    this(
        orderRepository,
        new KafkaOrderEventPublisher()
    );
  }

    public OrderService(
            OrderRepositoryInterface orderRepository,
            OrderEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    public OrderResponse getOrder(String orderId) {

        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException(
                    "orderId is required"
            );
        }

        return orderRepository.findById(orderId);
    }

    public OrderResponse createOrder(OrderRequest request) {

        validateOrder(request);

        String orderId = "ORD-" + UUID.randomUUID();

        OrderResponse order = new OrderResponse(
                orderId,
                request.getCustomerId(),
                request.getProductId(),
                request.getQuantity(),
                request.getAmount(),
                "CREATED"
        );

        orderRepository.save(order);

        eventPublisher.publishOrderCreated(order);

        return order;
    }

    private void validateOrder(OrderRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Order request cannot be null"
            );
        }

        if (request.getCustomerId() == null ||
                request.getCustomerId().isBlank()) {
            throw new IllegalArgumentException(
                    "customerId is required"
            );
        }

        if (request.getProductId() == null ||
                request.getProductId().isBlank()) {
            throw new IllegalArgumentException(
                    "productId is required"
            );
        }

        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "quantity must be greater than 0"
            );
        }

        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException(
                    "amount must be greater than 0"
            );
        }
    }
}