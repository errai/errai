/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.ui.rebind;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ui.shared.InternationalizationUtil;
import org.jboss.errai.ui.shared.api.annotations.Bundle;
import org.jboss.errai.ui.shared.api.annotations.I18n;
import org.jboss.errai.ui.shared.api.annotations.I18nDefault;

import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasText;

/**
 * Handles fields and parameters annotated with @I18N.  This class will insert the code that
 * will perform the i18n replacement.
 *
 * @author eric.wittmann@redhat.com
 */
@CodeDecorator
public class DecoratorI18N extends IOCDecoratorExtension<I18n> {

  private static final Logger logger = Logger.getLogger(DecoratorI18N.class.getName());

  /**
   * Constructor.
   * @param decoratesWith
   */
  public DecoratorI18N(Class<I18n> decoratesWith) {
    super(decoratesWith);
  }

  /**
   * @see org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension#generateDecorator(org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance)
   */
  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<I18n> instance) {
    instance.ensureMemberExposed();

    String methodName = null;
    if (instance.getType().isAssignableTo(HasHTML.class)) {
      methodName = "i18nHtmlReplace";
    } else if (instance.getType().isAssignableTo(HasText.class)) {
      methodName = "i18nTextReplace";
    }
    if (methodName == null) {
      throw new GenerationException("@I18n annotated element ["
              + instance.getMemberName() + "] in class ["
              + instance.getEnclosingType().getFullyQualifiedName()
              + "] is of type [" + instance.getType().getFullyQualifiedName()
              + "] which does not implement HasText or HasHtml.");
    }
    String bundleName = "erraiBundle";
    Bundle classBundle = instance.getEnclosingType().getAnnotation(Bundle.class);
    if (classBundle != null) {
      bundleName = classBundle.value();
    }
    Bundle memberBundle = instance.getAnnotation(Bundle.class);
    if (memberBundle != null) {
      bundleName = memberBundle.value();
    }
    String i18nKey = instance.getAnnotation().value();
    String defaultValue = null;
    I18nDefault i18nDefault = instance.getAnnotation(I18nDefault.class);
    if (i18nDefault != null) {
      defaultValue = i18nDefault.value();
    }

    logger.info("i18n replacement of ["
            + instance.getEnclosingType().getFullyQualifiedName() + "."
            + instance.getMemberName() + "] using key [" + i18nKey
            + "] from bundle [" + bundleName + "]");

    Statement s = Stmt.invokeStatic(InternationalizationUtil.class, methodName,
            instance.getEnclosingType().getFullyQualifiedName(),
            instance.getMemberName(), Cast.to(HasText.class, instance.getValueStatement()),
            bundleName, i18nKey, defaultValue);
    return Collections.singletonList(s);
  }

}