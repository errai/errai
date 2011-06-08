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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder.control;

import org.jboss.errai.ioc.rebind.ioc.codegen.AbstractStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;

/**
 * Foreach statement (enhanced for loop).
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ForeachLoop extends AbstractStatement {

  private Variable loopVar;
  private String collectionExpr;
  private Statement body;

  public ForeachLoop(Variable loopVar, String collectionExpr, Statement body) {
    this.loopVar = loopVar;
    this.collectionExpr = collectionExpr;
    this.body = body;
  }

  public String generate(Context context) {
    StringBuilder buf = new StringBuilder();

    buf.append("for (").append(loopVar.getType().getFullyQualifedName()).append(" ").append(loopVar.getName())
            .append(" : ").append(collectionExpr).append(") {")
            .append("\n\t").append(body.generate(context).replaceAll("\n", "\n\t"))
            .append("\n}");

    return buf.toString();
  }
}