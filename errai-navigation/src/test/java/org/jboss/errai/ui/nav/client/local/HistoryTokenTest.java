/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.nav.client.local;


import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.junit.client.GWTTestCase;

public class HistoryTokenTest extends GWTTestCase {
  private URLPatternMatcher patternMatcher;
  private HistoryTokenFactory htFactory;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    patternMatcher = new URLPatternMatcher();
    patternMatcher.add("PageWithPath/{path}", "PageWithPath");
    patternMatcher.add("PageName", "PageName");
    patternMatcher.add("Pa;ge=Na&me", "EscapedPage");
    htFactory = new HistoryTokenFactory(patternMatcher);
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.nav.NavigationTest";
  }

  public void testNameOnly() throws Exception {
    HistoryToken token = patternMatcher.parseURL("PageName");
    assertEquals("PageName", token.getPageName());
    assertTrue(token.getState().isEmpty());
  }

  public void testNameAndOneParam() throws Exception {
    HistoryToken token = patternMatcher.parseURL("PageName;key=value");
    assertEquals("PageName", token.getPageName());
    assertEquals("Unexpected state map contents: " + token.getState(), 1, token.getState().size());
    assertEquals("value", token.getState().get("key").iterator().next());
  }

  public void testStateThatNeedsEscaping() throws Exception {
    Multimap<String, String> state = ImmutableMultimap.<String, String>builder().put("k=ey", "v&a=lue").build();
    String encodedToken = htFactory.createHistoryToken("PageName", state).toString();

    HistoryToken token = patternMatcher.parseURL(encodedToken);
    assertEquals("PageName", token.getPageName());
    assertEquals("Unexpected state map contents: " + token.getState(), 1, token.getState().size());
    assertEquals("Unexpected state map contents: " + token.getState(), 1, token.getState().get("k=ey").size());
    assertEquals("v&a=lue", token.getState().get("k=ey").iterator().next());
  }

  public void testPathThatNeedsEscaping() throws Exception {
    Multimap<String, String> state = ImmutableMultimap.<String, String>builder().put("path", "p=&/%2Fath").build();
    String encodedToken = htFactory.createHistoryToken("PageWithPath", state).toString();

    HistoryToken token = patternMatcher.parseURL(encodedToken);
    assertEquals("PageWithPath", token.getPageName());
    assertEquals("Unexpected state map contents: " + token.getState(), 1, token.getState().size());
    assertEquals("Unexpected state map contents: " + token.getState(), 1, token.getState().get("path").size());
    assertEquals("p=&/%2Fath", token.getState().get("path").iterator().next());
  }

  public void testToStringNoParams() throws Exception {
    String encodedToken = htFactory.createHistoryToken("PageName", ImmutableMultimap.<String, String>of()).toString();
    assertEquals("PageName", encodedToken);
  }

  public void testToString1Param() throws Exception {
    String encodedToken = htFactory.createHistoryToken("PageName",
            ImmutableMultimap.of("p1", "v1"))
            .toString();
    assertEquals("PageName;p1=v1", encodedToken);
  }

  public void testToString2Params() throws Exception {
    String encodedToken = htFactory.createHistoryToken("PageName",
            ImmutableMultimap.of("p1", "v1", "p2", "v2"))
            .toString();
    assertEquals("PageName;p1=v1&p2=v2", encodedToken);
  }

}
