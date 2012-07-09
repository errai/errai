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
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
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
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.metadata.ScannerSingleton;
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
  private final MarshallerOuputTarget target;

  private GeneratorMappingContext mappingContext;

  ClassStructureBuilder<?> classStructureBuilder;
  ConstructorBlockBuilder<?> constructor;
  Context classContext;

  private final Set<String> arrayMarshallers = new HashSet<String>();

  private static final Logger log = LoggerFactory.getLogger(MarshallerGeneratorFactory.class);

  long startTime;


  private MarshallerGeneratorFactory(MarshallerOuputTarget target) {
    this.target = target;
  }

  public static MarshallerGeneratorFactory getFor(MarshallerOuputTarget target) {
    return new MarshallerGeneratorFactory(target);
  }

  public String generate(final String packageName, final String clazzName) {
    final File fileCacheDir = RebindUtils.getErraiCacheDir();
    final File cacheFile = new File(fileCacheDir.getAbsolutePath() + "/" + clazzName + ".java");

    final Set<Class<? extends Annotation>> annos = new HashSet<Class<? extends Annotation>>();
    annos.add(Portable.class);

    String gen;

    log.info("generating marshalling class " + packageName + "." + clazzName + "...");
    long st = System.currentTimeMillis();
    gen = _generate(packageName, clazzName);
    log.info("generated marshalling class in " + (System.currentTimeMillis() - st) + "ms.");

    if (Boolean.getBoolean("errai.codegen.printOut")) {
      System.out.println(gen);
    }

    RebindUtils.writeStringToFile(cacheFile, gen);

    return gen;
  }

  private String _generate(String packageName, String clazzName) {
    startTime = System.currentTimeMillis();

    classStructureBuilder = implement(MarshallerFactory.class, packageName, clazzName);
    classContext = classStructureBuilder.getClassDefinition().getContext();
    mappingContext = new GeneratorMappingContext(classContext, classStructureBuilder.getClassDefinition(),
            classStructureBuilder, new ArrayMarshallerCallback() {
      @Override
      public Statement marshal(MetaClass type, Statement value) {
        createDemarshallerIfNeeded(type);
        return value;
      }

      @Override
      public Statement demarshall(MetaClass type, Statement value) {
        String variable = createDemarshallerIfNeeded(type);

        return Stmt.loadVariable(variable).invoke("demarshall", value, Stmt.loadVariable("a1"));
      }

      private String createDemarshallerIfNeeded(MetaClass type) {
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

    for (Class<?> cls : mappingContext.getDefinitionsFactory().getExposedClasses()) {
      String clsName = cls.getName();

      if (!mappingContext.getDefinitionsFactory().hasDefinition(clsName)) {
        continue;
      }

      Class<? extends Marshaller> marshallerCls = mappingContext.getDefinitionsFactory().getDefinition(clsName)
              .getClientMarshallerClass();

      if (marshallerCls == null) {
        continue;
      }

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

      for (Map.Entry<String, String> aliasEntry : mappingContext.getDefinitionsFactory().getMappingAliases().entrySet()) {
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

    // special support for Object[]
    addArrayMarshaller(MetaClassFactory.get(Object[].class));
    addArrayMarshaller(MetaClassFactory.get(String[].class));
    addArrayMarshaller(MetaClassFactory.get(int[].class));
    addArrayMarshaller(MetaClassFactory.get(long[].class));
    addArrayMarshaller(MetaClassFactory.get(double[].class));
    addArrayMarshaller(MetaClassFactory.get(float[].class));
    addArrayMarshaller(MetaClassFactory.get(short[].class));
    addArrayMarshaller(MetaClassFactory.get(boolean[].class));
    addArrayMarshaller(MetaClassFactory.get(byte[].class));

    addArrayMarshaller(MetaClassFactory.get(Integer[].class));
    addArrayMarshaller(MetaClassFactory.get(Long[].class));
    addArrayMarshaller(MetaClassFactory.get(Double[].class));
    addArrayMarshaller(MetaClassFactory.get(Float[].class));
    addArrayMarshaller(MetaClassFactory.get(Short[].class));
    addArrayMarshaller(MetaClassFactory.get(Boolean[].class));
    addArrayMarshaller(MetaClassFactory.get(Byte[].class));

    return classStructureBuilder.toJavaString();
  }

  private void generateMarshallers() {
    final Set<Class<?>> exposed = mappingContext.getDefinitionsFactory().getExposedClasses();

    for (Class<?> clazz : exposed) {
      mappingContext.registerGeneratedMarshaller(clazz.getName());
    }

    for (Class<?> clazz : exposed) {
      final MappingDefinition definition = mappingContext.getDefinitionsFactory().getDefinition(clazz);
      if (definition.getClientMarshallerClass() != null || definition.alreadyGenerated()) {
        continue;
      }

      final MetaClass metaClazz = MetaClassFactory.get(clazz);
      final Statement marshaller = marshal(metaClazz);
      final MetaClass type = marshaller.getType();
      final String varName = getVarName(clazz);

      classStructureBuilder.privateField(varName, type).finish();

      if (clazz.isAnnotationPresent(AlwaysQualify.class)) {
        constructor.append(loadVariable(varName).assignValue(
                Stmt.newObject(QualifyingMarshallerWrapper.class).withParameters(marshaller)));
      }
      else {
        constructor.append(loadVariable(varName).assignValue(marshaller));
      }


      constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
              .invoke("put", clazz.getName(), loadVariable(varName)));

      if (!clazz.getName().equals(clazz.getCanonicalName())) {
        constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
                .invoke("put", clazz.getCanonicalName(), loadVariable(varName)));
      }

      for (Map.Entry<String, String> aliasEntry : mappingContext.getDefinitionsFactory().getMappingAliases().entrySet()) {
        if (aliasEntry.getValue().equals(clazz.getName())) {
          constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
                  .invoke("put", aliasEntry.getKey(), loadVariable(varName)));
        }
      }
    }

    constructor.finish();
  }

  private Statement marshal(MetaClass cls) {
    final MappingStrategy strategy = MappingStrategyFactory
            .createStrategy(target == MarshallerOuputTarget.GWT, mappingContext, cls);
    if (strategy == null) {
      throw new RuntimeException("no available marshaller for class: " + cls.getName());
    }
    return strategy.getMapper().getMarshaller();
  }

  private String addArrayMarshaller(MetaClass type) {
    final String varName = getVarName(type);

    if (!arrayMarshallers.contains(varName)) {
      final Statement marshaller = generateArrayMarshaller(type);

      classStructureBuilder.privateField(varName,
              MetaClassFactory.parameterizedAs(QualifyingMarshallerWrapper.class,
                      MetaClassFactory.typeParametersOf(type)))
              .finish();

      constructor.append(loadVariable(varName).assignValue(
              Stmt.newObject(QualifyingMarshallerWrapper.class)
                      .withParameters(marshaller)));

      constructor.append(Stmt.create(classContext).loadVariable(MARSHALLERS_VAR)
              .invoke("put", type.getFullyQualifiedName(), loadVariable(varName)));


      arrayMarshallers.add(varName);
    }

    return varName;
  }

  private Statement generateArrayMarshaller(MetaClass arrayType) {
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
    
    Class<?> arrayOfArrayType = Array.newInstance(arrayType.asClass(), 0).getClass();
    classStructureBuilder.publicMethod(arrayOfArrayType, "getEmptyArray")
            .append(Stmt.throw_(UnsupportedOperationException.class, "Not implemented!"))
            .finish();


    final BlockBuilder<?> bBuilder = classStructureBuilder.publicOverridesMethod("demarshall",
            Parameter.of(EJValue.class, "a0"), Parameter.of(MarshallingSession.class, "a1"));

    bBuilder.append(
            Stmt.if_(Bool.isNull(loadVariable("a0")))
                    .append(Stmt.load(null).returnValue())
                    .finish()
                    .else_()
                    .append(Stmt.declareVariable(EJArray.class).named("arr")
                            .initializeWith(Stmt.loadVariable("a0").invoke("isArray")))
                    .append(Stmt.nestedCall(Stmt.loadVariable("this")).invoke("_demarshall" + dimensions,
                            loadVariable("arr"), loadVariable("a1")).returnValue())
                    .finish());
    bBuilder.finish();

    arrayDemarshallCode(toMap, dimensions, classStructureBuilder);

    BlockBuilder<?> marshallMethodBlock = classStructureBuilder.publicOverridesMethod("marshall",
            Parameter.of(toMap.asArrayOf(dimensions), "a0"), Parameter.of(MarshallingSession.class, "a1"));

    marshallMethodBlock.append(
            Stmt.if_(Bool.isNull(loadVariable("a0")))
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

  private void arrayDemarshallCode(MetaClass toMap, int dim, AnonymousClassStructureBuilder anonBuilder) {
    Object[] dimParms = new Object[dim];
    dimParms[0] = Stmt.loadVariable("a0").invoke("size");

    final MetaClass arrayType = toMap.asArrayOf(dim);

    MetaClass outerType = toMap.getOuterComponentType();
    if (!outerType.isArray() && outerType.isPrimitive()) {
      outerType = outerType.asBoxed();
    }

    final Statement demarshallerStatement =
            Stmt.loadVariable(getVarName(outerType)).invoke("demarshall", loadVariable("a0")
                    .invoke("get", loadVariable("i")), Stmt.loadVariable("a1"));

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
            .initializeWith(Stmt.newObject(StringBuilder.class).withParameters("[")))
            .append(autoForLoop("i", Stmt.loadVariable("a0").loadField("length"))
                    .append(Stmt.if_(Bool.greaterThan(Stmt.loadVariable("i"), 0))
                            .append(Stmt.loadVariable("sb").invoke("append", ",")).finish())
                    .append(Stmt.loadVariable("sb").invoke("append", dim == 1 ?
                            Stmt.loadVariable(MarshallingGenUtil.getVarName(outerType))
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
}
