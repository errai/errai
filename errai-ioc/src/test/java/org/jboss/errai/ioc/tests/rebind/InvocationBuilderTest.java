package org.jboss.errai.ioc.tests.rebind;

import org.jboss.errai.ioc.client.api.builtin.MessageBusProvider;
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
 * Tests the generation of method invocations using the {@link org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder} API.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InvocationBuilderTest extends AbstractStatementBuilderTest {

    @Test
    public void testInvoke() {
        Builder invokeStatement = StatementBuilder.create()
                .addVariable("injector", MessageBusProvider.class)
                .loadVariable("injector")
                .invoke("provide");

        assertEquals("failed to generate invocation on variable",
                "injector.provide()", invokeStatement.toJavaString());

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
                    .addVariable("injector", MessageBusProvider.class)
                    .addVariable("param", String.class)
                    .loadVariable("injector")
                    .invoke("provide", Variable.get("param"))
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
                    .invoke("idontexist", Variable.get("regex"), Variable.get("replacement"))
                    .toJavaString();
            fail("expected UndefinedMethodException");
        } catch (UndefinedMethodException udme) {
            //expected
            assertEquals("Wrong exception thrown", udme.getMethodName(), "idontexist");
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
                    .addVariable("injector", MessageBusProvider.class)
                    .addVariable("param", String.class)
                    .loadVariable("injector")
                    .invoke("provide", Variable.get("param"), Variable.get("param2"))
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
}
