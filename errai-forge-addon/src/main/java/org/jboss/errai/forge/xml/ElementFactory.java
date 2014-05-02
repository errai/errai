package org.jboss.errai.forge.xml;

import org.w3c.dom.Element;

public interface ElementFactory {

  public Element createElement(String tagName);
  
  public Element importElement(Element element, boolean deep);

}
