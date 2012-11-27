package org.jboss.errai.ui.nav.rebind;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.jboss.errai.codegen.meta.impl.gwt.GWTUtil;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.If;
import org.jboss.errai.codegen.util.Implementations;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.PrivateAccessUtil;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;
import org.jboss.errai.ui.nav.client.local.HistoryToken;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageHiding;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.nav.client.local.spi.NavigationGraph;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.user.client.ui.Widget;

/**
 * Generates the GeneratedNavigationGraph class based on {@code @Page} and
 * {@code @DefaultPage} annotations.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class NavigationGraphGenerator extends Generator {

  @Override
  public String generate(TreeLogger logger, GeneratorContext context,
          String typeName) throws UnableToCompleteException {
    GWTUtil.populateMetaClassFactoryFromTypeOracle(context, logger);

    final ClassStructureBuilder<?> classBuilder =
            Implementations.extend(NavigationGraph.class, "GeneratedNavigationGraph");

    // accumulation of (name, pageclass) mappings for dupe detection and dot file generation
    BiMap<String, MetaClass> pageNames = HashBiMap.create();

    // accumulation of pages with startingPage=true (for ensuring there is exactly one default page)
    List<MetaClass> defaultPages = new ArrayList<MetaClass>();

    ConstructorBlockBuilder<?> ctor = classBuilder.publicConstructor();
    final Collection<MetaClass> pages = ClassScanner.getTypesAnnotatedWith(Page.class);
    for (MetaClass pageClass : pages) {
      if (!pageClass.isAssignableTo(Widget.class)) {
        throw new GenerationException(
                "Class " + pageClass.getFullyQualifiedName() + " is annotated with @Page, so it must be a subtype " +
                "of Widget.");
      }
      Page annotation = pageClass.getAnnotation(Page.class);
      String pageName = annotation.path().equals("") ? pageClass.getName() : annotation.path();

      MetaClass prevPageWithThisName = pageNames.put(pageName, pageClass);
      if (prevPageWithThisName != null) {
        throw new GenerationException(
                "Page names must be unique, but " + prevPageWithThisName + " and " + pageClass +
                " are both named [" + pageName + "]");
      }
      Statement pageImplStmt = generateNewInstanceOfPageImpl(pageClass, pageName);
      if (annotation.startingPage() == true) {
        defaultPages.add(pageClass);

        // need to assign the page impl to a variable and add it to the map twice
        ctor.append(Stmt.declareFinalVariable("defaultPage", PageNode.class, pageImplStmt));
        pageImplStmt = Variable.get("defaultPage");
        ctor.append(
                Stmt.nestedCall(Refs.get("pagesByName"))
                .invoke("put", "", pageImplStmt));
      }
      else if (pageName.equals("")) {
        throw new GenerationException(
                "Page " + pageClass.getFullyQualifiedName() + " has an empty path. Only the" +
                " page with startingPage=true is permitted to have an empty path.");
      }
      ctor.append(
              Stmt.nestedCall(Refs.get("pagesByName"))
              .invoke("put", pageName, pageImplStmt));
    }
    ctor.finish();

    renderNavigationToDotFile(pageNames);

    if (defaultPages.size() == 0) {
      throw new GenerationException(
              "No @Page classes have startingPage=true. Exactly one @Page class" +
              " must be designated as the starting page.");
    }
    if (defaultPages.size() > 1) {
      StringBuilder defaultPageList = new StringBuilder();
      for (MetaClass mc : defaultPages) {
        defaultPageList.append("\n  ").append(mc.getFullyQualifiedName());
      }
      throw new GenerationException(
              "Found more than one @Page with startingPage=true: " + defaultPageList +
              "\nExactly one @Page class must be designated as the starting page.");
    }

    String out = classBuilder.toJavaString();
    final File fileCacheDir = RebindUtils.getErraiCacheDir();
    final File cacheFile = new File(fileCacheDir.getAbsolutePath() + "/"
            + classBuilder.getClassDefinition().getName() + ".java");

    RebindUtils.writeStringToFile(cacheFile, out);

    if (Boolean.getBoolean("errai.codegen.printOut")) {
      System.out.println("---NavigationGraph-->");
      System.out.println(out);
      System.out.println("<--NavigationGraph---");
    }

    PrintWriter printWriter = context.tryCreate(
            logger,
            classBuilder.getClassDefinition().getPackageName(),
            classBuilder.getClassDefinition().getName());

    // printWriter is null if code has already been generated.
    if (printWriter != null) {
      printWriter.append(out);
      context.commit(logger, printWriter);
    }

    return classBuilder.getClassDefinition().getFullyQualifiedName();

  }

  /**
   * Generates a new instance of an anonymous inner class that implements the
   * PageNode interface.
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
        .publicMethod(Class.class, "contentType")
            .append(Stmt.loadLiteral(pageClass).returnValue()).finish()
        .publicMethod(pageClass, "content")
            .append(Stmt.nestedCall(Refs.get("bm"))
                    .invoke("lookupBean", Stmt.loadLiteral(pageClass)).invoke("getInstance").returnValue()).finish();

    List<MetaField> pageStateFields = new ArrayList<MetaField>();
    for (MetaField field : pageClass.getFields()) {
      if (field.isAnnotationPresent(PageState.class)) {
        pageStateFields.add(field);
      }
    }

    appendPageHidingMethod(pageImplBuilder, pageClass);
    appendPageShowingMethod(pageImplBuilder, pageClass, pageStateFields);

    return pageImplBuilder.finish();
  }

  /**
   * Appends the method that calls the {@code @PageHiding} method of the widget.
   *
   * @param pageImplBuilder
   *          The class builder for the implementation of PageNode we are adding
   *          the method to.
   * @param pageClass
   *          The "content type" (Widget subclass) of the page. This is the type
   *          the user annotated with {@code @Page}.
   */
  private void appendPageHidingMethod(AnonymousClassStructureBuilder pageImplBuilder, MetaClass pageClass) {

    BlockBuilder<?> method = pageImplBuilder.publicMethod(void.class, "pageHiding",
            Parameter.of(pageClass, "widget"))
            .body();

    List<MetaMethod> pageHidingMethods = pageClass.getMethodsAnnotatedWith(PageHiding.class);
    if (pageHidingMethods.size() > 1) {
      throw new UnsupportedOperationException(
              "A @Page can have at most 1 @PageHiding method, but " + pageClass + " has " + pageHidingMethods.size());
    }
    for (MetaMethod pageHidingMethod : pageHidingMethods) {
      if (!pageHidingMethod.getReturnType().equals(MetaClassFactory.get(void.class))) {
        throw new UnsupportedOperationException(
                "@PageHiding methods must have a void return type, but " +
                pageHidingMethod.getDeclaringClass().getFullyQualifiedName() + "." + pageHidingMethod.getName() +
                " returns " + pageHidingMethod.getReturnType().getFullyQualifiedName());
      }
      if (pageHidingMethod.getParameters().length != 0) {
        throw new UnsupportedOperationException(
                "@PageHiding methods cannot take parameters, but " +
                pageHidingMethod.getDeclaringClass().getFullyQualifiedName() + "." + pageHidingMethod.getName() +
                " does.");
      }

      PrivateAccessUtil.addPrivateAccessStubs("jsni", pageImplBuilder, pageHidingMethod, new Modifier[] {});
      method.append(Stmt.loadVariable("this").invoke(PrivateAccessUtil.getPrivateMethodName(pageHidingMethod), Stmt.loadVariable("widget")));
    }

    method.finish();
  }

  private void appendPageShowingMethod(AnonymousClassStructureBuilder pageImplBuilder, MetaClass pageClass, List<MetaField> pageStateFields) {
    BlockBuilder<?> method = pageImplBuilder.publicMethod(void.class, "pageShowing",
            Parameter.of(pageClass, "widget"),
            Parameter.of(HistoryToken.class, "state"))
            .body();

    int idx = 0;
    for (MetaField field : pageStateFields) {
      PrivateAccessUtil.addPrivateAccessStubs(PrivateAccessType.Write, "jsni", pageImplBuilder, field, new Modifier[] {});
      String injectorName = PrivateAccessUtil.getPrivateFieldInjectorName(field);

      MetaClass erasedFieldType = field.getType().getErased();
      if (erasedFieldType.isAssignableTo(Collection.class)) {
        MetaClass elementType = MarshallingGenUtil.getConcreteCollectionElementType(field.getType());
        if (elementType == null) {
          throw new UnsupportedOperationException(
                  "Found a @PageState field with a Collection type but without a concrete type parameter. " +
                  "Collection-typed @PageState fields must specify a concrete type parameter.");
        }
        if (erasedFieldType.equals(MetaClassFactory.get(Set.class))) {
          method.append(Stmt.declareVariable(field.getName(), Stmt.newObject(HashSet.class)));
        }
        else if (erasedFieldType.equals(MetaClassFactory.get(List.class))
                || erasedFieldType.equals(MetaClassFactory.get(Collection.class))) {
          method.append(Stmt.declareVariable(field.getName(), Stmt.newObject(ArrayList.class)));
        }
        else {
          throw new UnsupportedOperationException(
                  "Found a @PageState field which is a collection of type " + erasedFieldType.getFullyQualifiedName() +
                  ". For collection-valued fields, only the exact types java.util.Collection, java.util.Set, and " +
                  "java.util.List are supported at this time.");
        }

        // for (String fv{idx} : state.get({fieldName}))
        method.append(
          Stmt.loadVariable("state").invoke("getState").invoke("get", field.getName()).foreach("elem", Object.class)
            .append(Stmt.declareVariable("fv" + idx, Stmt.castTo(String.class, Stmt.loadVariable("elem"))))
            .append(Stmt.loadVariable(field.getName()).invoke("add", paramFromStringStatement(elementType, Stmt.loadVariable("fv" + idx))))
            .finish()
          );
        method.append(Stmt.loadVariable("this").invoke(injectorName, Stmt.loadVariable("widget"), Stmt.loadVariable(field.getName())));
      }
      else {
        method.append(Stmt.declareFinalVariable("fv" + idx, Collection.class, Stmt.loadVariable("state").invoke("getState").invoke("get", field.getName())));
        method.append(
          If.cond(Bool.or(Bool.isNull(Stmt.loadVariable("fv" + idx)), Stmt.loadVariable("fv" + idx).invoke("isEmpty")))
              .append(Stmt.loadVariable("this").invoke(injectorName, Stmt.loadVariable("widget"), defaultValueStatement(erasedFieldType))).finish()
            .else_()
              .append(Stmt.loadVariable("this").invoke(injectorName, Stmt.loadVariable("widget"), paramFromStringStatement(erasedFieldType, Stmt.loadVariable("fv" + idx).invoke("iterator").invoke("next"))))
              .finish()
          );
      }
      idx++;
    }

    List<MetaMethod> pageShowingMethods = pageClass.getMethodsAnnotatedWith(PageShowing.class);
    if (pageShowingMethods.size() > 1) {
      throw new UnsupportedOperationException(
              "A @Page can have at most 1 @PageShowing method, but " + pageClass + " has " + pageShowingMethods.size());
    }
    for (MetaMethod pageShowingMethod : pageShowingMethods) {
      if (!pageShowingMethod.getReturnType().equals(MetaClassFactory.get(void.class))) {
        throw new UnsupportedOperationException(
                "@PageShowing methods must have a void return type, but " +
                pageShowingMethod.getDeclaringClass().getFullyQualifiedName() + "." + pageShowingMethod.getName() +
                " returns " + pageShowingMethod.getReturnType().getFullyQualifiedName());
      }

      // assemble parameters for private method invoker (first param is the widget instance)
      PrivateAccessUtil.addPrivateAccessStubs("jsni", pageImplBuilder, pageShowingMethod, new Modifier[] {});
      Object[] paramValues = new Object[pageShowingMethod.getParameters().length + 1];
      paramValues[0] = Stmt.loadVariable("widget");

      for (int i = 1; i < paramValues.length; i++) {
        MetaParameter paramSpec = pageShowingMethod.getParameters()[i - 1];
        if (paramSpec.getType().equals(MetaClassFactory.get(HistoryToken.class))) {
          paramValues[i] = Stmt.loadVariable("state");
        }
        else {
          throw new UnsupportedOperationException(
                  "@PageShowing method " +
                  pageShowingMethod.getDeclaringClass().getFullyQualifiedName() + "." + pageShowingMethod.getName() +
                  " has an illegal parameter of type " + paramSpec.getType().getFullyQualifiedName());
        }
      }
      method.append(Stmt.loadVariable("this").invoke(PrivateAccessUtil.getPrivateMethodName(pageShowingMethod), paramValues));
    }

    method.finish();
  }

  /**
   * Renders the page-to-page navigation graph into the file {@code navgraph.gv}
   * in the {@code .errai} cache directory.
   *
   * @param fromClass
   */
  private void renderNavigationToDotFile(BiMap<String, MetaClass> pages) {
    final File dotFile = new File(RebindUtils.getErraiCacheDir().getAbsolutePath(), "navgraph.gv");
    PrintWriter out = null;
    try {
      out = new PrintWriter(dotFile);
      out.println("digraph Navigation {");
      final MetaClass transitionToType = MetaClassFactory.get(TransitionTo.class);
      for (Map.Entry<String, MetaClass> entry : pages.entrySet()) {
        String pageName = entry.getKey();
        MetaClass pageClass = entry.getValue();

        // entry for the node itself
        out.print("\"" + pageName + "\"");
        if (pageClass.getAnnotation(Page.class).startingPage() == true) {
          out.print(" [penwidth=3]");
        }
        out.println();

        for (MetaField field : getAllFields(pageClass)) {
          if (field.getType().getErased().equals(transitionToType)) {
            MetaType targetPageType = field.getType().getParameterizedType().getTypeParameters()[0];
            String targetPageName = pages.inverse().get(targetPageType);

            // entry for the link between nodes
            out.println("\"" + pageName + "\" -> \"" + targetPageName + "\" [label=\"" + field.getName() + "\"]");
          }
        }
      }
      out.println("}");
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    finally {
      if (out != null) {
        out.close();
      }
    }
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
      throw new UnsupportedOperationException("@PageState fields of type " + toType.getFullyQualifiedName() + " are not supported");
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
        throw new UnsupportedOperationException("Don't know how to make a default value for @PageState field of type " + type.getFullyQualifiedName());
      }
    }
    else {
      return Stmt.load(null);
    }
  }


}
