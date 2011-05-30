package org.jboss.errai.ioc.tests.rebind;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.VariableReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ContextBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link StatementBuilder} API.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class StatementBuilderTest extends AbstractStatementBuilderTest {

    @Test
    public void testAddVariable() {
        Context ctx = StatementBuilder.create().addVariable("n", Integer.class, 10).getContext();
        VariableReference n = ctx.getVariable("n");
        assertEquals("n", n.getName());
        Assert.assertEquals(MetaClassFactory.get(Integer.class), n.getType());
        Assert.assertEquals(LiteralFactory.getLiteral(10), n.getValue());
        
        ctx = StatementBuilder.create().addVariable("n", 10).getContext();
        n = ctx.getVariable("n");
        assertEquals("n", n.getName());
        Assert.assertEquals(MetaClassFactory.get(Integer.class), n.getType());
        Assert.assertEquals(LiteralFactory.getLiteral(10), n.getValue());
               
        ctx = StatementBuilder.create().addVariable("n", "10").getContext();
        n = ctx.getVariable("n");
        assertEquals("n", n.getName());
        Assert.assertEquals(MetaClassFactory.get(String.class), n.getType());
        Assert.assertEquals(LiteralFactory.getLiteral("10"), n.getValue());
        
        ctx = StatementBuilder.create().addVariable("n", Integer.class, "10").getContext();
        n = ctx.getVariable("n");
        assertEquals("n", n.getName());
        Assert.assertEquals(MetaClassFactory.get(Integer.class), n.getType());
        Assert.assertEquals(LiteralFactory.getLiteral(10), n.getValue());
        
        try {
            ctx = StatementBuilder.create().addVariable("n", Integer.class, "abc").getContext();
            fail("Expected InvalidTypeException");
        } catch(InvalidTypeException ive) {
            //expected
            assertTrue(ive.getCause() instanceof NumberFormatException);
        } 
    }
}
