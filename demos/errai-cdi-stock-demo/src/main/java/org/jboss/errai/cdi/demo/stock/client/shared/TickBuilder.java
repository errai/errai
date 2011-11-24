package org.jboss.errai.cdi.demo.stock.client.shared;

import java.util.Date;

import org.jboss.errai.common.client.api.annotations.ExposeEntity;

/**
 * A mutable representation of a tick (price change) in some tradable
 * instrument.
 * 
 * @author jfuerth
 */
@ExposeEntity
public class TickBuilder {

  private String symbol;
  
  // XXX these want to be BigDecimals, but serialization
  // doesn't support those yet. See ERRAI-141.
  private long bid;
  private long ask;
  private long change;
  
  private int decimalPlaces;
  
  private Date time;

  private String currencyCode = "USD";
  
  public TickBuilder() {
    
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public long getBid() {
    return bid;
  }

  public void setBid(long bid) {
    this.bid = bid;
  }

  public long getAsk() {
    return ask;
  }

  public void setAsk(long ask) {
    this.ask = ask;
  }

  public long getChange() {
    return change;
  }
  
  public void setChange(long change) {
    this.change = change;
  }
  
  public int getDecimalPlaces() {
    return decimalPlaces;
  }

  public void setDecimalPlaces(int decimalPlaces) {
    this.decimalPlaces = decimalPlaces;
  }
  
  public String getCurrencyCode() {
    return currencyCode;
  }
  
  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }
  
  public String getFormattedBid() {
    return formatNumber(bid);
  }

  public String getFormattedAsk() {
    return formatNumber(ask);
  }
  
  public String getFormattedChange() {
    return (change >= 0 ? "+" : "") + formatNumber(change);
  }
  
  /**
   * Puts a decimal place at the right spot in the number, adding leading zeroes
   * if necessary.
   * <p>
   * This routine is not locale-sensitive, because GWT does not provide a
   * number formatting API that works on both the client and the server.
   * 
   * @param num The number to format (eg, the bid, ask, or change of this tick)
   * @return A formatted representation of the number
   */
  private String formatNumber(long num) {
    StringBuilder sb = new StringBuilder(String.valueOf(Math.abs(num)));
    int len = sb.length();
    
    if (len == decimalPlaces) {
      sb.insert(0, "0.");
    }
    else if (len < decimalPlaces) {
      for (int i = 0; i < decimalPlaces - len; i++) {
        sb.insert(0, "0");
      }
      sb.insert(0, "0.");
    }
    else {
      sb.insert(len - decimalPlaces, '.');
    }
    
    if (num < 0) {
      sb.insert(0, '-');
    }
    return sb.toString();
  }
  
  @Override
  public String toString() {
    return symbol + ": " + getFormattedBid() + "/" + getFormattedAsk() + " (" + getFormattedChange() + ")";
  }
}
