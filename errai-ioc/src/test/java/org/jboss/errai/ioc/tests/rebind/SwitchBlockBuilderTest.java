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

import static org.junit.Assert.fail;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Stmt;
import org.junit.Test;

/**
 * Tests the generation of switch blocks using the {@link StatementBuilder} API.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class SwitchBlockBuilderTest extends AbstractStatementBuilderTest implements SwitchBlockBuilderTestResult {
  public enum TestEnum {
    A, B;
  }

  @Test
  public void testInvalidSwitchStatment() {
    try {
      StatementBuilder.create()
          .switch_(Stmt.create().loadStatic(System.class, "out"))
          .toJavaString();
      fail("expected InvalidTypeException");
    }
    catch (InvalidTypeException e) {
      // expected
    }
  }

  @Test
  public void testSwitchBlockWithInvalidCaseValueWithEnumForInt() {
    try {
      Context c = Context.create().addVariable("n", int.class);
      StatementBuilder.create(c)
          .switch_(c.getVariable("n"))
          .case_(TestEnum.A)
          .finish()
          .toJavaString();
      fail("expected InvalidTypeException");
    }
    catch (InvalidTypeException e) {
      // expected
    }
  }

  @Test
  public void testSwitchBlockWithInvalidCaseValueWithIntForEnum() {
    try {
      Context c = Context.create().addVariable("t", TestEnum.class);
      StatementBuilder.create(c)
          .switch_(Stmt.create().loadVariable("t"))
          .case_(1)
          .finish()
          .toJavaString();
      fail("expected InvalidTypeException");
    }
    catch (InvalidTypeException e) {
      // expected
    }
  }

  @Test
  public void testEmptySwitchBlock() {
    String s = StatementBuilder.create()
        .addVariable("n", int.class)
        .switch_(Stmt.create().loadVariable("n"))
        .toJavaString();

    assertEquals("Failed to generate empty switch block", SWITCH_BLOCK_EMPTY, s);
  }

  @Test
  public void testIntSwitchBlock() {
    String s = StatementBuilder.create()
        .addVariable("n", int.class)
        .switch_(Stmt.create().loadVariable("n"))
        .case_(0)
        .append(Stmt.create().loadStatic(System.class, "out").invoke("println", "0"))
        .append(Stmt.create().loadStatic(System.class, "out").invoke("println", "break"))
        .finish()
        .case_(1)
        .finish()
        .default_()
        .finish()
        .toJavaString();

    assertEquals("Failed to generate int switch block", SWITCH_BLOCK_INT, s);
  }

  @Test
  public void testIntegerSwitchBlockWithoutDefault() {
    String s = StatementBuilder.create()
        .addVariable("n", Integer.class)
        .switch_(Stmt.create().loadVariable("n"))
        .case_(0)
        .append(Stmt.create().loadStatic(System.class, "out").invoke("println", "0"))
        .append(Stmt.create().loadStatic(System.class, "out").invoke("println", "break"))
        .finish()
        .case_(1)
        .finish()
        .toJavaString();

    assertEquals("Failed to generate Integer switch block without default", SWITCH_BLOCK_INTEGER_NO_DEFAULT, s);
  }

  @Test
  public void testEnumSwitchBlock() {
    Context c = Context.create().autoImport();
    String s = StatementBuilder.create(c)
        .addVariable("t", TestEnum.class)
        .switch_(Stmt.create().loadVariable("t"))
        .case_(TestEnum.A)
        .append(Stmt.create().loadStatic(System.class, "out").invoke("println", "A"))
        .append(Stmt.create().loadStatic(System.class, "out").invoke("println", "break"))
        .finish()
        .case_(TestEnum.B)
        .finish()
        .default_()
        .finish()
        .toJavaString();

    assertEquals("Failed to generate enum switch block", SWITCH_BLOCK_ENUM, s);
  }

  @Test
  public void testSwitchBlockWithFallThrough() {
    String s = StatementBuilder.create()
        .addVariable("n", int.class)
        .switch_(Stmt.create().loadVariable("n"))
        .caseFallThrough(0)
        .finish()
        .case_(1)
        .append(Stmt.create().loadStatic(System.class, "out").invoke("println", "0"))
        .append(Stmt.create().loadStatic(System.class, "out").invoke("println", "break"))
        .finish()
        .toJavaString();

    assertEquals("Failed to generate int switch block", SWITCH_BLOCK_INT_FALLTHROUGH, s);
  }
}