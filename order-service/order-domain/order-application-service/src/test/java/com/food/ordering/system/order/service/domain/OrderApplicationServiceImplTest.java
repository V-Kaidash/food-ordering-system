package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.create.OrderItemDTO;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.input.service.OrderApplicationService;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
class OrderApplicationServiceImplTest {

  @Autowired
  private OrderApplicationService orderApplicationService;
  @Autowired
  private OrderDataMapper orderDataMapper;
  @Autowired
  private OrderRepository orderRepository;
  @Autowired
  private CustomerRepository customerRepository;
  @Autowired
  private RestaurantRepository restaurantRepository;

  private CreateOrderCommand createOrderCommand;
  private CreateOrderCommand createOrderCommandWrongPrice;
  private CreateOrderCommand createOrderCommandWrongProductPrice;
  private final UUID CUSTOMER_ID = UUID.fromString("d1b3b3b3-3b3b-3b3b-3b3b-3b3b3b3b3b01");
  private final UUID RESTAURANT_ID = UUID.fromString("d1b3b3b3-3b3b-3b3b-3b3b-3b3b3b3b3b02");
  private final UUID ORDER_ID = UUID.fromString("d1b3b3b3-3b3b-3b3b-3b3b-3b3b3b3b3b03");
  private final UUID PRODUCT_ID = UUID.fromString("d1b3b3b3-3b3b-3b3b-3b3b-3b3b3b3b3b04");
  private final BigDecimal PRICE = new BigDecimal("200.00");

  @BeforeAll
  public void setUp() {
    createOrderCommand = CreateOrderCommand.builder()
        .customerId(CUSTOMER_ID)
        .restaurantId(RESTAURANT_ID)
        .address(OrderAddress.builder()
            .street("Street")
            .postalCode("12345")
            .city("City")
            .build())
        .price(PRICE)
        .items(List.of(OrderItemDTO.builder()
            .productId(PRODUCT_ID)
                .quantity(1)
                .price(new BigDecimal("50.00"))
                .subTotal(new BigDecimal("50.00"))
                .build(),
            OrderItemDTO.builder()
                .productId(PRODUCT_ID)
                .quantity(3)
                .price(new BigDecimal("50.00"))
                .subTotal(new BigDecimal("150.00"))
                .build()))
        .build();

    createOrderCommandWrongPrice = CreateOrderCommand.builder()
        .customerId(CUSTOMER_ID)
        .restaurantId(RESTAURANT_ID)
        .address(OrderAddress.builder()
            .street("Street")
            .postalCode("12345")
            .city("City")
            .build())
        .price(new BigDecimal("250.00"))
        .items(List.of(OrderItemDTO.builder()
                .productId(PRODUCT_ID)
                .quantity(1)
                .price(new BigDecimal("50.00"))
                .subTotal(new BigDecimal("50.00"))
                .build(),
            OrderItemDTO.builder()
                .productId(PRODUCT_ID)
                .quantity(3)
                .price(new BigDecimal("50.00"))
                .subTotal(new BigDecimal("150.00"))
                .build()))
        .build();

    createOrderCommandWrongProductPrice = CreateOrderCommand.builder()
        .customerId(CUSTOMER_ID)
        .restaurantId(RESTAURANT_ID)
        .address(OrderAddress.builder()
            .street("Street")
            .postalCode("12345")
            .city("City")
            .build())
        .price(new BigDecimal("210.00"))
        .items(List.of(OrderItemDTO.builder()
                .productId(PRODUCT_ID)
                .quantity(1)
                .price(new BigDecimal("60.00"))
                .subTotal(new BigDecimal("60.00"))
                .build(),
            OrderItemDTO.builder()
                .productId(PRODUCT_ID)
                .quantity(3)
                .price(new BigDecimal("50.00"))
                .subTotal(new BigDecimal("150.00"))
                .build()))
        .build();

    Customer customer = new Customer();
    customer.setId(new CustomerId(CUSTOMER_ID));

    Restaurant restaurantResponse = Restaurant.builder()
        .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
        .products(List.of(new Product(new ProductId(PRODUCT_ID), "product-1", new Money(new BigDecimal("50.00"))),
            new Product(new ProductId(PRODUCT_ID), "product-2", new Money(new BigDecimal("50.00")))))
        .isActive(true)
        .build();

    Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
    order.setId(new OrderId(ORDER_ID));

    when(customerRepository.findCustomer(CUSTOMER_ID)).thenReturn(Optional.of(customer));
    when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
        .thenReturn(Optional.of(restaurantResponse));
    when(orderRepository.save(any(Order.class))).thenReturn(order);
  }

  @Test
  public void testCreateOrder() {
    CreateOrderResponse createOrderResponse = orderApplicationService.createOrder(createOrderCommand);
    assertEquals(OrderStatus.PENDING, createOrderResponse.getOrderStatus());
    assertEquals("Order created successfully", createOrderResponse.getMessage());
    assertNotNull(createOrderResponse.getOrderTrackingId());
  }

  @Test
  public void testCreateOrderWithWrongTotalPrice() {
    OrderDomainException orderDomainException = assertThrows(OrderDomainException.class, () -> orderApplicationService.createOrder(createOrderCommandWrongPrice));
    assertEquals("Total price: 250.00 is not equal to order items total: 200.00!", orderDomainException.getMessage());
  }

  @Test
  public void testCreateOrderWithWrongProductPrice() {
    OrderDomainException orderDomainException = assertThrows(OrderDomainException.class, () -> orderApplicationService.createOrder(createOrderCommandWrongProductPrice));
    assertEquals("Order item price: 60.00 is not valid for product: d1b3b3b3-3b3b-3b3b-3b3b-3b3b3b3b3b04!", orderDomainException.getMessage());
  }

  @Test
  public void testCreateOrderWithNotActiveRestaurant() {
    Restaurant restaurantResponse = Restaurant.builder()
        .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
        .products(List.of(new Product(new ProductId(PRODUCT_ID), "product-1", new Money(new BigDecimal("50.00"))),
            new Product(new ProductId(PRODUCT_ID), "product-2", new Money(new BigDecimal("50.00")))))
        .isActive(false)
        .build();

    when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
        .thenReturn(Optional.of(restaurantResponse));

    OrderDomainException orderDomainException = assertThrows(OrderDomainException.class, () -> orderApplicationService.createOrder(createOrderCommand));
    assertEquals("Restaurant "+ RESTAURANT_ID + " is not active!", orderDomainException.getMessage());
  }
}