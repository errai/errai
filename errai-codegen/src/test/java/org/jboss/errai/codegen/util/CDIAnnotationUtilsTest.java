package org.jboss.errai.codegen.util;

import org.jboss.errai.codegen.meta.impl.java.JavaReflectionAnnotation;
import org.junit.Assert;
import org.junit.Test;

import javax.enterprise.util.Nonbinding;
import java.lang.annotation.Annotation;

public class CDIAnnotationUtilsTest {

  @Test
  public void testEquals() {
    assertEquals(foo(""), foo(""));
    assertEquals(foo("foo"), foo("foo"));
    assertEquals(fooBarNonBinding("", ""), fooBarNonBinding("", "bar"));
    assertEquals(fooBarNonBinding("foo", ""), fooBarNonBinding("foo", "bar"));
    assertEquals(fooBarNonBinding("foo", "bar"), fooBarNonBinding("foo", "bar"));
    assertEquals(fooArray(), fooArray());
    assertEquals(fooArray(""), fooArray(""));
    assertEquals(fooArray("foo"), fooArray("foo"));
    assertEquals(fooNested(foo("")), fooNested(foo("")));
    assertEquals(fooNested(foo("foo")), fooNested(foo("foo")));
    assertEquals(fooNested(foo("bar")), fooNested(foo("bar")));
    assertEquals(complex(), complex());
    assertEquals(complexEmpty(), complexEmpty());

    assertNotEquals(foo(""), bar(""));
    assertNotEquals(foo(""), fooBar("", ""));
    assertNotEquals(foo(""), fooBarNonBinding("", ""));
    assertNotEquals(foo("foo"), fooBarNonBinding("foo", "bar"));
    assertNotEquals(foo("foo"), foo(""));
    assertNotEquals(foo("foo"), foo("bar"));
    assertNotEquals(foo("foo"), bar("foo"));
    assertNotEquals(foo("foo"), bar("bar"));
    assertNotEquals(foo("foo"), bar(""));
    assertNotEquals(foo("bar"), fooBar("bar", "foo"));
    assertNotEquals(foo("bar"), fooBar("foo", "bar"));
    assertNotEquals(foo("foo"), fooBar("foo", "foo"));

    assertNotEquals(fooArray(), fooArray(""));
    assertNotEquals(fooArray(""), fooArray("", ""));
    assertNotEquals(fooArray("foo"), fooArray(""));
    assertNotEquals(fooArray("foo"), fooArray("bar"));
    assertNotEquals(fooArray("foo"), fooArray("foo", "foo"));
    assertNotEquals(fooArray("bar"), fooArray("foo", "bar"));
    assertNotEquals(fooArray("bar"), fooArray("bar", "foo"));
    assertNotEquals(fooArray("bar"), foo("bar"));
    assertNotEquals(fooArray("bar"), bar("bar"));
    assertNotEquals(fooArray("bar"), fooBar("bar", "bar"));
    assertNotEquals(fooArray("bar"), fooBar("bar", "foo"));
    assertNotEquals(fooArray("bar"), fooBar("foo", "foo"));
    assertNotEquals(fooArray("bar"), fooBarNonBinding("bar", "bar"));
    assertNotEquals(fooArray("bar"), fooBarNonBinding("foo", "foo"));
    assertNotEquals(fooArray("bar"), fooBarNonBinding("bar", ""));
    assertNotEquals(fooArray("bar"), fooBarNonBinding("foo", ""));

    assertNotEquals(fooNested(foo("")), fooNested(foo("foo")));
    assertNotEquals(fooNested(foo("")), foo("foo"));
    assertNotEquals(fooNested(foo("foo")), fooNested(foo("bar")));
    assertNotEquals(fooNested(foo("foo")), bar("foo"));
    assertNotEquals(fooNested(foo("bar")), fooBarNonBinding("bar", ""));
    assertNotEquals(fooNested(foo("bar")), fooBarNonBinding("foo", ""));

    assertNotEquals(fooBarNonBinding("", ""), fooBarNonBinding("foo", "bar"));
    assertNotEquals(fooBarNonBinding("foo", ""), fooBarNonBinding("", "bar"));
    assertNotEquals(fooBarNonBinding("foo", "bar"), fooBarNonBinding("bar", "bar"));

    assertNotEquals(complex(), complexEmpty());
  }

