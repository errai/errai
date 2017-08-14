package org.jboss.errai.ui.rebind.ioc.element;

import elemental2.dom.HTMLDivElement;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.jboss.errai.ui.rebind.ioc.element.Elemental2TagMapping.getTags;
import static org.junit.Assert.assertEquals;

public class Elemental2TagMappingTest {

  @Test
  public void testGetTags() {
    assertEquals("null should return no tag", emptyList(), getTags(null));

    assertEquals("Object.class should not have any mapped tag name", emptyList(), getTags(Object.class));

    assertEquals("String.class should not have any mapped tag name", emptyList(), getTags(String.class));

    assertEquals("HTMLDivElement should have a tag mapped to it", singletonList("div"), getTags(HTMLDivElement.class));

    assertEquals("HTMLDivElement subclass should have a tag mapped to it", singletonList("div"),
            getTags(CustomElement.class));

    assertEquals("HTMLDivElement subclass should have a tag mapped to it", singletonList("div"),
            getTags(CustomElement.Child.class));
  }

}