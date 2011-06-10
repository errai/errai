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

import static org.jboss.errai.ioc.rebind.ioc.codegen.util.PrettyPrinter.prettyPrintJava;

import org.jboss.errai.ioc.rebind.ioc.codegen.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.CallElement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.CallWriter;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * Base class of all {@link StatementBuilder}s
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractStatementBuilder implements Statement, Builder {
  protected Context context = null;
  protected CallElementBuilder callElementBuilder;

  protected AbstractStatementBuilder(Context context, CallElementBuilder callElementBuilder) {
    this(context);
    this.callElementBuilder = callElementBuilder;
  }

  protected AbstractStatementBuilder(Context context) {
    if (context == null) {
      context = Context.create();
    }

    this.context = context;
    this.callElementBuilder = new CallElementBuilder();
  }

  public Context getContext() {
    return context;
  }

  public String generate(Context context) {
    CallWriter writer = new CallWriter();
    callElementBuilder.getRootElement().handleCall(writer, context, null);
    return prettyPrintJava(writer.getCallString());
  }

  public void appendCallElement(CallElement element) {
    callElementBuilder.appendCallElement(element);
  }

  public MetaClass getType() {
    if (callElementBuilder.getCallElement() == null)
      return null;

    return callElementBuilder.getCallElement().getResultType();
  }

  public String toJavaString() {
    return generate(context);
  }
}
