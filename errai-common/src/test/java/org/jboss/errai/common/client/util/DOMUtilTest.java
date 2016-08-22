/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.common.client.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.errai.common.client.dom.CSSStyleDeclaration;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class DOMUtilTest {

  @Mock
  private CSSStyleDeclaration style;

  @Test
  public void emptyStreamForEmptyCssDeclaration() throws Exception {
    when(style.getCssText()).thenReturn("");
    final List<String> styleNames = DOMUtil
      .cssPropertyNameStream(style)
      .collect(Collectors.toList());

    assertEquals(Collections.emptyList(), styleNames);
  }

  @Test
  public void splitsNonEmptyCssDeclaration() throws Exception {
    when(style.getCssText()).thenReturn("  display:block;   height: 100px;  width:     200px;   ");
    final List<String> styleNames = DOMUtil
      .cssPropertyNameStream(style)
      .collect(Collectors.toList());

    assertEquals(Arrays.asList("display", "height", "width"), styleNames);
  }

}
