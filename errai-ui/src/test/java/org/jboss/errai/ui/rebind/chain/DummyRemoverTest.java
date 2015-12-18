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

package org.jboss.errai.ui.rebind.chain;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.net.URL;

import static junit.framework.Assert.assertEquals;

/**
 * @author edewit@redhat.com
 */
public class DummyRemoverTest {

  @Test
  public void shouldRemoveDummyNodes() throws TransformerException {
    // given
    DummyRemover command = new DummyRemover();

    final URL resource = getClass().getResource("/dummy.html");

    final Document document = new TemplateCatalog().parseTemplate(resource);
    final Node root = document.getElementsByTagName("body").item(0).getFirstChild();

    //when
    command.execute((Element) root);

    //then
    assertEquals("<div data-role=\"dummy\"></div>", toString(root).trim());
  }

  public static String toString(Node node) throws TransformerException {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.METHOD, "html");

    StringWriter writer = new StringWriter();
    transformer.transform(new DOMSource(node), new StreamResult(writer));
    return writer.toString();
  }
}
