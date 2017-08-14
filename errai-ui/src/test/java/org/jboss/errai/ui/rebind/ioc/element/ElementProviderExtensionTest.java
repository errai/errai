package org.jboss.errai.ui.rebind.ioc.element;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.junit.Test;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.jboss.errai.ui.rebind.ioc.element.ElementProviderExtension.elemental2ElementTags;
import static org.junit.Assert.assertEquals;

public class ElementProviderExtensionTest {

  @Test
  public void elemental2ElementTag_customElement() {
    final Collection<String> tags = elemental2ElementTags(metaClass(CustomElement.class));
    assertEquals(singletonList("div"), tags);
  }

  @Test
  public void elemental2ElementTag_customElementChild() {
    final Collection<String> tags = elemental2ElementTags(metaClass(CustomElement.Child.class));
    assertEquals(singletonList("div"), tags);
  }

  @Test
  public void elemental2ElementTag_customElementWithCustomTag() {
    final Collection<String> tags = elemental2ElementTags(metaClass(CustomElement.WithCustomTag.class));
    assertEquals(singletonList("foo"), tags);
  }


  @Test
  public void elemental2ElementTag_customElementWithCustomTagChild() {
    final Collection<String> tags = elemental2ElementTags(
            metaClass(CustomElement.WithCustomTag.ChildWithoutElementAnnotation.class));

    assertEquals(emptyList(), tags);
  }

  @Test
  public void elemental2ElementTag_customElementWithCustomTagChildWithElementAnnotation() {

    final Collection<String> tags = elemental2ElementTags(
            metaClass(CustomElement.WithCustomTag.ChildWithoutElementAnnotation.class));

    assertEquals(emptyList(), tags);
  }

  @Test
  public void elemental2ElementTag_customElementWithCustomTagChildWithCustomTag() {

    final Collection<String> tags = elemental2ElementTags(
            metaClass(CustomElement.WithCustomTag.ChildWithCustomTag.class));

    assertEquals(singletonList("sub-foo"), tags);
  }

  private static MetaClass metaClass(final Class<?> clazz) {
    return JavaReflectionClass.newInstance(clazz);
  }
}