package org.jboss.errai.ioc.tests.rebind;

import static org.junit.Assert.fail;

import java.util.List;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.TypeNotIterableException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedVariableException;
import org.junit.Test;

/**
 * Tests the generation of loops using our {@link StatementBuilder} API.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class LoopStatementBuilderTest extends AbstractStatementBuilderTest implements LoopStatementBuilderTestResult {
    
    @Test
    public void testForeachLoop() throws Exception {
        Statement createObject = StatementBuilder.create()
            .newObject(String.class);

        Statement createAnotherObject = StatementBuilder.create()
            .newObject(Object.class);

        String foreachWithListOfStrings = StatementBuilder.create()
            .loadVariable("list", new TypeLiteral<List<String>>(){})
            .foreach("element")
            .execute(createObject)
            .generate();
         
       /* String foreachWithStringArray = StatementBuilder.create()
            .loadVariable("list", String[].class)
            .foreach("element")
            .execute(createObject)
            .generate();
            
        String foreachWithList = StatementBuilder.create()
            .loadVariable("list", List.class)
            .foreach("element")
            .execute(createObject)
            .execute(createAnotherObject)
            .generate();*/
        
        assertEquals(FOREACH_RESULT_STRING_IN_LIST, foreachWithListOfStrings);
        //assertEquals(FOREACH_RESULT_STRING_IN_LIST, foreachWithStringArray);
        //assertEquals(FOREACH_RESULT_OBJECT_IN_LIST_TWO_STATEMENTS, foreachWithList);
    }
    
    @Test
    public void testForeachLoopWithProvidedLoopVarType() throws Exception {
        Statement loop = StatementBuilder.create()
            .loadVariable("list", new TypeLiteral<List<String>>(){})
            .foreach("element", Object.class, "list");
        
        assertEquals(FOREACH_RESULT_OBJECT_IN_LIST_EMPTY_BODY, loop.generate());
        
        try {
            StatementBuilder.create()
                .loadVariable("list", new TypeLiteral<List<String>>(){})
                .foreach("element", Integer.class)
                .generate();
  
            fail("Expected InvalidTypeException");
        } catch(InvalidTypeException ite) {
            // expected
        }
    }
    
    @Test
    public void testNestedForeachLoops() throws Exception {
        Statement createObject = StatementBuilder.create().newObject(Integer.class);

        Statement outerLoop = StatementBuilder.create()
            .loadVariable("list", new TypeLiteral<List<String>>(){})
            .foreach("element")
            .execute(StatementBuilder.create()
                    .loadVariable("anotherList", new TypeLiteral<List<String>>(){})
                    .foreach("anotherElement", "anotherList")
                    .execute(createObject)
             );
                
        assertEquals(FOREACH_RESULT_NESTED_STRING_IN_LIST, outerLoop.generate());
    }
    
    @Test
    public void testNestedForeachLoopsWithInvalidVariable() throws Exception {
        Statement createObject = StatementBuilder.create().newObject(Integer.class);

        // uses a not existing list in inner loop -> should fail with UndefinedVariableExcpetion
        try {
            StatementBuilder.create()
                .loadVariable("list", List.class)
                .foreach("element", "list")
                .execute(StatementBuilder.create()
                        .loadVariable("list2", new TypeLiteral<List<String>>(){})
                        .foreach("element2", "listDoesNotExist")
                        .execute(createObject)
                 )
                 .generate();

            fail("Expected UndefinedVariableException");
        } catch(UndefinedVariableException ude) {
            // expected
        }
    }
    
    @Test
    public void testForeachLoopWithInvalidCollectionType() throws Exception {
        
        try {
            StatementBuilder.create()
                .loadVariable("list", String.class)
                .foreach("element")
                .generate();

            fail("Expected TypeNotIterableException");
        } catch(TypeNotIterableException tnie) {
            // expected
        }
    }
}
