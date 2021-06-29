/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ui.nav.rebind;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.user.client.ui.IsWidget;
import org.apache.commons.lang3.StringUtils;
import org.jboss.errai.codegen.Modifier;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ConstructorBlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.If;
import org.jboss.errai.codegen.util.Implementations;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.PrivateAccessUtil;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.PageRequest;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.rebind.AbstractAsyncGenerator;
import org.jboss.errai.config.rebind.GenerateAsync;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;
import org.jboss.errai.ui.nav.client.local.*;
import org.jboss.errai.ui.nav.client.local.api.NavigationControl;
import org.jboss.errai.ui.nav.client.local.spi.NavigationGraph;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;
import org.jboss.errai.ui.nav.client.shared.NavigationEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates the GeneratedNavigationGraph class based on {@code @Page} annotations.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@GenerateAsync(NavigationGraph.class)
public class NavigationGraphGenerator extends AbstractAsyncGenerator {

  private static final String GENERATED_CLASS_NAME = "GeneratedNavigationGraph";

  /*
   * These pages should not cause @Page validation if no other pages exist.
   */
  private static final Collection<String> DENYLISTED_PAGES = Arrays
          .asList("org.jboss.errai.security.client.local.context.SecurityContextImpl.SecurityRolesConstraintPage");

  @Override
  public String generate(TreeLogger logger, GeneratorContext context,
          String typeName) throws UnableToCompleteException {

    return startAsyncGeneratorsAndWaitFor(NavigationGraph.class,
        context, logger, NavigationGraph.class.getPackage().getName(), GENERATED_CLASS_NAME);
  }

  @Override
  protected String generate(TreeLogger logger, GeneratorContext context) {
    final ClassStructureBuilder<?> classBuilder =
        Implementations.extend(NavigationGraph.class, GENERATED_CLASS_NAME);

    // accumulation of (name, pageclass) mappings for dupe detection and dot file generation
    BiMap<String, MetaClass> pageNames = HashBiMap.create();

    // accumulation UniquePageRoles for ensuring there is exactly one.
    Multimap<Class<?>, MetaClass> pageRoles = ArrayListMultimap.create();

    ConstructorBlockBuilder<?> ctor = classBuilder.publicConstructor();
    final Collection<MetaClass> pages = ClassScanner.getTypesAnnotatedWith(Page.class, context);
    addCacheRelevantClasses(pages);

    /*
     * This prevents @Page validation logic from aborting compilation if a user has errai security
     * but is not using errai-navigation.
     */
    final boolean hasNonDenylistedPages = containsNonDenylistedPages(pages);

    if (hasNonDenylistedPages) {
      for (MetaClass pageClass : pages) {
        if (!(pageClass.isAssignableTo(IsWidget.class)
                || pageClass.isAssignableTo(IsElement.class)
                || pageClass.isAssignableTo(org.jboss.errai.common.client.api.elemental2.IsElement.class)
                || pageClass.isAnnotationPresent(Templated.class))) {
          throw new GenerationException("Class " + pageClass.getFullyQualifiedName()
                  + " is annotated with @Page, so it must implement IsWidget or be @Templated");
        }
        Page annotation = pageClass.getAnnotation(Page.class);
        String pageName = getPageName(pageClass);
        List<Class<? extends PageRole>> annotatedPageRoles = Arrays.asList(annotation.role());

        MetaClass prevPageWithThisName = pageNames.put(pageName, pageClass);
        if (prevPageWithThisName != null) {
          throw new GenerationException(
              "Page names must be unique, but " + prevPageWithThisName + " and " + pageClass +
                  " are both named [" + pageName + "]");
        }
        Statement pageImplStmt = generateNewInstanceOfPageImpl(pageClass, pageName);
        if (annotatedPageRoles.contains(DefaultPage.class)) {
          // need to assign the page impl to a variable and add it to the map twice
          URLPattern pattern = URLPatternMatcher.generatePattern(annotation.path());
          if (pattern.getParamList().size() > 0) {
            throw new GenerationException("Default Page must not contain any path parameters.");
          }
          ctor.append(Stmt.declareFinalVariable("defaultPage", PageNode.class, pageImplStmt));
          pageImplStmt = Variable.get("defaultPage");
          ctor.append(
              Stmt.nestedCall(Refs.get("pagesByName"))
                  .invoke("put", "", pageImplStmt));
          ctor.append(
                  Stmt.nestedCall(Refs.get("pagesByRole"))
                          .invoke("put", DefaultPage.class, pageImplStmt));
        }
        else if (pageName.equals("")) {
          throw new GenerationException(
              "Page " + pageClass.getFullyQualifiedName() + " has an empty path. Only the" +
                  " page with the role DefaultPage is permitted to have an empty path.");
        }

        final String fieldName = StringUtils.uncapitalize(pageClass.getName());
        ctor.append(Stmt.declareFinalVariable(fieldName, PageNode.class, pageImplStmt));
        ctor.append(
            Stmt.nestedCall(Refs.get("pagesByName"))
                .invoke("put", pageName, Refs.get(fieldName)));

        for (Class<? extends PageRole> annotatedPageRole : annotatedPageRoles) {
          pageRoles.put(annotatedPageRole, pageClass);
          // DefaultPage is already added above.
          if (!annotatedPageRole.equals(DefaultPage.class))
            ctor.append(
                Stmt.nestedCall(Refs.get("pagesByRole"))
                  .invoke("put", annotatedPageRole, Refs.get(fieldName)));
        }
      }
    }
    ctor.finish();

    if (hasNonDenylistedPages) {
      validateDefaultPagePresent(pages, pageRoles);
      validateUnique(pageRoles);
      validateExistingRolesPresent(pages, pageRoles);
      validateSafePagePath(pages);

      renderNavigationToDotFile(pageNames, pageRoles);
    }

    return classBuilder.toJavaString();
  }

