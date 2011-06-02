package org.jboss.errai.ioc.tests.rebind;

import org.jboss.errai.ioc.client.api.builtin.MessageBusProvider;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.VariableReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the {@link org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder} API.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class StatementBuilderTest extends AbstractStatementBuilderTest {

    @Test
    public void testAddVariableWithLiteralInitialization() {
        Context ctx = StatementBuilder.create().addVariable("n", Integer.class, 10).getContext();

        VariableReference n = ctx.getVariable("n");
        assertEquals("Wrong variable name", "n", n.getName());
        Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
        Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());

        ctx = StatementBuilder.create().addVariable("n", 10).getContext();

        n = ctx.getVariable("n");
        assertEquals("Wrong variable name", "n", n.getName());
        Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
        Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());

        ctx = StatementBuilder.create().addVariable("n", "10").getContext();

        n = ctx.getVariable("n");
        assertEquals("Wrong variable name", "n", n.getName());
        Assert.assertEquals("Wrong variable type", MetaClassFactory.get(String.class), n.getType());
        Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral("10"), n.getValue());

        ctx = StatementBuilder.create().addVariable("n", Integer.class, "10").getContext();

        n = ctx.getVariable("n");
        assertEquals("Wrong variable name", "n", n.getName());
        Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
        Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());

        try {
            ctx = StatementBuilder.create().addVariable("n", Integer.class, "abc").getContext();
            fail("Expected InvalidTypeException");
        } catch (InvalidTypeException ive) {
            //expected
            assertTrue(ive.getCause() instanceof NumberFormatException);
        }
    }

    @Test
    public void testAddVariableWithObjectInitialization() {
        Context ctx = StatementBuilder.create().addVariable("injector", MessageBusProvider.class,
                ObjectBuilder.newInstanceOf(MessageBusProvider.class)).getContext();

        VariableReference injector = ctx.getVariable("injector");
        assertEquals("Wrong variable name", "injector", injector.getName());
        Assert.assertEquals("Wrong variable type", MetaClassFactory.get(MessageBusProvider.class), injector.getType());

        ctx = StatementBuilder.create().addVariable("injector",
                ObjectBuilder.newInstanceOf(MessageBusProvider.class)).getContext();

        injector = ctx.getVariable("injector");
        assertEquals("Wrong variable name", "injector", injector.getName());
        Assert.assertEquals("Wrong variable type", MetaClassFactory.get(MessageBusProvider.class), injector.getType());
    }

    @Test
    public void testUndefinedVariable() {
        try {
            StatementBuilder.create().loadVariable("n").toJavaString();
            fail("Expected OutOfScopeException");
        } catch (OutOfScopeException oose) {
            // expected
        }
    }
}