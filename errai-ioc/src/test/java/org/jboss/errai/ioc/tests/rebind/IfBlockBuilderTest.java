/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.tests.rebind;

import org.jboss.errai.ioc.rebind.ioc.codegen.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ContextBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidExpressionException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
                .if_()
                .finish();

        assertEquals("Failed to generate empty if block using no rhs",
                EMPTY_IF_BLOCK_RESULT_NO_RHS, s.generate(Context.create()));
    }

    @Test
    public void testEmptyIfBlockUsingLiteralRhs() {
        Statement s = StatementBuilder.create()
                .addVariable("n", int.class)
                .loadVariable("n")
                .if_(BooleanOperator.Equals, 1)
                .finish();

        assertEquals("Failed to generate empty if block using a literal rhs",
                EMPTY_IF_BLOCK_RESULT_LITERAL_RHS, s.generate(Context.create()));
    }

    @Test
    public void testIfElseBlockUsingNoRhs() {
        Statement s = StatementBuilder.create()
                .addVariable("str", String.class)
                .loadVariable("str")
                .invoke("endsWith", "abc")
                .if_()
                .append(ContextBuilder.create().declareVariable("n", Integer.class).initializeWith(0))
                .finish()
                .else_()
                .append(ContextBuilder.create().declareVariable("n", Integer.class).initializeWith(1))
                .finish();

        assertEquals("Failed to generate empty if block using no rhs",
                IF_ELSE_BLOCK_RESULT_NO_RHS, s.generate(Context.create()));
    }

    @Test
    public void testIfElseBlockUsingRhs() {
        Statement s = StatementBuilder.create()
                .addVariable("n", Integer.class)
                .addVariable("m", Integer.class)
                .loadVariable("n")
                .if_(BooleanOperator.GreaterThan, Variable.get("m"))
                .append(ContextBuilder.create().declareVariable("n", Integer.class).initializeWith(0))
                .finish()
                .else_()
                .append(ContextBuilder.create().declareVariable("n", Integer.class).initializeWith(1))
                .finish();

        assertEquals("Failed to generate empty if block using a rhs",
                IF_ELSE_BLOCK_RESULT_RHS, s.generate(Context.create()));
    }

    @Test
    public void testIfElseIfBlockUsingNoRhs() {
        Context c = ContextBuilder.create().addVariable("s", String.class).addVariable("n", Integer.class).getContext();

        Statement s = StatementBuilder.create(c)
                .loadVariable("s")
                .invoke("endsWith", "abc")
                .if_()
                .append(StatementBuilder.create(c).loadVariable("n").assignValue(0))
                .finish()
                .else_()
                .append(StatementBuilder.create(c).loadVariable("s")
                        .invoke("startsWith", "def")
                        .if_()
                        .append(StatementBuilder.create(c).loadVariable("n").assignValue(1))
                        .finish()
                )
                .finish();

        assertEquals("Failed to generate if - if - block using no rhs",
                IF_ELSEIF_BLOCK_RESULT_NO_RHS_NESTED, s.generate(Context.create()));

        s = StatementBuilder.create(c)
                .loadVariable("s")
                .invoke("endsWith", "abc")
                .if_()
                .append(StatementBuilder.create(c).loadVariable("n").assignValue(0))
                .finish()
                .else_()
                .append(StatementBuilder.create(c).loadVariable("s")
                        .invoke("startsWith", "def")
                        .if_()
                        .append(StatementBuilder.create(c).loadVariable("n").assignValue(1))
                        .finish()
                        .else_()
                        .append(StatementBuilder.create(c).loadVariable("n").assignValue(2))
                        .finish()
                )
                .finish();

        assertEquals("Failed to generate if - else if - else block using no rhs",
                IF_ELSEIF_ELSE_BLOCK_RESULT_NO_RHS_NESTED, s.generate(Context.create()));
    }

    @Test
    public void testIfElseIfBlockUsingRhs() {
        Context c = ContextBuilder.create().addVariable("n", Integer.class).addVariable("m", Integer.class).getContext();

        Statement s = StatementBuilder.create(c)
                .loadVariable("n")
                .if_(BooleanOperator.GreaterThan, Variable.get("m"))
                .append(StatementBuilder.create(c).loadVariable("n").assignValue(0))
                .finish()
                .else_()
                .append(StatementBuilder.create(c).loadVariable("m")
                        .if_(BooleanOperator.GreaterThan, Variable.get("n"))
                        .append(StatementBuilder.create(c).loadVariable("n").assignValue(1))
                        .finish()
                        .else_()
                        .append(StatementBuilder.create(c).loadVariable("n").assignValue(2))
                        .finish()
                )
                .finish();

        assertEquals("Failed to generate if - else if - else block using rhs",
                IF_ELSEIF_ELSE_BLOCK_RESULT_RHS_NESTED, s.generate(Context.create()));
    }

    @Test
    public void testIfElseIfBlockUsingNoRhsElseIfKeyword() {
        Context c = ContextBuilder.create().addVariable("s", String.class).addVariable("n", Integer.class).getContext();

        Statement s = StatementBuilder.create(c)
                .loadVariable("s")
                .invoke("endsWith", "abc")
                .if_()
                .append(StatementBuilder.create(c).loadVariable("n").assignValue(0))
                .finish()
                .elseif_(StatementBuilder.create(c).loadVariable("s").invoke("startsWith", "def"))
                .append(StatementBuilder.create(c).loadVariable("n").assignValue(1))
                .finish();

        assertEquals("Failed to generate if - if - block using no rhs",
                IF_ELSEIF_BLOCK_RESULT_NO_RHS, s.generate(Context.create()));

        s = StatementBuilder.create(c)
                .loadVariable("s")
                .invoke("endsWith", "abc")
                .if_()
                .append(StatementBuilder.create(c).loadVariable("n").assignValue(0))
                .finish()
                .elseif_(StatementBuilder.create(c).loadVariable("s").invoke("startsWith", "def"))
                .append(StatementBuilder.create(c).loadVariable("n").assignValue(1))
                .finish()
                .else_()
                .append(StatementBuilder.create(c).loadVariable("n").assignValue(2))
                .finish();

        assertEquals("Failed to generate if - else if - else block using no rhs",
                IF_ELSEIF_ELSE_BLOCK_RESULT_NO_RHS, s.generate(Context.create()));
    }

    @Test
    public void testIfElseIfBlockUsingRhsElseIfKeyword() {
        Context c = ContextBuilder.create().addVariable("n", Integer.class).addVariable("m", Integer.class).getContext();

        Statement s = StatementBuilder.create(c)
                .loadVariable("n")
                .if_(BooleanOperator.GreaterThan, Variable.get("m"))
                .append(StatementBuilder.create(c).loadVariable("n").assignValue(0))
                .finish()
                .elseif_(StatementBuilder.create(c).loadVariable("m"), BooleanOperator.GreaterThan, Variable.get("n"))
                .append(StatementBuilder.create(c).loadVariable("n").assignValue(1))
                .finish()
                .else_()
                .append(StatementBuilder.create(c).loadVariable("n").assignValue(2))
                .finish();

        assertEquals("Failed to generate if - else if - else block using rhs",
                IF_ELSEIF_ELSE_BLOCK_RESULT_RHS, s.generate(Context.create()));
    }

    @Test
    public void testIfBlockWithInvalidBooleanExpression() {
        try {
            StatementBuilder.create()
                    .addVariable("str", String.class)
                    .loadVariable("str")
                    .invoke("compareTo", "asd")
                    .if_().finish()
                    .toJavaString();

            fail("Expected InvalidTypeException");
        } catch (InvalidTypeException e) {
            // expected
        }
    }

    @Test
    public void testIfBlockWithInvalidExpression() {
        try {
            StatementBuilder.create()
                    .addVariable("str", String.class)
                    .addVariable("str2", String.class)
                    .loadVariable("str")
                    .if_(BooleanOperator.GreaterThan, Variable.get("str2")).finish()
                    .toJavaString();

            fail("Expected InvalidExpressionException");
        } catch (InvalidExpressionException e) {
            assertTrue("Wrong exception thrown", e.getMessage().contains(String.class.getName()));
        }
    }

    @Test
    public void testIfBlockWithInstanceOfExpression() {
        String s = StatementBuilder.create()
                .addVariable("str", String.class)
                .loadVariable("str")
                .if_(BooleanOperator.InstanceOf, MetaClassFactory.getAsStatement(String.class)).finish()
                .toJavaString();

        assertEquals("Failed to generate empty if block using an instance of expression",
                EMPTY_IF_BLOCK_RESULT_INSTANCE_OF_RHS, s);
    }
}