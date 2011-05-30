package org.jboss.errai.ioc.tests.rebind;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.junit.Test;

/**
 * Tests the {@link StatementBuilder} API.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class StatementBuilderTest extends AbstractStatementBuilderTest {

    @Test
    public void testDeclareVariable() {
        Statement declaration = Context.create()
                .declareVariable("n", Integer.class)
                .initializeWith(10);
                
        assertEquals("failed to generate variable declaration using a literal initialization", 
                "java.lang.Integer n = 10;", declaration.generate());

        declaration = Context.create()
            .declareVariable("n")
            .initializeWith(10);
        
        assertEquals("failed to generate variable declaration using a literal initialization and type inference", 
                "java.lang.Integer n = 10;", declaration.generate());

        declaration = Context.create()
            .declareVariable("n")
            .initializeWith("10");
        
        assertEquals("failed to generate variable declaration using a literal initialization and type inference", 
            "java.lang.String n = \"10\";", declaration.generate());
    
        declaration = Context.create()
            .declareVariable("n", Integer.class)
            .initializeWith("10");

        assertEquals("failed to generate variable declaration using a literal initialization and type conversion", 
               "java.lang.Integer n = 10;", declaration.generate());
        
        try {
            Context.create()
                .declareVariable("n", Integer.class)
                .initializeWith("abc")
                .generate();
            fail("Expected InvalidTypeException");
        } catch(InvalidTypeException ive) {
            //expected
            assertTrue(ive.getCause() instanceof NumberFormatException);
        }
    }
}
