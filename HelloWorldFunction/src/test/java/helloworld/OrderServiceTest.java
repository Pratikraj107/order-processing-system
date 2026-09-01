package helloworld;

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

    @Test
    public void shouldCreateValidOrder() {

        FakeOrderRepository fakeRepository =
                new FakeOrderRepository();

        OrderService orderService =
                new OrderService(fakeRepository);

        OrderRequest request = new OrderRequest();

        request.setCustomerId("C1001");
        request.setProductId("P5001");
        request.setQuantity(2);
        request.setAmount(1499);

        OrderResponse response =
                orderService.createOrder(request);

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

        assertNotNull(
                fakeRepository.getSavedOrder()
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectMissingCustomerId() {

        FakeOrderRepository fakeRepository =
                new FakeOrderRepository();

        OrderService orderService =
                new OrderService(fakeRepository);

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
                new OrderService(fakeRepository);

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
                new OrderService(fakeRepository);

        OrderRequest request = new OrderRequest();

        request.setCustomerId("C1001");
        request.setProductId("P5001");
        request.setQuantity(2);
        request.setAmount(0);

        orderService.createOrder(request);
    }
}