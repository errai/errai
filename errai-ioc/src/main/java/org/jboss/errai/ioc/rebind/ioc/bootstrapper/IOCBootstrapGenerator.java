package org.jboss.errai.ioc.rebind.ioc.bootstrapper;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.StringSourceWriter;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.bus.server.ErraiBootstrapFailure;
import org.jboss.errai.codegen.framework.*;
import org.jboss.errai.codegen.framework.meta.*;
import org.jboss.errai.codegen.framework.util.*;
import org.jboss.errai.ioc.client.ContextualProviderContext;
import org.jboss.errai.ioc.client.InterfaceInjectionContext;
import org.jboss.errai.ioc.client.api.*;
import org.jboss.errai.ioc.rebind.AnnotationHandler;
import org.jboss.errai.ioc.rebind.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.IOCProcessorFactory;
import org.jboss.errai.ioc.rebind.ioc.*;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaClass;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * The main generator class for the Errai IOC system.
 *
 * @author Mike Brock <cbrock@redhat.com>
 */
public class IOCBootstrapGenerator {
  IOCProcessingContext procContext;
  TypeOracle typeOracle;
  GeneratorContext context;

  InjectorFactory injectFactory;
  IOCProcessorFactory procFactory;

  private String packageFilter = null;
  private boolean useReflectionStubs = false;
  private List<Runnable> deferredTasks = new LinkedList<Runnable>();

  public static final String QUALIFYING_METADATA_FACTORY_PROPERTY = "errai.ioc.QualifyingMetaDataFactory";

  TreeLogger logger = new TreeLogger() {
    @Override
    public TreeLogger branch(Type type, String msg, Throwable caught, HelpInfo helpInfo) {
      return null;
    }

    @Override
    public boolean isLoggable(Type type) {
      return false;
    }

    @Override
    public void log(Type type, String msg, Throwable caught, HelpInfo helpInfo) {
      System.out.println(type.getLabel() + ": " + msg);
      if (caught != null) {
        caught.printStackTrace();
      }
    }
  };

  public IOCBootstrapGenerator(TypeOracle typeOracle,
                               GeneratorContext context,
                               TreeLogger logger) {
    this.typeOracle = typeOracle;
    this.context = context;
    this.logger = logger;
  }

  public IOCBootstrapGenerator() {
  }

  public String generate(String packageName, String className) {
    ClassStructureBuilder<?> classStructureBuilder
            = Implementations.implement(Bootstrapper.class, packageName, className);

    BuildMetaClass bootStrapClass = classStructureBuilder.getClassDefinition();
    Context buildContext = bootStrapClass.getContext();

    BlockBuilder<?> blockBuilder =
            classStructureBuilder.publicMethod(InterfaceInjectionContext.class, "bootstrapContainer");

    SourceWriter sourceWriter = new StringSourceWriter();

    procContext = new IOCProcessingContext(logger, context, sourceWriter,
            typeOracle, buildContext, bootStrapClass, blockBuilder);

    injectFactory = new InjectorFactory(procContext);
    procFactory = new IOCProcessorFactory(injectFactory);

    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();
    Properties props = scanner.getProperties("ErraiApp.properties");
    if (props != null) {
      logger.log(TreeLogger.Type.INFO, "Checking ErraiApp.properties for configured types ...");

      for (Object o : props.keySet()) {
        String key = (String) o;
        if (key.equals(QUALIFYING_METADATA_FACTORY_PROPERTY)) {
          String fqcnQualifyingMetadataFactory = String.valueOf(props.get(key));

          try {
            QualifyingMetadataFactory factory = (QualifyingMetadataFactory)
                    Class.forName
                            (fqcnQualifyingMetadataFactory).newInstance();

            procContext.setQualifyingMetadataFactory(factory);
          }
          catch (ClassNotFoundException e) {
            e.printStackTrace();
          }
          catch (InstantiationException e) {
            e.printStackTrace();
          }
          catch (IllegalAccessException e) {
            e.printStackTrace();
          }
        }
      }
    }

    procContext.setPackageFilter(packageFilter);

    defaultConfigureProcessor();

    // generator constructor source code
    initializeProviders();
    generateExtensions(sourceWriter, classStructureBuilder, blockBuilder);
    // close generated class

    return sourceWriter.toString();
  }


