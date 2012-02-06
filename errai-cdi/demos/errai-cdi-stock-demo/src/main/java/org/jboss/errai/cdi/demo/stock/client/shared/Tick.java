package org.jboss.errai.cdi.demo.stock.client.shared;

import java.math.BigDecimal;
import java.util.Date;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * Immutable representation of a tick (price change) in some tradable instrument.
 *
 * @author Jonathan Fuerth
 */
@Portable
public class Tick {

  private final String symbol;
  private final BigDecimal price;
  private final BigDecimal change;
  private final Date time;
  private final String currencyCode;

  /**
   * Constructs a new Tick with all the given values. For a more convenient API
   * for creating ticks, see {@link TickBuilder}.
   *
   * @param symbol The ticker symbol of the instrument this tick is for
   * @param price The price that the given symbol was traded at
   * @param change The difference in price (+ or -) since the previous tick
   * @param time The timestamp of the trade that caused this tick
   * @param currencyCode The currency the trade happened in
   */
  public Tick(
      @MapsTo("symbol") String symbol,
      @MapsTo("price") BigDecimal price,
      @MapsTo("change") BigDecimal change,
      @MapsTo("time") Date time,
      @MapsTo("currencyCode") String currencyCode) {
    this.symbol = symbol;
    this.price = price;
    this.change = change;
    this.time = time;
    this.currencyCode = currencyCode;
  }

  public String getFormattedPrice() {
    return formatNumber(price);
  }

  public String getFormattedChange() {
    return (change.signum() >= 0 ? "+" : "") + formatNumber(change);
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

  /**
   * Puts a decimal place at the right spot in the number, adding leading zeroes if necessary.
   * <p>
   * This routine is not locale-sensitive, because GWT does not provide a number formatting API that works on both the
   * client and the server.
   *
   * @param num
   *          The number to format (eg, the price or change-in-price of this tick)
   * @return A formatted representation of the number
   */
  private String formatNumber(BigDecimal num) {
    return num.toPlainString();
  }

  @Override
  public String toString() {
    return symbol + ": " + getFormattedPrice() + " (" + getFormattedChange() + ")";
  }
}