  private boolean containsNonDenylistedPages(final Collection<MetaClass> pages) {
    for (final MetaClass page : pages) {
      if (!DENYLISTED_PAGES.contains(page.getCanonicalName())) {
        return true;
      }
    }

    return false;
  }

  private String getPageName(MetaClass pageClass) {
    return pageClass.getName();
  }

  private void validateDefaultPagePresent(Collection<MetaClass> pages, Multimap<Class<?>, MetaClass> pageRoles) {
    Collection<MetaClass> defaultPages = pageRoles.get(DefaultPage.class);
    if (!pages.isEmpty() && defaultPages.isEmpty()) {
      throw new GenerationException(
              "No @Page classes have role = DefaultPage. Exactly one @Page class" +
                      " must be designated as the default starting page.");
    }
  }

  private void validateUnique(Multimap<Class<?>, MetaClass> pageRoles) {
    for (Class<?> pageRole : pageRoles.keys()) {
      final Collection<MetaClass> pages = pageRoles.get(pageRole);
      if (UniquePageRole.class.isAssignableFrom(pageRole) && pages.size() > 1) {
        createValidationError(pages, pageRole);
      }
    }
  }

  private void validateExistingRolesPresent(final Collection<MetaClass> pages,
          final Multimap<Class<?>, MetaClass> pageRoles) {
    for (final MetaClass page : pages) {
      for (final MetaField field : getAllFields(page)) {
        if (field.getType().getErased().equals(MetaClassFactory.get(TransitionToRole.class))) {
          final MetaType uniquePageRole = field.getType().getParameterizedType().getTypeParameters()[0];
          try {
            getPageWithRole(uniquePageRole, pageRoles);
          }
          catch (IllegalStateException e) {
            // give a more descriptive error message.
            throw new GenerationException("No @Page with the UniquePageRole " + uniquePageRole.getName()
                    + " exists to satisfy TransitionToRole<" + uniquePageRole.getName()
                    + "> in " + page.getFullyQualifiedName() + "."
                    + "\nThere must be exactly 1 @Page with this role.", e);
          }
        }
      }
    }
  }

  private void validateSafePagePath(Collection<MetaClass> pages) {
    for (final MetaClass page : pages) {
      String pageName = getPageName(page);
      String path = getPageURL(page, pageName);

      if (!path.equals(pageName)) {
        if (path.contains(";") || path.contains("=") || path.contains("&")) {
          throw new GenerationException("Invalid page path for page " + pageName + ". Page paths must not contain "
                   + "semicolons(;), ampersands(&) or equal signs(=).");
        }
      }
    }

  }

