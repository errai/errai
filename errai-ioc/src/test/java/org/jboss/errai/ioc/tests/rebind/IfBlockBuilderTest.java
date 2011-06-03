package org.jboss.errai.ioc.tests.rebind;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ContextBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder;
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

        assertEquals("Failed to generate empty if block using no rhs",
                EMPTY_IF_BLOCK_RESULT_NO_RHS, s.generate(Context.create()));
    }

    @Test
    public void testEmptyIfBlockUsingLiteralRhs() {
        Statement s = StatementBuilder.create()
                .addVariable("n", int.class)
                .loadVariable("n")
                .if_(BooleanOperator.Equals, 1, null);

        assertEquals("Failed to generate empty if block using a literal rhs",
                EMPTY_IF_BLOCK_RESULT_LITERAL_RHS, s.generate(Context.create()));
    }

    @Test
    public void testIfElseBlockUsingNoRhs() {
        Statement s = StatementBuilder.create()
                .addVariable("str", String.class)
                .loadVariable("str")
                .invoke("endsWith", "abc")
                .if_(ContextBuilder.create().declareVariable("n", Integer.class).initializeWith(0))
                .else_(ContextBuilder.create().declareVariable("n", Integer.class).initializeWith(1));

        assertEquals("Failed to generate empty if block using no rhs",
                IF_ELSE_BLOCK_RESULT_NO_RHS, s.generate(Context.create()));
    }

    @Test
    public void testIfElseBlockUsingRhs() {
        Statement s = StatementBuilder.create()
                .addVariable("n", Integer.class)
                .addVariable("m", Integer.class)
                .loadVariable("n")
                .if_(BooleanOperator.GreaterThan, Variable.get("m"),
                        ContextBuilder.create().declareVariable("n", Integer.class).initializeWith(0))
                .else_(ContextBuilder.create().declareVariable("n", Integer.class).initializeWith(1));

        assertEquals("Failed to generate empty if block using a rhs",
                IF_ELSE_BLOCK_RESULT_RHS, s.generate(Context.create()));
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
                    .if_(StatementBuilder.create(c).loadVariable("n").assignValue(1)));

        assertEquals("Failed to generate if - if - block using no rhs",
                IF_ELSEIF_BLOCK_RESULT_NO_RHS, s.generate(Context.create()));

         s = StatementBuilder.create(c)
                .loadVariable("s")
                .invoke("endsWith", "abc")
                .if_(StatementBuilder.create(c).loadVariable("n").assignValue(0),
                        StatementBuilder.create(c).loadVariable("s")
                        .invoke("startsWith", "def")
                        .if_(StatementBuilder.create(c).loadVariable("n").assignValue(1))
                        .else_(StatementBuilder.create(c).loadVariable("n").assignValue(2)));

        assertEquals("Failed to generate if - else if - else block using no rhs",
                IF_ELSEIF_ELSE_BLOCK_RESULT_NO_RHS, s.generate(Context.create()));
    }

    @Test
    public void testIfElseIfBlockUsingRhs() {
        Context c = ContextBuilder.create().addVariable("n", Integer.class).addVariable("m", Integer.class).getContext();

        Statement s = StatementBuilder.create(c)
                .loadVariable("n")
                .if_(BooleanOperator.GreaterThan, Variable.get("m"), 
                        StatementBuilder.create(c).loadVariable("n").assignValue(0),
                            StatementBuilder.create(c).loadVariable("m")
                                .if_(BooleanOperator.GreaterThan, Variable.get("n"),
                                        StatementBuilder.create(c).loadVariable("n").assignValue(1))
                                .else_(StatementBuilder.create(c).loadVariable("n").assignValue(2)));

        assertEquals("Failed to generate if - else if - else block using rhs",
                IF_ELSEIF_ELSE_BLOCK_RESULT_RHS, s.generate(Context.create()));
    }
}