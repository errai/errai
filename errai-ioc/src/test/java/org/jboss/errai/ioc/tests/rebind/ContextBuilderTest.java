package org.jboss.errai.ioc.tests.rebind;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.errai.ioc.client.api.builtin.MessageBusProvider;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.VariableReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ContextBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ObjectBuilder;
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
public class ContextBuilderTest extends AbstractStatementBuilderTest {

    @Test
    public void testDeclareVariable() {
        Statement declaration = ContextBuilder.create()
                .declareVariable("n", Integer.class)
                .initializeWith(10);
                
        assertEquals("failed to generate variable declaration using a literal initialization", 
                "java.lang.Integer n = 10;", declaration.generate());

        declaration = ContextBuilder.create()
            .declareVariable("n")
            .initializeWith(10);
        
        assertEquals("failed to generate variable declaration using a literal initialization and type inference", 
                "java.lang.Integer n = 10;", declaration.generate());

        declaration = ContextBuilder.create()
            .declareVariable("n")
            .initializeWith("10");
        
        assertEquals("failed to generate variable declaration using a literal initialization and type inference", 
            "java.lang.String n = \"10\";", declaration.generate());
    
        declaration = ContextBuilder.create()
            .declareVariable("n", Integer.class)
            .initializeWith("10");

        assertEquals("failed to generate variable declaration using a literal initialization and type conversion", 
               "java.lang.Integer n = 10;", declaration.generate());
        
        try {
            ContextBuilder.create()
                .declareVariable("n", Integer.class)
                .initializeWith("abc")
                .generate();
            fail("Expected InvalidTypeException");
        } catch(InvalidTypeException ive) {
            //expected
            assertTrue(ive.getCause() instanceof NumberFormatException);
        }
    }
    
    @Test
    public void testDeclareVariableWithObjectInitialization() {
        Statement declaration = ContextBuilder.create()
            .declareVariable("injector", MessageBusProvider.class)
            .initializeWith(ObjectBuilder.newInstanceOf(MessageBusProvider.class));
        
        assertEquals("failed to generate variable declaration using an objectbuilder initialization", 
                "org.jboss.errai.ioc.client.api.builtin.MessageBusProvider injector = " +
                "new org.jboss.errai.ioc.client.api.builtin.MessageBusProvider();", declaration.generate());
        
        declaration = ContextBuilder.create()
            .declareVariable("str", String.class)
            .initializeWith(ObjectBuilder.newInstanceOf(String.class).withParameters("abc"));
    
        assertEquals("failed to generate variable declaration using an objectbuilder initialization with parameters", 
            "java.lang.String str = new java.lang.String(\"abc\");", declaration.generate());
        
        try {
            ContextBuilder.create()
                .declareVariable("str", Integer.class)
                .initializeWith(ObjectBuilder.newInstanceOf(String.class).withParameters("abc"));
            fail("Expected InvalidTypeException");
        } catch (InvalidTypeException ive) {
            // expected
        }
    }
    
    @Test
    public void testAddVariableWithLiteralInitialization() {
        Context ctx = ContextBuilder.create().addVariable("n", Integer.class, 10).getContext();
        VariableReference n = ctx.getVariable("n");
        assertEquals("Wrong variable name", "n", n.getName());
        Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
        Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());
        
        ctx = ContextBuilder.create().addVariable("n", 10).getContext();
        n = ctx.getVariable("n");
        assertEquals("Wrong variable name", "n", n.getName());
        Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
        Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());
               
        ctx = ContextBuilder.create().addVariable("n", "10").getContext();
        n = ctx.getVariable("n");
        assertEquals("Wrong variable name", "n", n.getName());
        Assert.assertEquals("Wrong variable type", MetaClassFactory.get(String.class), n.getType());
        Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral("10"), n.getValue());
        
        ctx = ContextBuilder.create().addVariable("n", Integer.class, "10").getContext();
        n = ctx.getVariable("n");
        assertEquals("Wrong variable name", "n", n.getName());
        Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
        Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());
       
        try {
            ctx = ContextBuilder.create().addVariable("n", Integer.class, "abc").getContext();
            fail("Expected InvalidTypeException");
        } catch(InvalidTypeException ive) {
            //expected
            assertTrue(ive.getCause() instanceof NumberFormatException);
        } 
    }
    
    @Test
    public void testAddVariableWithObjectInitialization() {
        Context ctx = ContextBuilder.create().addVariable("injector", MessageBusProvider.class, 
                ObjectBuilder.newInstanceOf(MessageBusProvider.class)).getContext();
        
        VariableReference injector = ctx.getVariable("injector");
        assertEquals("Wrong variable name", "injector", injector.getName());
        Assert.assertEquals("Wrong variable type", MetaClassFactory.get(MessageBusProvider.class), injector.getType());
        
        ctx = ContextBuilder.create().addVariable("injector", 
                ObjectBuilder.newInstanceOf(MessageBusProvider.class)).getContext();
        
        injector = ctx.getVariable("injector");
        assertEquals("Wrong variable name", "injector", injector.getName());
        Assert.assertEquals("Wrong variable type", MetaClassFactory.get(MessageBusProvider.class), injector.getType());
    }
}