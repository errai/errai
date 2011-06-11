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

import org.jboss.errai.ioc.rebind.ioc.codegen.BlockStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanExpression;
import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BuildCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.LoopBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.CallWriter;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.DeferredCallElement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.DeferredCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.ForLoop;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.ForeachLoop;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.WhileLoop;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.NullLiteral;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.GenUtil;

/**
 * StatementBuilder to generate loops.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class LoopBuilderImpl extends AbstractStatementBuilder implements LoopBuilder {

  protected LoopBuilderImpl(Context context, CallElementBuilder callElementBuilder) {
    super(context, callElementBuilder);
  }

  public BlockBuilder<LoopBuilder> foreach(String loopVarName) {
    return foreach(loopVarName, (MetaClass) null);
  }

  public BlockBuilder<LoopBuilder> foreach(String loopVarName, Class<?> loopVarType) {
    return foreach(loopVarName, MetaClassFactory.get(loopVarType));
  }

  private BlockBuilder<LoopBuilder> foreach(final String loopVarName, final MetaClass loopVarType) {
    final BlockStatement body = new BlockStatement();

    appendCallElement(new DeferredCallElement(new DeferredCallback() {
      public void doDeferred(CallWriter writer, Context context, Statement statement) {
        GenUtil.assertIsIterable(statement);
        Variable loopVar = createForEachLoopVar(statement, loopVarName, loopVarType);
        String collection = writer.getCallString();
        writer.reset();
        writer.append(new ForeachLoop(loopVar, collection, body).generate(Context.create(context)));
      }
    }));

    return createLoopBody(body);
  }

  public BlockBuilder<LoopBuilder> while_() {
    return _while_(new BooleanExpressionBuilder());
  }

  public BlockBuilder<LoopBuilder> while_(BooleanExpression stmt) {
    return _while_(stmt);
  }

  public BlockBuilder<LoopBuilder> while_(BooleanOperator op, Object rhs) {
    return while_(op, GenUtil.generate(context, rhs));
  }

  public BlockBuilder<LoopBuilder> while_(BooleanOperator op, Statement rhs) {
    if (rhs==null) rhs = NullLiteral.INSTANCE;
    return _while_(new BooleanExpressionBuilder(rhs, op));
  }
  
  private BlockBuilder<LoopBuilder> _while_(final BooleanExpression condition) {
    final BlockStatement body = new BlockStatement();
   
    appendCallElement(new DeferredCallElement(new DeferredCallback() {
      public void doDeferred(CallWriter writer, Context context, Statement lhs) {
        if (lhs!=null) {
          condition.setLhs(lhs);
          condition.setLhsExpr(writer.getCallString());
        }  
        WhileLoop whileLoop = new WhileLoop(condition, body);
        writer.reset();
        writer.append(whileLoop.generate(Context.create(context)));
      }
    }));
    
    return createLoopBody(body);
  }
  
  public BlockBuilder<LoopBuilder> for_(BooleanExpression condition) {
    return for_(condition, (Statement) null);
  }

  public BlockBuilder<LoopBuilder> for_(BooleanExpression condition, Statement countingExpression) {
    return _for_(null, condition, countingExpression);
  }
  
  public BlockBuilder<LoopBuilder> for_(Statement initializer, BooleanExpression condition) {
    return for_(initializer, condition, null);
  }
  
  public BlockBuilder<LoopBuilder> for_(Statement initializer, BooleanExpression condition, Statement countingExpression) {
    return _for_(initializer, condition, countingExpression);
  }
  
  private BlockBuilder<LoopBuilder> _for_(final Statement initializer, final BooleanExpression condition, 
      final Statement countingExpression) {
    final BlockStatement body = new BlockStatement();

    appendCallElement(new DeferredCallElement(new DeferredCallback() {
      public void doDeferred(CallWriter writer, Context context, Statement lhs) {
        ForLoop forLoop = null;
        if (initializer!=null) {
          forLoop = new ForLoop(condition, body, initializer, countingExpression);
          if (initializer instanceof Variable) {
            context.addVariable((Variable) initializer);
          }
        } else {
          forLoop = new ForLoop(condition, body, writer.getCallString(), countingExpression);
        }
        writer.reset();
        writer.append(forLoop.generate(Context.create(context)));
      }
    }));

    return createLoopBody(body);
  }
  
  private BlockBuilder<LoopBuilder> createLoopBody(BlockStatement body) {
    return new BlockBuilder<LoopBuilder>(body, new BuildCallback<LoopBuilder>() {
      public LoopBuilder callback(Statement statement) {
        return LoopBuilderImpl.this;
      }
    });    
  }
  
  private Variable createForEachLoopVar(Statement collection, String loopVarName, MetaClass providedLoopVarType) {
    // infer the loop variable type
    MetaClass loopVarType = MetaClassFactory.get(Object.class);
    MetaParameterizedType parameterizedType = collection.getType().getParameterizedType();
    if (parameterizedType != null && parameterizedType.getTypeParameters().length != 0) {
      loopVarType = (MetaClass) parameterizedType.getTypeParameters()[0];
    }
    else if (collection.getType().getComponentType() != null) {
      loopVarType = collection.getType().getComponentType();
    }

    // try to use the provided loop variable type if possible (assignable from the inferred type)
    if (providedLoopVarType != null) {
      GenUtil.assertAssignableTypes(loopVarType, providedLoopVarType);
      loopVarType = providedLoopVarType;
    }

    Variable loopVar = Variable.create(loopVarName, loopVarType);
    context.addVariable(loopVar);
    return loopVar;
  }
}