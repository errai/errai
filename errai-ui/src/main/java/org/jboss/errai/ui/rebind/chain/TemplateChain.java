package org.jboss.errai.ui.rebind.chain;

import java.net.URL;

import static org.jboss.errai.ui.rebind.chain.TemplateCatalog.createTemplateCatalog;

/**
 * @author edewit@redhat.com
 */
public class TemplateChain {
  private static final TemplateChain INSTANCE = new TemplateChain();
  private static final TemplateCatalog catalog = createTemplateCatalog(new TranslateCommand(), new SelectorReplacer());

  private URL template;

  public static TemplateChain getInstance() {
    return INSTANCE;
  }

  public void visitTemplate(URL template) {
    this.template = template;
    catalog.visitTemplate(template);
  }

  @SuppressWarnings("unchecked")
  public <T> T getLastResult(String key) {
    final Object result = catalog.getResult(template, key);
    return (T) result;
  }
}