  private void createValidationError(Collection<MetaClass> pages, Class<?> role) {
    StringBuilder builder = new StringBuilder();
    for (MetaClass mc : pages) {
      builder.append("\n  ").append(mc.getFullyQualifiedName());
    }
    throw new GenerationException(
            "Found more than one @Page with role = '" + role + "': " + builder +
                    "\nExactly one @Page class must be designated with this unique role.");
  }

  /**
   * Generates a new instance of an anonymous inner class that implements the PageNode interface.
   *
   * @param pageClass
   *          The class providing the widget content for the page.
   * @param pageName
   *          The name of the page (to be used in the URL history fragment).
   */
  private ObjectBuilder generateNewInstanceOfPageImpl(MetaClass pageClass, String pageName) {
    AnonymousClassStructureBuilder pageImplBuilder = ObjectBuilder.newInstanceOf(
            MetaClassFactory.parameterizedAs(PageNode.class, MetaClassFactory.typeParametersOf(pageClass))).extend();

    pageImplBuilder
        .publicMethod(String.class, "name")
            .append(Stmt.loadLiteral(pageName).returnValue()).finish()
        .publicMethod(String.class, "toString")
            .append(Stmt.loadLiteral(pageName).returnValue()).finish()
        .publicMethod(String.class, "getURL")
            .append(Stmt.loadLiteral(getPageURL(pageClass, pageName)).returnValue()).finish()
        .publicMethod(Class.class, "contentType")
            .append(Stmt.loadLiteral(pageClass).returnValue()).finish()
        .publicMethod(void.class, "produceContent", Parameter.of(CreationalCallback.class, "callback"))
            .append(Stmt.nestedCall(Refs.get("bm"))
                    .invoke("lookupBean", Stmt.loadLiteral(pageClass))
                    .invoke("getInstance", Stmt.loadVariable("callback")))
                    .finish();

    appendPageHidingMethod(pageImplBuilder, pageClass);
    appendPageHiddenMethod(pageImplBuilder, pageClass);

    appendPageShowingMethod(pageImplBuilder, pageClass);
    appendPageShownMethod(pageImplBuilder, pageClass);
    appendPageUpdateMethod(pageImplBuilder, pageClass);

    appendDestroyMethod(pageImplBuilder, pageClass);

    return pageImplBuilder.finish();
  }

  private String getPageURL(MetaClass pageClass, String pageName) {
    Page pageAnnotation = pageClass.getAnnotation(Page.class);
    String path = pageAnnotation.path();

    if (path.equals("")) {
      return pageName;
    }

    return path;
  }

  /**
   * Appends the method that calls the {@code @PageHiding} method of the widget.
   *
   * @param pageImplBuilder
   *          The class builder for the implementation of PageNode we are adding the method to.
   * @param pageClass
   *          The "content type" (Widget subclass) of the page. This is the type the user annotated
   *          with {@code @Page}.
   */
  private void appendPageHidingMethod(AnonymousClassStructureBuilder pageImplBuilder, MetaClass pageClass) {
    BlockBuilder<?> method = pageImplBuilder.publicMethod(
                    void.class,
                    createMethodNameFromAnnotation(PageHiding.class),
                    Parameter.of(pageClass, "widget"),
                    Parameter.of(NavigationControl.class, "control")).body();
    final MetaMethod pageHidingMethod = checkMethodAndAddPrivateAccessors(pageImplBuilder, method, pageClass,
            PageHiding.class, Parameter.of(NavigationControl.class, "control"));

    /*
     * If the user did not provide a control parameter, we must proceed for them after the method is invoked.
     */
    if (pageHidingMethod == null || pageHidingMethod.getParameters().length != 1) {
      method.append(Stmt.loadVariable("control").invoke("proceed"));
    }

    method.finish();
  }

  /**
   * Appends the method that calls the {@code @PageHidden} method of the widget.
   *
   * @param pageImplBuilder
   *          The class builder for the implementation of PageNode we are adding the method to.
   * @param pageClass
   *          The "content type" (Widget subclass) of the page. This is the type the user annotated
   *          with {@code @Page}.
   */
  private void appendPageHiddenMethod(AnonymousClassStructureBuilder pageImplBuilder, MetaClass pageClass) {
    BlockBuilder<?> method = pageImplBuilder.publicMethod(
            void.class,
            createMethodNameFromAnnotation(PageHidden.class),
            Parameter.of(pageClass, "widget")).body();
    checkMethodAndAddPrivateAccessors(pageImplBuilder, method, pageClass, PageHidden.class,
            Parameter.of(HistoryToken.class, "state"));
    method.finish();
  }

