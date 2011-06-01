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
   public void testSimpleIfWithLiteral() {
       Statement s = StatementBuilder.create()
           .addVariable("n", Integer.class)
           .loadVariable("n")
           .if_(BooleanOperator.Equals, 10);
       
       System.out.println(s.generate());
   }
}
