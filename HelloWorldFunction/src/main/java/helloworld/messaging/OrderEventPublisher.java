package helloworld.messaging;

import helloworld.model.OrderResponse;

public interface OrderEventPublisher {

    void publishOrderCreated(OrderResponse order);
}