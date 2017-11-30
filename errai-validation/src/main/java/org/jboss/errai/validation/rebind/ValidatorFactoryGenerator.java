/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.validation.rebind;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.validation.client.AbstractGwtValidatorFactory;
import com.google.gwt.validation.client.impl.AbstractGwtValidator;
import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.rebind.AbstractAsyncGenerator;
import org.jboss.errai.config.rebind.GenerateAsync;
import org.jboss.errai.validation.client.BeanValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ValidatorFactory;

/**
 * Generates an implementation of {@link ValidatorFactory} which provides a generated implementation
 * of a GWT {@link javax.validation.Validator}.
 *
 * @author Johannes Barop <jb@barop.de>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@GenerateAsync(ValidatorFactory.class)
public class ValidatorFactoryGenerator extends AbstractAsyncGenerator {

  private static final Logger log = LoggerFactory.getLogger(ValidatorFactoryGenerator.class);
  private final String packageName = "org.jboss.errai.validation.client";
  private final String className = "ValidatorFactoryImpl";

  @Override
  public String generate(final TreeLogger logger, final GeneratorContext context, final String typeName)
          throws UnableToCompleteException {
    return startAsyncGeneratorsAndWaitFor(ValidatorFactory.class, context, logger, packageName, className);
  }

  @Override
  protected String generate(final TreeLogger logger, final GeneratorContext context) {

    long start = System.currentTimeMillis();
    log.info("Generating validator factory...");
    ClassStructureBuilder<?> validatorInterface = new GwtValidatorGenerator().generate(context);
    ClassStructureBuilder<?> builder = ClassBuilder.define(packageName + "." + className,
            AbstractGwtValidatorFactory.class).publicScope().body();

    BlockBuilder<?> methodBuilder = builder.publicMethod(AbstractGwtValidator.class, "createValidator");
    if (validatorInterface == null) {
      methodBuilder.append(Stmt.nestedCall(Stmt.newObject(BeanValidator.class, Stmt.loadLiteral(null))).returnValue())
              .finish();
    } else {
      methodBuilder.append(Stmt.nestedCall(Stmt.newObject(BeanValidator.class, Cast.to(AbstractGwtValidator.class,
              Stmt.invokeStatic(GWT.class, "create", validatorInterface.getClassDefinition())))).returnValue())
              .finish();
      builder.getClassDefinition().addInnerClass(new InnerClass(validatorInterface.getClassDefinition()));
    }

    final String gen = builder.toJavaString();
    log.info("Generated validator factory in " + (System.currentTimeMillis() - start) + "ms");
    return gen;
  }
}
