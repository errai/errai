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

package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.LiteralValue;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.TypeNotIterableException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedMethodException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.mvel2.DataConversion;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class GenUtil {
    public static Statement[] generateCallParameters(Context context, Object... parameters) {
        Statement[] statements = new Statement[parameters.length];
        int i = 0;
        for (Object parameter : parameters) {
            statements[i++] = generate(context, parameter);
        }
        return statements;
    }

    public static Statement[] generateCallParameters(MetaMethod method, Context context, Object... parameters) {
        if (parameters.length != method.getParameters().length) {
            throw new UndefinedMethodException("Wrong number of parameters");
        }

        Statement[] statements = new Statement[parameters.length];
        int i = 0;
        for (Object parameter : parameters) {
            if (parameter instanceof Statement) {
                if (((Statement) parameter).getType() == null) {
                    parameter = generate(context, parameter);
                }
            }
            statements[i] = convert(context, parameter, method.getParameters()[i++].getType());
        }
        return statements;
    }

    public static Statement generate(Context context, Object o) {
        if (o instanceof VariableReference) {
            return context.getVariable(((VariableReference) o).getName());
        } else if (o instanceof Variable) {
            Variable v = (Variable) o;
            if (context.isScoped(v)) {
                return v.getReference();
            } else {
                throw new OutOfScopeException("variable cannot be referenced from this scope: " + v.getName());
            }
        } else if (o instanceof Statement) {
            return (Statement) o;
       } else {
            return LiteralFactory.getLiteral(o);
        }
    }

    public static void assertIsIterable(Statement statement) {
        Class<?> cls = statement.getType().asClass();

        if (!cls.isArray() && !Iterable.class.isAssignableFrom(cls))
            throw new TypeNotIterableException(statement.generate(Context.create()));
    }

    public static void assertAssignableTypes(MetaClass from, MetaClass to) {
        if(!to.asBoxed().isAssignableFrom(from.asBoxed())) {
            throw new InvalidTypeException(to.getFullyQualifedName() + " is not assignable from "
                    + from.getFullyQualifedName());
        }
    }

    public static Statement convert(Context context, Object input, MetaClass targetType) {
        try {
            if (input instanceof Statement) {
                if (input instanceof VariableReference
                        && (((VariableReference) input).getValue() instanceof LiteralValue)) {
                    input = ((LiteralValue<?>) ((VariableReference) input).getValue()).getValue();
                } else {
                    assertAssignableTypes(((Statement) input).getType(), targetType);
                    return (Statement) input;
                }
            }

            Class<?> targetClass = targetType.asBoxed().asClass();
            if (DataConversion.canConvert(targetClass, input.getClass())) {
                return generate(context, DataConversion.convert(input, targetClass));
            } else {
                throw new InvalidTypeException("cannot convert input to target type:" + targetClass.getName());
            }
        } catch (Throwable t) {
            throw new InvalidTypeException(t);
        }
    }
}