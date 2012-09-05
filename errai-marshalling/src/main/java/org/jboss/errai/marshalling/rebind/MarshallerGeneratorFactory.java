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

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ConstructorBlockBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.GenUtil;
import org.jboss.errai.codegen.util.If;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.config.rebind.ReachableTypes;
import org.jboss.errai.config.util.ThreadUtil;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.AlwaysQualify;
import org.jboss.errai.marshalling.client.api.json.EJArray;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.marshallers.QualifyingMarshallerWrapper;
import org.jboss.errai.marshalling.rebind.api.ArrayMarshallerCallback;
import org.jboss.errai.marshalling.rebind.api.GeneratorMappingContext;
import org.jboss.errai.marshalling.rebind.api.MappingStrategy;
import org.jboss.errai.marshalling.rebind.api.MarshallingExtension;
import org.jboss.errai.marshalling.rebind.api.MarshallingExtensionConfigurator;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallerGeneratorFactory {
  private static final String MARSHALLERS_VAR = "marshallers";
  private final MarshallerOutputTarget target;
  private final ReachableTypes reachableTypes;

  private GeneratorMappingContext mappingContext;

  ClassStructureBuilder<?> classStructureBuilder;
  ConstructorBlockBuilder<?> constructor;
  Context classContext;

  private final Set<String> arrayMarshallers = new HashSet<String>();

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

  public static MarshallerGeneratorFactory getFor(final MarshallerOutputTarget target, final ReachableTypes reachableTypes) {
    return new MarshallerGeneratorFactory(target, reachableTypes);
  }

  public String generate(final String packageName, final String clazzName) {
    final File fileCacheDir = RebindUtils.getErraiCacheDir();
    final File cacheFile = new File(fileCacheDir.getAbsolutePath() + "/" + clazzName + ".java");

    final String gen;

    log.info("generating marshalling class...");
    final long time = System.currentTimeMillis();
    gen = _generate(packageName, clazzName);
    log.info("generated marshalling class in " + (System.currentTimeMillis() - time) + "ms.");

    if (Boolean.getBoolean("errai.codegen.printOut")) {
      System.out.println(gen);
    }

    ThreadUtil.execute(new Runnable() {
      @Override
      public void run() {
        RebindUtils.writeStringToFile(cacheFile, gen);
      }
    });


    return gen;
  }

  private String _generate(final String packageName, final String clazzName) {
    startTime = System.currentTimeMillis();

    classStructureBuilder = implement(MarshallerFactory.class, packageName, clazzName);
    classContext = classStructureBuilder.getClassDefinition().getContext();
    mappingContext = new GeneratorMappingContext(this, classContext, classStructureBuilder.getClassDefinition(),
        classStructureBuilder, new ArrayMarshallerCallback() {
      @Override
      public Statement marshal(final MetaClass type, final Statement value) {
        createDemarshallerIfNeeded(type);
        return value;
      }

      @Override
      public Statement demarshall(final MetaClass type, final Statement value) {
        final String variable = createDemarshallerIfNeeded(type);

        return Stmt.loadVariable(variable).invoke("demarshall", value, Stmt.loadVariable("a1"));
      }

      private String createDemarshallerIfNeeded(final MetaClass type) {
        return addArrayMarshaller(type);
      }
    });

    classStructureBuilder.getClassDefinition().addAnnotation(new Dependent() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return Dependent.class;
      }
    });

    final MetaClass javaUtilMap = MetaClassFactory.get(
        new TypeLiteral<Map<String, Marshaller>>() {
        }
    );

    autoInitializedField(classStructureBuilder, javaUtilMap, MARSHALLERS_VAR, HashMap.class);

    for (final Class<?> extensionClass :
        ScannerSingleton.getOrCreateInstance().getTypesAnnotatedWith(MarshallingExtension.class)) {
      if (!MarshallingExtensionConfigurator.class.isAssignableFrom(extensionClass)) {
        throw new RuntimeException("class " + extensionClass.getName() + " is not a valid marshalling extension. " +
            "marshalling extensions should implement: " + MarshallingExtensionConfigurator.class.getName());
      }

      try {
        final MarshallingExtensionConfigurator configurator
            = extensionClass.asSubclass(MarshallingExtensionConfigurator.class).newInstance();

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

      if (marshallerCls.isAnnotationPresent(AlwaysQualify.class)) {
        classStructureBuilder.privateField(varName,
            MetaClassFactory.parameterizedAs(QualifyingMarshallerWrapper.class,
                MetaClassFactory.typeParametersOf(cls)))
            .finish();

        constructor.append(Stmt.create(classContext)
            .loadVariable(varName).assignValue(
                Stmt.newObject(QualifyingMarshallerWrapper.class)
                    .withParameters(Stmt.newObject(marshallerCls))));
      }
      else {
        classStructureBuilder.privateField(varName, marshallerCls).finish();

        constructor.append(Stmt.create(classContext)
            .loadVariable(varName).assignValue(Stmt.newObject(marshallerCls)));
      }

      constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
          .invoke("put", clsName, loadVariable(varName)));

      for (final Map.Entry<String, String> aliasEntry :
          mappingContext.getDefinitionsFactory().getMappingAliases().entrySet()) {

        if (aliasEntry.getValue().equals(clsName)) {
          constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
              .invoke("put", aliasEntry.getKey(), loadVariable(varName)));
        }
      }
    }

    generateMarshallers();

    classStructureBuilder.publicMethod(parameterizedAs(Marshaller.class, typeParametersOf(Object.class)),
        "getMarshaller").parameters(String.class, String.class)
        .body()
        .append(loadVariable(MARSHALLERS_VAR).invoke("get", loadVariable("a1")).returnValue())
        .finish();

    for (final MetaClass arrayType : MarshallingGenUtil.getDefaultArrayMarshallers()) {
      addArrayMarshaller(arrayType);
    }

    return classStructureBuilder.toJavaString();
  }

  private void generateMarshallers() {
    final Set<MetaClass> exposed = mappingContext.getDefinitionsFactory().getExposedClasses();

    for (final MetaClass clazz : exposed) {
      mappingContext.registerGeneratedMarshaller(clazz.getFullyQualifiedName());
    }

    for (final MetaClass clazz : exposed) {
      final MappingDefinition definition = mappingContext.getDefinitionsFactory().getDefinition(clazz);
      if (definition.getClientMarshallerClass() != null || definition.alreadyGenerated() || !reachable(clazz)) {
        continue;
      }

      addMarshaller(clazz);
    }

    constructor.finish();
  }

  public void addMarshaller(final MetaClass type) {
    if (!mappingContext.isRendered(type)) {
      mappingContext.markRendered(type);
      final MappingStrategy strategy = MappingStrategyFactory
          .createStrategy(target == MarshallerOutputTarget.GWT, mappingContext, type);
      if (strategy == null) {
        throw new RuntimeException("no available marshaller for class: " + type.getFullyQualifiedName());
      }

      final String varName = getVarName(type);

      final Statement marshaller = strategy.getMapper().getMarshaller();
      classStructureBuilder.privateField(varName, marshaller.getType()).finish();

      if (type.isAnnotationPresent(AlwaysQualify.class)) {
        constructor.append(loadVariable(varName).assignValue(
            Stmt.newObject(QualifyingMarshallerWrapper.class, marshaller)));
      }
      else {
        constructor.append(loadVariable(varName).assignValue(marshaller));
      }

      constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
          .invoke("put", type.getFullyQualifiedName(), loadVariable(varName)));

      if (!type.getFullyQualifiedName().equals(type.getCanonicalName())) {
        constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
            .invoke("put", type.getCanonicalName(), loadVariable(varName)));
      }

      for (final Map.Entry<String, String> aliasEntry :
          mappingContext.getDefinitionsFactory().getMappingAliases().entrySet()) {

        if (aliasEntry.getValue().equals(type.getFullyQualifiedName())) {
          constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
              .invoke("put", aliasEntry.getKey(), loadVariable(varName)));
        }
      }
    }
  }

  private String addArrayMarshaller(final MetaClass type) {
    final String varName = getVarName(type);

    if (!arrayMarshallers.contains(varName)) {
      final Statement marshaller = generateArrayMarshaller(type);

      classStructureBuilder.privateField(varName,
          MetaClassFactory.parameterizedAs(QualifyingMarshallerWrapper.class,
              MetaClassFactory.typeParametersOf(type)))
          .finish();

      constructor.append(loadVariable(varName).assignValue(
          Stmt.newObject(QualifyingMarshallerWrapper.class, marshaller)));

      constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
          .invoke("put", type.getFullyQualifiedName(), loadVariable(varName)));

      arrayMarshallers.add(varName);
    }

    return varName;
  }

  private Statement generateArrayMarshaller(final MetaClass arrayType) {
    MetaClass toMap = arrayType;
    while (toMap.isArray()) {
      toMap = toMap.getComponentType();
    }

    final int dimensions = GenUtil.getArrayDimensions(arrayType);

    final AnonymousClassStructureBuilder classStructureBuilder
        = Stmt.create(mappingContext.getCodegenContext())
        .newObject(parameterizedAs(Marshaller.class, typeParametersOf(arrayType))).extend();

    classStructureBuilder.publicOverridesMethod("getTypeHandled")
        .append(Stmt.load(toMap).returnValue())
        .finish();

    final MetaClass arrayOfArrayType = arrayType.asArrayOf(1);

    classStructureBuilder.publicMethod(arrayOfArrayType, "getEmptyArray")
        .append(Stmt.throw_(UnsupportedOperationException.class, "Not implemented!"))
        .finish();

    final BlockBuilder<?> bBuilder = classStructureBuilder.publicOverridesMethod("demarshall",
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

    final BlockBuilder<?> marshallMethodBlock = classStructureBuilder.publicOverridesMethod("marshall",
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

    return classStructureBuilder.finish();
  }

  private void arrayDemarshallCode(final MetaClass toMap,
                                   final int dim,
                                   final AnonymousClassStructureBuilder anonBuilder) {

    final Object[] dimParms = new Object[dim];
    dimParms[0] = Stmt.loadVariable("a0").invoke("size");

    final MetaClass arrayType = toMap.asArrayOf(dim);

    MetaClass outerType = toMap.getOuterComponentType();
    if (!outerType.isArray() && outerType.isPrimitive()) {
      outerType = outerType.asBoxed();
    }

    String marshallerVarName;
    if (DefinitionsFactorySingleton.get().shouldUseObjectMarshaller(toMap)) {
      marshallerVarName = getVarName(MetaClassFactory.get(Object.class));
    }
    else {
      marshallerVarName = getVarName(toMap);
    }

    final Statement demarshallerStatement = Stmt.castTo(toMap.asBoxed().asClass(),
        Stmt.loadVariable(marshallerVarName).invoke("demarshall", loadVariable("a0")
            .invoke("get", loadVariable("i")), Stmt.loadVariable("a1")));

    final Statement outerAccessorStatement =
        loadVariable("newArray", loadVariable("i"))
            .assignValue(demarshallerStatement);


    final BlockBuilder<?> dmBuilder =
        anonBuilder.privateMethod(arrayType, "_demarshall" + dim)
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

    final BlockBuilder<?> mBuilder = anonBuilder.privateMethod(String.class, "_marshall" + dim)
        .parameters(arrayType, MarshallingSession.class).body();

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
      arrayDemarshallCode(toMap, dim - 1, anonBuilder);
    }
  }

  public boolean reachable(final MetaClass cls) {
    if (reachableTypes.isEmpty()) return true;

    String name = cls.getFullyQualifiedName();
    if (name.contains("$")) {
      name = name.substring(0, name.indexOf('$'));
    }

    return reachableTypes.contains(name);
  }
}
