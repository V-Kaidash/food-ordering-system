package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
public class OrderTrackCommandHandler {
  private final OrderDataMapper orderDataMapper;
  private final OrderRepository orderRepository;

  public OrderTrackCommandHandler(OrderDataMapper orderDataMapper, OrderRepository orderRepository) {
    this.orderDataMapper = orderDataMapper;
    this.orderRepository = orderRepository;
  }

  @Transactional(readOnly = true)
  public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
    Optional<Order> optionalOrder = orderRepository.findOrderByTrackingId(new TrackingId(trackOrderQuery.getOrderTrackingId()));

    if (optionalOrder.isEmpty()) {
      log.warn("Order not found: {}", trackOrderQuery.getOrderTrackingId());
      throw new OrderNotFoundException("Order with tracking id: " + trackOrderQuery.getOrderTrackingId() + " not found");
    }

    return orderDataMapper.orderToTrackOrderResponse(optionalOrder.get());
  }
}
