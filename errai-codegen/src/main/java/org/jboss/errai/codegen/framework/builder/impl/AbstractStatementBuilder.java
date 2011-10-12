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

package org.jboss.errai.codegen.framework.builder.impl;

import static org.jboss.errai.codegen.framework.util.PrettyPrinter.prettyPrintJava;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.Builder;
import org.jboss.errai.codegen.framework.builder.StatementEnd;
import org.jboss.errai.codegen.framework.builder.callstack.CallElement;
import org.jboss.errai.codegen.framework.builder.callstack.CallWriter;
import org.jboss.errai.codegen.framework.exception.*;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.util.GenUtil;

/**
 * Base class of all {@link StatementBuilder}s
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractStatementBuilder implements Statement, Builder, StatementEnd {
  protected Context context = null;
  protected CallElementBuilder callElementBuilder;

  protected AbstractStatementBuilder(Context context) {
    if (context == null) {
      context = Context.create();
    }

    this.context = context;
    this.callElementBuilder = new CallElementBuilder();
  }

  protected AbstractStatementBuilder(Context context, CallElementBuilder callElementBuilder) {
    this(context);
    this.callElementBuilder = callElementBuilder;
  }

  @Override
  public String generate(Context context) {

    CallWriter writer = new CallWriter();
    try {
      callElementBuilder.getRootElement().handleCall(writer, context, null);
    }
    catch (Exception e) {
      GenUtil.throwIfUnhandled("generation failure at: " + writer.getCallString(), e);
    }
    return prettyPrintJava(writer.getCallString());
  }

  public void appendCallElement(CallElement element) {
    callElementBuilder.appendCallElement(element);
  }

  @Override
  public MetaClass getType() {
    if (callElementBuilder.getCallElement() == null)
      return null;

    return callElementBuilder.getCallElement().getResultType();
  }

  @Override
  public String toJavaString() {
    return generate(context);
  }
}
