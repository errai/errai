package org.jboss.errai.ui.rebind.chain;

import org.apache.xpath.XPathAPI;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author edewit@redhat.com
 */
public class SelectorReplacerTest {

  public static final String OBFUSCATED_NAME = "asdfasdf";

  @Test
  public void shouldReplaceClassSelectors() throws TransformerException {
    //given
    SelectorReplacer minifier = new SelectorReplacer() {
      @Override
      protected Map<String, String> getStyleMapping() {
        final HashMap<String, String> mapping = new HashMap<String, String>();
        mapping.put("dropdown", OBFUSCATED_NAME);
        return mapping;
      }
    };
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
