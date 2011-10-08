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

package org.jboss.errai.codegen.framework.builder.impl;

import static org.jboss.errai.codegen.framework.CallParameters.fromStatements;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.codegen.framework.CallParameters;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.BuildCallback;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.StatementEnd;
import org.jboss.errai.codegen.framework.builder.callstack.CallWriter;
import org.jboss.errai.codegen.framework.builder.callstack.DeferredCallElement;
import org.jboss.errai.codegen.framework.builder.callstack.DeferredCallback;
import org.jboss.errai.codegen.framework.builder.callstack.LoadClassReference;
import org.jboss.errai.codegen.framework.exception.UndefinedConstructorException;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.util.GenUtil;

import com.google.gwt.core.ext.typeinfo.JClassType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ObjectBuilder extends AbstractStatementBuilder {

  private MetaClass type;
  private Object[] parameters;

  private Statement extendsBlock;

  ObjectBuilder(MetaClass type, Context context, CallElementBuilder callElementBuilder) {
    super(context, callElementBuilder);
    if (context != null) {
      context.attachClass(type);

      for (MetaField field : type.getDeclaredFields()) {
        context.addVariable(Variable.create(field.getName(), field.getType()));
      }
    }

    this.type = type;
  }

  ObjectBuilder(MetaClass type, Context context) {
    this(type, context, new CallElementBuilder());
  }

  ObjectBuilder(MetaClass type) {
    this(type, Context.create(), new CallElementBuilder());
  }

  ObjectBuilder() {
    super(Context.create());
    context.attachClass(type);
  }

  public static ObjectBuilder newInstanceOf(MetaClass type) {
    return new ObjectBuilder(type);
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
    return new ObjectBuilder(type, context);
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

  public static ObjectBuilder newInstanceOf(MetaClass type, Context context, CallElementBuilder callElementBuilder) {
    return new ObjectBuilder(type, context, callElementBuilder);
  }

  public static ObjectBuilder newInstanceOf(Class<?> type, Context context, CallElementBuilder callElementBuilder) {
    return newInstanceOf(MetaClassFactory.get(type), context, callElementBuilder);
  }

  public static ObjectBuilder newInstanceOf(TypeLiteral<?> type, Context context, CallElementBuilder callElementBuilder) {
    return newInstanceOf(MetaClassFactory.get(type), context, callElementBuilder);
  }

  public static ObjectBuilder newInstanceOf(JClassType type, Context context, CallElementBuilder callElementBuilder) {
    return newInstanceOf(MetaClassFactory.get(type), context, callElementBuilder);
  }

  public StatementEnd withParameters(Object... parameters) {
    this.parameters = parameters;
    return this;
  }

  //todo: return a builder interface -- not a concrete implementation
  public AnonymousClassStructureBuilder extend() {
    return new AnonymousClassStructureBuilderImpl(type, new BuildCallback<ObjectBuilder>() {
      @Override
      public ObjectBuilder callback(Statement statement) {
        extendsBlock = statement;
        return ObjectBuilder.this;
      }

      @Override
      public Context getParentContext() {
        return context;
      }
    });
  }

  @Override
  public MetaClass getType() {
    return type;
  }

  @Override
  public String generate(final Context context) {

    appendCallElement(new DeferredCallElement(new DeferredCallback() {
      @Override
      public void doDeferred(CallWriter writer, Context context, Statement statement) {
        writer.reset();

        CallParameters callParameters = (parameters != null) ?
                fromStatements(GenUtil.generateCallParameters(context, parameters)) : CallParameters.none();

        if (!type.isInterface() && type.getBestMatchingConstructor(callParameters.getParameterTypes()) == null)
          throw new UndefinedConstructorException(type, callParameters.getParameterTypes());

        StringBuilder buf = new StringBuilder();
        buf.append("new ").append(LoadClassReference.getClassReference(type, context, true));
        if (callParameters != null) {
          buf.append(callParameters.generate(Context.create(context)));
        }
        if (extendsBlock != null) {
          for (MetaField field : type.getDeclaredFields()) {
            context.addVariable(Variable.create(field.getName(), field.getType()));
          }
          buf.append(" {\n").append(extendsBlock.generate(context)).append("\n}\n");
        }
        writer.append(buf.toString());
      }
    }));

    try {
    return super.generate(context);
    }
    catch (Throwable t) {
      GenUtil.throwIfUnhandled("while instantiating class: " + type.getFullyQualifiedName(), t);
      return null;
    }
  }
}
