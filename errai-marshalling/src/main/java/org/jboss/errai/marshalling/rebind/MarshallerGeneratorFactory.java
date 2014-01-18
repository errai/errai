/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.marshalling.rebind;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.util.Implementations.autoForLoop;
import static org.jboss.errai.codegen.util.Implementations.autoInitializedField;
import static org.jboss.errai.codegen.util.Implementations.implement;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;
import static org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil.getVarName;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.BlockBuilder;
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
import org.jboss.errai.config.rebind.CommonConfigAttribs;
import org.jboss.errai.config.rebind.ReachableTypes;
import org.jboss.errai.marshalling.client.api.DeferredMarshallerCreationCallback;
import org.jboss.errai.marshalling.client.api.GeneratedMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.AlwaysQualify;
import org.jboss.errai.marshalling.client.api.json.EJArray;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.marshallers.QualifyingMarshallerWrapper;
import org.jboss.errai.marshalling.rebind.api.ArrayMarshallerCallback;
import org.jboss.errai.marshalling.rebind.api.GeneratorMappingContext;
import org.jboss.errai.marshalling.rebind.api.GeneratorMappingContextFactory;
import org.jboss.errai.marshalling.rebind.api.MappingStrategy;
import org.jboss.errai.marshalling.rebind.api.MarshallingExtension;
import org.jboss.errai.marshalling.rebind.api.MarshallingExtensionConfigurator;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.shared.GWT;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MarshallerGeneratorFactory {
  public static final String MARSHALLER_NAME_PREFIX = "Marshaller_for_";
  private static final String MARSHALLERS_VAR = "marshallers";
  private final MarshallerOutputTarget target;
  private final ReachableTypes reachableTypes;

  private GeneratorMappingContext mappingContext;

  ClassStructureBuilder<?> classStructureBuilder;
  ConstructorBlockBuilder<?> constructor;
  BlockBuilder<?> getMarshallerMethod;
  Context classContext;

  private final Set<String> arrayMarshallers = new HashSet<String>();
  private final Set<String> unlazyMarshallers = new HashSet<String>();

  private static final Logger log = LoggerFactory.getLogger(MarshallerGeneratorFactory.class);

  long startTime;

  private MarshallerGeneratorFactory(final MarshallerOutputTarget target, final ReachableTypes reachableTypes) {
    this.target = target;

    this.reachableTypes = reachableTypes;
    if (reachableTypes.isBasedOnReachabilityAnalysis()) {
      this.reachableTypes.add(Object.class.getName());
      this.reachableTypes.add(Map.class.getName());
      this.reachableTypes.add(Set.class.getName());
      this.reachableTypes.add(String.class.getName());
      this.reachableTypes.add(Double.class.getName());
      this.reachableTypes.add(Long.class.getName());
      this.reachableTypes.add(Float.class.getName());
      this.reachableTypes.add(Integer.class.getName());
      this.reachableTypes.add(Short.class.getName());
      this.reachableTypes.add(List.class.getName());
      this.reachableTypes.add(Character.class.getName());
      this.reachableTypes.add(Float.class.getName());
      this.reachableTypes.add(Byte.class.getName());
      this.reachableTypes.add(Boolean.class.getName());
      this.reachableTypes.add(StackTraceElement.class.getName());

      this.reachableTypes.add("char");
      this.reachableTypes.add("long");
      this.reachableTypes.add("float");
      this.reachableTypes.add("double");
      this.reachableTypes.add("int");
      this.reachableTypes.add("boolean");
      this.reachableTypes.add("boolean");
      this.reachableTypes.add("byte");
      this.reachableTypes.add("short");
    }
  }

  public static MarshallerGeneratorFactory getFor(final MarshallerOutputTarget target) {
    return new MarshallerGeneratorFactory(target, ReachableTypes.EVERYTHING_REACHABLE_INSTANCE);
  }

  public static MarshallerGeneratorFactory getFor(final MarshallerOutputTarget target,
      final ReachableTypes reachableTypes) {
    return new MarshallerGeneratorFactory(target, reachableTypes);
  }

  public String generate(final String packageName, final String clazzName) {
    final String gen;
    log.info("generating marshaller factory class for " + ((target == MarshallerOutputTarget.GWT) ? "client" : "server"));
    final long time = System.currentTimeMillis();
    gen = _generate(packageName, clazzName);
    log.info("generated marshaller factory class in " + (System.currentTimeMillis() - time) + "ms.");
    return gen;
  }

  private String _generate(final String packageName, final String clazzName) {
    startTime = System.currentTimeMillis();

    classStructureBuilder = implement(MarshallerFactory.class, packageName, clazzName);
    classContext = classStructureBuilder.getClassDefinition().getContext();
    mappingContext = GeneratorMappingContextFactory.create(target, this,
        new ArrayMarshallerCallback() {
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
            return addArrayMarshaller(type.asBoxed());
          }

          @Override
          public Statement deferred(MetaClass type, MetaClass marshaller) {
            return
            Stmt.newObject(parameterizedAs(DeferredMarshallerCreationCallback.class, typeParametersOf(type)))
                .extend()
                .publicOverridesMethod("create", Parameter.of(Class.class, "type"))
                .append(
                    Stmt.nestedCall(
                        Stmt.newObject(QualifyingMarshallerWrapper.class,
                            Stmt.castTo(Marshaller.class, Stmt.invokeStatic(GWT.class, "create", marshaller)), type))
                        .returnValue())
                .finish()
                .finish();
          }
        });

    classStructureBuilder.getClassDefinition().addAnnotation(new Dependent() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return Dependent.class;
      }
    });

    final MetaClass javaUtilMap = MetaClassFactory.get(new TypeLiteral<Map<String, Marshaller>>() {});
    autoInitializedField(classStructureBuilder, javaUtilMap, MARSHALLERS_VAR, HashMap.class);

    for (final Class<?> extensionClass : ScannerSingleton.getOrCreateInstance().getTypesAnnotatedWith(
        MarshallingExtension.class)) {
      if (!MarshallingExtensionConfigurator.class.isAssignableFrom(extensionClass)) {
        throw new RuntimeException("class " + extensionClass.getName() + " is not a valid marshalling extension. " +
            "marshalling extensions should implement: " + MarshallingExtensionConfigurator.class.getName());
      }

      try {
        final MarshallingExtensionConfigurator configurator =
            extensionClass.asSubclass(MarshallingExtensionConfigurator.class).newInstance();

        configurator.configure(mappingContext);
      }
      catch (Exception e) {
        throw new RuntimeException("error loading marshalling extension: " + extensionClass.getName(), e);
      }
    }

    constructor = classStructureBuilder.publicConstructor();

    for (final MetaClass cls : mappingContext.getDefinitionsFactory().getExposedClasses()) {
      final String clsName = cls.getFullyQualifiedName();

      if (!mappingContext.getDefinitionsFactory().hasDefinition(clsName) || !reachable(cls)) {
        continue;
      }

      final Class<? extends Marshaller> marshallerCls = mappingContext.getDefinitionsFactory().getDefinition(clsName)
          .getClientMarshallerClass();

      if (marshallerCls == null) {
        continue;
      }

      mappingContext.markRendered(cls);

      final String varName = getVarName(clsName);

      Statement marshaller = null;
      if (marshallerCls.isAnnotationPresent(AlwaysQualify.class)) {
        MetaClass type = MetaClassFactory.parameterizedAs(QualifyingMarshallerWrapper.class,
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

    getMarshallerMethod =
        classStructureBuilder.publicMethod(parameterizedAs(Marshaller.class, typeParametersOf(Object.class)),
            "getMarshaller").parameters(String.class)
            .body()
            .append(
                If.isNull(loadVariable("a0"))
                    .append(Stmt.loadLiteral(null).returnValue()).finish())
            .append(
                If.cond(Stmt.loadVariable(MARSHALLERS_VAR).invoke("containsKey", Stmt.loadVariable("a0")))
                    .append(Stmt.loadVariable(MARSHALLERS_VAR).invoke("get", loadVariable("a0")).returnValue())
                    .finish()
            )
            .append(Stmt.declareVariable("m", Marshaller.class, Stmt.loadLiteral(null)));

    generateMarshallers();

    getMarshallerMethod.append(
        If.isNotNull(Stmt.loadVariable("m")).append(
            Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
                .invoke("put", loadVariable("a0"), loadVariable("m"))).finish());

    getMarshallerMethod.append(Stmt.loadVariable("m").returnValue()).finish();

    if (CommonConfigAttribs.MAKE_DEFAULT_ARRAY_MARSHALLERS.getBoolean()) {
      for (final MetaClass arrayType : MarshallingGenUtil.getDefaultArrayMarshallers()) {
        addArrayMarshaller(arrayType);
      }
    }

    for (final MetaClass metaClass : mappingContext.getDefinitionsFactory().getArraySignatures()) {
      addArrayMarshaller(metaClass);
    }

    classStructureBuilder.publicMethod(void.class, "registerMarshaller").parameters(String.class, Marshaller.class)
        .body()
        .append(Stmt.loadVariable(MARSHALLERS_VAR).invoke("put", Stmt.loadVariable("a0"), Stmt.loadVariable("a1")))
        .finish();

    return classStructureBuilder.toJavaString();
  }

  private void generateMarshallers() {
    final Set<MetaClass> exposed = mappingContext.getDefinitionsFactory().getExposedClasses();

    for (final MetaClass clazz : exposed) {
      mappingContext.registerGeneratedMarshaller(clazz.getFullyQualifiedName());
    }

    boolean lazyEnabled = CommonConfigAttribs.LAZY_LOAD_BUILTIN_MARSHALLERS.getBoolean();

    for (final MetaClass cls : exposed) {
      final MetaClass compType = cls.getOuterComponentType();
      final MappingDefinition definition = mappingContext.getDefinitionsFactory().getDefinition(compType);

      if (definition.getClientMarshallerClass() != null || definition.alreadyGenerated() || !reachable(compType)) {
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
    }

    constructor.finish();
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
                .implementsInterface(
                    MetaClassFactory.get(GeneratedMarshaller.class))
                .body().getClassDefinition();
      }
      else {
        final MappingStrategy strategy = MappingStrategyFactory
            .createStrategy(false, GeneratorMappingContextFactory.getFor(target), type);

        String marshallerClassName =
            MarshallerGeneratorFactory.MARSHALLER_NAME_PREFIX + MarshallingGenUtil.getVarName(type) + "Impl";

        final ClassStructureBuilder<?> marshaller = strategy.getMapper().getMarshaller(marshallerClassName);
        customMarshaller = marshaller.getClassDefinition();
      }
      classStructureBuilder.declaresInnerClass(new InnerClass(customMarshaller));
      addMarshaller(customMarshaller, type);
    }
  }

  private void addMarshaller(final BuildMetaClass marshaller, final MetaClass type) {
    final BlockBuilder<ElseBlockBuilder> conditionalGenerationBlock =
          If.objEquals(Stmt.loadVariable("a0"), Stmt.loadLiteral(type.getFullyQualifiedName()));

    if (target == MarshallerOutputTarget.GWT) {
      if (type.isAnnotationPresent(AlwaysQualify.class)) {
        conditionalGenerationBlock.append(
            Stmt.loadVariable("m").assignValue(Stmt.nestedCall(
                Stmt.newObject(QualifyingMarshallerWrapper.class,
                    Stmt.castTo(Marshaller.class, Stmt.invokeStatic(GWT.class, "create", marshaller)), type))));
      }
      else {
        conditionalGenerationBlock.append(
            Stmt.loadVariable("m").assignValue(Stmt.invokeStatic(GWT.class, "create", marshaller)));
      }
    }
    else {
      if (type.isAnnotationPresent(AlwaysQualify.class)) {
        conditionalGenerationBlock.append(
              Stmt.loadVariable("m").assignValue(Stmt.newObject(QualifyingMarshallerWrapper.class, marshaller, type)));
      }
      else {
        conditionalGenerationBlock.append(
              Stmt.loadVariable("m").assignValue(Stmt.newObject(marshaller)));
      }
    }

    for (final Map.Entry<String, String> aliasEntry : mappingContext.getDefinitionsFactory().getMappingAliases()
        .entrySet()) {

      if (aliasEntry.getValue().equals(type.getFullyQualifiedName())) {
        MetaClass aliasType = MetaClassFactory.get(aliasEntry.getKey());
        if (!mappingContext.isRendered(aliasType)) {
          addMarshaller(marshaller, aliasType);
        }
      }
    }

    getMarshallerMethod.append(conditionalGenerationBlock.finish());
  }

  private String addArrayMarshaller(final MetaClass type) {
    final String varName = getVarName(type);

    if (!arrayMarshallers.contains(varName)) {
      final String marshallerClassName = MARSHALLER_NAME_PREFIX + getVarName(type) + "_Impl";
      final InnerClass arrayMarshaller = new InnerClass(generateArrayMarshaller(type, marshallerClassName));
      classStructureBuilder.declaresInnerClass(arrayMarshaller);

      final BlockBuilder<ElseBlockBuilder> conditionalGenerationBlock =
            If.objEquals(Stmt.loadVariable("a0"), Stmt.loadLiteral(type.getFullyQualifiedName()));

      conditionalGenerationBlock.append(
            Stmt.loadVariable("m").assignValue(
                Stmt.newObject(QualifyingMarshallerWrapper.class, Stmt.newObject(arrayMarshaller.getType()), type
                    .asClass())));

      getMarshallerMethod.append(conditionalGenerationBlock.finish());
    }
    arrayMarshallers.add(varName);

    return varName;
  }

  static BuildMetaClass generateArrayMarshaller(final MetaClass arrayType, final String marshallerClassName) {
    MetaClass toMap = arrayType.getOuterComponentType();

    final int dimensions = GenUtil.getArrayDimensions(arrayType);

    final ClassStructureBuilder<?> classStructureBuilder =
        ClassBuilder.define(marshallerClassName).publicScope().
            implementsInterface(parameterizedAs(Marshaller.class, typeParametersOf(arrayType))).body();

    final MetaClass arrayOfArrayType = arrayType.asArrayOf(1);

    classStructureBuilder.publicMethod(arrayOfArrayType, "getEmptyArray")
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

    arrayDemarshallCode(toMap, dimensions, classStructureBuilder);

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

    return classStructureBuilder.getClassDefinition();
  }

  static void arrayDemarshallCode(MetaClass toMap,
                                   final int dim,
                                   final ClassStructureBuilder<?> classBuilder) {

    final Object[] dimParms = new Object[dim];
    dimParms[0] = Stmt.loadVariable("a0").invoke("size");

    final MetaClass arrayType = toMap.asArrayOf(dim);

    String marshallerVarName;
    if (DefinitionsFactorySingleton.get().shouldUseObjectMarshaller(toMap)) {
      marshallerVarName = getVarName(MetaClassFactory.get(Object.class));
      MarshallingGenUtil.ensureMarshallerFieldCreated(classBuilder, toMap, MetaClassFactory.get(Object.class));
    }
    else {
      marshallerVarName = getVarName(toMap);
      MarshallingGenUtil.ensureMarshallerFieldCreated(classBuilder, null, toMap);
    }

    final Statement demarshallerStatement = Stmt.castTo(toMap.asBoxed().asClass(),
        Stmt.loadVariable(marshallerVarName).invoke("demarshall", loadVariable("a0")
            .invoke("get", loadVariable("i")), Stmt.loadVariable("a1")));

    final Statement outerAccessorStatement =
        loadVariable("newArray", loadVariable("i"))
            .assignValue(demarshallerStatement);

    final BlockBuilder<?> dmBuilder =
        classBuilder.privateMethod(arrayType, "_demarshall" + dim)
            .parameters(EJArray.class, MarshallingSession.class).body();

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

    MarshallingGenUtil.ensureMarshallerFieldCreated(classBuilder, null, MetaClassFactory.get(Object.class));

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
      arrayDemarshallCode(toMap, dim - 1, classBuilder);
    }
  }

  public boolean reachable(final MetaClass cls) {
    if (reachableTypes.isEmpty())
      return true;

    String name = cls.getFullyQualifiedName();
    if (name.contains("$")) {
      name = name.substring(0, name.indexOf('$'));
    }

    return reachableTypes.contains(name);
  }

  public static BuildMetaClass createArrayMarshallerClass(MetaClass type) {
    BuildMetaClass arrayMarshaller =
        ClassBuilder
            .define(MARSHALLER_NAME_PREFIX + getVarName(type)).packageScope()
            .abstractClass()
            .implementsInterface(
                MetaClassFactory.get(GeneratedMarshaller.class))
            .body().getClassDefinition();

    return arrayMarshaller;
  }
}
