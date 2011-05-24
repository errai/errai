package org.jboss.errai.ioc.tests.rebind;

import static org.junit.Assert.fail;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedVariableException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.JavaReflectionClass;
import org.junit.Test;

/**
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class StatementBuilderTest {

    @Test
    public void testLoop() throws Exception {
        Statement createObject = StatementBuilder.create().newObject(
                new JavaReflectionClass(Class.forName("java.lang.Integer")));

        Statement createAnotherObject = StatementBuilder.create().newObject(
                new JavaReflectionClass(Class.forName("java.lang.Integer")));

        Statement loop = StatementBuilder.create()
                .loadVariable("element", new JavaReflectionClass(Class.forName("java.lang.Integer")))
                .loadVariable("list", new JavaReflectionClass(Class.forName("java.util.List")))
                .loop("element", "list")
                .addStatement(createObject)
                .addStatement(createAnotherObject);

        System.out.println(loop.generate());
    }
    
    @Test
    public void testNestedLoops() throws Exception {
        Statement createObject = StatementBuilder.create().newObject(
                new JavaReflectionClass(Class.forName("java.lang.Integer")));

        Statement outerLoop = StatementBuilder.create()
                .loadVariable("element", new JavaReflectionClass(Class.forName("java.lang.Integer")))
                .loadVariable("list", new JavaReflectionClass(Class.forName("java.util.List")))
                .loop("element", "list")
                .addStatement(StatementBuilder.create()
                        .loadVariable("element2", new JavaReflectionClass(Class.forName("java.lang.Integer")))
                        .loop("element2", "list")
                        .addStatement(createObject)
                 );
                
        System.out.println(outerLoop.generate());
    }
    
    @Test
    public void testNestedLoopsWithInvalidVariable() throws Exception {
        Statement createObject = StatementBuilder.create().newObject(
                new JavaReflectionClass(Class.forName("java.lang.Integer")));

        // uses a not existing list in inner loop -> should fail with UndefinedVariableExcpetion
        try {
            StatementBuilder.create()
                .loadVariable("element", new JavaReflectionClass(Class.forName("java.lang.Integer")))
                .loadVariable("list", new JavaReflectionClass(Class.forName("java.util.List")))
                .loop("element", "list")
                .addStatement(StatementBuilder.create()
                        .loadVariable("element2", new JavaReflectionClass(Class.forName("java.lang.Integer")))
                        .loop("element2", "listDoesNotExist")
                        .addStatement(createObject)
                 )
                 .generate();
            fail("Expected UndefinedVariableException");
        } catch(UndefinedVariableException ude) {
            // expected
            System.out.println(ude.getMessage());
        }
    }
}