  private void generateExtensions(SourceWriter sourceWriter, ClassStructureBuilder<?> classBuilder,
                                  BlockBuilder<?> blockBuilder) {
    blockBuilder.append(Stmt.declareVariable("ctx", InterfaceInjectionContext.class,
            Stmt.newObject(InterfaceInjectionContext.class)));
    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();

    procFactory.process(scanner, procContext);
    procFactory.processAll();

    runAllDeferred();

    blockBuilder.append(Stmt.loadVariable("ctx").returnValue());

    Collection<MetaField> privateFields = injectFactory.getInjectionContext().getPrivateFieldsToExpose();
    for (MetaField f : privateFields) {
      GenUtil.addPrivateAccessStubs(!useReflectionStubs, classBuilder, f, f.getType());
    }

    Collection<MetaMethod> privateMethods = injectFactory.getInjectionContext().getPrivateMethodsToExpose();

    for (MetaMethod m : privateMethods) {
      GenUtil.addPrivateAccessStubs(!useReflectionStubs, classBuilder, m);
    }

    blockBuilder.finish();

    String generated = classBuilder.toJavaString();

    if (Boolean.getBoolean("errai.ioc.generator.print_out_result")) {
      System.out.println("----Emitting Class--->\n\n");
      System.out.println(generated);
      System.out.println("<---Emitting Class----");
    }
    else {
      System.out.println("not printing results...");
    }

    sourceWriter.print(generated);
  }


  public void addDeferred(Runnable task) {
    deferredTasks.add(task);
  }

  private void runAllDeferred() {
    injectFactory.getInjectionContext().runAllDeferred();

    for (Runnable r : deferredTasks)
      r.run();
  }

  public void addType(final MetaClass type) {
    injectFactory.addType(type);
  }

  public Statement generateWithSingletonSemantics(final MetaClass visit) {
    return injectFactory.generateSingleton(visit);
  }

  public Statement generateInjectors(final MetaClass visit) {
    return injectFactory.generate(visit);
  }

  public String generateAllProviders() {
    return injectFactory.generateAllProviders();
  }

