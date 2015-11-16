package org.jboss.errai.cdi.producer.client;

/**
 * @author Mike Brock
 */
public class WrappedThang {
  private Thang thang;

  public WrappedThang() {
  }

  public WrappedThang(Thang thang) {
    this.thang = thang;
  }

  public Thang getThang() {
    return thang;
  }

  public void setThang(Thang thing) {
    this.thang = thing;
  }
}
