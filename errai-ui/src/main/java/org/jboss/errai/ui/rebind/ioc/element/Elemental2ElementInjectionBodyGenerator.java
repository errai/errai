/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.rebind.ioc.element;

import com.google.common.base.Strings;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.common.client.api.annotations.Property;

import java.util.List;
import java.util.Set;

import static org.jboss.errai.codegen.util.Stmt.declareFinalVariable;
import static org.jboss.errai.codegen.util.Stmt.loadLiteral;
import static org.jboss.errai.codegen.util.Stmt.loadStatic;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class Elemental2ElementInjectionBodyGenerator extends ElementInjectionBodyGenerator {

  Elemental2ElementInjectionBodyGenerator(final MetaClass type,
          final String tagName,
          final Set<Property> properties,
          final List<String> classNames) {

    super(type, tagName, properties, classNames);
  }

  @Override
  protected String createInstanceAndConfigure(final List<Statement> statements) {

    final String elementVar = "element";

    statements.add(declareFinalVariable(elementVar, Element.class,
            loadStatic(DomGlobal.class, "document").invoke("createElement", tagName)));

    for (final Property property : properties) {
      statements.add(loadVariable(elementVar).invoke("setAttribute", loadLiteral(property.name()),
              loadLiteral(property.value())));
    }

    if (!Strings.isNullOrEmpty(spaceSeparatedClassNames)) {
      statements.add(
              loadVariable(elementVar).loadField("className").assignValue(loadLiteral(spaceSeparatedClassNames)));
    }

    return elementVar;
  }
}
