package org.jboss.errai.ioc.tests.rebind;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBuilder;
import org.junit.Test;

/**
 * Tests the generation of if blocks using the {@link StatementBuilder} API.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class IfBlockBuilderTest extends AbstractStatementBuilderTest {

    @Test
    public void testEmptyIfBlockUsingNoRhs() {
        Statement s = StatementBuilder.create()
            .addVariable("str", String.class)
            .loadVariable("str")
            .invoke("endsWith", "abc")
            .if_();

        assertEquals("Failed to generate empty if block using no rhs", "if (str.endsWith(\"abc\")) { }\n", s.generate());
    }

    @Test
   public void testEmptyIfBlockUsingLiteralRhs() {
       Statement s = StatementBuilder.create()
           .addVariable("n", Integer.class)
           .loadVariable("n")
           .if_(BooleanOperator.Equals, 10);
       
       assertEquals("Failed to generate empty if block using a literal rhs", "if (n == 10) { }\n", s.generate());
       
       s = StatementBuilder.create()
           .addVariable("str", String.class)
           .loadVariable("str")
           .if_(BooleanOperator.Equals, "abc");
   
       assertEquals("Failed to generate empty if block using a literal rhs", "if (str == \"abc\") { }\n", s.generate());
   }
}