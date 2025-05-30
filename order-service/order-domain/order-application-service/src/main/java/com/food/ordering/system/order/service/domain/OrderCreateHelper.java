package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class OrderCreateHelper {
  private final OrderDomainService orderDomainService;
  private final OrderRepository orderRepository;
  private final CustomerRepository customerRepository;
  private final RestaurantRepository restaurantRepository;
  private final OrderDataMapper orderDataMapper;

  public OrderCreateHelper(OrderDomainService orderDomainService,
                           OrderRepository orderRepository,
                           CustomerRepository customerRepository,
                           RestaurantRepository restaurantRepository,
                           OrderDataMapper orderDataMapper) {
    this.orderDomainService = orderDomainService;
    this.orderRepository = orderRepository;
    this.customerRepository = customerRepository;
    this.restaurantRepository = restaurantRepository;
    this.orderDataMapper = orderDataMapper;
  }

  @Transactional
  public OrderCreatedEvent persistOrder(CreateOrderCommand createOrderCommand) {
    checkCustomer(createOrderCommand.getCustomerId());
    Restaurant restaurant = checkRestaurant(createOrderCommand);
    Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
    OrderCreatedEvent orderCreatedEvent = orderDomainService.validateAndInitiateOrder(order, restaurant);
    saveOrder(order);
    log.info("Order created event: {}", orderCreatedEvent);
    return orderCreatedEvent;
  }

  private Restaurant checkRestaurant(CreateOrderCommand createOrderCommand) {
    Restaurant restaurant = orderDataMapper.createOrderCommandToRestaurant(createOrderCommand);
    Optional<Restaurant> optionalRestaurant = restaurantRepository.findRestaurantInformation(restaurant);

    if (optionalRestaurant.isEmpty()) {
      log.warn("Restaurant not found: {}", createOrderCommand.getRestaurantId());
      throw new OrderDomainException("Restaurant " + createOrderCommand.getRestaurantId() + " not found");
    }
    return optionalRestaurant.get();
  }

  private void checkCustomer(@NotNull UUID customerId) {
    Optional<Customer> customer = customerRepository.findCustomer(customerId);

    if (customer.isEmpty()) {
      log.warn("Customer not found: {}", customerId);
      throw new OrderDomainException("Customer " + customerId + " not found");
    }
  }

  private Order saveOrder(Order order) {
    Order orderResult = orderRepository.save(order);

    if (orderResult == null) {
      log.warn("Order not saved: {}", order.getId().getValue());
      throw new OrderDomainException("Order " + order.getId().getValue() + " not saved");
    }

    log.info("Order saved: {}", order.getId().getValue());
    return orderResult;
  }
}
