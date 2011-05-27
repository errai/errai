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
                    Context.create().add(Variable.get("injector", MessageBusProvider.class)))
                .loadVariable("injector")
                .invoke("provide");
        assertEquals("injector.provide()", invokeStatement.generate());

        invokeStatement = StatementBuilder.create(Context.create()
                    .add(Variable.get("i", Integer.class))
                    .add(Variable.get("regex", String.class))
                    .add(Variable.get("replacement", String.class)))
                .loadVariable("i")
                .invoke("toString")
                .invoke("replaceAll", Variable.get("regex"), Variable.get("replacement"));
        assertEquals("i.toString().replaceAll(regex, replacement)", invokeStatement.generate());
    }

    @Test
    public void testInvokeWithLiterals() {
        String result = StatementBuilder.create(Context.create().add(Variable.get("s", String.class)))
                .loadVariable("s").invoke("replaceAll", "foo", "foo\t\n").generate();

        assertEquals("s.replaceAll(\"foo\", \"foo\\t\\n\")", result);
        
        result = StatementBuilder.create().loadLiteral("foo").invoke("toString").generate();
        assertEquals("\"foo\".toString()", result);
    }
    
    @Test
    public void testInvokeOnUndefinedMethods() {
        try {
            StatementBuilder.create(
                    Context.create()
                        .add(Variable.get("injector", MessageBusProvider.class))
                        .add(Variable.get("param", String.class)))
                .loadVariable("injector")
                .invoke("provide", Variable.get("param"));
            fail("expected UndefinedMethodException");
        } catch (UndefinedMethodException udme) {
            //expected
        }

        try {
            Context context = Context.create()
                .add(Variable.get("s", String.class))
                .add(Variable.get("regex", String.class))
                .add(Variable.get("replacement", String.class));
            
            StatementBuilder.create(context)
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
                .invoke("provide", Variable.get("param"), Variable.get("param2"));
            fail("expected OutOfScopeException");
        } catch(OutOfScopeException oose) {
            //expected
            assertTrue(oose.getMessage().contains("injector"));
        } 
        
        try {
            // param2 undefined
            StatementBuilder.create(Context.create()
                    .add(Variable.get("injector", MessageBusProvider.class))
                    .add(Variable.get("param", String.class)))
                .loadVariable("injector")
                .invoke("provide", Variable.get("param"), Variable.get("param2"));
            fail("expected OutOfScopeException");
        } catch(OutOfScopeException oose) {
            //expected
            assertTrue(oose.getMessage().contains("param2"));
        } 
    }
}
