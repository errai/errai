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
