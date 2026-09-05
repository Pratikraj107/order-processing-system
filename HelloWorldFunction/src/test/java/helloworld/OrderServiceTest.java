package helloworld;

import helloworld.messaging.OrderEventPublisher;
import helloworld.model.OrderRequest;
import helloworld.model.OrderResponse;
import helloworld.repository.OrderRepositoryInterface;
import helloworld.service.OrderService;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OrderServiceTest {

    /*
     * Fake repository for unit testing.
     *
     * This does NOT connect to DynamoDB.
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

        public OrderResponse getSavedOrder() {
            return savedOrder;
        }
    }

    /*
     * Fake event publisher for unit testing.
     *
     * This does NOT connect to Kafka.
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
    public void shouldCreateValidOrder() {

        FakeOrderRepository fakeRepository =
                new FakeOrderRepository();

        FakeOrderEventPublisher fakePublisher =
                new FakeOrderEventPublisher();

        OrderService orderService =
                new OrderService(
                        fakeRepository,
                        fakePublisher
                );

        OrderRequest request = new OrderRequest();

        request.setCustomerId("C1001");
        request.setProductId("P5001");
        request.setQuantity(2);
        request.setAmount(1499);

        OrderResponse response =
                orderService.createOrder(request);

        // Verify order was created
        assertNotNull(response);
        assertNotNull(response.getOrderId());

        assertEquals(
                "C1001",
                response.getCustomerId()
        );

        assertEquals(
                "P5001",
                response.getProductId()
        );

        assertEquals(
                2,
                response.getQuantity()
        );

        assertEquals(
                1499,
                response.getAmount(),
                0.01
        );

        assertEquals(
                "CREATED",
                response.getStatus()
        );

        // Verify order was saved
        assertNotNull(
                fakeRepository.getSavedOrder()
        );

        // Verify OrderCreated event was published
        assertNotNull(
                fakePublisher.getPublishedOrder()
        );

        assertEquals(
                response.getOrderId(),
                fakePublisher
                        .getPublishedOrder()
                        .getOrderId()
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectMissingCustomerId() {

        FakeOrderRepository fakeRepository =
                new FakeOrderRepository();

        OrderService orderService =
                new OrderService(
                        fakeRepository,
                        new FakeOrderEventPublisher()
                );

        OrderRequest request = new OrderRequest();

        request.setCustomerId("");
        request.setProductId("P5001");
        request.setQuantity(2);
        request.setAmount(1499);

        orderService.createOrder(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectInvalidQuantity() {

        FakeOrderRepository fakeRepository =
                new FakeOrderRepository();

        OrderService orderService =
                new OrderService(
                        fakeRepository,
                        new FakeOrderEventPublisher()
                );

        OrderRequest request = new OrderRequest();

        request.setCustomerId("C1001");
        request.setProductId("P5001");
        request.setQuantity(0);
        request.setAmount(1499);

        orderService.createOrder(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectInvalidAmount() {

        FakeOrderRepository fakeRepository =
                new FakeOrderRepository();

        OrderService orderService =
                new OrderService(
                        fakeRepository,
                        new FakeOrderEventPublisher()
                );

        OrderRequest request = new OrderRequest();

        request.setCustomerId("C1001");
        request.setProductId("P5001");
        request.setQuantity(2);
        request.setAmount(0);

        orderService.createOrder(request);
    }
}