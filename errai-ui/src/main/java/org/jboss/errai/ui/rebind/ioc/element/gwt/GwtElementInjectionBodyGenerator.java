package org.jboss.errai.ui.rebind.ioc.element.gwt;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.ui.rebind.ioc.element.ElementInjectionBodyGenerator;

import static org.jboss.errai.codegen.util.Stmt.invokeStatic;
import static org.jboss.errai.codegen.util.Stmt.loadLiteral;

public class GwtElementInjectionBodyGenerator extends ElementInjectionBodyGenerator {

  private final String tagName;

  public GwtElementInjectionBodyGenerator(final String tagName, final MetaClass type) {
    super(type);
    this.tagName = tagName;
  }

  @Override
  protected ContextualStatementBuilder elementInitialization() {
    return invokeStatic(Document.class, "get").invoke("createElement", loadLiteral(tagName));
  }

  @Override
  protected Class<?> elementClass() {
    return Element.class;
  }
}
