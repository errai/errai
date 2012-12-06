package org.jboss.errai.ui.nav.client.local;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.junit.client.GWTTestCase;

public class HistoryTokenTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.nav.NavigationTest";
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

  public void testToStringNoParams() throws Exception {
    String encodedToken = HistoryToken.of("PageName", ImmutableMultimap.<String, String>of()).toString();
    assertEquals("PageName", encodedToken);
  }

  public void testToString1Param() throws Exception {
    String encodedToken = HistoryToken.of("PageName",
            ImmutableMultimap.<String, String>of("p1", "v1"))
            .toString();
    assertEquals("PageName;p1=v1", encodedToken);
  }

  public void testToString2Params() throws Exception {
    String encodedToken = HistoryToken.of("PageName",
            ImmutableMultimap.<String, String>of("p1", "v1", "p2", "v2"))
            .toString();
    assertEquals("PageName;p1=v1&p2=v2", encodedToken);
  }

}