  private Complex complex() {
    return complex(foo(""), new Bar[] { bar("foo"), bar("bar") }, "str", new Class[] { String.class, Long.class },
            Exception.class, new long[] { 1L, -1L }, 'c', TestEnum.Foo, new TestEnum[] { TestEnum.Foo, TestEnum.Bar });
  }

  private Complex complexEmpty() {
    return complex(foo(""), new Bar[0], "", new Class[0], Exception.class, new long[0], ' ', TestEnum.Foo,
            new TestEnum[0]);
  }

  @Test
  public void testHashCode() {
    assertHashCodeEquals(12899898, foo(""));
    assertHashCodeEquals(12933884, foo("foo"));
    assertHashCodeEquals(12954153, foo("bar"));

    assertHashCodeEquals(12356973, bar(""));
    assertHashCodeEquals(12386731, bar("foo"));
    assertHashCodeEquals(12448126, bar("bar"));

    assertHashCodeEquals(25256871, fooBar("", ""));
    assertHashCodeEquals(25348024, fooBar("", "bar"));
    assertHashCodeEquals(25290857, fooBar("foo", ""));
    assertHashCodeEquals(25320615, fooBar("foo", "foo"));
    assertHashCodeEquals(25382010, fooBar("foo", "bar"));
    assertHashCodeEquals(25340884, fooBar("bar", "foo"));

    assertHashCodeEquals(12899899, fooArray());
    assertHashCodeEquals(12899877, fooArray(""));
    assertHashCodeEquals(12933855, fooArray("foo"));
    assertHashCodeEquals(12899835, fooArray("", ""));
    assertHashCodeEquals(16046465, fooArray("foo", ""));
    assertHashCodeEquals(15391796, fooArray("bar", ""));
    assertHashCodeEquals(12928701, fooArray("", "foo"));
    assertHashCodeEquals(12954094, fooArray("", "bar"));
    assertHashCodeEquals(16075451, fooArray("foo", "foo"));
    assertHashCodeEquals(16080372, fooArray("foo", "bar"));
    assertHashCodeEquals(15417582, fooArray("bar", "foo"));

    assertHashCodeEquals(0, fooNested(foo("")));
    assertHashCodeEquals(101574, fooNested(foo("foo")));
    assertHashCodeEquals(97299, fooNested(foo("bar")));

    assertHashCodeEquals(0, barNested(bar("")));
    assertHashCodeEquals(101574, barNested(bar("foo")));
    assertHashCodeEquals(97299, barNested(bar("bar")));

    assertHashCodeEquals(12899898, fooBarNonBinding("", ""));
    assertHashCodeEquals(12899898, fooBarNonBinding("", "bar"));
    assertHashCodeEquals(12899898, fooBarNonBinding("", "foo"));

    assertHashCodeEquals(12933884, fooBarNonBinding("foo", ""));
    assertHashCodeEquals(12933884, fooBarNonBinding("foo", "bar"));
    assertHashCodeEquals(12933884, fooBarNonBinding("foo", "foo"));

    assertHashCodeEquals(12954153, fooBarNonBinding("bar", ""));
    assertHashCodeEquals(12954153, fooBarNonBinding("bar", "foo"));
    assertHashCodeEquals(12954153, fooBarNonBinding("bar", "bar"));
  }

  private static void assertHashCodeEquals(final int expected, final Annotation a1) {
    Assert.assertEquals(expected, CDIAnnotationUtils.hashCode(a1));
    Assert.assertEquals(expected, CDIAnnotationUtils.hashCode(new JavaReflectionAnnotation(a1)));
  }

