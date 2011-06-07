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

package org.jboss.errai.ioc.tests.rebind;

import org.jboss.errai.ioc.rebind.ioc.codegen.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Refs;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ContextBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedMethodException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the generation of method invocations using the {@link StatementBuilder} API.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InvocationBuilderTest extends AbstractStatementBuilderTest {
    @Test
    public void testInvoke() {
        Builder invokeStatement = StatementBuilder.create()
                .addVariable("obj", Object.class)
                .loadVariable("obj")
                .invoke("toString");

        assertEquals("failed to generate invocation on variable",
                "obj.toString()", invokeStatement.toJavaString());

        invokeStatement = StatementBuilder.create()
                .addVariable("i", Integer.class)
                .addVariable("regex", String.class)
                .addVariable("replacement", String.class)
                .loadVariable("i")
                .invoke("toString")
                .invoke("replaceAll", Variable.get("regex"), Variable.get("replacement"));

        assertEquals("failed to generate multiple invocations on variable",
                "i.toString().replaceAll(regex, replacement)", invokeStatement.toJavaString());
    }

    @Test
    public void testInvokeWithLiterals() {
        String result = StatementBuilder.create().addVariable("s", String.class)
                .loadVariable("s").invoke("replaceAll", "foo", "foo\t\n").toJavaString();

        assertEquals("failed to generate invocation using literal parameters",
                "s.replaceAll(\"foo\", \"foo\\t\\n\")", result);

        result = StatementBuilder.create().loadLiteral("foo").invoke("toString").toJavaString();

        assertEquals("failed to generate invocation using literal parameters",
                "\"foo\".toString()", result);
    }

    @Test
    public void testInvokeOnBestMatchingMethod() {
        Builder statement = StatementBuilder.create()
                .addVariable("n", Integer.class)
                .loadVariable("n")
                        // 1 will be inferred to LiteralValue<Integer>, equals(Integer.class) should match equals(Object.class)
                .invoke("equals", 1);

        assertEquals("failed to generate invocation on matched method", "n.equals(1)", statement.toJavaString());
    }

    @Test
    public void testInvokeOnUndefinedMethods() {
        try {
            StatementBuilder.create()
                    .addVariable("obj", Object.class)
                    .addVariable("param", String.class)
                    .loadVariable("obj")
                    .invoke("udefinedMethod", Variable.get("param"))
                    .toJavaString();
            fail("expected UndefinedMethodException");
        } catch (UndefinedMethodException udme) {
            //expected
        }

        try {
            StatementBuilder.create()
                    .addVariable("s", String.class)
                    .addVariable("regex", String.class)
                    .addVariable("replacement", String.class)
                    .loadVariable("s")
                    .invoke("replaceAll", Variable.get("regex"), Variable.get("replacement"))
                    .invoke("undefinedMethod", Variable.get("regex"), Variable.get("replacement"))
                    .toJavaString();
            fail("expected UndefinedMethodException");
        } catch (UndefinedMethodException udme) {
            //expected
            assertEquals("Wrong exception thrown", udme.getMethodName(), "undefinedMethod");
        }
    }

    @Test
    public void testInvokeWithUndefinedVariables() {
        try {
            // injector undefined
            StatementBuilder.create()
                    .loadVariable("injector")
                    .invoke("provide", Refs.get("param"), Refs.get("param2"))
                    .toJavaString();
            fail("expected OutOfScopeException");
        } catch (OutOfScopeException oose) {
            //expected
            assertTrue("Wrong exception thrown", oose.getMessage().contains("injector"));
        }

        try {
            // param2 undefined
            StatementBuilder.create()
                    .addVariable("obj", Object.class)
                    .addVariable("param", String.class)
                    .loadVariable("obj")
                    .invoke("undefinedMethod", Variable.get("param"), Variable.get("param2"))
                    .toJavaString();
            fail("expected OutOfScopeException");
        } catch (OutOfScopeException oose) {
            //expected
            assertTrue(oose.getMessage().contains("param2"));
        }
    }

    @Test
    public void testStandardizedReferences() {
        Context context = ContextBuilder.create()
                .addVariable("s", String.class)
                .addVariable("regex", String.class)
                .addVariable("replacement", String.class)
                .getContext();

        String s = StatementBuilder.create(context)
                .load(Variable.get("s"))
                .invoke("toUpperCase").toJavaString();

        assertEquals("failed using load() passing a Reference",
                "s.toUpperCase()", s);

        Variable v = Variable.create("s", String.class);
        s = StatementBuilder.create(context)
                .load(v)
                .invoke("toUpperCase").toJavaString();

        assertEquals("failed using load() passing a Variable instance",
                "s.toUpperCase()", s);

        s = StatementBuilder.create(context)
                .load("foo")
                .invoke("toUpperCase").toJavaString();

        assertEquals("failed injecting literal with load()",
                "\"foo\".toUpperCase()", s);
    }

    @Test
    public void testInvokeWithParameterTypeConversion() {
        Builder invokeStatement = StatementBuilder.create()
                .addVariable("str", String.class)
                .loadVariable("str")
                .invoke("endsWith", 123);

        assertEquals("failed to generate invocation with parameter type conversion",
                "str.endsWith(\"123\")", invokeStatement.toJavaString());

        invokeStatement = StatementBuilder.create()
                .addVariable("str", String.class)
                .loadVariable("str")
                .invoke("substring", "1", "3");

        assertEquals("failed to generate invocation with parameter type conversion",
                "str.substring(1, 3)", invokeStatement.toJavaString());

        invokeStatement = StatementBuilder.create()
                .addVariable("str", String.class)
                .addVariable("n", Integer.class, 123)
                .loadVariable("str")
                .invoke("endsWith", Variable.get("n"));

        assertEquals("failed to generate invocation with parameter type conversion",
                "str.endsWith(\"123\")", invokeStatement.toJavaString());
    }

    @Test
    public void testInvokeStaticMethod() {
        Builder invokeStatement = StatementBuilder.create()
                .invokeStatic(Integer.class, "getInteger", "123");

        assertEquals("failed to generate static method invocation",
                "java.lang.Integer.getInteger(\"123\")", invokeStatement.toJavaString());
    }

    public void testInvokeUndefinedStaticMethod() {

        try {
            StatementBuilder.create()
                    .invokeStatic(Integer.class, "undefinedMethod", "123")
                    .toJavaString();
            fail("expected UndefinedMethodException");
        } catch (UndefinedMethodException udme) {
            assertTrue(udme.getMessage().contains("undefinedMethod"));
        }
    }
}
