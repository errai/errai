package org.jboss.errai.ui.nav.test.client.local.testpages;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;

import com.google.gwt.user.client.ui.SimplePanel;

@ApplicationScoped
@Page
public class PageWithExtraState extends SimplePanel {

  @PageState private String stringThing;
  @PageState private byte byteThing;
  @PageState private short shortThing;
  @PageState private int intThing;
  @PageState private long longThing;
  @PageState private double doubleThing;
  @PageState private float floatThing;
  @PageState private boolean boolThing;

  // TODO include fields for all the types we want to support

  public String getStringThing() {
    return stringThing;
  }

  public byte getByteThing() {
    return byteThing;
  }

  public short getShortThing() {
    return shortThing;
  }

  public int getIntThing() {
    return intThing;
  }

  public long getLongThing() {
    return longThing;
  }

  public double getDoubleThing() {
    return doubleThing;
  }

  public float getFloatThing() {
    return floatThing;
  }

  public boolean getBoolThing() {
    return boolThing;
  }
}
