package org.jboss.errai.ui.nav.test.client.local;

import org.jboss.errai.ui.nav.client.local.HistoryToken;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.junit.client.GWTTestCase;

public class HistoryTokenTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.nav.test.NavigationTest";
  }

  public void testNameOnly() throws Exception {
    HistoryToken token = HistoryToken.parse("PageName");
    assertEquals("PageName", token.getPageName());
    assertTrue(token.getState().isEmpty());
  }

  public void testNameAndOneParam() throws Exception {
    HistoryToken token = HistoryToken.parse("PageName;key=value");
    assertEquals("PageName", token.getPageName());
    assertEquals("Unexpected state map contents: " + token.getState(), 1, token.getState().size());
    assertEquals("value", token.getState().get("key").iterator().next());
  }

  public void testStateThatNeedsEscaping() throws Exception {
    Multimap<String, String> state = ImmutableMultimap.<String, String>builder().put("k=ey", "v&a=lue").build();
    String encodedToken = HistoryToken.of("PageName", state).toString();

    HistoryToken token = HistoryToken.parse(encodedToken);
    assertEquals("PageName", token.getPageName());
    assertEquals("Unexpected state map contents: " + token.getState(), 1, token.getState().size());
    assertEquals("Unexpected state map contents: " + token.getState(), 1, token.getState().get("k=ey").size());
    assertEquals("v&a=lue", token.getState().get("k=ey").iterator().next());
  }

  public void testPageNameThatNeedsEscaping() throws Exception {
    Multimap<String, String> state = ImmutableMultimap.<String, String>builder().put("key", "value").build();
    String encodedToken = HistoryToken.of("Pa;ge=Na&me", state).toString();

    HistoryToken token = HistoryToken.parse(encodedToken);
    assertEquals("Pa;ge=Na&me", token.getPageName());
    assertEquals("Unexpected state map contents: " + token.getState(), 1, token.getState().size());
    assertEquals("Unexpected state map contents: " + token.getState(), 1, token.getState().get("key").size());
    assertEquals("value", token.getState().get("key").iterator().next());
  }

}
