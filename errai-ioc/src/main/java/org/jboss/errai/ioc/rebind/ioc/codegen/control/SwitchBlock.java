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

package org.jboss.errai.ioc.rebind.ioc.codegen.control;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.errai.ioc.rebind.ioc.codegen.AbstractStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.BlockStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.IntValue;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.LiteralValue;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class SwitchBlock extends AbstractStatement {

  private class CaseBlock {
    public CaseBlock(BlockStatement block, boolean fallThrough) {
      this.block = block;
      this.fallThrough = fallThrough;
    }

    private BlockStatement block;
    private boolean fallThrough;
  }

  private Statement statement;
  private Map<LiteralValue<?>, CaseBlock> caseBlocks = new LinkedHashMap<LiteralValue<?>, CaseBlock>();
  private BlockStatement defaultCase;

  public SwitchBlock(Statement statement) {
    this.statement = statement;
  }

  public void addCase(IntValue value) {
    addCase(value, false);
  }

  public void addCase(IntValue value, boolean fallThrough) {
    caseBlocks.put(value, new CaseBlock(new BlockStatement(), fallThrough));
  }

  public void addCase(LiteralValue<Enum<?>> value) {
    addCase(value, false);
  }

  public void addCase(LiteralValue<Enum<?>> value, boolean fallThrough) {
    caseBlocks.put(value, new CaseBlock(new BlockStatement(), fallThrough));
  }

  public BlockStatement getCaseBlock(LiteralValue<?> value) {
    return caseBlocks.get(value).block;
  }

  public BlockStatement getDefaultCaseBlock() {
    if (defaultCase == null)
      defaultCase = new BlockStatement();

    return defaultCase;
  }

  public String generate(Context context) {
    StringBuilder buf = new StringBuilder("switch (");
    buf.append(statement.generate(context)).append(") {\n ");

    if (!MetaClassFactory.get(Integer.class).isAssignableFrom(statement.getType().asBoxed())
        && !MetaClassFactory.get(Enum.class).isAssignableFrom(statement.getType())) {
      throw new InvalidTypeException("Only int values or enum constants are permitted in switch statements");
    }
    
    if (!caseBlocks.isEmpty()) {
      for (LiteralValue<?> value : caseBlocks.keySet()) {
        if (!statement.getType().asBoxed().isAssignableFrom(value.getType())) {
          throw new InvalidTypeException(
              value.generate(context)+" is not a valid value for " + statement.getType().getFullyQualifiedName());
        }
        
        // case labels must be unqualified
        Context ctx = Context.create(context);
        String val = value.generate(context);
        int idx = val.lastIndexOf('.');
        if (idx != -1) {
          val = val.substring(idx+1);
        }
        
        buf.append("case ").append(val).append(": ")
            .append(caseBlocks.get(value).block.generate(ctx));

        if (!caseBlocks.get(value).fallThrough) {
          buf.append(" break;");
        }
        buf.append("\n");
      }
    }
    else if (defaultCase == null) {
      defaultCase = new BlockStatement();
    }

    if (defaultCase != null) {
      Context ctx = Context.create(context);
      buf.append("default: ").append(defaultCase.generate(ctx)).append(" break;\n");
    }

    return buf.append("}").toString();
  }
}