  public void initializeProviders() {

    final MetaClass typeProviderCls = MetaClassFactory.get(TypeProvider.class);
    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();
    /*
    * IOCDecoratorExtension.class
    */
    Set<Class<?>> iocExtensions = scanner
            .getTypesAnnotatedWith(org.jboss.errai.ioc.client.api.IOCExtension.class);
    List<IOCExtensionConfigurator> extensionConfigurators = new ArrayList<IOCExtensionConfigurator>();
    for (Class<?> clazz : iocExtensions) {
      try {
        Class<? extends IOCExtensionConfigurator> configuratorClass = clazz.asSubclass(IOCExtensionConfigurator.class);

        IOCExtensionConfigurator configurator = configuratorClass.newInstance();

        configurator.configure(procContext, injectFactory, procFactory);

        extensionConfigurators.add(configurator);
      }
      catch (Exception e) {
        throw new ErraiBootstrapFailure("unable to load IOC Extension Configurator: " + e.getMessage(), e);
      }
    }

    /**
     * CodeDecorator.class
     */
    Set<Class<?>> decorators = scanner.getTypesAnnotatedWith(CodeDecorator.class);
    for (Class<?> clazz : decorators) {
      try {
        Class<? extends IOCDecoratorExtension> decoratorClass = clazz.asSubclass(IOCDecoratorExtension.class);

        Class<? extends Annotation> annoType = null;
        Type t = decoratorClass.getGenericSuperclass();
        if (!(t instanceof ParameterizedType)) {
          throw new ErraiBootstrapFailure("code decorator must extend IOCDecoratorExtension<@AnnotationType>");
        }

        ParameterizedType pType = (ParameterizedType) t;
        if (IOCDecoratorExtension.class.equals(pType.getRawType())) {
          if (pType.getActualTypeArguments().length == 0
                  || !Annotation.class.isAssignableFrom((Class) pType.getActualTypeArguments()[0])) {
            throw new ErraiBootstrapFailure("code decorator must extend IOCDecoratorExtension<@AnnotationType>");
          }

          //noinspection unchecked
          annoType = ((Class) pType.getActualTypeArguments()[0]).asSubclass(Annotation.class);
        }

        injectFactory.getInjectionContext().registerDecorator(
                decoratorClass.getConstructor(new Class[]{Class.class}).newInstance(annoType));
      }
      catch (Exception e) {
        throw new ErraiBootstrapFailure("unable to load code decorator: " + e.getMessage(), e);
      }
    }

    /**
     * IOCProvider.class
     */
    Set<Class<?>> providers = scanner.getTypesAnnotatedWith(IOCProvider.class);
    for (Class<?> clazz : providers) {
      MetaClass bindType = null;
      MetaClass type = MetaClassFactory.get(clazz);

      boolean contextual = false;
      for (MetaClass iface : type.getInterfaces()) {
        if (iface.getFullyQualifiedName().equals(Provider.class.getName())) {
          injectFactory.addType(type);

          MetaParameterizedType pType = iface.getParameterizedType();
          MetaType typeParm = pType.getTypeParameters()[0];
          if (typeParm instanceof MetaParameterizedType) {
            bindType = (MetaClass) ((MetaParameterizedType) typeParm).getRawType();
          }
          else {
            bindType = (MetaClass) pType.getTypeParameters()[0];
          }

          boolean isContextual = false;
          for (MetaField field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)
                    && field.getType().isAssignableTo(ContextualProviderContext.class)) {

              isContextual = true;
              break;
            }
          }

          if (isContextual) {
            injectFactory.addInjector(new ContextualProviderInjector(bindType, type, procContext));
          }
          else {
            injectFactory.addInjector(new ProviderInjector(bindType, type, procContext));
          }
          break;
        }

        if (iface.getFullyQualifiedName().equals(ContextualTypeProvider.class.getName())) {
          contextual = true;

          MetaParameterizedType pType = iface.getParameterizedType();

          if (pType == null) {
            throw new InjectionFailure("could not determine the bind type for the IOCProvider class: "
                    + type.getFullyQualifiedName());
          }

          //todo: check for nested type parameters
          MetaType typeParm = pType.getTypeParameters()[0];
          if (typeParm instanceof MetaParameterizedType) {
            bindType = (MetaClass) ((MetaParameterizedType) typeParm).getRawType();
          }
          else {
            bindType = (MetaClass) pType.getTypeParameters()[0];
          }
          break;
        }
      }

      if (bindType == null) {
        for (MetaClass iface : type.getInterfaces()) {
          if (!typeProviderCls.isAssignableFrom(iface)) {
            continue;
          }

          MetaParameterizedType pType = iface.getParameterizedType();

          if (pType == null) {
            throw new InjectionFailure("could not determine the bind type for the IOCProvider class: "
                    + type.getFullyQualifiedName());
          }

          //todo: check for nested type parameters
          bindType = (MetaClass) pType.getTypeParameters()[0];
        }
      }

      if (bindType == null) {
        throw new InjectionFailure("the annotated provider class does not appear to implement " +
                TypeProvider.class.getName() + ": " + type.getFullyQualifiedName());
      }

      final MetaClass finalBindType = bindType;

      Injector injector;
      if (contextual) {
        injector = new ContextualProviderInjector(finalBindType, type, procContext);
      }
      else {
        injector = new ProviderInjector(finalBindType, type, procContext);
      }

