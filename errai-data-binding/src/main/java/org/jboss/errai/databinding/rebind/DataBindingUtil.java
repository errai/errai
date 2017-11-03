/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.databinding.rebind;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.HasAnnotations;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.util.PrivateAccessUtil;
import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.config.MetaClassFinder;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.Dependency;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.FieldDependency;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.ParamDependency;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Injectable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.Decorable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.Decorable.DecorableType;
import org.jboss.errai.ioc.rebind.ioc.injector.api.FactoryController;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Model;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.jboss.errai.codegen.util.Stmt.invokeStatic;

/**
 * Utility to retrieve a data binder reference. The reference is either to an
 * injected {@link AutoBound} data binder or to a generated data binder for an
 * injected {@link Model}.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class DataBindingUtil {

  public static final String BINDER_VAR_NAME = "DataModelBinder";
  public static final String MODEL_VAR_NAME = "DataModel";
  public static final String BINDER_MODEL_TYPE_VALUE = "DataBinderModelType";

  private DataBindingUtil() {}

  public static Set<MetaClass> getAllBindableTypes(final ErraiConfiguration erraiConfiguration,
          final MetaClassFinder metaClassFinder) {
    return metaClassFinder.extend(Bindable.class, erraiConfiguration.modules()::getBindableTypes)
            .remove(Bindable.class, erraiConfiguration.modules()::getNonBindableTypes)
            .findAnnotatedWith(Bindable.class);
  }
  /**
   * Represents a reference to an injected or generated data binder.
   */
  public static class DataBinderRef {
    private final MetaClass dataModelType;
    private final Statement valueAccessor;

    public DataBinderRef(final MetaClass dataModelType, final Statement valueAccessor) {
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

  /**
   * Tries to find a data binder reference for either an injected {@link Model}
   * or an injected {@link AutoBound} data binder.
   *
   * @param inst
   *          the injectable instance
   *
   * @return the data binder reference or null if not found.
   */
  public static DataBinderRef lookupDataBinderRef(final Decorable decorable,
          final FactoryController controller,
          final Set<MetaClass> allConfiguredBindableTypes) {

    DataBinderRef ref = lookupBinderForModel(decorable, controller, allConfiguredBindableTypes);
    if (ref == null) {
      ref = lookupAutoBoundBinder(decorable, controller);
    }
    return ref;
  }

  /**
   * Tries to find a data binder reference for an injected {@link Model}.
   *
   * @param inst
   *          the injectable instance
   *
   * @return the data binder reference or null if not found.
   */
  private static DataBinderRef lookupBinderForModel(final Decorable decorable,
          final FactoryController controller,
          final Set<MetaClass> allConfiguredBindableTypes) {

    Statement dataBinderRef;
    MetaClass dataModelType;

    final MetaClass enclosingType = decorable.getEnclosingInjectable().getInjectedType();
    final Collection<HasAnnotations> allAnnotated = getMembersAndParamsAnnotatedWith(enclosingType, Model.class);

    if (!allAnnotated.isEmpty()) {
      if (allAnnotated.size() > 1) {
        throw new GenerationException("Multiple @Models injected in " + enclosingType);
      }
      else {
        final HasAnnotations annotated = allAnnotated.iterator().next();

        if (annotated instanceof MetaParameter) {
          final MetaParameter mp = (MetaParameter) annotated;

          dataModelType = mp.getType();
          assertTypeIsBindable(dataModelType, allConfiguredBindableTypes);
          controller.addInitializationStatements(
                  Collections.<Statement>singletonList(
                          controller.setReferenceStmt(MODEL_VAR_NAME, DecorableType.PARAM.getAccessStatement(mp, decorable.getFactoryMetaClass()))));
          dataBinderRef = controller.getInstancePropertyStmt(
                  controller.getReferenceStmt(MODEL_VAR_NAME, dataModelType), BINDER_VAR_NAME,
                  DataBinder.class);
        }
        else {
          final MetaField field = (MetaField) allAnnotated.iterator().next();

          dataModelType = field.getType();
          assertTypeIsBindable(dataModelType, allConfiguredBindableTypes);

          if (!field.isPublic()) {
            controller.exposedFieldStmt(field);
          }
          dataBinderRef = controller.getInstancePropertyStmt(
                  DecorableType.FIELD.getAccessStatement(field, decorable.getFactoryMetaClass()), BINDER_VAR_NAME,
                  DataBinder.class);
        }
        return new DataBinderRef(dataModelType, dataBinderRef);
      }
    }
    else {
      final List<MetaField> modelFields = decorable.getDecorableDeclaringType().getFieldsAnnotatedWith(Model.class);
      if (!modelFields.isEmpty()) {
        throw new GenerationException("Found one or more fields annotated with @Model but missing @Inject "
                + modelFields.toString());
      }

      final List<MetaParameter> modelParameters = decorable.getDecorableDeclaringType().getParametersAnnotatedWith(Model.class);
      if (!modelParameters.isEmpty()) {
        throw new GenerationException(
                "Found one or more constructor or method parameters annotated with @Model but missing @Inject "
                        + modelParameters.toString());
      }
    }

    return null;
  }

  private static Collection<HasAnnotations> getMembersAndParamsAnnotatedWith(final MetaClass enclosingType, final Class<? extends Annotation> annoType) {
    final Collection<HasAnnotations> annotated = new ArrayList<>();

    final Target target = annoType.getAnnotation(Target.class);
    final Collection<ElementType> allowedTypes = (target == null) ? null : Arrays.asList(target.value());

    if (allowedTypes == null || allowedTypes.contains(ElementType.FIELD)) {
      annotated.addAll(enclosingType.getFieldsAnnotatedWith(annoType));
    }

    if (allowedTypes == null || allowedTypes.contains(ElementType.METHOD)) {
      annotated.addAll(enclosingType.getMethodsAnnotatedWith(annoType));
    }

    if (allowedTypes == null || allowedTypes.contains(ElementType.CONSTRUCTOR)) {
      for (final MetaConstructor ctor : enclosingType.getConstructors()) {
        if (ctor.isAnnotationPresent(annoType)) {
          annotated.add(ctor);
        }
      }
    }

    if (allowedTypes == null || allowedTypes.contains(ElementType.PARAMETER)) {
      for (final MetaMethod method : enclosingType.getMethods()) {
        for (final MetaParameter param : method.getParameters()) {
          if (param.isAnnotationPresent(annoType)) {
            annotated.add(param);
          }
        }
      }
      for (final MetaConstructor ctor : enclosingType.getConstructors()) {
        for (final MetaParameter param : ctor.getParameters()) {
          if (param.isAnnotationPresent(annoType)) {
            annotated.add(param);
          }
        }
      }
    }

    return annotated;
  }

  /**
   * Tries to find a reference for an injected {@link AutoBound} data binder.
   *
   * @return the data binder reference or null if not found.
   */
  private static DataBinderRef lookupAutoBoundBinder(final Decorable decorable, final FactoryController controller) {
    Statement dataBinderRef = null;
    MetaClass dataModelType = null;

    final MetaClass enclosingType = decorable.getEnclosingInjectable().getInjectedType();
    final Collection<HasAnnotations> allAnnotated = getMembersAndParamsAnnotatedWith(enclosingType, AutoBound.class);

    if (allAnnotated.size() > 1) {
      throw new GenerationException("Multiple @AutoBound data binders injected in " + enclosingType);
    }
    else if (allAnnotated.size() == 1) {
      final HasAnnotations annotated = allAnnotated.iterator().next();

      if (annotated instanceof MetaParameter) {
        final MetaParameter mp = (MetaParameter) annotated;

        assertTypeIsDataBinder(mp.getType());
        dataModelType = getFirstErasedTypeParameterClass(mp.getType());
        dataBinderRef = getAccessStatementForAutoBoundDataBinder(decorable, controller);
      }
      else {
        final MetaField field = (MetaField) allAnnotated.iterator().next();

        assertTypeIsDataBinder(field.getType());
        dataModelType = getFirstErasedTypeParameterClass(field.getType());
        dataBinderRef = DecorableType.FIELD.getAccessStatement(field, decorable.getFactoryMetaClass());
        if (!field.isPublic()) {
          controller.addExposedField(field);
        }
      }
    }
    else {
      for (final MetaField field : enclosingType.getFields()) {
        if (field.isAnnotationPresent(AutoBound.class)) {
          assertTypeIsDataBinder(field.getType());
          dataModelType = (MetaClass) field.getType().getParameterizedType().getTypeParameters()[0];
          dataBinderRef = invokeStatic(decorable.getInjectionContext().getProcessingContext().getBootstrapClass(),
                  PrivateAccessUtil.getPrivateFieldAccessorName(field),
                  Variable.get("instance"));
          controller.exposedFieldStmt(field);
          break;
        }
      }
    }

    return (dataBinderRef != null) ? new DataBinderRef(dataModelType, dataBinderRef) : null;
  }

  private static MetaClass getFirstErasedTypeParameterClass(final MetaClass metaClass) {
    final MetaType typeParameter = metaClass.getParameterizedType().getTypeParameters()[0];

    if (typeParameter instanceof MetaParameterizedType) {
      return (MetaClass) ((MetaParameterizedType) typeParameter).getRawType();
    }

    return (MetaClass) typeParameter;
  }

  private static Statement getAccessStatementForAutoBoundDataBinder(final Decorable decorable, final FactoryController controller) {
    final Injectable enclosingInjectable = decorable.getEnclosingInjectable();
    for (final Dependency dep : enclosingInjectable.getDependencies()) {
      switch (dep.getDependencyType()) {
      case Constructor:
      case SetterParameter:
        if (!(dep instanceof ParamDependency)) {
          throw new RuntimeException("Found " + dep.getDependencyType() + " dependency that was not of type " + ParamDependency.class.getName());
        }
        final ParamDependency paramDep = (ParamDependency) dep;
        if (paramDep.getParameter().isAnnotationPresent(AutoBound.class)) {
          return DecorableType.PARAM.getAccessStatement(paramDep.getParameter(), decorable.getFactoryMetaClass());
        } else {
          break;
        }
      case Field:
        if (!(dep instanceof FieldDependency)) {
          throw new RuntimeException("Found " + dep.getDependencyType() + " dependency that was not of type " + FieldDependency.class.getName());
        }
        final FieldDependency fieldDep = (FieldDependency) dep;
        if (fieldDep.getField().isAnnotationPresent(AutoBound.class)) {
          if (!fieldDep.getField().isPublic()) {
            controller.exposedFieldStmt(fieldDep.getField());
          }
          return DecorableType.FIELD.getAccessStatement(fieldDep.getField(), decorable.getFactoryMetaClass());
        }
      default:
        break;
      }
    }

    return null;
  }

  /**
   * Ensures the provided type is a {@link DataBinder} and throws a
   * {@link GenerationException} in case it's not.
   *
   * @param type
   *          the type to check
   */
  private static void assertTypeIsDataBinder(final MetaClass type) {
    final MetaClass databinderMetaClass = MetaClassFactory.get(DataBinder.class);

    if (!databinderMetaClass.isAssignableFrom(type)) {
      throw new GenerationException("Type of @AutoBound element must be " + DataBinder.class.getName() + " but is: "
              + type.getFullyQualifiedName());
    }
  }

  /**
   * Ensured the provided type is bindable and throws a
   * {@link GenerationException} in case it's not.
   *
   * @param type
   *          the type to check
   */
  private static void assertTypeIsBindable(final MetaClass type, final Set<MetaClass> allConfiguredBindableTypes) {
    if (!isTypeBindable(type, allConfiguredBindableTypes)) {
      throw new GenerationException(type.getName() + " must be a @Bindable type when used as @Model");
    }
  }

  private static boolean isTypeBindable(final MetaClass type, final Set<MetaClass> allConfiguredBindableTypes) {
    return type.isAnnotationPresent(Bindable.class) || allConfiguredBindableTypes.contains(type);
  }
}
