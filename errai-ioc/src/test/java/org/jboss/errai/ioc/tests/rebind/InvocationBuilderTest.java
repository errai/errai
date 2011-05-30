package org.jboss.errai.ioc.tests.rebind;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.errai.ioc.client.api.builtin.MessageBusProvider;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Refs;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ContextBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedMethodException;
import org.junit.Test;

/**
 * Tests the generation of method invocations using the {@link StatementBuilder} API.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InvocationBuilderTest extends AbstractStatementBuilderTest {

    @Test
    public void testInvoke() {
        Statement invokeStatement = StatementBuilder.create()
                .addVariable(Variable.create("injector", MessageBusProvider.class))
                .loadVariable("injector")
                .invoke("provide");

        assertEquals("failed to generate invocation on variable",
                "injector.provide()", invokeStatement.generate());

        invokeStatement = StatementBuilder.create()
                .addVariable("i", Integer.class)
                .addVariable("regex", String.class)
                .addVariable("replacement", String.class)
                .loadVariable("i")
                .invoke("toString")
                .invoke("replaceAll", Variable.get("regex"), Variable.get("replacement"));

        assertEquals("failed to generate multiple invocations on variable",
                "i.toString().replaceAll(regex, replacement)", invokeStatement.generate());
    }

    @Test
    public void testInvokeWithLiterals() {
        String result = StatementBuilder.create().addVariable(Variable.create("s", String.class))
                .loadVariable("s").invoke("replaceAll", "foo", "foo\t\n").generate();

        assertEquals("failed to generate invocation using literal parameters",
                "s.replaceAll(\"foo\", \"foo\\t\\n\")", result);

        result = StatementBuilder.create().loadLiteral("foo").invoke("toString").generate();

        assertEquals("failed to generate invocation using literal parameters",
                "\"foo\".toString()", result);
    }

    @Test
    public void testInvokeOnUndefinedMethods() {
        try {
            StatementBuilder.create()
                    .addVariable("injector", MessageBusProvider.class)
                    .addVariable("param", String.class)
                    .loadVariable("injector")
                    .invoke("provide", Variable.get("param"));
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
                    .invoke("idontexist", Variable.get("regex"), Variable.get("replacement"));
            fail("expected UndefinedMethodException");
        } catch (UndefinedMethodException udme) {
            //expected
        }
    }

    @Test
    public void testInvokeWithUndefinedVariables() {
        try {
            // injector undefined
            StatementBuilder.create()
                    .loadVariable("injector")
                    .invoke("provide", Refs.get("param"), Refs.get("param2"));
            fail("expected OutOfScopeException");
        } catch (OutOfScopeException oose) {
            //expected
            assertTrue(oose.getMessage().contains("injector"));
        }

        try {
            // param2 undefined
            StatementBuilder.create()
                    .addVariable(Variable.create("injector", MessageBusProvider.class))
                    .addVariable(Variable.create("param", String.class))
                    .loadVariable("injector")
                    .invoke("provide", Variable.get("param"), Variable.get("param2"));
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
                .invoke("toUpperCase").generate();

        assertEquals("failed using load() passing a Reference",
                "s.toUpperCase()", s);

        Variable v = Variable.create("s", String.class);
        s = StatementBuilder.create(context)
                .load(v)
                .invoke("toUpperCase").generate();

        assertEquals("failed using load() passing a Variable instance",
                "s.toUpperCase()", s);

        s = StatementBuilder.create(context)
                .load("foo")
                .invoke("toUpperCase").generate();

        assertEquals("failed injecting literal with load()",
                "\"foo\".toUpperCase()", s);
    }
}
