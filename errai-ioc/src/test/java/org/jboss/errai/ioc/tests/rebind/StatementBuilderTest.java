package org.jboss.errai.ioc.tests.rebind;

import static org.junit.Assert.fail;

import java.util.List;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.TypeNotIterableException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedVariableException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.JavaReflectionClass;
import org.junit.Test;

/**
 * Tests for our {@link StatementBuilder} API.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class StatementBuilderTest {
    
    @Test
    public void testLoop() throws Exception {
        
        Statement createObject = StatementBuilder.create()
            .newObject(new JavaReflectionClass(Integer.class));

        Statement createAnotherObject = StatementBuilder.create()
            .newObject(new JavaReflectionClass(Integer.class));

        Statement loop = StatementBuilder.create()
                .loadVariable("list", new JavaReflectionClass(new TypeLiteral<List<String>>(){}))
                .foreach("element")
                .addStatement(createObject)
                .addStatement(createAnotherObject);

        Statement loopWithArray = StatementBuilder.create()
            .loadVariable("list", new JavaReflectionClass(String[].class))
            .foreach("element")
            .addStatement(createObject)
            .addStatement(createAnotherObject);
        

        Statement loopWithList = StatementBuilder.create()
            .loadVariable("list", new JavaReflectionClass(List.class))
            .foreach("element")
            .addStatement(createObject)
            .addStatement(createAnotherObject);
        
        System.out.println(loop.generate());
        System.out.println(loopWithArray.generate());
        System.out.println(loopWithList.generate());
    }
    
    @Test
    public void testNestedLoops() throws Exception {
        Statement createObject = StatementBuilder.create().newObject(
                new JavaReflectionClass(Integer.class));

        Statement outerLoop = StatementBuilder.create()
                .loadVariable("list", new JavaReflectionClass(new TypeLiteral<List<String>>(){}))
                .foreach("element")
                .addStatement(StatementBuilder.create()
                        .foreach("element2", "list")
                        .addStatement(createObject)
                 );
                
        System.out.println(outerLoop.generate());
    }
    
    @Test
    public void testNestedLoopsWithInvalidVariable() throws Exception {
        Statement createObject = StatementBuilder.create().newObject(
                new JavaReflectionClass(Integer.class));

        // uses a not existing list in inner loop -> should fail with UndefinedVariableExcpetion
        try {
            StatementBuilder.create()
                .loadVariable("list", new JavaReflectionClass(List.class))
                .foreach("element", "list")
                .addStatement(StatementBuilder.create()
                        .foreach("element2", "listDoesNotExist")
                        .addStatement(createObject)
                 )
                 .generate();
            fail("Expected UndefinedVariableException");
        } catch(UndefinedVariableException ude) {
            // expected
            System.out.println(ude.getMessage());
        }
    }
    
    @Test
    public void testLoopWithInvalidSequenceType() throws Exception {
        
        try {
            StatementBuilder.create()
                .loadVariable("list", new JavaReflectionClass(String.class))
                .foreach("element")
                .generate();
            fail("Expected TypeNotIterableException");
        } catch(TypeNotIterableException tnie) {
            // expected
            System.out.println(tnie.getMessage());
        }
    }
}
