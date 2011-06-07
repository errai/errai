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

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ArrayBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBegin;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.VariableReferenceContextualStatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java.JavaReflectionClass;

import javax.enterprise.util.TypeLiteral;

/**
 * The root of our fluent StatementBuilder API.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 */
public class StatementBuilder extends AbstractStatementBuilder implements StatementBegin {

    public StatementBuilder(Context context) {
        super(context);

        if (context != null) {
            for (Variable v : context.getDeclaredVariables()) {
                appendCallElement(new DeclareVariable(v));
            }
        }
    }

    public static StatementBegin create() {
        return new StatementBuilder(null);
    }

    public static StatementBegin create(Context context) {
        return new StatementBuilder(context);
    }

    public StatementBuilder addVariable(String name, Class<?> type) {
        Variable v = Variable.create(name, type);
        return addVariable(v);
    }

    public StatementBuilder addVariable(String name, TypeLiteral<?> type) {
        Variable v = Variable.create(name, type);
        return addVariable(v);
    }

    public StatementBuilder addVariable(String name, Object initialization) {
        Variable v = Variable.create(name, initialization);
        return addVariable(v);
    }

    public StatementBuilder addVariable(String name, Class<?> type, Object initialization) {
        Variable v = Variable.create(name, type, initialization);
        return addVariable(v);
    }

    public StatementBuilder addVariable(String name, TypeLiteral<?> type, Object initialization) {
        Variable v = Variable.create(name, type, initialization);
        return addVariable(v);
    }

    private StatementBuilder addVariable(Variable v) {
        appendCallElement(new DeclareVariable(v));
        return this;
    }

    public VariableReferenceContextualStatementBuilder loadVariable(String name) {
        appendCallElement(new LoadVariable(name));
        return new ContextualStatementBuilderImpl(context, callElementBuilder);
    }

    public ContextualStatementBuilder loadLiteral(Object o) {
        appendCallElement(new LoadLiteral(o));
        return new ContextualStatementBuilderImpl(context, callElementBuilder);
    }

    public ContextualStatementBuilder load(Object o) {
        appendCallElement(new DynamicLoad(o));
        return new ContextualStatementBuilderImpl(context, callElementBuilder);
    }

    public ContextualStatementBuilder invokeStatic(Class<?> clazz, String methodName, Object... parameters) {
        appendCallElement(new StaticLoad(clazz));
        return new ContextualStatementBuilderImpl(context, callElementBuilder).invokeStatic(methodName, parameters);
    }

    public ObjectBuilder newObject(MetaClass type) {
        return ObjectBuilder.newInstanceOf(type);
    }

    public ObjectBuilder newObject(JavaReflectionClass type) {
        return ObjectBuilder.newInstanceOf(type);
    }

    public ObjectBuilder newObject(Class<?> type) {
        return ObjectBuilder.newInstanceOf(type);
    }
    
    public ArrayBuilder newArray(MetaClass type) {
        return ObjectBuilder.newArrayOf(type);
    }

    public ArrayBuilder newArray(JavaReflectionClass type) {
        return ObjectBuilder.newArrayOf(type);
    }

    public ArrayBuilder newArray(Class<?> type) {
        return ObjectBuilder.newArrayOf(type);
    }
    
    public ArrayBuilder newArray(MetaClass type, int length) {
        return ObjectBuilder.newArrayOf(type, length);
    }

    public ArrayBuilder newArray(JavaReflectionClass type,  int length) {
        return ObjectBuilder.newArrayOf(type, length);
    }

    public ArrayBuilder newArray(Class<?> type,  int length) {
        return ObjectBuilder.newArrayOf(type, length);
    }
}