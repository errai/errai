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

import com.google.gwt.core.ext.GeneratorContext;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.PrivateAccessUtil;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Utility to retrieve an injected {@link AutoBound} data binder.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class DataBindingUtil {
  public static final Annotation[] MODEL_QUALIFICATION = new Annotation[]{new Model() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Model.class;
    }
  }};

  private static final Logger log = LoggerFactory.getLogger(DataBindingUtil.class);

  public static enum DataBindingType {
    DATA_BINDER,
    RAW_MODEL
  }

  public static class DataBinderLookup {
    private final MetaClass dataModelType;
    private final Statement valueAccessor;
    private final DataBindingType type;

    public DataBinderLookup(final MetaClass dataModelType, final Statement valueAccessor, DataBindingType type) {
      this.dataModelType = dataModelType;
      this.valueAccessor = valueAccessor;
      this.type = type;
    }

    public MetaClass getDataModelType() {
      return dataModelType;
    }

    public Statement getValueAccessor() {
      return valueAccessor;
    }

    public DataBindingType getType() {
      return type;
    }
  }

  public static DataBinderLookup getDataBinder(final InjectableInstance<?> ctx) {
    Statement dataBinderRef = null;
    MetaClass dataModelType = null;

    InjectUtil.BeanMetric beanMetric =
        InjectUtil.getFilteredBeanMetric(ctx.getInjectionContext(),
            ctx.getInjector().getInjectedType(), Model.class);

    if (!beanMetric.getAllInjectors().isEmpty()) {
      final Collection<Object> allInjectors = beanMetric.getAllInjectors();
      if (allInjectors.size() > 1) {
        throw new GenerationException("Multiple @Models injected in " + ctx.getEnclosingType());
      }
      else if (allInjectors.size() == 1) {
        final Object injectorElement = allInjectors.iterator().next();

        if (injectorElement instanceof MetaConstructor || injectorElement instanceof MetaMethod) {
          final MetaParameter mp = beanMetric.getConsolidatedMetaParameters().iterator().next();

          dataModelType = mp.getType();
          checkTypeIsBindable(dataModelType);
          dataBinderRef = ctx.getInjectionContext().getInlineBeanReference(mp);
          ctx.ensureMemberExposed();
        }
        else {
          final MetaField field = (MetaField) allInjectors.iterator().next();

          dataModelType = field.getType();
          checkTypeIsBindable(dataModelType);
          dataBinderRef = Stmt.loadVariable("dataBinderHolder").invoke("get");
          ctx.getInjectionContext().addExposedField(field, PrivateAccessType.Both);
        }
        return new DataBinderLookup(dataModelType, dataBinderRef, DataBindingType.RAW_MODEL);
      }
    }
    else {
      List<MetaField> modelFields = ctx.getInjector().getInjectedType().getFieldsAnnotatedWith(Model.class);
      if (!modelFields.isEmpty()) {
        throw new GenerationException("Found one or more fields annotated with @Model but missing @Inject "
            + modelFields.toString());
      }

      List<MetaParameter> modelParameters = ctx.getInjector().getInjectedType().getParametersAnnotatedWith(Model.class);
      if (!modelParameters.isEmpty()) {
        throw new GenerationException(
            "Found one or more constructor or method parameters annotated with @Model but missing @Inject "
                + modelParameters.toString());
      }
    }

    beanMetric = InjectUtil.getFilteredBeanMetric(ctx.getInjectionContext(),
        ctx.getInjector().getInjectedType(), AutoBound.class);

    final Collection<Object> allInjectors = beanMetric.getAllInjectors();
    if (allInjectors.size() > 1) {
      throw new GenerationException("Multiple @AutoBound data binders injected in " + ctx.getEnclosingType());
    }
    else if (allInjectors.size() == 1) {
      final Object injectorElement = allInjectors.iterator().next();

      if (injectorElement instanceof MetaConstructor || injectorElement instanceof MetaMethod) {
        final MetaParameter mp = beanMetric.getConsolidatedMetaParameters().iterator().next();

        checkTypeIsDataBinder(mp.getType());
        dataModelType = (MetaClass) mp.getType().getParameterizedType().getTypeParameters()[0];
        dataBinderRef = ctx.getInjectionContext().getInlineBeanReference(mp);
        ctx.ensureMemberExposed();
      }
      else {
        final MetaField field = (MetaField) allInjectors.iterator().next();

        checkTypeIsDataBinder(field.getType());
        dataModelType = (MetaClass) field.getType().getParameterizedType().getTypeParameters()[0];
        dataBinderRef = Stmt.invokeStatic(ctx.getInjectionContext().getProcessingContext().getBootstrapClass(),
            PrivateAccessUtil.getPrivateFieldInjectorName(field),
            Variable.get(ctx.getInjector().getInstanceVarName()));
        ctx.getInjectionContext().addExposedField(field, PrivateAccessType.Both);
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
          ctx.getInjectionContext().addExposedField(field, PrivateAccessType.Both);
          break;
        }
      }
    }

    return (dataBinderRef != null) ? new DataBinderLookup(dataModelType, dataBinderRef, DataBindingType.DATA_BINDER) : null;
  }

  private static void checkTypeIsDataBinder(MetaClass type) {
    final MetaClass databinderMetaClass = MetaClassFactory.get(DataBinder.class);

    if (!databinderMetaClass.isAssignableFrom(type)) {
      throw new GenerationException("type of @AutoBound element must be " + DataBinder.class.getName() +
          "; was: " + type.getFullyQualifiedName());
    }
  }

  private static void checkTypeIsBindable(MetaClass type) {
    if (!type.isAnnotationPresent(Bindable.class) && !getConfiguredBindableTypes().contains(type)) {
      throw new GenerationException(type.getName() + " must be a @Bindable type when used as @Model");
    }

  }

  /**
   * Reads bindable types from all ErraiApp.properties files on the classpath.
   *
   * @return a set of meta classes representing the configured bindable types.
   */
  public static Set<MetaClass> getConfiguredBindableTypes() {
    final Set<MetaClass> bindableTypes = new HashSet<MetaClass>();

    final Collection<URL> erraiAppProperties = EnvUtil.getErraiAppProperties();
    for (URL url : erraiAppProperties) {
      InputStream inputStream = null;
      try {
        log.debug("Checking " + url.getFile() + " for bindable types...");
        inputStream = url.openStream();

        final ResourceBundle props = new PropertyResourceBundle(inputStream);
        for (final String key : props.keySet()) {
          if (key.equals("errai.ui.bindableTypes")) {
            for (final String s : props.getString(key).split(" ")) {
              try {
                bindableTypes.add(MetaClassFactory.get(s.trim()));
              }
              catch (Exception e) {
                throw new RuntimeException("Could not find class defined in ErraiApp.properties as bindable type: " + s);
              }
            }
            break;
          }
        }
      }
      catch (IOException e) {
        throw new RuntimeException("Error reading ErraiApp.properties", e);
      }
      finally {
        if (inputStream != null) {
          try {
            inputStream.close();
          }
          catch (IOException e) {
            log.warn("Failed to close input stream", e);
          }
        }
      }
    }
    return bindableTypes;
  }

  public static Collection<MetaClass> getAllBindableTypes(final GeneratorContext context) {
    Collection<MetaClass> annotatedBindableTypes =
        ClassScanner.getTypesAnnotatedWith(Bindable.class, RebindUtils.findTranslatablePackages(context));

    Set<MetaClass> bindableTypes = new HashSet<MetaClass>(annotatedBindableTypes);
    bindableTypes.addAll(DataBindingUtil.getConfiguredBindableTypes());
    return annotatedBindableTypes;
  }
}