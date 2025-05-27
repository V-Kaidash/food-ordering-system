package com.food.ordering.system.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Money {
  public static final Money ZERO = new Money(BigDecimal.ZERO);
  private final BigDecimal amount;
  
  public Money(BigDecimal amount) {
    this.amount = amount;
  }
  
  public BigDecimal getAmount() {
    return amount;
  }
  
  public boolean isGreaterThanZero() {
    return this.amount.compareTo(BigDecimal.ZERO) > 0;
  }
  
  public boolean isGreaterThan(Money money) {
    return this.amount.compareTo(money.amount) > 0;
  }
  
  public Money add(Money money) {
    return new Money(setScale(this.amount.add(money.amount)));
  }
  
  public Money subtract(Money money) {
    return new Money(setScale(this.amount.subtract(money.amount)));
  }
  
  public Money multiply(BigDecimal value) {
    return new Money(setScale(this.amount.multiply(value)));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Money money = (Money) o;
    return Objects.equals(amount, money.amount);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(amount);
  }
  
  private BigDecimal setScale(BigDecimal value) {
    return value.setScale(2, RoundingMode.HALF_EVEN);
  }
}
