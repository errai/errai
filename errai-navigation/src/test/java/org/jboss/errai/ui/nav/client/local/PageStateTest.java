package org.jboss.errai.ui.nav.client.local;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ui.nav.client.local.spi.NavigationGraph;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithExtraState;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

public class PageStateTest extends AbstractErraiCDITest {

  private IOCBeanManager beanManager = IOC.getBeanManager();
  private Navigation navigation;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.nav.NavigationTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    disableBus = true;
    super.gwtSetUp();
    navigation = beanManager.lookupBean(Navigation.class).getInstance();
  }

  private static void assertAllFieldsHaveDefaultValues(PageWithExtraState page) {
    assertNull(page.getStringThing());

    assertEquals((byte) 0, page.getByteThing());
    assertEquals((short) 0, page.getShortThing());
    assertEquals(0, page.getIntThing());
    assertEquals(0L, page.getLongThing());
    assertEquals(0f, page.getFloatThing(), 0f);
    assertEquals(0.0, page.getDoubleThing(), 0.0);
    assertEquals(false, page.getBoolThing());

    assertNull(page.getBoxedByteThing());
    assertNull(page.getBoxedShortThing());
    assertNull(page.getBoxedIntThing());
    assertNull(page.getBoxedLongThing());
    assertNull(page.getBoxedFloatThing());
    assertNull(page.getBoxedDoubleThing());
    assertNull(page.getBoxedBoolThing());

    assertNotNull(page.getStringCollection());
    assertNotNull(page.getStringList());
    assertNotNull(page.getStringSet());
    assertNotNull(page.getIntList());
    assertTrue(page.getStringCollection().isEmpty());
    assertTrue(page.getStringList().isEmpty());
    assertTrue(page.getStringSet().isEmpty());
    assertTrue(page.getIntList().isEmpty());
  }

  /**
   * If a {@code @PageState} field has no corresponding parameter in the history
   * token, its value should be set to default (0 or null) when we call
   * putState().
   */
  public void testAbsentParameterGivesDefault() throws Exception {
    PageWithExtraState page = beanManager.lookupBean(PageWithExtraState.class).getInstance();

    assertAllFieldsHaveDefaultValues(page);

    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String,String>builder()
            .put("stringThing", "string")
            .put("byteThing", "12")
            .put("shortThing", "123")
            .put("intThing", "1234")
            .put("longThing", "12345")
            .put("floatThing", "1.2")
            .put("doubleThing", "1.23")
            .put("boolThing", "true")
            .put("boxedByteThing", "12")
            .put("boxedShortThing", "123")
            .put("boxedIntThing", "1234")
            .put("boxedLongThing", "12345")
            .put("boxedFloatThing", "1.2")
            .put("boxedDoubleThing", "1.23")
            .put("boxedBoolThing", "true")
            .put("stringList", "0")
            .put("stringList", "1")
            .put("stringList", "0")
            .put("stringSet", "0")
            .put("stringSet", "1")
            .put("stringSet", "0")
            .put("stringCollection", "0")
            .put("stringCollection", "1")
            .put("stringCollection", "0")
            .put("intList", "0")
            .put("intList", "1")
            .put("intList", "0")
            .build());

    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String,String>of());

    // this is the point of the test: the empty goTo() should have reset all state fields
    assertAllFieldsHaveDefaultValues(page);
  }

  public void testPassAllStateTokens() throws Exception {
    PageWithExtraState page = beanManager.lookupBean(PageWithExtraState.class).getInstance();

    assertAllFieldsHaveDefaultValues(page);

    ImmutableMultimap<String, String> stateValues = ImmutableMultimap.<String,String>builder()
            .put("stringThing", "string")
            .put("byteThing", "12")
            .put("shortThing", "123")
            .put("intThing", "1234")
            .put("longThing", "12345")
            .put("floatThing", "1.2")
            .put("doubleThing", "1.23")
            .put("boolThing", "true")
            .put("boxedByteThing", "12")
            .put("boxedShortThing", "123")
            .put("boxedIntThing", "1234")
            .put("boxedLongThing", "12345")
            .put("boxedFloatThing", "1.2")
            .put("boxedDoubleThing", "1.23")
            .put("boxedBoolThing", "true")
            .put("stringList", "0")
            .put("stringList", "1")
            .put("stringList", "0")
            .put("stringSet", "0")
            .put("stringSet", "1")
            .put("stringSet", "0")
            .put("stringCollection", "0")
            .put("stringCollection", "1")
            .put("stringCollection", "0")
            .put("intList", "0")
            .put("intList", "1")
            .put("intList", "0")
            .build();

    navigation.goTo(PageWithExtraState.class, stateValues);

    // ensure all values were set on the page object
    assertEquals("string", page.getStringThing());

    assertEquals((byte) 12, page.getByteThing());
    assertEquals((short) 123, page.getShortThing());
    assertEquals(1234, page.getIntThing());
    assertEquals(12345L, page.getLongThing());
    assertEquals(1.2f, page.getFloatThing(), 0f);
    assertEquals(1.23, page.getDoubleThing(), 0.0);
    assertEquals(true, page.getBoolThing());

    assertEquals(Byte.valueOf("12"), page.getBoxedByteThing());
    assertEquals(Short.valueOf("123"), page.getBoxedShortThing());
    assertEquals(Integer.valueOf("1234"), page.getBoxedIntThing());
    assertEquals(Long.valueOf("12345"), page.getBoxedLongThing());
    assertEquals(Float.valueOf("1.2"), page.getBoxedFloatThing(), 0f);
    assertEquals(Double.valueOf("1.23"), page.getBoxedDoubleThing(), 0.0);
    assertEquals(Boolean.TRUE, page.getBoxedBoolThing());

    assertEquals(Arrays.asList("0", "1", "0"), page.getStringList());
    assertEquals(Arrays.asList("0", "1", "0"), page.getStringCollection());
    assertEquals(new HashSet<String>(Arrays.asList("0", "1", "0")), page.getStringSet());
    assertEquals(Arrays.asList(0, 1, 0), page.getIntList());

    // finally, ensure getState() properly reconstitutes the map we started with
    PageNode<PageWithExtraState> pageNode = navigation.getNavGraph().getPage(PageWithExtraState.class);
    Multimap<String, String> refetchedState = pageNode.getState(page);
    Multimap<String, String> expectedState = ArrayListMultimap.create(stateValues);

    // the set loses an entry during conversion (that's correct behaviour); test it separately
    Set<String> expectedSet = new HashSet<String>(expectedState.removeAll("stringSet"));
    Set<String> actualSet = new HashSet<String>(refetchedState.removeAll("stringSet"));
    assertEquals(expectedSet, actualSet);

    // and everything that remains should come out identically
    assertEquals(expectedState, refetchedState);
  }

  /**
   * If there are multiple values for the same key, but the corresponding
   * {@code @PageState} field in the page is not a collection, the field should
   * get the <i>first</i> value for its key.
   */
  public void testScalarGetsFirstValueInToken() throws Exception {
    PageWithExtraState page = beanManager.lookupBean(PageWithExtraState.class).getInstance();
    assertNull(page.getStringThing());

    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.of(
            "nonexistent", "====",
            "stringThing", "string0",
            "stringThing", "string1",
            "stringThing", "string2"));

    assertEquals("string0", page.getStringThing());
  }

  public void testReadNullsWithGetState() throws Exception {
    PageWithExtraState page = beanManager.lookupBean(PageWithExtraState.class).getInstance();

    assertAllFieldsHaveDefaultValues(page);

    NavigationGraph navGraph = navigation.getNavGraph();
    PageNode<PageWithExtraState> pageNode = navGraph.getPage(PageWithExtraState.class);
    Multimap<String, String> state = pageNode.getState(page);
    assertTrue("Default state map should only contain the primitives, but was: " + state,
            state.keySet().equals(ImmutableSet.of(
                    "byteThing", "shortThing", "intThing", "longThing", "floatThing", "doubleThing", "boolThing")));
  }

}
