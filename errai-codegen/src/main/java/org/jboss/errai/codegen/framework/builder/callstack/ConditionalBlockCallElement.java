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

package org.jboss.errai.codegen.framework.builder.callstack;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.control.AbstractConditionalBlock;

/**
 * A {@link CallElement} for conditional blocks. It can only be the last
 * element in a chain of calls.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ConditionalBlockCallElement extends AbstractCallElement {
  private AbstractConditionalBlock conditionalBlock;
  
  public ConditionalBlockCallElement(final AbstractConditionalBlock conditionalBlock) {
    this.conditionalBlock = conditionalBlock;
  }

  @Override
  public void handleCall(CallWriter writer, Context context, Statement lhs) {
    if (lhs != null) {
      // The LHS value is on the current callstack. So we grab the value from there at generation time.
      conditionalBlock.getCondition().setLhs(lhs);
      conditionalBlock.getCondition().setLhsExpr(writer.getCallString());
    }
    writer.reset();
    writer.append(conditionalBlock.generate(Context.create(context)));
  }

  @Override
  public String toString() {
    return "[Conditional<" + conditionalBlock + ">]";
  }
}