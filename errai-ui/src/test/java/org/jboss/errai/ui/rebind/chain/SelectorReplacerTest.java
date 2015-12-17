/**
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

package org.jboss.errai.ui.rebind.chain;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author edewit@redhat.com
 */
public class SelectorReplacerTest {

  public static final String OBFUSCATED_NAME = "asdfasdf";

  @Test
  public void shouldReplaceClassSelectors() throws TransformerException {
    //given
    final HashMap<String, String> mapping = new HashMap<String, String>();
    mapping.put("dropdown", OBFUSCATED_NAME);

    SelectorReplacer minifier = new SelectorReplacer(mapping);
    final Element element = getElement();

    //when
    minifier.execute(element);

    //then
    assertEquals("btn " + OBFUSCATED_NAME, element.getAttribute("class"));
  }

  private Element getElement() throws TransformerException {
    final Document document = new TemplateCatalog().parseTemplate(getClass().getResource("/simple.html"));
    return (Element) XPathAPI.selectSingleNode(document, "//div[@class]");
  }
}
