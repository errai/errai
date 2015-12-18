package org.jboss.errai.demo.busstress.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class RecursiveObject {

  private RecursiveObject nextInChain;
  private Long time;
  private String string;
  private String string1;
  private Double aDouble;

  public RecursiveObject getNextInChain() {
    return nextInChain;
  }
  public void setNextInChain(RecursiveObject nextInChain) {
    this.nextInChain = nextInChain;
  }
  public Long getTime() {
    return time;
  }
  public void setTime(Long time) {
    this.time = time;
  }
  public String getString() {
    return string;
  }
  public void setString(String string) {
    this.string = string;
  }
  public String getString1() {
    return string1;
  }
  public void setString1(String string1) {
    this.string1 = string1;
  }
  public Double getaDouble() {
    return aDouble;
  }
  public void setaDouble(Double aDouble) {
    this.aDouble = aDouble;
  }
}
