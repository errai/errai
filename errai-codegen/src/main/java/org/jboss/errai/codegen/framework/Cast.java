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

import org.jboss.errai.codegen.framework.builder.callstack.LoadClassReference;
import org.jboss.errai.codegen.framework.exception.InvalidTypeException;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class Cast implements Statement {
  private MetaClass toType;
  private Statement statement;

  private Cast(MetaClass toType, Statement statement) {
    this.toType = toType;
    this.statement = statement;
  }

  public static Statement to(Class<?> cls, Statement stmt) {
    return to(MetaClassFactory.get(cls), stmt);
  }

  public static Cast to(MetaClass cls, Statement stmt) {
    return new Cast(cls, stmt);
  }

  @Override
  public String generate(Context context) {
    String stmt = statement.generate(context);

    if (!toType.isAssignableFrom(statement.getType()) && !toType.isAssignableTo(statement.getType())) {
      throw new InvalidTypeException(statement.getType() + " cannot be cast to " + toType);
    }
    else if (toType.isAssignableFrom(statement.getType())) {
      return stmt;
    }
    else {
      return "(" + LoadClassReference.getClassReference(toType, context) + ") " + stmt;
    }
  }

  @Override
  public MetaClass getType() {
    return toType;
  }

  @Override
  public String toString() {
    return "((" + toType.getFullyQualifiedName() + ")" + statement.toString() + ")";
  }
}