      injectFactory.addInjector(injector);
    }

    /**
     * GeneratedBy.class
     */
    Set<Class<?>> generatedBys = scanner.getTypesAnnotatedWith(GeneratedBy.class);
    for (Class<?> clazz : generatedBys) {
      MetaClass type = MetaClassFactory.get(typeOracle, clazz);
      GeneratedBy anno = type.getAnnotation(GeneratedBy.class);
      Class<? extends ContextualTypeProvider> injectorClass = anno.value();

      try {
        injectFactory.addInjector(new ContextualProviderInjector(type, MetaClassFactory.get(injectorClass), procContext));
      }
      catch (Exception e) {
        throw new ErraiBootstrapFailure("could not load injector: " + e.getMessage(), e);
      }
    }

    for (IOCExtensionConfigurator extensionConfigurator : extensionConfigurators) {
      extensionConfigurator.afterInitialization(procContext, injectFactory, procFactory);
    }
  }

  private void defaultConfigureProcessor() {
    final MetaClass widgetType = MetaClassFactory.get(Widget.class);

    procFactory.registerHandler(EntryPoint.class, new AnnotationHandler<EntryPoint>() {
      @Override
      public boolean handle(final InjectableInstance type, EntryPoint annotation, IOCProcessingContext context) {
        generateWithSingletonSemantics(type.getType());
        return true;
      }
    });

    procFactory.registerHandler(ToRootPanel.class, new AnnotationHandler<ToRootPanel>() {
      @Override
      public boolean handle(final InjectableInstance type, final ToRootPanel annotation,
                            final IOCProcessingContext context) {
        if (widgetType.isAssignableFrom(type.getType())) {

          addDeferred(new Runnable() {
            @Override
            public void run() {
              context.getWriter().println("ctx.addToRootPanel(" + generateWithSingletonSemantics(type.getType()) + ");");
            }
          });
        }
        else {
          throw new InjectionFailure("type declares @" + annotation.getClass().getSimpleName()
                  + "  but does not extend type Widget: " + type.getType().getFullyQualifiedName());
        }

        return true;
      }
    });

    procFactory.registerHandler(CreatePanel.class, new AnnotationHandler<CreatePanel>() {
      @Override
      public boolean handle(final InjectableInstance type, final CreatePanel annotation,
                            final IOCProcessingContext context) {
        if (widgetType.isAssignableFrom(type.getType())) {

          addDeferred(new Runnable() {
            @Override
            public void run() {
              context.getWriter().println("ctx.registerPanel(\"" + (annotation.value().equals("")
                      ? type.getType().getName() : annotation.value()) + "\", " + generateInjectors(type.getType()) + ");");
            }
          });
        }
        else {
          throw new InjectionFailure("type declares @" + annotation.getClass().getSimpleName()
                  + "  but does not extend type Widget: " + type.getType().getFullyQualifiedName());
        }
        return true;
      }
    });

    procFactory.registerHandler(ToPanel.class, new AnnotationHandler<ToPanel>() {
      @Override
      public boolean handle(final InjectableInstance type, final ToPanel annotation,
                            final IOCProcessingContext context) {
        if (widgetType.isAssignableFrom(type.getType())) {

          addDeferred(new Runnable() {
            @Override
            public void run() {
              context.getWriter()
                      .println("ctx.widgetToPanel(" + generateWithSingletonSemantics(type.getType())
                              + ", \"" + annotation.value() + "\");");
            }
          });
        }
        else {
          throw new InjectionFailure("type declares @" + annotation.getClass().getSimpleName()
                  + "  but does not extend type Widget: " + type.getType().getFullyQualifiedName());
        }
        return true;
      }
    });
  }

  public void setUseReflectionStubs(boolean useReflectionStubs) {
    this.useReflectionStubs = useReflectionStubs;
  }

  public void setPackageFilter(String packageFilter) {
    this.packageFilter = packageFilter;
  }
}
