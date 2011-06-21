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

package org.jboss.errai.ioc.rebind.ioc.codegen;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.DeclareAssignmentBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.LiteralValue;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.GenUtil;

/**
 * This class represents a variable.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Variable extends AbstractStatement {
  private String name;
  private MetaClass type;
  private Statement value;

  private Object initialization;
  private boolean classMember;

  private Variable(String name, MetaClass type) {
    this.name = name;
    this.type = type;
  }

  private Variable(String name, MetaClass type, boolean classMember) {
    this.name = name;
    this.type = type;
    this.classMember = classMember;
  }

  private Variable(String name, MetaClass type, Object initialization) {
    this(name, type);
    
    LiteralValue<?> val = LiteralFactory.isLiteral(initialization);
    if (val != null) {
      this.type = (type == null) ? val.getType() : type;
      this.value = GenUtil.convert(getContext(), initialization, this.type);
    }
    else {
      // deferred initialization
      this.initialization = initialization;
    }
  }

  public void initialize(Object initializationValue) {
    this.initialization = initializationValue;
  }
  
  private MetaClass inferType(Context context, Object initialization) {
    Statement initStatement = GenUtil.generate(context, initialization);
    MetaClass inferredType = (initStatement != null) ? initStatement.getType() : null;
    if (inferredType == null) {
      throw new InvalidTypeException("No type specified and no initialization provided to infer the type.");
    }

    return inferredType;
  }

  public static Variable create(String name, Class<?> type) {
    return new Variable(name, MetaClassFactory.get(type));
  }

  public static Variable create(String name, TypeLiteral<?> type) {
    return new Variable(name, MetaClassFactory.get(type));
  }

  public static Variable create(String name, MetaClass type) {
    return new Variable(name, type);
  }

  public static Variable createClassMember(String name, MetaClass type) {
    return new Variable(name, type, true);
  }

  public static Variable create(String name, Object initialization) {
    return new Variable(name, null, initialization);
  }

  public static Variable create(String name, Class<?> type, Object initialization) {
    return new Variable(name, MetaClassFactory.get(type), initialization);
  }

  public static Variable create(String name, TypeLiteral<?> type, Object initialization) {
    return new Variable(name, MetaClassFactory.get(type), initialization);
  }

  public static Variable create(String name, MetaClass type, Object initialization) {
    return new Variable(name, type, initialization);
  }

  public static VariableReference get(final String name) {
    return new VariableReference() {
      public String getName() {
        return name;
      }

      public Statement getValue() {
        return null;
      }
    };
  }

  public VariableReference getReference() {
    return new VariableReference() {
      public String getName() {
        return classMember ? "this." + name : name;
      }

      public MetaClass getType() {
        return type;
      }

      public Statement getValue() {
        return value;
      }
    };
  }

  public String getName() {
    return name;
  }

  public MetaClass getType() {
    return type;
  }

  public Statement getValue() {
    return value;
  }

  public boolean isClassMember() {
    return classMember;
  }
  
  private String hashString;

  private String hashString() {
    if (hashString == null) {
      hashString = Variable.class.getName() + ":" + name + ":" + type.getFullyQualifiedName();
    }
    return hashString;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Variable
            && hashString().equals(Variable.class.getName() + ":" + name + ":" + ((Variable) o).type.getFullyQualifiedName());
  }

  @Override
  public int hashCode() {
    return hashString().hashCode();
  }

  @Override
  public String toString() {
    return "Variable [name=" + name + ", type=" + type + ", classMember=" + classMember + "]";
  }

  public String generate(Context context) {
    if (initialization != null) {
      this.type = (type == null) ? inferType(context, initialization) : type;
      this.value = GenUtil.convert(context, initialization, type);
    }

    return new DeclareAssignmentBuilder(getReference(), value).generate(context);
  }
}
