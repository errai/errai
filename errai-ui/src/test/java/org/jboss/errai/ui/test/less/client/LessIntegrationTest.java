package org.jboss.errai.ui.test.less.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.client.local.spi.LessStyle;
import org.jboss.errai.ui.test.less.client.res.PageTemplate;

/**
 * @author edewit@redhat.com
 */
public class LessIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  public void testIntegrateLessFileInHead() {
    final LessStyle lessStyle = IOC.getBeanManager().lookupBean(LessStyle.class).getInstance();
    final PageTemplate template = IOC.getBeanManager().lookupBean(PageTemplate.class).getInstance();

    assertNotNull(template.getBox());
    final String expected = "Eoxl56A";
    assertEquals(expected, lessStyle.get("box"));
    assertEquals(expected, template.getBox().getClassName());
  }
}
