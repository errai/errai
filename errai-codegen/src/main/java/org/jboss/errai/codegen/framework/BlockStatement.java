/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.codegen.framework;

import org.jboss.errai.codegen.framework.builder.ClosedBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a code block (e.g. a loop body).
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BlockStatement extends AbstractStatement {
  private List<Statement> statements = new ArrayList<Statement>();

  public BlockStatement(Statement... statements) {
    if (statements != null) {
      for (Statement statement : statements) {
        if (statement != null)
          this.statements.add(statement);
      }
    }
  }

  public BlockStatement addStatement(Statement statement) {
    if (statement != null)
      statements.add(statement);
    return this;
  }

  String generatedCache;
  @Override
  public String generate(Context context) {
    if (generatedCache != null) return generatedCache;
    
    StringBuilder buf = new StringBuilder();

    boolean lastIsBlock = false;
    for (Statement statement : statements) {
      if (buf.length() != 0)
        buf.append("\n");

      buf.append(statement.generate(context));

      if (!buf.toString().endsWith(";") && !buf.toString().endsWith(":") && !buf.toString().endsWith("}"))
        buf.append(";");

      lastIsBlock = statement instanceof ClosedBlock;
    }

    if (buf.length() != 0 && buf.charAt(buf.length() - 1) != ';' && !lastIsBlock) {
      buf.append(';');
    }

    return generatedCache = buf.toString();
  }

  public List<Statement> getStatements() {
    return statements;
  }

  public boolean isEmpty() {
    return statements.isEmpty();
  }
}
