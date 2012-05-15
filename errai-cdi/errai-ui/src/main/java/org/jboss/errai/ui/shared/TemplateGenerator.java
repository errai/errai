package org.jboss.errai.ui.shared;

import java.io.File;
import java.io.PrintWriter;

import org.jboss.errai.bus.client.framework.ProxyProvider;
import org.jboss.errai.bus.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.dev.resource.Resource;
import com.google.gwt.dev.resource.ResourceOracle;
import com.google.gwt.dev.util.Util;
import com.google.gwt.uibinder.rebind.DesignTimeUtils;
import com.google.gwt.uibinder.rebind.MortalLogger;
import com.google.gwt.uibinder.rebind.W3cDomHelper;

public class TemplateGenerator extends Generator {
  private Logger log = LoggerFactory.getLogger(TemplateGenerator.class);

  @Override
  public String generate(TreeLogger logger, GeneratorContext context,
          String typeName) throws UnableToCompleteException {
    String packageName = null;
    String className = null;

    try {
      JClassType classType = context.getTypeOracle().getType(typeName);

      if (classType.isInterface() == null) {
        logger.log(TreeLogger.ERROR, typeName + "is not an interface.");
        throw new RuntimeException("invalid type: not an interface");
      }

      packageName = classType.getPackage().getName();
      className = classType.getSimpleSourceName() + "Impl";

      PrintWriter printWriter = context.tryCreate(logger, packageName,
              className);
      // If code has not already been generated.
      if (printWriter != null) {
        printWriter.append(generate(context, logger, className));
        context.commit(logger, printWriter);
      }
    } catch (Throwable e) {
      logger.log(TreeLogger.ERROR, "Error generating JAX-RS extensions", e);
    }

    return packageName + "." + className;
  }
  
  
  
  
  


  private Document getW3cDoc(MortalLogger logger, DesignTimeUtils designTime,
                             ResourceOracle resourceOracle, String templatePath)
          throws UnableToCompleteException {

    Resource resource = resourceOracle.getResourceMap().get(templatePath);
    if (null == resource) {
      logger.die("Unable to find resource: " + templatePath);
    }

    Document doc = null;
    try {
      String content = designTime.getTemplateContent(templatePath);
      if (content == null) {
        content = Util.readStreamAsString(resource.openContents());
      }
      doc = new W3cDomHelper(logger.getTreeLogger(), resourceOracle).documentFor(
              content, resource.getPath());
    }
    catch (SAXParseException e) {
      logger.die(
              "Error parsing XML (line " + e.getLineNumber() + "): "
                      + e.getMessage(), e);
    }
    return doc;
  }
  
  
  
  
  
  
  
  
  

  private String generate(final GeneratorContext context,
          final TreeLogger logger, final String className) {
    File fileCacheDir = RebindUtils.getErraiCacheDir();
    File cacheFile = new File(fileCacheDir.getAbsolutePath() + "/" + className
            + ".java");

    String gen;
    if (RebindUtils.hasClasspathChangedForAnnotatedWith(Insert.class)
            || !cacheFile.exists()) {
      log.info("generating jax-rs proxy loader class.");
      gen = generate(context, logger);
      RebindUtils.writeStringToFile(cacheFile, gen);
    }
    else {
      log.info("nothing has changed. using cached jax-rs proxy loader class.");
      gen = RebindUtils.readFileToString(cacheFile);
    }

    return gen;
  }

  private String generate(final GeneratorContext context,
          final TreeLogger logger) {
    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();
    ClassStructureBuilder<?> classBuilder = ClassBuilder
            .implement(Template.class);

    MethodBlockBuilder<?> loadProxies = classBuilder.publicMethod(void.class,
            "loadProxies");
    for (Class<?> remote : scanner.getTypesAnnotatedWith(Insert.class,
            RebindUtils.findTranslatablePackages(context))) {
      if (remote.isInterface()) {
        // create the remote proxy for this interface
        ClassStructureBuilder<?> remoteProxy = null; // new
                                                     // JaxrsProxyGenerator(remote).generate();
        loadProxies.append(new InnerClass((BuildMetaClass) remoteProxy
                .getClassDefinition()));

        // create the proxy provider
        Statement proxyProvider = ObjectBuilder
                .newInstanceOf(ProxyProvider.class)
                .extend()
                .publicOverridesMethod("getProxy")
                .append(Stmt.nestedCall(
                        Stmt.newObject(remoteProxy.getClassDefinition()))
                        .returnValue()).finish().finish();

        loadProxies.append(Stmt.invokeStatic(RemoteServiceProxyFactory.class,
                "addRemoteProxy", remote, proxyProvider));
      }
    }
    classBuilder = (ClassStructureBuilder<?>) loadProxies.finish();

    String out = classBuilder.toJavaString();

    if (Boolean.getBoolean("errai.codegen.printOut")) {
      System.out.println("---JAX-RS Proxy-->");
      System.out.println(out);
      System.out.println("<--JAX-RS Proxy---");
    }
    return out;
  }

}
