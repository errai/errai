package org.jboss.errai.ui.test.stylebinding.client.res;

import org.jboss.errai.databinding.client.api.Bindable;

/**
 * @author Mike Brock
 */
@Bindable
public class TestModel {
  private String testA;
  private String testB;
  private String testC;

  public String getTestA() {
    return testA;
  }

  public void setTestA(String testA) {
    this.testA = testA;
  }

  public String getTestB() {
    return testB;
  }

  public void setTestB(String testB) {
    this.testB = testB;
  }

  public String getTestC() {
    return testC;
  }

  public void setTestC(String testC) {
    this.testC = testC;
  }
}