  /**
   * Appends a method that destroys the IOC bean associated with a page node if the bean is
   * dependent scope.
   *
   * @param pageImplBuilder
   *          The class builder for the implementation of PageNode we are adding the method to.
   * @param pageClass
   *          The "content type" (Widget subclass) of the page. This is the type the user annotated
   *          with {@code @Page}.
   */
  private void appendDestroyMethod(AnonymousClassStructureBuilder pageImplBuilder, MetaClass pageClass) {
    BlockBuilder<?> method = pageImplBuilder.publicMethod(
            void.class,
            "destroy",
            Parameter.of(pageClass, "widget")).body();

    if (pageClass.getAnnotation(Singleton.class) == null && pageClass.getAnnotation(ApplicationScoped.class) == null
            && pageClass.getAnnotation(EntryPoint.class) == null) {
      method.append(Stmt.loadVariable("bm").invoke("destroyBean", Stmt.loadVariable("widget")));
    }

    method.finish();
  }

  /**
   * Searches the given class for methods bearing the given annotation. Verifies
   * that such methods follow the rules (returns void; takes PageState as a
   * parameter (or not); is the only method with this annotation), creates a
   * private accessor if required, and then appends a call to that method to the
   * given method builder.
   *
   * @param pageImplBuilder
   *          the class to add private accessors to if necessary
   * @param methodToAppendTo
   *          the method builder to append to
   * @param pageClass
   *          the class to search for annotated methods in
   * @param annotation
   *          the annotation to search for in pageClass
   * @param optionalParams
   *          optional params in linear ordering
   * @return
   *          The meta-method for which code was generated
   * @throws UnsupportedOperationException
   *           if the annotated methods in pageClass violate any of the rules
   */
  private MetaMethod checkMethodAndAddPrivateAccessors(AnonymousClassStructureBuilder pageImplBuilder,
      BlockBuilder<?> methodToAppendTo, MetaClass pageClass, Class<? extends Annotation> annotation,
      Parameter... optionalParams) {
    List<MetaMethod> annotatedMethods = pageClass.getMethodsAnnotatedWith(annotation);
    if (annotatedMethods.size() > 1) {
      throw new UnsupportedOperationException(
          "A @Page can have at most 1 " + createAnnotionName(annotation) + " method, but " + pageClass + " has "
              + annotatedMethods.size());
    }

    for (MetaMethod metaMethod : annotatedMethods) {
      if (!metaMethod.getReturnType().equals(MetaClassFactory.get(void.class))) {
        throw new UnsupportedOperationException(
              createAnnotionName(annotation) + " methods must have a void return type, but " +
                  metaMethod.getDeclaringClass().getFullyQualifiedName() + "." + metaMethod.getName() +
                  " returns " + metaMethod.getReturnType().getFullyQualifiedName());
      }

      int realParamLength = metaMethod.getParameters().length;
      Object[] paramValues = new Object[realParamLength + 1];
      paramValues[0] = Stmt.loadVariable("widget");

      // assemble parameters for private method invoker (first param is the widget instance)
      PrivateAccessUtil.addPrivateAccessStubs("jsni", pageImplBuilder, metaMethod, new Modifier[] {});

      if (optionalParams != null) {
        if (realParamLength <= optionalParams.length) {
          for (int i = 1; i < paramValues.length; i++) {
            Parameter param = optionalParams[i - 1];
            MetaParameter realParam = metaMethod.getParameters()[i - 1];

            if (realParam.getType().equals(MetaClassFactory.get(param.getType().asClass()))) {
              paramValues[i] = Stmt.loadVariable(param.getName());
            } else {
              throw new UnsupportedOperationException(
                createAnnotionName(annotation) + " method " + metaMethod.getDeclaringClass().getFullyQualifiedName() +
                "." + metaMethod.getName() + " has an illegal parameter of type " + realParam.getType().getFullyQualifiedName());
            }
          }
        } else {
          throw new UnsupportedOperationException(
              createAnnotionName(annotation) + " methods should only take "+optionalParams.length+" or less " +
              "optional parameters, but " + metaMethod.getDeclaringClass().getFullyQualifiedName() + "." +
              metaMethod.getName() + " is implementing "+realParamLength+".");
        }
      } else {
        if (realParamLength != 0) {
          throw new UnsupportedOperationException(
              createAnnotionName(annotation) + " methods cannot take parameters, but " +
              metaMethod.getDeclaringClass().getFullyQualifiedName() + "." + metaMethod.getName() + " does.");
        }
      }

      methodToAppendTo.append(Stmt.loadVariable("this").invoke(PrivateAccessUtil.getPrivateMethodName(metaMethod), paramValues));

      return annotatedMethods.get(0);
    }

    return null;
  }