  private static void assertEquals(final Annotation a1, final Annotation a2) {
    Assert.assertTrue(CDIAnnotationUtils.equals(a1, a2));
    Assert.assertTrue(CDIAnnotationUtils.equals(a2, a1));
    Assert.assertTrue(CDIAnnotationUtils.equals(new JavaReflectionAnnotation(a1), new JavaReflectionAnnotation(a2)));
    Assert.assertTrue(CDIAnnotationUtils.equals(new JavaReflectionAnnotation(a2), new JavaReflectionAnnotation(a1)));
  }

  private static void assertNotEquals(final Annotation a1, final Annotation a2) {
    Assert.assertFalse(CDIAnnotationUtils.equals(a1, a2));
    Assert.assertFalse(CDIAnnotationUtils.equals(a2, a1));
    Assert.assertFalse(CDIAnnotationUtils.equals(new JavaReflectionAnnotation(a1), new JavaReflectionAnnotation(a2)));
    Assert.assertFalse(CDIAnnotationUtils.equals(new JavaReflectionAnnotation(a2), new JavaReflectionAnnotation(a1)));
  }

  public @interface Foo {
    String foo();
  }

  public @interface Bar {
    String bar();
  }

  public @interface FooBar {
    String foo();

    String bar();
  }

  public @interface FooBarNonbinding {
    String foo();

    @Nonbinding String bar();
  }

  public @interface FooArray {
    String[] foo();
  }

  public @interface FooNested {
    Foo foo();
  }

  public @interface BarNested {
    Bar bar();
  }

  public @interface Complex {
    Foo foo();

    Bar[] barArray();

    String string();

    Class clazz();

    Class[] classArray();

    long[] pLongArray();

    char pChar();

    TestEnum testEnum();

    TestEnum[] testEnumArray();
  }

  enum TestEnum {
    Foo, Bar;
  }

  private Foo foo(final String foo) {
    return new Foo() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return Foo.class;
      }

      @Override
      public String foo() {
        return foo;
      }
    };
  }

  private Bar bar(final String bar) {
    return new Bar() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return Bar.class;
      }

      @Override
      public String bar() {
        return bar;
      }
    };
  }

  private FooBar fooBar(final String foo, final String bar) {
    return new FooBar() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return FooBar.class;
      }

      @Override
      public String foo() {
        return foo;
      }

      @Override
      public String bar() {
        return bar;
      }
    };
  }

  private FooBarNonbinding fooBarNonBinding(final String foo, final String bar) {
    return new FooBarNonbinding() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return FooBarNonbinding.class;
      }

      @Override
      public String foo() {
        return foo;
      }

      @Override
      public String bar() {
        return bar;
      }
    };
  }

  private FooArray fooArray(final String... foo) {
    return new FooArray() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return FooArray.class;
      }

      @Override
      public String[] foo() {
        return foo;
      }
    };
  }

  private FooNested fooNested(final Foo foo) {
    return new FooNested() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return FooNested.class;
      }

      @Override
      public Foo foo() {
        return foo;
      }
    };
  }

  private BarNested barNested(final Bar bar) {
    return new BarNested() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return BarNested.class;
      }

      @Override
      public Bar bar() {
        return bar;
      }
    };
  }

  private Complex complex(Foo foo,
          Bar[] barArray,
          String string,
          Class[] classArray,
          Class clazz,
          long[] pLongArray,
          char pChar,
          TestEnum testEnum,
          TestEnum[] testEnumArray) {

    return new Complex() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return Complex.class;
      }

      @Override
      public Foo foo() {
        return foo;
      }

      @Override
      public Bar[] barArray() {
        return barArray;
      }

      @Override
      public String string() {
        return string;
      }

      @Override
      public Class clazz() {
        return clazz;
      }

      @Override
      public Class[] classArray() {
        return classArray;
      }

      @Override
      public long[] pLongArray() {
        return pLongArray;
      }

      @Override
      public char pChar() {
        return pChar;
      }

      @Override
      public TestEnum testEnum() {
        return testEnum;
      }

      @Override
      public TestEnum[] testEnumArray() {
        return testEnumArray;
      }
    };
  }

}