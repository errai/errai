package org.jboss.errai.cdi.demo.stock.client.shared;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Mutable companion class to {@link Tick}. Useful for constructing ticks in a
 * more obvious way than the megaconstructor on that class.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class TickBuilder {

  private String symbol;
  private BigDecimal price = BigDecimal.ZERO;
  private BigDecimal change = BigDecimal.ZERO;
  private Date time = new Date();
  private String currencyCode = "USD";

  /**
   * Creates a TickBuilder with all fields set to their defaults.
   */
  public TickBuilder(String symbol) {
    this.symbol = symbol;
  }

  public String getSymbol() {
    return symbol;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public BigDecimal getChange() {
    return change;
  }

  public Date getTime() {
    return time;
  }

  public String getCurrencyCode() {
    return currencyCode;
  }

  public TickBuilder symbol(String symbol) {
    this.symbol = symbol;
    return this;
  }

  public TickBuilder price(BigDecimal price) {
    this.price = price;
    return this;
  }

  public TickBuilder change(BigDecimal change) {
    this.change = change;
    return this;
  }

  public TickBuilder time(Date time) {
    this.time = time;
    return this;
  }

  public TickBuilder currencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
    return this;
  }

  public Tick toTick() {
    return new Tick(symbol, price, change, time, currencyCode);
  }
}