  private String createAnnotionName(Class<? extends Annotation> annotation) {
    return "@" + annotation.getSimpleName();
  }

  private String createMethodNameFromAnnotation(Class<? extends Annotation> annotation) {
    return StringUtils.uncapitalize(annotation.getSimpleName());
  }

  private void appendPageShowingMethod(AnonymousClassStructureBuilder pageImplBuilder, MetaClass pageClass) {
    BlockBuilder<?> method = pageImplBuilder.publicMethod(void.class, createMethodNameFromAnnotation(PageShowing.class),
            Parameter.of(pageClass, "widget"),
            Parameter.of(HistoryToken.class, "state"),
            Parameter.of(NavigationControl.class, "control"))
            .body();

    MetaMethod pageShowMethod = appendPageShowMethod(method, pageImplBuilder, pageClass, PageShowing.class, true,
        Parameter.of(HistoryToken.class, "state"), Parameter.of(NavigationControl.class, "control"));

    /*
     * If the user did not provide a control parameter, we must proceed for them after the method is invoked.
     */
    if (pageShowMethod == null || pageShowMethod.getParameters().length != 2) {
      method.append(Stmt.loadVariable("control").invoke("proceed"));
    }

    method.finish();
  }

  private void appendPageShownMethod(AnonymousClassStructureBuilder pageImplBuilder, MetaClass pageClass) {
    BlockBuilder<?> method = pageImplBuilder.publicMethod(void.class, createMethodNameFromAnnotation(PageShown.class),
            Parameter.of(pageClass, "widget"),
            Parameter.of(HistoryToken.class, "state"))
            .body();

    appendPageShowMethod(method, pageImplBuilder, pageClass, PageShown.class, false,
        Parameter.of(HistoryToken.class, "state"));

    method.finish();
  }

  private void appendPageUpdateMethod(AnonymousClassStructureBuilder pageImplBuilder, MetaClass pageClass) {
    BlockBuilder<?> method = pageImplBuilder.publicMethod(void.class, createMethodNameFromAnnotation(PageUpdate.class),
        Parameter.of(pageClass, "widget"),
        Parameter.of(HistoryToken.class, "state"))
        .body();

    appendPageShowMethod(method, pageImplBuilder, pageClass, PageUpdate.class, false,
        Parameter.of(HistoryToken.class, "state"));

    method.finish();
  }

