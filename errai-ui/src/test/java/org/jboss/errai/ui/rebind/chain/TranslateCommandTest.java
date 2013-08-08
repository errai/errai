package org.jboss.errai.ui.rebind.chain;

import static org.jboss.errai.ui.rebind.chain.TranslateCommand.Constants.DONE;
import static org.jgroups.util.Util.assertTrue;

import java.net.URL;

import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.ui.shared.chain.Context;
import org.jboss.errai.ui.test.i18n.client.res.I18nComponent;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * @author edewit@redhat.com
 */
public class TranslateCommandTest {

  @Test
  public void shouldIgnoreDummyElements() {
    // given
    TranslateCommand command = new TranslateCommand();
    final URL resource = getClass().getResource("/dummy.html");
    command.contexts.put(MetaClassFactory.get(I18nComponent.class), new Context());
    final Document document = new TemplateCatalog().parseTemplate(resource);
    Context context = new Context();
    context.put(DONE, document.getFirstChild());
    context.put(TemplateCatalog.ELEMENT, document.getElementsByTagName("span").item(0));
    context.put(TemplateCatalog.FILENAME, resource);

    // when
    command.execute(context);

    //then
    assertTrue(command.getI18nValues().isEmpty());
  }
}
