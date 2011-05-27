package org.jboss.errai.ioc.tests.rebind;

import org.jboss.errai.ioc.client.api.builtin.MessageBusProvider;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedMethodException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for the {@link StatementBuilder} API.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InvocationBuilderTest extends AbstractStatementBuilderTest {

    @Test
    public void testInvoke() {
        Statement invokeStatement = StatementBuilder.create(
                    Context.create().push(Variable.get("injector", MessageBusProvider.class)))
                .loadVariable("injector", MessageBusProvider.class)
                .invoke("provide");
        assertEquals("injector.provide()", invokeStatement.generate());

        invokeStatement = StatementBuilder.create(Context.create()
                    .push(Variable.get("i", Integer.class))
                    .push(Variable.get("regex", String.class))
                    .push(Variable.get("replacement", String.class)))
                .loadVariable("i", Integer.class)
                .invoke("toString")
                .invoke("replaceAll", Variable.get("regex", String.class), Variable.get("replacement", String.class));
        assertEquals("i.toString().replaceAll(regex, replacement)", invokeStatement.generate());
    }

    @Test
    public void testInvokeWithLiterals() {
        final String expected = "s.replaceAll(\"foo\", \"foo\\t\\n\")";
        final String result = StatementBuilder.create(Context.create().push(Variable.get("s", String.class)))
                .loadVariable("s", String.class).invoke("replaceAll", "foo", "foo\t\n").generate();

        assertEquals(expected, result);
    }
    
    @Test
    public void testInvokeOnUndefinedMethods() {
        try {
            StatementBuilder.create(
                    Context.create()
                        .push(Variable.get("injector", MessageBusProvider.class))
                        .push(Variable.get("param", String.class)))
                .loadVariable("injector", MessageBusProvider.class)
                .invoke("provide", Variable.get("param", String.class));
            fail("expected UndefinedMethodException");
        } catch (UndefinedMethodException udme) {
            //expected
        }

        try {
            Context context = Context.create()
                .push(Variable.get("s", String.class))
                .push(Variable.get("regex", String.class))
                .push(Variable.get("replacement", String.class));
            
            StatementBuilder.create(context)
                .loadVariable("s", String.class)
                .invoke("replaceAll", Variable.get("regex", String.class), Variable.get("replacement", String.class))
                .invoke("idontexist", Variable.get("regex", String.class), Variable.get("replacement", String.class));
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
                .loadVariable("injector", MessageBusProvider.class)
                .invoke("provide", Variable.get("param", String.class), Variable.get("param2", Integer.class));
            fail("expected OutOfScopeException");
        } catch(OutOfScopeException oose) {
            //expected
            assertTrue(oose.getMessage().contains("injector"));
        } 
        
        try {
            // param2 undefined
            StatementBuilder.create(Context.create()
                    .push(Variable.get("injector", MessageBusProvider.class))
                    .push(Variable.get("param", String.class)))
                .loadVariable("injector", MessageBusProvider.class)
                .invoke("provide", Variable.get("param", String.class), Variable.get("param2", Integer.class));
            fail("expected OutOfScopeException");
        } catch(OutOfScopeException oose) {
            //expected
            assertTrue(oose.getMessage().contains("param2"));
        } 
    }
}
