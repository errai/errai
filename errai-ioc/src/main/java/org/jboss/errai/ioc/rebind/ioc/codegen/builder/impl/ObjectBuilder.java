/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BuildCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.CallParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadClassReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedConstructorException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.GenUtil;

import com.google.gwt.core.ext.typeinfo.JClassType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ObjectBuilder extends AbstractStatementBuilder {

  private static final int CONSTRUCT_STATEMENT_COMPLETE = 1;
  private static final int SUBCLASSED = 2;
  private static final int FINISHED = 3;

  private MetaClass type;
  private int buildState;

  private CallParameters callParameters;
  private Statement extendsBlock;

  ObjectBuilder(MetaClass type, Context context) {
    super(context);
    this.type = type;

    for (MetaField field : type.getFields()) {
      context.addVariable(Variable.create(field.getName(), field.getType()));
    }
  }

  ObjectBuilder(MetaClass type) {
    this(type, Context.create());
  }

  public static ObjectBuilder newInstanceOf(MetaClass type) {
    return new ObjectBuilder(type).newInstance();
  }

  public static ObjectBuilder newInstanceOf(Class<?> type) {
    return newInstanceOf(MetaClassFactory.get(type));
  }

  public static ObjectBuilder newInstanceOf(TypeLiteral<?> type) {
    return newInstanceOf(MetaClassFactory.get(type));
  }

  public static ObjectBuilder newInstanceOf(JClassType type) {
    return newInstanceOf(MetaClassFactory.get(type));
  }

  public static ObjectBuilder newInstanceOf(MetaClass type, Context context) {
    return new ObjectBuilder(type, context).newInstance();
  }

  public static ObjectBuilder newInstanceOf(Class<?> type, Context context) {
    return newInstanceOf(MetaClassFactory.get(type), context);
  }

  public static ObjectBuilder newInstanceOf(TypeLiteral<?> type, Context context) {
    return newInstanceOf(MetaClassFactory.get(type), context);
  }

  public static ObjectBuilder newInstanceOf(JClassType type, Context context) {
    return newInstanceOf(MetaClassFactory.get(type), context);
  }


  private ObjectBuilder newInstance() {
    return this;
  }

  public ObjectBuilder withParameters(Object... parameters) {
    return withParameters(GenUtil.generateCallParameters(getContext(), parameters));
  }

  public ObjectBuilder withParameters(Statement... parameters) {
    return withParameters(CallParameters.fromStatements(parameters));
  }

  public ObjectBuilder withParameters(CallParameters parameters) {
    if (!type.isInterface() && type.getBestMatchingConstructor(parameters.getParameterTypes()) == null)
      throw new UndefinedConstructorException(type, parameters.getParameterTypes());

    callParameters = parameters;

    buildState |= CONSTRUCT_STATEMENT_COMPLETE;
    return this;
  }

  public ExtendsClassStructureBuilderImpl extend() {
    return new ExtendsClassStructureBuilderImpl(type, new BuildCallback<ObjectBuilder>() {
      public ObjectBuilder callback(Statement statement) {
        finishConstructIfNecessary();
        extendsBlock = statement;
        return ObjectBuilder.this;
      }
    });
  }

  private void finishConstructIfNecessary() {
    if ((buildState & CONSTRUCT_STATEMENT_COMPLETE) == 0) {
      withParameters(CallParameters.none());
    }
  }

  public MetaClass getType() {
    return type;
  }

  public String generate(Context context) {
    StringBuilder buf = new StringBuilder();

    buf.append("new ").append(LoadClassReference.getClassReference(type, context));
    if (callParameters != null) {
      buf.append(callParameters.generate(Context.create()));
    }
    if (extendsBlock != null) {
      buf.append(" {\n").append(extendsBlock.generate(context)).append("\n}\n");
    }

    finishConstructIfNecessary();
    return buf.toString();
  }
}
