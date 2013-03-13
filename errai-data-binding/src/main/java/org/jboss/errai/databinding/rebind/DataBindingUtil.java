/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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

package org.jboss.errai.databinding.rebind;

import java.util.Collection;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.PrivateAccessUtil;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;

/**
 * Utility to retrieve an injected {@link AutoBound} data binder.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class DataBindingUtil {
  public static class DataBinderLookup {
    private final MetaClass dataModelType;
    private final Statement valueAccessor;

    public DataBinderLookup(final MetaClass dataModelType, final Statement valueAccessor) {
      this.dataModelType = dataModelType;
      this.valueAccessor = valueAccessor;
    }

    public MetaClass getDataModelType() {
      return dataModelType;
    }

    public Statement getValueAccessor() {
      return valueAccessor;
    }
  }

  public static DataBinderLookup getDataBinder(final InjectableInstance<?> ctx) {
    Statement dataBinderRef = null;
    MetaClass dataModelType = null;

    final InjectUtil.BeanMetric beanMetric =
        InjectUtil.getFilteredBeanMetric(ctx.getInjectionContext(), ctx.getEnclosingType(), AutoBound.class);

    final Collection<Object> allInjectors = beanMetric.getAllInjectors();
    if (allInjectors.size() > 1) {
      throw new GenerationException("multiple @AutoBound data binders found in constructor of " +
          ctx.getEnclosingType());
    }
    else if (allInjectors.size() == 1) {
      final Object injectorElement = allInjectors.iterator().next();

      if (injectorElement instanceof MetaConstructor || injectorElement instanceof MetaMethod) {
        final MetaParameter mp = beanMetric.getConsolidatedMetaParameters().iterator().next();

        checkTypeIsDataBinder(mp.getType());
        dataModelType = (MetaClass) mp.getType().getParameterizedType().getTypeParameters()[0];
        dataBinderRef = ctx.getInjectionContext().getInlineBeanReference(mp);
      }
      else {
        final MetaField field = (MetaField) allInjectors.iterator().next();

        checkTypeIsDataBinder(field.getType());
        dataModelType = (MetaClass) field.getType().getParameterizedType().getTypeParameters()[0];
        dataBinderRef = Stmt.invokeStatic(ctx.getInjectionContext().getProcessingContext().getBootstrapClass(),
            PrivateAccessUtil.getPrivateFieldInjectorName(field),
            Variable.get(ctx.getInjector().getInstanceVarName()));
      }
    }
    else {
      final MetaClass declaringClass = ctx.getEnclosingType();
      for (final MetaField field : declaringClass.getFields()) {
        if (field.isAnnotationPresent(AutoBound.class)) {
          checkTypeIsDataBinder(field.getType());
          dataModelType = (MetaClass) field.getType().getParameterizedType().getTypeParameters()[0];
          dataBinderRef = Stmt.invokeStatic(ctx.getInjectionContext().getProcessingContext().getBootstrapClass(),
              PrivateAccessUtil.getPrivateFieldInjectorName(field),
              Variable.get(ctx.getInjector().getInstanceVarName()));
          break;
        }
      }
    }

    return (dataBinderRef != null) ? new DataBinderLookup(dataModelType, dataBinderRef) : null;
  }

  private static void checkTypeIsDataBinder(MetaClass type) {
    final MetaClass databinderMetaClass = MetaClassFactory.get(DataBinder.class);

    if (!databinderMetaClass.isAssignableFrom(type)) {
      throw new GenerationException("type of @AutoBound element must be " + DataBinder.class.getName() +
          "; was: " + type.getFullyQualifiedName());
    }
  }
}