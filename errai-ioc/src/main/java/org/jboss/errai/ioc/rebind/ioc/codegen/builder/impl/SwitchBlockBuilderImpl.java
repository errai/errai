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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BuildCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.CaseBlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementEnd;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.SwitchBlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.CallWriter;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.DeferredCallElement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.DeferredCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.control.SwitchBlock;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.IntValue;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.LiteralValue;

/**
 * StatementBuilder to generate switch blocks.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class SwitchBlockBuilderImpl extends AbstractStatementBuilder implements SwitchBlockBuilder, CaseBlockBuilder {
  private SwitchBlock switchBlock;

  protected SwitchBlockBuilderImpl(Context context, CallElementBuilder callElementBuilder) {
    super(context, callElementBuilder);
  }

  public CaseBlockBuilder switch_(Statement statement) {
    switchBlock = new SwitchBlock(statement);

    appendCallElement(new DeferredCallElement(new DeferredCallback() {
      public void doDeferred(CallWriter writer, Context context, Statement statement) {
        writer.reset();
        writer.append(switchBlock.generate(Context.create(context)));
      }
    }));

    return this;
  }

  public BlockBuilder<CaseBlockBuilder> case_(IntValue value) {
    switchBlock.addCase(value, false);
    return caseBlock(value);
  }

  public BlockBuilder<CaseBlockBuilder> case_(int value) {
    IntValue val = (IntValue) LiteralFactory.getLiteral(context, value);
    return case_(val);
  }

  public BlockBuilder<CaseBlockBuilder> case_(LiteralValue<Enum<?>> value) {
    switchBlock.addCase(value, false);
    return caseBlock(value);
  }

  public BlockBuilder<CaseBlockBuilder> case_(Enum<?> value) {
    LiteralValue<Enum<?>> val = (LiteralValue<Enum<?>>) LiteralFactory.getLiteral(context, value);
    return case_(val);
  }
  
  public BlockBuilder<CaseBlockBuilder> caseFallThrough(IntValue value) {
    switchBlock.addCase(value, true);
    return caseBlock(value);
  }

  public BlockBuilder<CaseBlockBuilder> caseFallThrough(int value) {
    IntValue val = (IntValue) LiteralFactory.getLiteral(value);
    return caseFallThrough(val);
  }

  public BlockBuilder<CaseBlockBuilder> caseFallThrough(LiteralValue<Enum<?>> value) {
    switchBlock.addCase(value, true);
    return caseBlock(value);
  }

  public BlockBuilder<CaseBlockBuilder> caseFallThrough(Enum<?> value) {
    LiteralValue<Enum<?>> val = (LiteralValue<Enum<?>>) LiteralFactory.getLiteral(value);
    return caseFallThrough(val);
  }
  
  private BlockBuilder<CaseBlockBuilder> caseBlock(LiteralValue<?> value) {
    return new BlockBuilder<CaseBlockBuilder>(switchBlock.getCaseBlock(value),
        new BuildCallback<CaseBlockBuilder>() {

          public CaseBlockBuilder callback(Statement statement) {
            return SwitchBlockBuilderImpl.this;
          }
        });
  }

  public BlockBuilder<StatementEnd> default_() {
    return new BlockBuilder<StatementEnd>(switchBlock.getDefaultCaseBlock(),
        new BuildCallback<StatementEnd>() {

          public StatementEnd callback(Statement statement) {
            return SwitchBlockBuilderImpl.this;
          }
        });
  }
}