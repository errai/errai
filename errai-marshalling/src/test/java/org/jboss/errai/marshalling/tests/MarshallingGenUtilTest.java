package org.jboss.errai.marshalling.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;
import org.junit.Test;

public class MarshallingGenUtilTest {

  @Test
  public void testGetConcreteElementType() throws Exception {
    class Container {
      @SuppressWarnings("unused")
      List<String> list;
    }
    MetaClass listType = MetaClassFactory.get(Container.class).getDeclaredField("list").getType();
    assertEquals("This test is supposed to use the Java Reflection implementations", JavaReflectionClass.class, listType.getClass());
    MetaClass elementType = MarshallingGenUtil.getConcreteElementType(listType);
    assertEquals("java.lang.String", elementType.getFullyQualifiedName());
    assertEquals(MetaClassFactory.get(String.class), elementType);
  }

  @Test
  public void testGetWildcardElementType() throws Exception {
    class Container {
      @SuppressWarnings("unused")
      List<? extends String> list;
    }
    MetaClass listType = MetaClassFactory.get(Container.class).getDeclaredField("list").getType();
    assertEquals("This test is supposed to use the Java Reflection implementations", JavaReflectionClass.class, listType.getClass());
    MetaClass elementType = MarshallingGenUtil.getConcreteElementType(listType);
    assertNull("getConcreteElementType must return null for wildcard element types", elementType);
  }

}
