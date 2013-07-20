package org.jboss.errai.ui.test.quickhandler.client.res;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.SinkNative;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

@Templated
public class QuickHandlerComponent extends Composite {

  @DataField
  private final AnchorElement c1 = DOM.createAnchor().cast();

  @Inject
  @DataField
  private Button c2;

  private boolean c0EventFired = false;
  private boolean c1EventFired = false;
  private boolean c1_dupEventFired = false;
  private boolean c2EventFired = false; 
  private boolean thisEventFired = false;
  private final boolean c0EventFired2 = false;


  public AnchorElement getC1() {
    return c1;
  }

  public Button getC2() {
    return c2;
  }

  @EventHandler("c0")
  @SinkNative(Event.ONCLICK | Event.ONFOCUS)
  private void doSomethingC0(Event e) {
    c0EventFired = true;
  }

  @EventHandler("c1")
  private void doSomethingC1(ClickEvent e) {
    c1EventFired = true;
  }

  @EventHandler("c1")
  public void doSomethingC1_dup(ClickEvent e) {
    c1_dupEventFired = true;
  }

  @EventHandler("c2")
  public void doSomethingC2(ClickEvent e) {
    c2EventFired = true;
  }
  
  @EventHandler
  public void doSomethingOnThis(ClickEvent e) {
    thisEventFired = true;
  }

  public boolean isC0EventFired() {
    return c0EventFired;
  }

  public boolean isC0EventFired2() {
    return c0EventFired2;
  }

  public boolean isC1EventFired() {
    return c1EventFired;
  }

  public boolean isC1_dupEventFired() {
    return c1_dupEventFired;
  }

  public boolean isC2EventFired() {
    return c2EventFired;
  }

  public boolean isThisEventFired() {
    return thisEventFired;
  }
}
