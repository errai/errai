package org.jboss.errai.ui.nav.rebind;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ConstructorBlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.gwt.GWTUtil;
import org.jboss.errai.codegen.util.Implementations;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.spi.NavigationGraph;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;

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

    final Collection<MetaClass> defaultPages = ClassScanner.getTypesAnnotatedWith(DefaultPage.class);
    if (defaultPages.size() == 0) {
      throw new GenerationException(
              "No @Page classes are annotated with @DefaultPage. Exactly one page class" +
              " must have the @DefaultPage annotation.");
    }
    if (defaultPages.size() > 1) {
      StringBuilder defaultPageList = new StringBuilder();
      for (MetaClass mc : defaultPages) {
        defaultPageList.append("\n  ").append(mc.getFullyQualifiedName());
      }
      throw new GenerationException(
              "Found more than one @DefaultPage: " + defaultPageList +
              "\nExactly one page class must have the @DefaultPage annotation.");
    }
    MetaClass defaultPageClass = defaultPages.iterator().next();
    if (defaultPageClass.getAnnotation(Page.class) == null) {
      throw new GenerationException(
              "Class " + defaultPageClass + " has the @DefaultPage annotation but not the @Page annotation." +
              " The default page must be a page.");
    }

    ConstructorBlockBuilder<?> ctor = classBuilder.publicConstructor();
    final Collection<MetaClass> pages = ClassScanner.getTypesAnnotatedWith(Page.class);
    for (MetaClass pageClass : pages) {
      Page annotation = pageClass.getAnnotation(Page.class);
      List<String> template = parsePageUriTemplate(pageClass, annotation.value());
      String pageName = template.get(0);
      Statement pageImplStmt = generateNewInstanceOfPageImpl(pageClass, pageName);
      if (pageClass == defaultPageClass) {
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
                " page annotated with @DefaultPage is permitted to have an empty path.");
      }
      ctor.append(
              Stmt.nestedCall(Refs.get("pagesByName"))
              .invoke("put", pageName, pageImplStmt));
    }
    ctor.finish();

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
   */
  private ObjectBuilder generateNewInstanceOfPageImpl(MetaClass pageClass, String pageName) {
    return ObjectBuilder.newInstanceOf(PageNode.class).extend()
        .publicMethod(String.class, "name")
            .append(Stmt.loadLiteral(pageName).returnValue()).finish()
        .publicMethod(Class.class, "contentType")
            .append(Stmt.loadLiteral(pageClass).returnValue()).finish()
        .publicMethod(Widget.class, "content")
            .append(Stmt.nestedCall(Refs.get("bm"))
                    .invoke("lookupBean", Stmt.loadLiteral(pageClass)).invoke("getInstance").returnValue()).finish()
        .finish();
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
}
