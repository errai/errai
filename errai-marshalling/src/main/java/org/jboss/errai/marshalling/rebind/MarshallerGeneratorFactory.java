/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.marshalling.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.shared.GWT;
import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.StringStatement;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.CaseBlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ConstructorBlockBuilder;
import org.jboss.errai.codegen.builder.ElseBlockBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.GenUtil;
import org.jboss.errai.codegen.util.If;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.common.rebind.NameUtil;
import org.jboss.errai.common.rebind.UniqueNameGenerator;
import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.config.MetaClassFinder;
import org.jboss.errai.config.rebind.EnvironmentConfigExtension;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.marshalling.apt.MarshallerAptGenerator;
import org.jboss.errai.marshalling.client.api.DeferredMarshallerCreationCallback;
import org.jboss.errai.marshalling.client.api.GeneratedMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.AlwaysQualify;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.client.api.json.EJArray;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.marshallers.QualifyingMarshallerWrapper;
import org.jboss.errai.marshalling.rebind.api.ArrayMarshallerCallback;
import org.jboss.errai.marshalling.rebind.api.CustomMapping;
import org.jboss.errai.marshalling.rebind.api.GeneratorMappingContext;
import org.jboss.errai.marshalling.rebind.api.GeneratorMappingContextFactory;
import org.jboss.errai.marshalling.rebind.api.MappingStrategy;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.enterprise.util.TypeLiteral;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toSet;
import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.util.Implementations.autoForLoop;
import static org.jboss.errai.codegen.util.Implementations.autoInitializedField;
import static org.jboss.errai.codegen.util.Implementations.implement;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;
import static org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil.getVarName;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MarshallerGeneratorFactory {

  private final class ArrayMarshallerCallbackImpl implements ArrayMarshallerCallback {
    @Override
    public Statement marshal(final MetaClass type, final Statement value) {
      createDemarshallerIfNeeded(type);
      return value;
    }

    @Override
    public Statement demarshall(final MetaClass type, final Statement value) {
      final String variable = createDemarshallerIfNeeded(type.asBoxed());
      return Stmt.loadVariable(variable).invoke("demarshall", value, Stmt.loadVariable("a1"));
    }

    private String createDemarshallerIfNeeded(final MetaClass type) {
      if (done) {
        return getVarName(type);
      }
      return addArrayMarshaller(type.asBoxed(), target == MarshallerOutputTarget.GWT);
    }

    @Override
    public Statement deferred(final MetaClass type, final MetaClass marshaller) {

      return
      Stmt.newObject(parameterizedAs(DeferredMarshallerCreationCallback.class, typeParametersOf(type)))
          .extend()
          .publicOverridesMethod("create", Parameter.of(Class.class, "type"))
          .append(
              Stmt.nestedCall(
                  Stmt.newObject(QualifyingMarshallerWrapper.class, Stmt.castTo(Marshaller.class,
                          Stmt.invokeStatic(GWT.class, "create", getMarshallerMetaClass(type, marshaller))), type))
                  .returnValue())
          .finish()
          .finish();
    }
  }

  private MetaClass getMarshallerMetaClass(final MetaClass type, final MetaClass marshaller) {

    if (!erraiConfiguration.app().isAptEnvironment()) {
      return marshaller;
    } else {
      MarshallerAptGenerator.addExposedClass(type);
    }

    return ClassBuilder.define(getMarshallerImplClassName(type, true, erraiConfiguration))
            .publicScope()
            .implementsInterface(GeneratedMarshaller.class)
            .body()
            .getClassDefinition();

  }

  public static final String MARSHALLER_NAME_PREFIX = "Marshaller_for_";
  public static final String SHORT_MARSHALLER_PREFIX = "Marshaller_";
  public static final String VERY_SHORT_MARSHALLER_PREFIX = "M";
  private static final String MARSHALLERS_VAR = "marshallers";
  private static final boolean VERY_SHORT_MARSHALLER_NAMES = Boolean.parseBoolean(System.getProperty(MarshallingGenUtil.USE_VERY_SHORT_IMPL_NAMES, "false"));
  private static final boolean SHORT_MARSHALLER_NAMES = Boolean.parseBoolean(System.getProperty(MarshallingGenUtil.USE_SHORT_IMPL_NAMES, "true"));
  private static final int MARSHALLER_HELPER_METHOD_SIZE = 500;

  private final MarshallerOutputTarget target;

  private GeneratorMappingContext mappingContext;
  private final GeneratorContext context;

  private final Map<String, Statement> putStatementsByTypeName = new LinkedHashMap<>();
  private ClassStructureBuilder<?> classStructureBuilder;
  private Context classContext;
  private boolean done;

  private final Set<String> arrayMarshallers = new HashSet<>();
  private final Set<String> unlazyMarshallers = new HashSet<>();

  private final ErraiConfiguration erraiConfiguration;
  private final MetaClassFinder metaClassFinder;

  private static final Logger log = LoggerFactory.getLogger(MarshallerGeneratorFactory.class);
  private static boolean refresh = false;

  private static final UniqueNameGenerator uniqueGenerator = new UniqueNameGenerator();
  private static final Map<String, String> leasedNamesByTypeName = new HashMap<>();

  long startTime;

  private MarshallerGeneratorFactory(final GeneratorContext context,
          final MarshallerOutputTarget target,
          final ErraiConfiguration erraiConfiguration,
          final MetaClassFinder metaClassFinder) {

    this.context = context;
    this.target = target;
    this.erraiConfiguration = erraiConfiguration;
    this.metaClassFinder = metaClassFinder;
  }

  public static MarshallerGeneratorFactory getFor(final GeneratorContext context,
          final MarshallerOutputTarget target,
          final ErraiConfiguration erraiConfiguration) {

    return getFor(context, target, erraiConfiguration, getMetaClassFinder(context));
  }

  private static MetaClassFinder getMetaClassFinder(final GeneratorContext context) {
    return a -> {

      if (ServerMarshaller.class.equals(a)) {
        final Set<Class<?>> serverMarshallers = ScannerSingleton.getOrCreateInstance().getTypesAnnotatedWith(a, true);
        if (!serverMarshallers.isEmpty()) {
          return serverMarshallers.stream().map(MetaClassFactory::getUncached).collect(toSet());
        } else {
          return MarshallingOsgiEnvironmentHelper.getOsgiEnvironmentServerMarshallers();
        }
      }

      if (CustomMapping.class.equals(a)) {
        final Set<Class<?>> customMappings = ScannerSingleton.getOrCreateInstance().getTypesAnnotatedWith(a, true);
        if (!customMappings.isEmpty()) {
          return customMappings.stream().map(MetaClassFactory::getUncached).collect(toSet());
        } else {
          return MarshallingOsgiEnvironmentHelper.getOsgiEnvironmentCustomMappings();
        }
      }

      if (EnvironmentConfigExtension.class.equals(a)) {
        return new HashSet<>(ClassScanner.getTypesAnnotatedWith(a, true));
      }

      //Portable.class
      //NonPortable.class
      //ClientMarshaller.class
      return new HashSet<>(ClassScanner.getTypesAnnotatedWith(a));
    };
  }

  public static MarshallerGeneratorFactory getFor(final GeneratorContext context,
          final MarshallerOutputTarget target,
          final ErraiConfiguration erraiConfiguration,
          final MetaClassFinder metaClassFinder) {

    return new MarshallerGeneratorFactory(context, target, erraiConfiguration, metaClassFinder);
  }

  public String generate(final String packageName, final String clazzName) {
    return generate(packageName, clazzName, MarshallerGenerationCallback.NO_OP);
  }

  public String generate(final String packageName, final String clazzName, final MarshallerGenerationCallback callback) {
    final String gen;
    log.info("generating marshaller factory class for " + (((target == MarshallerOutputTarget.GWT) ? "client" : "server") + "..."));
    final long time = System.currentTimeMillis();
    if (target == MarshallerOutputTarget.GWT && refresh) {
      DefinitionsFactorySingleton.get(erraiConfiguration, metaClassFinder).resetDefinitionsAndReload();
    }
    gen = _generate(packageName, clazzName, callback);
    log.info("generated marshaller factory class in " + (System.currentTimeMillis() - time) + "ms.");
    return gen;
  }

  private String _generate(final String packageName, final String clazzName, final MarshallerGenerationCallback callback) {
    startTime = System.currentTimeMillis();

    classStructureBuilder = implement(MarshallerFactory.class, packageName, clazzName);
    classContext = classStructureBuilder.getClassDefinition().getContext();
    mappingContext = GeneratorMappingContextFactory.create(context, target, this, new ArrayMarshallerCallbackImpl(), erraiConfiguration, metaClassFinder);

    classStructureBuilder.getClassDefinition().addAnnotation(() -> Dependent.class);

    @SuppressWarnings({ "serial", "rawtypes" })
    final MetaClass javaUtilMap = MetaClassFactory.get(new TypeLiteral<Map<String, Marshaller>>() {});
    final Class<?> mapClass = (MarshallerOutputTarget.GWT.equals(target) ? HashMap.class : ConcurrentHashMap.class);
    autoInitializedField(classStructureBuilder, javaUtilMap, MARSHALLERS_VAR, mapClass);

    final ConstructorBlockBuilder<?> constructor = classStructureBuilder.publicConstructor();

    processExposedClasses(constructor);
    constructor.finish();

    final BlockBuilder<?> getMarshallerMethod =
        classStructureBuilder.publicMethod(parameterizedAs(Marshaller.class, typeParametersOf(Object.class)),
            "getMarshaller").parameters(String.class)
            .body()
            .append(
                If.isNull(loadVariable("a0"))
                    .append(Stmt.loadLiteral(null).returnValue()).finish())
            .append(Stmt.declareVariable("m", Marshaller.class, Stmt.loadVariable(MARSHALLERS_VAR).invoke("get", loadVariable("a0"))));

    generateMarshallers(callback);

    final ElseBlockBuilder getMarshallerConditional = generateGetMarshallerHelperMethods();
    getMarshallerMethod.append(getMarshallerConditional);
    getMarshallerMethod.append(Stmt.loadLiteral(null).returnValue()).finish();

    if (erraiConfiguration.app().makeDefaultArrayMarshallers()) {
      for (final MetaClass arrayType : MarshallingGenUtil.getDefaultArrayMarshallers()) {
        addArrayMarshaller(arrayType, target == MarshallerOutputTarget.GWT);
      }
    }

    for (final MetaClass metaClass : mappingContext.getDefinitionsFactory().getArraySignatures()) {
      addArrayMarshaller(metaClass, target == MarshallerOutputTarget.GWT);
    }

    classStructureBuilder.publicMethod(void.class, "registerMarshaller").parameters(String.class, Marshaller.class)
        .body()
        .append(Stmt.loadVariable(MARSHALLERS_VAR).invoke("put", Stmt.loadVariable("a0"), Stmt.loadVariable("a1")))
        .finish();

    done = true;
    if (target == MarshallerOutputTarget.GWT) {
      refresh = true;
    }
    return classStructureBuilder.toJavaString();
  }

  private ElseBlockBuilder generateGetMarshallerHelperMethods() {
    createPutMarshallerMethod();
    BlockBuilder<ElseBlockBuilder> getMarshallerConditionalBlock = If.isNotNull(Stmt.loadVariable("m"))
        .append(Stmt.loadVariable("m").returnValue());
    int methodIndex = 0, typeIndex = 0;
    CaseBlockBuilder switchBlock = null;
    final Iterator<Entry<String, Statement>> iter = putStatementsByTypeName.entrySet().iterator();
    while (iter.hasNext()) {
      final Entry<String, Statement> entry = iter.next();
      final String typeName = entry.getKey();
      final Statement stmt = entry.getValue();

      if (typeIndex % MARSHALLER_HELPER_METHOD_SIZE == 0) {
        if (typeIndex != 0) {
          switchBlock.default_().append(Stmt.loadLiteral(false).returnValue()).finish();
          getMarshallerConditionalBlock = addLoadMarshallerMethod(getMarshallerConditionalBlock, methodIndex, switchBlock);
          methodIndex++;
        }
        switchBlock = Stmt.switch_(Stmt.loadVariable("a0"));
      }
      BlockBuilder<CaseBlockBuilder> caseBlock = switchBlock.case_(typeName);
      caseBlock
        .append(Stmt.loadVariable("this").invoke("putMarshaller", Stmt.loadVariable("a0"), stmt).returnValue())
        .finish();
      typeIndex += 1;
    }
    if (typeIndex % MARSHALLER_HELPER_METHOD_SIZE != 1) {
      switchBlock.default_().append(Stmt.loadLiteral(false).returnValue()).finish();
      getMarshallerConditionalBlock = addLoadMarshallerMethod(getMarshallerConditionalBlock, methodIndex, switchBlock);
    }

    return getMarshallerConditionalBlock.finish();
  }

  private void createPutMarshallerMethod() {
    classStructureBuilder
      .privateMethod(boolean.class, "putMarshaller", Parameter.of(String.class, "fqcn"), Parameter.of(Marshaller.class, "m"))
      .append(Stmt.loadVariable(MARSHALLERS_VAR).invoke("put", Stmt.loadVariable("fqcn"), Stmt.loadVariable("m")))
      .append(Stmt.loadLiteral(true).returnValue())
      .finish();
  }

  private BlockBuilder<ElseBlockBuilder> addLoadMarshallerMethod(
          BlockBuilder<ElseBlockBuilder> getMarshallerConditionalBlock,
          final int methodIndex, final CaseBlockBuilder switchBlock) {
    final String helperMethodName = "loadMarshaller" + methodIndex;
    /*
     * Using the StringStatement is a workaround because the following line exposes a codegen bug,
     * resulting in an OutOfScopeException for "this":
     *    Stmt.loadVariable("this").invoke(helperMethodName, Stmt.loadVariable("a0"))
     */
    getMarshallerConditionalBlock = updateGetMarshallerConditionalBlock(getMarshallerConditionalBlock, helperMethodName);
    addLoadMarshallerMethod(switchBlock, helperMethodName);
    return getMarshallerConditionalBlock;
  }

  private BlockBuilder<ElseBlockBuilder> updateGetMarshallerConditionalBlock(BlockBuilder<ElseBlockBuilder> getMarshallerConditionalBlock,
          final String helperMethodName) {
    getMarshallerConditionalBlock = getMarshallerConditionalBlock.finish().elseif_(StringStatement.of(helperMethodName + "(a0)", boolean.class))
            .append(Stmt.loadVariable(MARSHALLERS_VAR).invoke("get", Stmt.loadVariable("a0")).returnValue());
    return getMarshallerConditionalBlock;
  }

  private void addLoadMarshallerMethod(final CaseBlockBuilder switchBlock, final String helperMethodName) {
    classStructureBuilder
      .privateMethod(boolean.class, helperMethodName)
      .parameters(String.class)
      .body()
      .append(switchBlock)
      .finish();
  }

  private void processExposedClasses(final ConstructorBlockBuilder<?> constructor) {
    mappingContext
      .getDefinitionsFactory()
      .getExposedClasses()
      .stream()
      .forEachOrdered(cls -> processExposedClass(cls, constructor));
  }

  private void processExposedClass(final MetaClass cls, final ConstructorBlockBuilder<?> constructor) {
    final String clsName = cls.getFullyQualifiedName();

    if (!mappingContext.getDefinitionsFactory().hasDefinition(clsName)) {
      return;
    }

    final MappingDefinition definition = mappingContext.getDefinitionsFactory().getDefinition(clsName);
    @SuppressWarnings("rawtypes")
    final MetaClass marshallerCls = (target == MarshallerOutputTarget.GWT) ?
            definition.getClientMarshallerClass() : definition.getServerMarshallerClass();

    if (marshallerCls == null) {
      return;
    }

    mappingContext.markRendered(cls);

    final String varName = getVarName(clsName);

    Statement marshaller = null;
    if (marshallerCls.isAnnotationPresent(AlwaysQualify.class)) {
      final MetaClass type = MetaClassFactory.parameterizedAs(QualifyingMarshallerWrapper.class,
          MetaClassFactory.typeParametersOf(cls));

      marshaller = Stmt.declareFinalVariable(varName, type, Stmt.newObject(QualifyingMarshallerWrapper.class)
          .withParameters(Stmt.newObject(marshallerCls), marshallerCls));
    }
    else {
      marshaller = Stmt.declareFinalVariable(varName, marshallerCls, Stmt.newObject(marshallerCls));
    }
    constructor.append(marshaller);
    constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR).invoke("put", clsName,
        loadVariable(varName)));

    for (final Map.Entry<String, String> aliasEntry : mappingContext.getDefinitionsFactory().getMappingAliases()
        .entrySet()) {

      if (aliasEntry.getValue().equals(clsName)) {
        constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
            .invoke("put", aliasEntry.getKey(), loadVariable(varName)));
      }
    }
  }

  private void generateMarshallers(final MarshallerGenerationCallback callback) {
    final Set<MetaClass> exposed = mappingContext.getDefinitionsFactory().getExposedClasses();

    for (final MetaClass clazz : exposed) {
      mappingContext.registerGeneratedMarshaller(clazz.getFullyQualifiedName());
    }

    final boolean lazyEnabled = erraiConfiguration.app().lazyLoadBuiltinMarshallers();

    for (final MetaClass cls : exposed) {
      final MetaClass compType = cls.getOuterComponentType();
      final MappingDefinition definition = mappingContext.getDefinitionsFactory().getDefinition(compType);

      if (definition.getClientMarshallerClass() != null || definition.alreadyGenerated()) {
        continue;
      }

      if (target == MarshallerOutputTarget.Java && lazyEnabled && definition.isLazy()) {
        if (unlazyMarshallers.contains(compType.getFullyQualifiedName())) {
          definition.setLazy(false);
        }
        else {
          continue;
        }
      }

      addMarshaller(compType);
      callback.callback(compType);
    }
  }

  public void addOrMarkMarshallerUnlazy(final MetaClass type) {
    final MappingDefinition definition = mappingContext.getDefinitionsFactory().getDefinition(type);
    if (definition == null) {
      unlazyMarshallers.add(type.getFullyQualifiedName());
    }
    else if (definition.isLazy()) {
      definition.setLazy(false);
      addMarshaller(type);
    }
  }

  public void addMarshaller(final MetaClass type) {
    if (!mappingContext.isRendered(type)) {
      mappingContext.markRendered(type);
      BuildMetaClass customMarshaller = null;
      if (target == MarshallerOutputTarget.GWT) {
        customMarshaller =
            ClassBuilder
                .define(MARSHALLER_NAME_PREFIX + getVarName(type)).packageScope()
                .abstractClass()
                .implementsInterface(GeneratedMarshaller.class)
                .body().getClassDefinition();
      }
      else {
        final MappingStrategy strategy = MappingStrategyFactory
            .createStrategy(false, GeneratorMappingContextFactory.getFor(context, target), type, erraiConfiguration);

        final String marshallerClassName = getMarshallerImplClassName(type, false, erraiConfiguration);

        final ClassStructureBuilder<?> marshaller = strategy.getMapper().getMarshaller(marshallerClassName);
        customMarshaller = marshaller.getClassDefinition();
      }
      classStructureBuilder.declaresInnerClass(new InnerClass(customMarshaller));
      addMarshaller(customMarshaller, type);
    }
  }

  private void addMarshaller(final BuildMetaClass marshaller, final MetaClass type) {
    if (target == MarshallerOutputTarget.GWT) {
      if (type.isAnnotationPresent(AlwaysQualify.class)) {
        addConditionalAssignment(
            type,
            Stmt.nestedCall(
                Stmt.newObject(QualifyingMarshallerWrapper.class, Stmt.castTo(Marshaller.class,
                        Stmt.invokeStatic(GWT.class, "create", getMarshallerMetaClass(type, marshaller))), type)));
      }
      else {
        addConditionalAssignment(
            type,
            Stmt.invokeStatic(GWT.class, "create", getMarshallerMetaClass(type, marshaller)));
      }
    }
    else {
      if (type.isAnnotationPresent(AlwaysQualify.class)) {
        addConditionalAssignment(
              type,
              Stmt.newObject(QualifyingMarshallerWrapper.class, marshaller, type));
      }
      else {
        addConditionalAssignment(type, Stmt.newObject(marshaller));
      }
    }

    for (final Map.Entry<String, String> aliasEntry : mappingContext.getDefinitionsFactory().getMappingAliases()
        .entrySet()) {

      if (aliasEntry.getValue().equals(type.getFullyQualifiedName())) {
        final MetaClass aliasType = MetaClassFactory.get(aliasEntry.getKey());
        if (!mappingContext.isRendered(aliasType)) {
          addMarshaller(marshaller, aliasType);
        }
      }
    }
  }

  private String addArrayMarshaller(final MetaClass type, final boolean gwtTarget) {
    final String varName = getVarName(type);

    if (!arrayMarshallers.contains(varName)) {
      final String marshallerClassName = getMarshallerImplClassName(type, gwtTarget, erraiConfiguration);
      final BuildMetaClass arrayMarshallerType = generateArrayMarshaller(type, marshallerClassName, gwtTarget);

      if (!erraiConfiguration.app().isAptEnvironment() || !gwtTarget) {
        final InnerClass arrayMarshallerInner = new InnerClass(arrayMarshallerType);
        classStructureBuilder.declaresInnerClass(arrayMarshallerInner);
        addConditionalAssignment(type,
                Stmt.newObject(QualifyingMarshallerWrapper.class, Stmt.newObject(arrayMarshallerInner.getType()),
                        type));
      } else {
        addConditionalAssignment(type,
                Stmt.newObject(QualifyingMarshallerWrapper.class, Stmt.newObject(arrayMarshallerType), type));
      }

    }
    arrayMarshallers.add(varName);

    return varName;
  }

  public static String getMarshallerImplClassName(final MetaClass type,
          final boolean gwtTarget,
          final ErraiConfiguration erraiConfiguration) {

    String implName = leasedNamesByTypeName.get(type.getFullyQualifiedName());
    if (implName == null) {
      implName = generateMarshallerImplClassName(type, gwtTarget);
      leasedNamesByTypeName.put(type.getFullyQualifiedName(), implName);
    }

    return erraiConfiguration.app().namespace() + implName;
  }

  private static String generateMarshallerImplClassName(final MetaClass type, final boolean gwtTarget) {
    final String varName = getVarName(type);
    if (VERY_SHORT_MARSHALLER_NAMES && !gwtTarget) {
      return VERY_SHORT_MARSHALLER_PREFIX + uniqueGenerator.uniqueName(NameUtil.getShortHashString(varName));
    }
    else if (SHORT_MARSHALLER_NAMES) {
      return SHORT_MARSHALLER_PREFIX + uniqueGenerator.uniqueName(NameUtil.shortenDerivedIdentifier(varName)) + "_Impl";
    }
    else {
      return MARSHALLER_NAME_PREFIX + varName + "_Impl";
    }
  }

  BuildMetaClass generateArrayMarshaller(final MetaClass arrayType, final String marshallerClassName, final boolean gwtTarget) {
    final MetaClass toMap = arrayType.getOuterComponentType();

    final int dimensions = GenUtil.getArrayDimensions(arrayType);

    final ClassStructureBuilder<?> classStructureBuilder =
        ClassBuilder.define(marshallerClassName).publicScope().
            implementsInterface(parameterizedAs(Marshaller.class, typeParametersOf(arrayType))).body();

    BlockBuilder<?> initMethod = null;
    if (gwtTarget) {
      initMethod = classStructureBuilder.privateMethod(void.class, "lazyInit");
    }

    classStructureBuilder.publicMethod(arrayType.asArrayOf(1), "getEmptyArray")
        .append(Stmt.load(null).returnValue())
        .finish();

    final BlockBuilder<?> bBuilder = classStructureBuilder.publicMethod(arrayType, "demarshall",
        Parameter.of(EJValue.class, "a0"), Parameter.of(MarshallingSession.class, "a1"));

    bBuilder.append(
        If.isNull(loadVariable("a0"))
            .append(Stmt.load(null).returnValue())
            .finish()
            .else_()

            .append(Stmt.nestedCall(Stmt.loadVariable("this")).invoke("_demarshall" + dimensions,
                Stmt.loadVariable("a0").invoke("isArray"), loadVariable("a1")).returnValue())
            .finish());
    bBuilder.finish();

    arrayDemarshallCode(toMap, dimensions, classStructureBuilder, initMethod);

    final BlockBuilder<?> marshallMethodBlock = classStructureBuilder.publicMethod(String.class, "marshall",
        Parameter.of(toMap.asArrayOf(dimensions), "a0"), Parameter.of(MarshallingSession.class, "a1"));

    marshallMethodBlock.append(
        If.isNull(loadVariable("a0"))
            .append(Stmt.load(null).returnValue())
            .finish()
            .else_()
            .append(Stmt.nestedCall(Stmt.loadVariable("this")).invoke("_marshall" + dimensions,
                loadVariable("a0"), loadVariable("a1")).returnValue())
            .finish()
        );

    marshallMethodBlock.finish();

    if (initMethod != null) {
      initMethod.finish();
    }

    return classStructureBuilder.getClassDefinition();
  }

  void arrayDemarshallCode(final MetaClass toMap,
                                   final int dim,
                                   final ClassStructureBuilder<?> classBuilder,
                                   final BlockBuilder<?> initMethod) {

    final Object[] dimParms = new Object[dim];
    dimParms[0] = Stmt.loadVariable("a0").invoke("size");

    final MetaClass arrayType = toMap.asArrayOf(dim);

    String marshallerVarName;
    if (DefinitionsFactorySingleton.get(erraiConfiguration, metaClassFinder).shouldUseObjectMarshaller(toMap)) {
      marshallerVarName = getVarName(MetaClassFactory.get(Object.class));
      MarshallingGenUtil.ensureMarshallerFieldCreated(classBuilder, toMap, MetaClassFactory.get(Object.class), initMethod);
    }
    else {
      marshallerVarName = getVarName(toMap);
      MarshallingGenUtil.ensureMarshallerFieldCreated(classBuilder, null, toMap, initMethod);
    }

    final Statement demarshallerStatement = Stmt.castTo(toMap.asBoxed(),
        Stmt.loadVariable(marshallerVarName).invoke("demarshall", loadVariable("a0")
            .invoke("get", loadVariable("i")), Stmt.loadVariable("a1")));

    final Statement outerAccessorStatement =
        loadVariable("newArray", loadVariable("i"))
            .assignValue(demarshallerStatement);

    final BlockBuilder<?> dmBuilder =
        classBuilder.privateMethod(arrayType, "_demarshall" + dim)
            .parameters(MetaClassFactory.getUncached(EJArray.class), MetaClassFactory.getUncached(MarshallingSession.class)).body();

    if (initMethod != null) {
      dmBuilder.append(Stmt.loadVariable("this").invoke("lazyInit"));
    }

    dmBuilder.append(Stmt
        .declareVariable(arrayType).named("newArray")
        .initializeWith(Stmt.newArray(toMap, dimParms)));

    dmBuilder.append(autoForLoop("i", Stmt.loadVariable("newArray").loadField("length"))
        .append(dim == 1 ? outerAccessorStatement
            : loadVariable("newArray", loadVariable("i")).assignValue(
                Stmt.loadVariable("this").invoke(
                    "_demarshall" + (dim - 1),
                    Stmt.loadVariable("a0").invoke("get", Stmt.loadVariable("i")).invoke("isArray"),
                    Stmt.loadVariable("a1"))))

        .finish())
        .append(Stmt.loadVariable("newArray").returnValue());

    dmBuilder.finish();

    final BlockBuilder<?> mBuilder = classBuilder.privateMethod(String.class, "_marshall" + dim)
        .parameters(arrayType, MarshallingSession.class).body();

    MarshallingGenUtil.ensureMarshallerFieldCreated(classBuilder, null, MetaClassFactory.get(Object.class), initMethod);

    if (initMethod != null) {
      mBuilder.append(Stmt.loadVariable("this").invoke("lazyInit"));
    }

    mBuilder.append(Stmt.declareVariable(StringBuilder.class).named("sb")
        .initializeWith(Stmt.newObject(StringBuilder.class, "[")))
        .append(autoForLoop("i", Stmt.loadVariable("a0").loadField("length"))
            .append(Stmt.if_(Bool.greaterThan(Stmt.loadVariable("i"), 0))
                .append(Stmt.loadVariable("sb").invoke("append", ",")).finish())
            .append(Stmt.loadVariable("sb").invoke("append", dim == 1 ?
                Stmt.loadVariable(MarshallingGenUtil.getVarName(MetaClassFactory.get(Object.class)))
                    .invoke("marshall",
                        Stmt.loadVariable("a0", Stmt.loadVariable("i")),
                        Stmt.loadVariable("a1"))
                :
                Stmt.loadVariable("this").invoke(
                    "_marshall" + (dim - 1), Stmt.loadVariable("a0", Stmt.loadVariable("i")), loadVariable("a1"))))
            .finish())
        .append(Stmt.loadVariable("sb").invoke("append", "]").invoke("toString").returnValue())
        .finish();

    if (dim > 1) {
      arrayDemarshallCode(toMap, dim - 1, classBuilder, initMethod);
    }
  }

  public BuildMetaClass createArrayMarshallerClass(final MetaClass type) {
    return ClassBuilder.define(MARSHALLER_NAME_PREFIX + getVarName(type))
            .packageScope()
            .abstractClass()
            .implementsInterface(GeneratedMarshaller.class)
            .body()
            .getClassDefinition();
  }

  private void addConditionalAssignment(final MetaClass type, final Statement assignment) {
    putStatementsByTypeName.put(type.getFullyQualifiedName(), assignment);
  }
}
