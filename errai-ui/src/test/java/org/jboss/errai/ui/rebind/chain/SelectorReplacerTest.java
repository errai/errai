package org.jboss.errai.ui.rebind.chain;

import org.apache.xpath.XPathAPI;
import org.jboss.errai.ui.shared.chain.Context;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * @author edewit@redhat.com
 */
public class SelectorReplacerTest {

  public static final String OBFUSCATED_NAME = "asdfasdf";

  @Test
  public void shouldReplaceClassSelectors() throws TransformerException {
    //given
    SelectorReplacer minifier = new SelectorReplacer();
    final Element element = getElement();
    final Context context = getContext();
    context.put(TemplateCatalog.ELEMENT, element);

    //when
    minifier.execute(context);

    //then
    assertEquals("btn " + OBFUSCATED_NAME, element.getAttribute("class"));
  }

  private Context getContext() {
    final Context context = new Context();
    final HashMap<String, String> mapping = new HashMap<String, String>();
    mapping.put("dropdown", OBFUSCATED_NAME);
    context.put(SelectorReplacer.MAPPING, mapping);
    return context;
  }

  private Element getElement() throws TransformerException {
    final Document document = new TemplateCatalog().parseTemplate(getClass().getResource("/simple.html"));
    return (Element) XPathAPI.selectSingleNode(document, "//div[@class]");
  }
}
