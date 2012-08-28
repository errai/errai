package org.jboss.errai.ui.nav.rebind;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jboss.errai.codegen.StringStatement;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ConstructorBlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.impl.gwt.GWTUtil;
import org.jboss.errai.codegen.util.Implementations;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.spi.NavigationGraph;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.user.client.ui.Widget;

public class NavigationGraphGenerator extends Generator {

  @Override
  public String generate(TreeLogger logger, GeneratorContext context,
          String typeName) throws UnableToCompleteException {
    GWTUtil.populateMetaClassFactoryFromTypeOracle(context, logger);

    final ClassStructureBuilder<?> classBuilder =
            Implementations.extend(NavigationGraph.class, "GeneratedNavigationGraph");

    ConstructorBlockBuilder<?> ctor = classBuilder.publicConstructor();
    final Collection<MetaClass> pages = ClassScanner.getTypesAnnotatedWith(Page.class);
    for (MetaClass pageClass : pages) {
      Page annotation = pageClass.getAnnotation(Page.class);
      List<String> template = parsePageUriTemplate(pageClass, annotation.value());
      String pageName = template.get(0);
      ctor.append(
              Stmt.nestedCall(new StringStatement("pagesByName", MetaClassFactory.get(Map.class)))
              .invoke("put", pageName, ObjectBuilder.newInstanceOf(PageNode.class).extend()
                  .publicMethod(String.class, "name")
                      .append(Stmt.loadLiteral(pageName).returnValue()).finish()
                  .publicMethod(Class.class, "contentType")
                      .append(Stmt.loadLiteral(pageClass).returnValue()).finish()
                  .publicMethod(Widget.class, "content")
                      .append(Stmt.nestedCall(new StringStatement("bm", MetaClassFactory.get(IOCBeanManager.class)))
                              .invoke("lookupBean", Stmt.loadLiteral(pageClass)).invoke("getInstance").returnValue()).finish()
                  .finish()
              ));
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
