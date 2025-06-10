package com.food.ordering.system.order.service.dataaccess.order.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEntityId implements Serializable {
  private Long id;
  private OrderEntity order;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    OrderItemEntityId that = (OrderItemEntityId) o;
    return Objects.equals(id, that.id) && Objects.equals(order, that.order);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, order);
  }
}
