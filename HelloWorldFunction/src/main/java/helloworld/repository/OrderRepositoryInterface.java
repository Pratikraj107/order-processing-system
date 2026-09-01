package helloworld.repository;

import helloworld.model.OrderResponse;

public interface OrderRepositoryInterface {

    void save(OrderResponse order);

    OrderResponse findById(String orderId);
}