package org.jboss.errai.ioc.tests.rebind;

import org.jboss.errai.ioc.client.api.builtin.MessageBusProvider;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedMethodException;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Tests for the {@link StatementBuilder} API.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class StatementBuilderTest extends AbstractStatementBuilderTest {

    @Test
    public void testInvoke() {
        /*  try {
           StatementBuilder.create()
               .loadVariable("injector", MessageBusProvider.class)
               .invoke("provide", Variable.get("param", String.class), Variable.get("param2", Integer.class));
           fail("expected UndefinedVariableException");
       } catch(UndefinedVariableException udve) {
           //expected
       } */

        try {
            StatementBuilder.create()
                    .loadVariable("injector", MessageBusProvider.class)
                    .invoke("provide", Variable.get("param", String.class));
            fail("expected UndefinedMethodException");
        } catch (UndefinedMethodException udme) {
            //expected
        }

        try {
            StatementBuilder.create()
                    .loadVariable("s", String.class)
                    .invoke("replaceAll", Variable.get("regex", String.class), Variable.get("replacement", String.class))
                    .invoke("idontexist", Variable.get("regex", String.class), Variable.get("replacement", String.class));
            fail("expected UndefinedMethodException");
        } catch (UndefinedMethodException udme) {
            //expected
        }

        Statement invokeStatement = StatementBuilder.create()
                .loadVariable("injector", MessageBusProvider.class)
                .invoke("provide");

        assertEquals("injector.provide()", invokeStatement.generate());

        invokeStatement = StatementBuilder.create()
                .loadVariable("i", Integer.class)
                .invoke("toString")
                .invoke("replaceAll", Variable.get("regex", String.class), Variable.get("replacement", String.class));

        assertEquals("i.toString().replaceAll(regex, replacement)", invokeStatement.generate());
    }

    @Test
    public void testCallLiterals() {
        System.out.println(StatementBuilder.create().loadVariable("s", String.class).invoke("replaceAll", "foo", "foo\t\n").generate());
    }
}
