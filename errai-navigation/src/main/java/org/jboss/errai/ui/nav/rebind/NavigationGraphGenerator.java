package org.jboss.errai.ui.nav.rebind;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.util.TypeLiteral;

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
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.meta.impl.gwt.GWTUtil;
import org.jboss.errai.codegen.util.Implementations;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.ui.nav.client.local.Page;
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
      List<String> template = parsePageUriTemplate(pageClass, annotation.path());
      String pageName = template.get(0);

      MetaClass prevPageWithThisName = pageNames.put(pageName, pageClass);
      if (prevPageWithThisName != null) {
        throw new GenerationException(
                "Page names must be unique, but " + prevPageWithThisName + " and " + pageClass +
                " are both named [" + pageName + "]");
      }
      Statement pageImplStmt = generateNewInstanceOfPageImpl(pageClass, pageName, template.subList(1, template.size()));
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
   *          The name of the page (normally obtained by a call to
   *          {@link #parsePageUriTemplate(MetaClass, String)}).
   * @param stateParamNames
   *          The names of the state parameters for the page in question
   *          (normally obtained by a call to
   *          {@link #parsePageUriTemplate(MetaClass, String)}).
   */
  private ObjectBuilder generateNewInstanceOfPageImpl(MetaClass pageClass, String pageName, List<String> stateParamNames) {
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

    appendGetStateMethod(pageImplBuilder, pageClass, stateParamNames);
    appendPutStateMethod(pageImplBuilder, pageClass, stateParamNames);

    return pageImplBuilder.finish();
  }

  private void appendGetStateMethod(AnonymousClassStructureBuilder pageImplBuilder, MetaClass pageClass, List<String> stateParamNames) {
    BlockBuilder<?> method = pageImplBuilder.publicMethod(List.class, "getState",
            Parameter.of(pageClass, "widget"))
            .body();

    method.append(Stmt.declareVariable("state", Stmt.newObject(ArrayList.class)));

    for (String paramName : stateParamNames) {
      // TODO use getters when possible; convert everything to strings (use/reuse data binding?)
      method.append(
              Stmt.loadVariable("state").invoke("add", Stmt.loadVariable("widget").loadField(paramName)));
    }
    method.append(Stmt.loadVariable("state").returnValue());
    method.finish();
  }

  private void appendPutStateMethod(AnonymousClassStructureBuilder pageImplBuilder, MetaClass pageClass, List<String> stateParamNames) {
    BlockBuilder<?> method = pageImplBuilder.publicMethod(void.class, "putState",
            Parameter.of(pageClass, "widget"),
            Parameter.of(MetaClassFactory.get(new TypeLiteral<List<String>>() {}), "state"))
            .body();

    int idx = 0;
    for (String paramName : stateParamNames) {
      // TODO use setters when possible; perform type coercion
      method.append(Stmt.loadVariable("widget").loadField(paramName).assignValue(Stmt.castTo(String.class, Stmt.loadVariable("state").invoke("get", idx))));
      idx++;
    }
    method.finish();
  }

  static List<String> parsePageUriTemplate(MetaClass pageType, String uriTemplate) {
    List<String> retval = new ArrayList<String>();
    StringBuilder name = new StringBuilder();
    int i = 0;
    while (i < uriTemplate.length() && uriTemplate.charAt(i) != '/' && uriTemplate.charAt(i) != '{') {
      name.append(uriTemplate.charAt(i));
      i++;
    }

    if (name.length() == 0) {
      name.append(pageType.getName());
    }
    retval.add(name.toString());

    StringBuilder pathParam = null;
    while (i < uriTemplate.length()) {
      char ch = uriTemplate.charAt(i);
      if (ch == '{') {
        if (pathParam != null) {
          throw new IllegalArgumentException("Found '{' inside parameter at position " + i + " of " + uriTemplate);
        }
        pathParam = new StringBuilder();
      }
      else if (ch == '}') {
        if (pathParam == null) {
          throw new IllegalArgumentException("Found '}' outside parameter at position " + i + " of " + uriTemplate);
        }
        if (pathParam.length() == 0) {
          throw new IllegalArgumentException("Found nameless parameter at position " + i + " of " + uriTemplate);
        }
        retval.add(pathParam.toString());
        pathParam = null;
      }
      else if (pathParam != null) {
        if (pathParam.length() == 0 && Character.isJavaIdentifierStart(ch)) {
          pathParam.append(ch);
        }
        else if (pathParam.length() > 0 && Character.isJavaIdentifierPart(ch)) {
          pathParam.append(ch);
        }
        else {
          throw new IllegalArgumentException("Found invalid Java identifier character '" + ch + "' in parameter name at position " + i + " of " + uriTemplate);
        }
      }
      else {
        if (ch != '/') {
          throw new IllegalArgumentException("Found unexpected character '" + ch + "' outside a parameter at position " + i + " of " + uriTemplate);
        }
      }
      i++;
    }
    if (pathParam != null) {
      throw new IllegalArgumentException("Found unterminated parameter at position " + i + " of " + uriTemplate);
    }
    return retval;
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
}
