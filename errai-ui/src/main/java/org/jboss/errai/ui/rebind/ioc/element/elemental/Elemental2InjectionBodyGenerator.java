package org.jboss.errai.ui.rebind.ioc.element.elemental;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.ui.rebind.ioc.element.ElementInjectionBodyGenerator;

import java.util.Collections;

import static org.jboss.errai.codegen.util.Stmt.loadStatic;

public class Elemental2InjectionBodyGenerator extends ElementInjectionBodyGenerator {

  private final String tagName;

  public Elemental2InjectionBodyGenerator(final String tagName, final MetaClass type) {
    super(type, Collections.emptySet());
    this.tagName = tagName;
  }

  @Override
  protected ContextualStatementBuilder elementInitialization() {
    return loadStatic(DomGlobal.class, "document").invoke("createElement", tagName);
  }

  @Override
  protected Class<?> elementClass() {
    return Element.class;
  }
}
