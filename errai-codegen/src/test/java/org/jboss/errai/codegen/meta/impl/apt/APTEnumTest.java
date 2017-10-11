package org.jboss.errai.codegen.meta.impl.apt;

import org.jboss.errai.codegen.apt.test.ErraiAptTest;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class APTEnumTest extends ErraiAptTest {

  private MetaEnum fooMetaEnum;
  private MetaEnum barMetaEnum;

  @Before
  public void before() {

    final MetaEnum[] enums = new APTClass(getTypeElement(TestAnnotatedClass5.class).asType()).getAnnotation(
            TestAnnotationWithArrayProperties.class).get().valueAsArray("enums", MetaEnum[].class);

    fooMetaEnum = enums[0];
    barMetaEnum = enums[1];
  }

  @Test
  public void testInstanceOf() {
    Assert.assertTrue(fooMetaEnum instanceof APTEnum);
    Assert.assertTrue(barMetaEnum instanceof APTEnum);
  }

  @Test
  public void testAs() {
    Assert.assertEquals(TestEnum.Foo, fooMetaEnum.as(TestEnum.class));
    Assert.assertEquals(TestEnum.Bar, barMetaEnum.as(TestEnum.class));
  }

  @Test
  public void testName() {
    Assert.assertEquals("Foo", fooMetaEnum.name());
    Assert.assertEquals("Bar", barMetaEnum.name());
  }

  @Test
  public void testGetDeclaringCass() {
    Assert.assertEquals(MetaClassFactory.get(TestEnum.class), fooMetaEnum.getDeclaringClass());
    Assert.assertEquals(MetaClassFactory.get(TestEnum.class), barMetaEnum.getDeclaringClass());
  }

}