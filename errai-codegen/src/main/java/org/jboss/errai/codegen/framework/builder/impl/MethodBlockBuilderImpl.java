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

import org.jboss.errai.codegen.framework.BlockStatement;
import org.jboss.errai.codegen.framework.DefModifiers;
import org.jboss.errai.codegen.framework.DefParameters;
import org.jboss.errai.codegen.framework.Modifier;
import org.jboss.errai.codegen.framework.ThrowsDeclaration;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.framework.builder.MethodBuildCallback;
import org.jboss.errai.codegen.framework.literal.LiteralFactory;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;

import javax.enterprise.util.TypeLiteral;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MethodBlockBuilderImpl<T> extends BlockBuilderImpl<T>
        implements MethodBlockBuilder<T> {

  protected ThrowsDeclaration throwsDeclaration = ThrowsDeclaration.none();
  protected MethodBuildCallback<T> callback;
  protected DefParameters defParameters;
  protected DefModifiers modifiers = new DefModifiers();


  public MethodBlockBuilderImpl(MethodBuildCallback<T> callback) {
    this.callback = callback;
  }

  public MethodBlockBuilderImpl(BlockStatement blockStatement, MethodBuildCallback<T> callback) {
    this.blockStatement = blockStatement;
    this.callback = callback;
  }

  @Override
  public BlockBuilder<T> throws_(Class<? extends Throwable>... exceptionTypes) {
    throwsDeclaration = ThrowsDeclaration.of(exceptionTypes);
    return this;
  }

  @Override
  public BlockBuilder<T> throws_(MetaClass... exceptions) {
    throwsDeclaration = ThrowsDeclaration.of(exceptions);
    return this;
  }


  @Override
  public MethodBlockBuilder<T> modifiers(Modifier... modifiers) {
    for (Modifier m : modifiers) {
      switch (m) {
        case Transient:
        case Volatile:
          throw new RuntimeException("illegal modifier for method: " + m);

        default:
          this.modifiers.addModifiers(m);
      }
    }

    return this;
  }

  @Override
  public MethodBlockBuilder<T> parameters(DefParameters parms) {
    defParameters = parms;
    return this;
  }

  @Override
  public MethodBlockBuilder<T> parameters(Class<?>... parms) {
    defParameters = DefParameters.fromTypeArray(MetaClassFactory.fromClassArray(parms)) ;
    return this;
  }

  @Override
  public MethodBlockBuilder<T> parameters(MetaClass... parms) {
    defParameters = DefParameters.fromTypeArray(parms);
    return this;
  }

  @Override
  public MethodBlockBuilder<T> parameters(Object... parms) {
    List<MetaClass> p = new ArrayList<MetaClass>();
    for (Object o : parms) {
      LiteralFactory.getLiteral(o);

      if (o instanceof MetaClass) {
        p.add((MetaClass) o);
      }
      else if (o instanceof Class<?>) {
        p.add(MetaClassFactory.get((Class<?>) o));
      }
      else if (o instanceof TypeLiteral) {
        p.add(MetaClassFactory.get((TypeLiteral) o));
      }
    }
    
    defParameters = DefParameters.fromTypeArray(p.toArray(new MetaClass[p.size()]));

    return this;
  }

  @Override
  public BlockBuilder<T> body() {
    return this;
  }

  @Override
  public T finish() {
    if (callback != null) {
      return callback.callback(blockStatement, defParameters, modifiers, throwsDeclaration);
    }
    return null;
  }
}
