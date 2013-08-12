package org.jboss.errai.ui.rebind.chain;

import org.jboss.errai.ui.shared.chain.Chain;
import org.jboss.errai.ui.shared.chain.Command;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author edewit@redhat.com
 */
public class TemplateCatalogTest {

  @Test
  public void shouldHaveAutomatedSetup() {
    // given
    final DummyCommand command = new DummyCommand();
    final TemplateCatalog catalog = TemplateCatalog.createTemplateCatalog(command);

    // when
    final URL template = getClass().getResource("/simple.html");
    final Document document = catalog.visitTemplate(template);

    // then
    final Chain chain = catalog.getChain();
    assertEquals(1, chain.getCommands().size());
    assertEquals(5, command.getCounter());
    assertNotNull(document);
  }

  public static class DummyCommand implements Command {
    private int counter;

    @Override
    public void execute(Element element) {
      counter++;
    }

    public int getCounter() {
      return counter;
    }
  }
}
