package org.jboss.errai.ioc.tests.rebind;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ContextBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBuilder;
import org.junit.Test;

/**
 * Tests the generation of if blocks using the {@link StatementBuilder} API.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class IfBlockBuilderTest extends AbstractStatementBuilderTest implements IfBlockBuilderTestResult {

    @Test
    public void testEmptyIfBlockUsingNoRhs() {
        Statement s = StatementBuilder.create()
            .addVariable("str", String.class)
            .loadVariable("str")
            .invoke("endsWith", "abc")
            .if_(null);

        assertEquals("Failed to generate empty if block using no rhs", EMPTY_IF_BLOCK_RESULT_NO_RHS, s.generate());
    }

    @Test
    public void testIfElseBlockUsingNoRhs() {
        Statement s = StatementBuilder.create()
            .addVariable("str", String.class)
            .loadVariable("str")
            .invoke("endsWith", "abc")
            .if_(ContextBuilder.create().declareVariable("n", Integer.class).initializeWith(0))
            .else_(ContextBuilder.create().declareVariable("n", Integer.class).initializeWith(1));
        
        assertEquals("Failed to generate empty if block using no rhs", IF_ELSE_BLOCK_RESULT_NO_RHS, s.generate());
   }

    @Test
    public void testIfElseIfBlockUsingNoRhs() {
       Context c = ContextBuilder.create().addVariable("s", String.class).addVariable("n", Integer.class).getContext();
        
        Statement s = StatementBuilder.create(c)
            .loadVariable("s")
            .invoke("endsWith", "abc")
            .if_(StatementBuilder.create(c).loadVariable("n").assignValue(0), 
                    StatementBuilder.create(c).loadVariable("s")
                    .invoke("startsWith", "def")
                    .if_(StatementBuilder.create(c).loadVariable("n").assignValue(1))
                    .else_(StatementBuilder.create(c).loadVariable("n").assignValue(2)));
        
        assertEquals("Failed to generate if - else if - else block using no rhs", 
                IF_ELSEIF_ELSE_BLOCK_RESULT_NO_RHS, s.generate());
   }

   @Test
   public void testEmptyIfBlockUsingLiteralRhs() {
       Statement s = StatementBuilder.create()
           .addVariable("n", int.class)
           .loadVariable("n")
           .if_(BooleanOperator.Equals, 1, null);
       
       assertEquals("Failed to generate empty if block using a literal rhs", 
               EMPTY_IF_BLOCK_RESULT_LITERAL_RHS, s.generate());
   }
}