  private MetaMethod appendPageShowMethod(BlockBuilder<?> method, AnonymousClassStructureBuilder pageImplBuilder, MetaClass pageClass,
      Class<? extends Annotation> annotation, boolean addPrivateAccessors, Parameter... optionalParams) {

    int idx = 0;

    method.append(Stmt.declareFinalVariable("pageState", Map.class, new HashMap<String, Object>()));
    for (MetaField field : pageClass.getFieldsAnnotatedWith(PageState.class)) {
      PageState psAnno = field.getAnnotation(PageState.class);
      String fieldName = field.getName();
      String queryParamName = psAnno.value();
      if (queryParamName == null || queryParamName.trim().isEmpty()) {
        queryParamName = fieldName;
      }

      if (addPrivateAccessors) {
        PrivateAccessUtil.addPrivateAccessStubs(PrivateAccessType.Write, "jsni", pageImplBuilder, field,
            new Modifier[] {});
      }

      String injectorName = PrivateAccessUtil.getPrivateFieldAccessorName(field);

      MetaClass erasedFieldType = field.getType().getErased();
      if (erasedFieldType.isAssignableTo(Collection.class)) {
        MetaClass elementType = MarshallingGenUtil.getConcreteCollectionElementType(field.getType());
        if (elementType == null) {
          throw new UnsupportedOperationException(
                    "Found a @PageState field with a Collection type but without a concrete type parameter. " +
                        "Collection-typed @PageState fields must specify a concrete type parameter.");
        }
        if (erasedFieldType.equals(MetaClassFactory.get(Set.class))) {
          method.append(Stmt.declareVariable(fieldName, Stmt.newObject(HashSet.class)));
        }
        else if (erasedFieldType.equals(MetaClassFactory.get(List.class))
                  || erasedFieldType.equals(MetaClassFactory.get(Collection.class))) {
          method.append(Stmt.declareVariable(fieldName, Stmt.newObject(ArrayList.class)));
        }
        else {
          throw new UnsupportedOperationException(
                    "Found a @PageState field which is a collection of type "
                        + erasedFieldType.getFullyQualifiedName()
                        +
                        ". For collection-valued fields, only the exact types java.util.Collection, java.util.Set, and "
                        +
                        "java.util.List are supported at this time.");
        }

        // for (String fv{idx} : state.get({fieldName}))
        method.append(
                Stmt.loadVariable("state").invoke("getState").invoke("get", queryParamName).foreach("elem", Object.class)
                        .append(Stmt.declareVariable("fv" + idx, Stmt.castTo(String.class, Stmt.loadVariable("elem"))))
                        .append(
                                Stmt.loadVariable(fieldName).invoke("add",
                                        paramFromStringStatement(elementType, Stmt.loadVariable("fv" + idx))))
                .append(
                        Stmt.loadVariable("pageState").invoke(
                                "put", fieldName, Stmt.loadVariable(fieldName)))
                        .finish()
        );
        method.append(Stmt.loadVariable("this").invoke(injectorName, Stmt.loadVariable("widget"),
                Stmt.loadVariable(fieldName)));
      }
      else {
        method.append(Stmt.declareFinalVariable("fv" + idx, Collection.class, Stmt.loadVariable("state").invoke(
                "getState").invoke("get", queryParamName)));
        method.append(
                If.cond(
                        Bool.or(Bool.isNull(Stmt.loadVariable("fv" + idx)), Stmt.loadVariable("fv" + idx).invoke("isEmpty")))
                        .append(
                                Stmt.loadVariable("this").invoke(injectorName, Stmt.loadVariable("widget"),
                                        defaultValueStatement(erasedFieldType))).finish()
                        .else_()
                        .append(
                                Stmt.loadVariable("this").invoke(
                                        injectorName,
                                        Stmt.loadVariable("widget"),
                                        paramFromStringStatement(erasedFieldType, Stmt.loadVariable("fv" + idx).invoke("iterator")
                                                .invoke("next"))))
                        .append(
                                Stmt.loadVariable("pageState").invoke(
                                        "put", fieldName, Stmt.loadVariable("fv" + idx).invoke("iterator").invoke("next")))
                        .finish()
        );
      }
      idx++;
    }

    if (addPrivateAccessors) {
      method.append(Stmt.invokeStatic(CDI.class, "fireEvent",
          Stmt.newObject(NavigationEvent.class).withParameters(
              Stmt.newObject(PageRequest.class).withParameters(getPageName(pageClass), Stmt.loadVariable("pageState")))
      ));
    }

    return checkMethodAndAddPrivateAccessors(pageImplBuilder, method, pageClass, annotation, optionalParams);
  }

