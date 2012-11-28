package org.jboss.errai.ui.nav.client.local.testpages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;

import com.google.gwt.user.client.ui.SimplePanel;

@ApplicationScoped
@Page
public class PageWithExtraState extends SimplePanel {

  @PageState private String stringThing;

  // primitives
  @PageState private byte    byteThing;
  @PageState private short   shortThing;
  @PageState private int     intThing;
  @PageState private long    longThing;
  @PageState private double  doubleThing;
  @PageState private float   floatThing;
  @PageState private boolean boolThing;

  // boxed primitives
  @PageState private Byte    boxedByteThing;
  @PageState private Short   boxedShortThing;
  @PageState private Integer boxedIntThing;
  @PageState private Long    boxedLongThing;
  @PageState private Double  boxedDoubleThing;
  @PageState private Float   boxedFloatThing;
  @PageState private Boolean boxedBoolThing;

  // all supported collection types, of String
  @PageState private Collection<String> stringCollection = new ArrayList<String>();
  @PageState private List<String> stringList = new ArrayList<String>();
  @PageState private Set<String> stringSet = new HashSet<String>();

  // random spot check to ensure collections of a numeric type work
  @PageState private List<Integer> intList = new ArrayList<Integer>();

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

  public Byte getBoxedByteThing() {
    return boxedByteThing;
  }

  public Short getBoxedShortThing() {
    return boxedShortThing;
  }

  public Integer getBoxedIntThing() {
    return boxedIntThing;
  }

  public Long getBoxedLongThing() {
    return boxedLongThing;
  }

  public Double getBoxedDoubleThing() {
    return boxedDoubleThing;
  }

  public Float getBoxedFloatThing() {
    return boxedFloatThing;
  }

  public Boolean getBoxedBoolThing() {
    return boxedBoolThing;
  }

  public Collection<String> getStringCollection() {
    return stringCollection;
  }

  public List<String> getStringList() {
    return stringList;
  }

  public Set<String> getStringSet() {
    return stringSet;
  }

  public List<Integer> getIntList() {
    return intList;
  }

}
