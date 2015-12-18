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

package org.jboss.errai.ui.nav.pattern;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.errai.ui.nav.client.local.URLPatternMatcher;
import org.junit.Before;
import org.junit.Test;

public class URLPatternMatchingTest {
  URLPatternMatcher matcher;
  Map<String, String> testPatterns;
  
  @Before
  public void setupTests() {
    matcher = new URLPatternMatcher();
    testPatterns = new HashMap<String, String>();
    testPatterns.put("foo/bar/baz", "NoVarsURL");
    testPatterns.put("{var}/some/other", "VarAtStartOfURL");
    testPatterns.put("some/other/{var}", "VarAtEndOfURL");
    testPatterns.put("pictures/pic{num}/show", "VarBesideNonSlashURL");
    testPatterns.put("{var1}/some/{var2}/{var3}", "MultipleVarsURL");
    testPatterns.put("{var}", "OnlyVarURL");
    testPatterns.put("some/{var}/", "TrailingSlashURL");
    testPatterns.put("путь/道/道路/переменная:{var}", "NonAsciiCharsURL");
    testPatterns.put("some/{var1}and{var2}/other", "MultipleVarsInSegmentURL");

    for (Entry<String, String> entry : testPatterns.entrySet()) {
      matcher.add(entry.getKey(), entry.getValue());
    }
    
    matcher.setAsDefaultPage("NoVarsURL");
  }
  
  @Test
  public void matchesPatternWithNoVars() throws Exception {
    String testURL = "foo/bar/baz";
    assertEquals("NoVarsURL", matcher.getPageName(testURL));
  }
  
  @Test
  public void matchesVariableAtStartOfPattern() throws Exception {
    String testURL = "123/some/other";
    assertEquals("VarAtStartOfURL", matcher.getPageName(testURL));
  }

  @Test
  public void matchesVariableAtEndOfPattern() throws Exception {
    String testURL = "some/other/123";
    assertEquals("VarAtEndOfURL", matcher.getPageName(testURL));
  }
  
  @Test
  public void matchesVariableBesideNonSlash() throws Exception {
    String testURL = "pictures/pic4/show";
    assertEquals("VarBesideNonSlashURL", matcher.getPageName(testURL));
  }
  
  @Test
  public void matchesMultipleVariables() {
    String testURL = "1/some/2/3";
    assertEquals("MultipleVarsURL", matcher.getPageName(testURL));
  }
  
  @Test
  public void matchesPatternOnlyContainingVar() throws Exception {
    String testURL = "test";
    assertEquals("OnlyVarURL", matcher.getPageName(testURL));
  }
  
  @Test
  public void matchesTrailingSlash() throws Exception {
    String testURL = "some/id/";
    assertEquals("TrailingSlashURL", matcher.getPageName(testURL));
  }
  
  @Test
  public void matchesEmptyStringPattern() throws Exception {
    String testURL = "";
    assertEquals("NoVarsURL", matcher.getPageName(testURL));
  }

  @Test
  public void matchesNonAsciiChars() throws Exception {
    String testURL = "путь/道/道路/переменная:value";
    assertEquals("NonAsciiCharsURL", matcher.getPageName(testURL));
  }

  @Test
  public void matchesPatternWithMultipleVariablesInSegment() throws Exception {
    String testURL = "some/eg1andeg2/other";
    assertEquals("MultipleVarsInSegmentURL", matcher.getPageName(testURL));
  }
}