  /**
   * Renders the page-to-page navigation graph into the file {@code navgraph.gv} in the
   * {@code .errai} cache directory.
   *
   */
  private void renderNavigationToDotFile(BiMap<String, MetaClass> pages, Multimap<Class<?>, MetaClass> pageRoles) {
    final File dotFile = new File(RebindUtils.getErraiCacheDir().getAbsolutePath(), "navgraph.gv");
    PrintWriter out = null;
    try {
      out = new PrintWriter(dotFile);
      out.println("digraph Navigation {");
      final MetaClass transitionToType = MetaClassFactory.get(TransitionTo.class);
      final MetaClass transitionAnchorType = MetaClassFactory.get(TransitionAnchor.class);
      final MetaClass transitionAnchorFactoryType = MetaClassFactory.get(TransitionAnchorFactory.class);
      final MetaClass transtionToRoleType = MetaClassFactory.get(TransitionToRole.class);
      for (Map.Entry<String, MetaClass> entry : pages.entrySet()) {
        String pageName = entry.getKey();
        MetaClass pageClass = entry.getValue();

        // entry for the node itself
        out.print("\"" + pageName + "\"");

        Page pageAnnotation = pageClass.getAnnotation(Page.class);
        List<Class<? extends PageRole>> roles = Arrays.asList(pageAnnotation.role());
        if (roles.contains(DefaultPage.class)) {
          out.print(" [penwidth=3]");
        }
        out.println();

        for (MetaField field : getAllFields(pageClass)) {
          final MetaClass erasedFieldType = field.getType().getErased();
          if (erasedFieldType.equals(transitionToType)
                  || erasedFieldType.equals(transitionAnchorType)
                  || erasedFieldType.equals(transitionAnchorFactoryType)
                  || erasedFieldType.equals(transtionToRoleType)) {

            final MetaType targetPageType;
            if (erasedFieldType.equals(transtionToRoleType)) {
              final MetaType uniquePageRoleType = field.getType().getParameterizedType().getTypeParameters()[0];
              targetPageType = getPageWithRole(uniquePageRoleType, pageRoles);
            }
            else {
              targetPageType = field.getType().getParameterizedType().getTypeParameters()[0];
            }

            final String targetPageName = pages.inverse().get(targetPageType);

            // entry for the link between nodes
            out.println("\"" + pageName + "\" -> \"" + targetPageName + "\" [label=\"" + field.getName() + "\"]");
          }
        }
      }
      out.println("}");
    }
    catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    finally {
      if (out != null) {
        out.close();
      }
    }
  }

  private MetaType getPageWithRole(final MetaType uniquePageRole, final Multimap<Class<?>, MetaClass> pageRoles) {
    for (final Class<?> pageRole : pageRoles.keySet()) {
      if (UniquePageRole.class.isAssignableFrom(pageRole) && uniquePageRole.getName().equals(pageRole.getSimpleName())) {
        final Collection<MetaClass> matchingPages = pageRoles.get(pageRole);
        if (matchingPages.size() == 1) {
          return matchingPages.iterator().next();
        }
        else {
          throw new IllegalStateException("Expected exactly 1 page with the role, " + uniquePageRole.getName()
                  + ", but found " + matchingPages.size());
        }
      }
    }

    throw new IllegalStateException("No page with the role " + uniquePageRole.getName() + " was found.");
  }

  private static List<MetaField> getAllFields(MetaClass c) {
    ArrayList<MetaField> fields = new ArrayList<MetaField>();
    for (; c != null; c = c.getSuperClass()) {
      fields.addAll(Arrays.asList(c.getDeclaredFields()));
    }
    return fields;
  }

  private static Statement paramFromStringStatement(MetaClass toType, Statement stringValue) {

    // make sure it's really a string
    stringValue = Stmt.castTo(String.class, stringValue);

    if (toType.isAssignableTo(String.class)) {
      return stringValue;
    }
    else if (toType.asBoxed().isAssignableTo(Number.class)) {
      return Stmt.invokeStatic(toType.asBoxed(), "valueOf", stringValue);
    }
    else if (toType.asBoxed().isAssignableTo(Boolean.class)) {
      return Stmt.invokeStatic(Boolean.class, "valueOf", stringValue);
    }
    else {
      throw new UnsupportedOperationException("@PageState fields of type " + toType.getFullyQualifiedName()
          + " are not supported");
    }
  }

  private Statement defaultValueStatement(MetaClass type) {
    if (type.isPrimitive()) {
      if (type.asBoxed().isAssignableTo(Number.class)) {
        return Stmt.castTo(type, Stmt.load(0));
      }
      else if (type.isAssignableTo(boolean.class)) {
        return Stmt.load(false);
      }
      else {
        throw new UnsupportedOperationException("Don't know how to make a default value for @PageState field of type "
            + type.getFullyQualifiedName());
      }
    }
    else {
      return Stmt.load(null);
    }
  }

  @Override
  protected boolean isRelevantClass(MetaClass clazz) {
    return clazz.isAnnotationPresent(Page.class);
  }
}
