package org.jboss.errai.ui.rebind.chain;

import org.jboss.errai.ui.shared.chain.Context;
import org.junit.Test;
import org.w3c.dom.Document;
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
import static org.jboss.errai.ui.rebind.chain.TemplateCatalog.RESULT;

/**
 * @author edewit@redhat.com
 */
public class DummyRemoverTest {

  @Test
  public void shouldRemoveDummyNodes() throws TransformerException {
    // given
    DummyRemover command = new DummyRemover();

    final URL resource = getClass().getResource("/dummy.html");
    Context context = new Context();

    final Document document = new TemplateCatalog().parseTemplate(resource);
    final Node root = document.getElementsByTagName("body").item(0).getFirstChild();
    context.put(TemplateCatalog.ELEMENT, root);
    context.put(TemplateCatalog.FILENAME, resource);


    //when
    command.execute(context);

    //then
    assertEquals("<div data-role=\"dummy\"></div>", toString(root).trim());
    assertEquals(document, context.get(RESULT));
  }

  public static String toString(Node node) throws TransformerException {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.METHOD, "html");

    StringWriter writer = new StringWriter();
    transformer.transform(new DOMSource(node), new StreamResult(writer));
    return writer.toString();
  }
}
