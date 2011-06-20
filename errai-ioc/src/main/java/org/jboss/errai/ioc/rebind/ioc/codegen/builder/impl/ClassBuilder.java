/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadClassReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.PrettyPrinter;

import java.util.*;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class ClassBuilder implements
        ClassDefinitionBuilderScope,
        ClassDefinitionBuilderAbstractOption,
        BaseClassStructureBuilder {

  private Context context;

  private String className;
  private Scope scope;
  private MetaClass parent;

  private Set<MetaClass> interfaces = new HashSet<MetaClass>();

  private List<Builder> constructors = new ArrayList<Builder>();
  private List<Builder> fields = new ArrayList<Builder>();
  private List<Builder> methods = new ArrayList<Builder>();

  private boolean isAbstract;

  ClassBuilder(String className, MetaClass parent, Context context) {
    this.className = className;
    this.parent = parent;
    this.context = context;
  }

  public static ClassDefinitionBuilderScope define(String fullyQualifiedName) {
    return new ClassBuilder(fullyQualifiedName, null, Context.create().autoImport());
  }

  public static ClassDefinitionBuilderScope define(String fullQualifiedName, MetaClass parent) {
    return new ClassBuilder(fullQualifiedName, parent, Context.create().autoImport());
  }

  public static BaseClassStructureBuilder implement(MetaClass cls) {
    return new ClassBuilder(cls.getFullyQualifiedName() + "Impl", null, Context.create().autoImport())
            .publicScope()
            .implementsInterface(cls).body();
  }

  public static BaseClassStructureBuilder implement(Class<?> cls) {
    return implement(MetaClassFactory.get(cls));
  }


  private String getSimpleName() {
    int idx = className.lastIndexOf('.');
    if (idx != -1) {
      return className.substring(idx + 1);
    }
    return className;
  }

  private String getPackageName() {
    int idx = className.lastIndexOf(".");
    if (idx != -1) {
      return className.substring(0, idx);
    }
    return "";
  }

  public ClassBuilder abstractClass() {
    isAbstract = true;
    return this;
  }

  public ClassBuilder importsClass(Class<?> clazz) {
    return importsClass(MetaClassFactory.get(clazz));
  }

  public ClassBuilder importsClass(MetaClass clazz) {
    context.addClassImport(clazz);
    return this;
  }

  public ClassDefinitionBuilderInterfaces implementsInterface(Class<?> clazz) {
    return implementsInterface(MetaClassFactory.get(clazz));
  }

  public ClassDefinitionBuilderInterfaces implementsInterface(MetaClass clazz) {
    if (!clazz.isInterface()) {
      throw new RuntimeException("not an interface: " + clazz.getFullyQualifiedName());
    }

    interfaces.add(clazz);
    return this;
  }

  public BaseClassStructureBuilder body() {
    return this;
  }

  public ClassDefinitionBuilderAbstractOption publicScope() {
    scope = Scope.Public;
    return this;
  }

  public ClassDefinitionBuilderAbstractOption privateScope() {
    scope = Scope.Private;
    return this;
  }

  public ClassDefinitionBuilderAbstractOption protectedScope() {
    scope = Scope.Protected;
    return this;
  }

  public ClassDefinitionBuilderAbstractOption packageScope() {
    scope = Scope.Package;
    return this;
  }

  public BlockBuilder<BaseClassStructureBuilder> publicConstructor(MetaClass... parms) {
    return genConstructor(Scope.Public, DefParameters.fromTypeArray(parms));
  }

  public BlockBuilder<BaseClassStructureBuilder> publicConstructor(Class<?>... parms) {
    return publicConstructor(MetaClassFactory.fromClassArray(parms));
  }

  public BlockBuilder<BaseClassStructureBuilder> publicConstructor(Parameter... parms) {
    return genConstructor(Scope.Public, DefParameters.fromParameters(parms));
  }


  public BlockBuilder<BaseClassStructureBuilder> privateConstructor(MetaClass... parms) {
    return genConstructor(Scope.Private, DefParameters.fromTypeArray(parms));
  }

  public BlockBuilder<BaseClassStructureBuilder> privateConstructor(Class<?>... parms) {
    return privateConstructor(MetaClassFactory.fromClassArray(parms));
  }

  public BlockBuilder<BaseClassStructureBuilder> privateConstructor(Parameter... parms) {
    return genConstructor(Scope.Private, DefParameters.fromParameters(parms));
  }


  public BlockBuilder<BaseClassStructureBuilder> protectedConstructor(MetaClass... parms) {
    return genConstructor(Scope.Protected, DefParameters.fromTypeArray(parms));
  }

  public BlockBuilder<BaseClassStructureBuilder> protectedConstructor(Class<?>... parms) {
    return protectedConstructor(MetaClassFactory.fromClassArray(parms));
  }

  public BlockBuilder<BaseClassStructureBuilder> protectedConstructor(Parameter... parms) {
    return genConstructor(Scope.Protected, DefParameters.fromParameters(parms));
  }


  public BlockBuilder<BaseClassStructureBuilder> packageConstructor(MetaClass... parms) {
    return genConstructor(Scope.Package, DefParameters.fromTypeArray(parms));
  }

  public BlockBuilder<BaseClassStructureBuilder> packageConstructor(Class<?>... parms) {
    return packageConstructor(MetaClassFactory.fromClassArray(parms));
  }

  public BlockBuilder<BaseClassStructureBuilder> packageConstructor(Parameter... parms) {
    return genConstructor(Scope.Package, DefParameters.fromParameters(parms));
  }


  private BlockBuilder<BaseClassStructureBuilder> genConstructor(final Scope scope, final DefParameters
          defParameters) {
    return new BlockBuilder<BaseClassStructureBuilder>(new BuildCallback<BaseClassStructureBuilder>() {
      public BaseClassStructureBuilder callback(final Statement statement) {
        constructors.add(new Builder() {
          public String toJavaString() {
            return new StringBuilder().append(scope.getCanonicalName())
                    .append(" ")
                    .append(getSimpleName())
                    .append(defParameters.generate(context))
                    .append(" {\n").append(statement.generate(context)).append("\n}\n")
                    .toString();
          }
        });

        return ClassBuilder.this;
      }
    });
  }

  public BlockBuilder<BaseClassStructureBuilder> publicMethod(MetaClass returnType, String name, MetaClass... parms) {
    return genMethod(Scope.Public, returnType, name, DefParameters.fromTypeArray(parms));
  }

  public BlockBuilder<BaseClassStructureBuilder> publicMethod(Class<?> returnType, String name, Class<?>... parms) {
    return publicMethod(MetaClassFactory.get(returnType), name, MetaClassFactory.fromClassArray(parms));
  }

  public BlockBuilder<BaseClassStructureBuilder> publicMethod(MetaClass returnType, String name, Parameter... parms) {
    return genMethod(Scope.Public, returnType, name, DefParameters.fromParameters(parms));
  }


  public BlockBuilder<BaseClassStructureBuilder> privateMethod(MetaClass returnType, String name, MetaClass... parms) {
    return genMethod(Scope.Private, returnType, name, DefParameters.fromTypeArray(parms));
  }

  public BlockBuilder<BaseClassStructureBuilder> privateMethod(Class<?> returnType, String name, Class<?>... parms) {
    return privateMethod(MetaClassFactory.get(returnType), name, MetaClassFactory.fromClassArray(parms));
  }

  public BlockBuilder<BaseClassStructureBuilder> privateMethod(MetaClass returnType, String name, Parameter... parms) {
    return genMethod(Scope.Private, returnType, name, DefParameters.fromParameters(parms));
  }


  public BlockBuilder<BaseClassStructureBuilder> protectedMethod(MetaClass returnType, String name, MetaClass... parms) {
    return genMethod(Scope.Protected, returnType, name, DefParameters.fromTypeArray(parms));
  }

  public BlockBuilder<BaseClassStructureBuilder> protectedMethod(Class<?> returnType, String name, Class<?>... parms) {
    return protectedMethod(MetaClassFactory.get(returnType), name, MetaClassFactory.fromClassArray(parms));
  }

  public BlockBuilder<BaseClassStructureBuilder> protectedMethod(MetaClass returnType, String name, Parameter... parms) {
    return genMethod(Scope.Protected, returnType, name, DefParameters.fromParameters(parms));
  }


  public BlockBuilder<BaseClassStructureBuilder> packageMethod(MetaClass returnType, String name, MetaClass... parms) {
    return genMethod(Scope.Package, returnType, name, DefParameters.fromTypeArray(parms));
  }

  public BlockBuilder<BaseClassStructureBuilder> packageMethod(Class<?> returnType, String name, Class<?>... parms) {
    return packageMethod(MetaClassFactory.get(returnType), name, MetaClassFactory.fromClassArray(parms));
  }

  public BlockBuilder<BaseClassStructureBuilder> packageMethod(MetaClass returnType, String name, Parameter... parms) {
    return genMethod(Scope.Package, returnType, name, DefParameters.fromParameters(parms));
  }

  private BlockBuilder<BaseClassStructureBuilder> genMethod(final Scope scope,
                                                            final MetaClass returnType,
                                                            final String name,
                                                            final DefParameters defParameters) {
    return new BlockBuilder<BaseClassStructureBuilder>(new BuildCallback<BaseClassStructureBuilder>() {
      public BaseClassStructureBuilder callback(final Statement statement) {
        methods.add(new Builder() {
          public String toJavaString() {
            return new StringBuilder().append(scope.getCanonicalName())
                    .append(" ")
                    .append(LoadClassReference.getClassReference(returnType, context))
                    .append(" ")
                    .append(name)
                    .append(defParameters.generate(context))
                    .append(" {\n").append(statement.generate(context)).append("\n}\n")
                    .toString();
          }
        });

        return ClassBuilder.this;
      }
    });
  }

  public FieldBuildInitializer<BaseClassStructureBuilder> publicField(String name, MetaClass type) {
    return genField(Scope.Public, name, type);
  }

  public FieldBuildInitializer<BaseClassStructureBuilder> publicField(String name, Class<?> type) {
    return publicField(name, MetaClassFactory.get(type));
  }

  public FieldBuildInitializer<BaseClassStructureBuilder> privateField(String name, MetaClass type) {
    return genField(Scope.Private, name, type);
  }

  public FieldBuildInitializer<BaseClassStructureBuilder> privateField(String name, Class<?> type) {
    return privateField(name, MetaClassFactory.get(type));
  }

  public FieldBuildInitializer<BaseClassStructureBuilder> protectedField(String name, MetaClass type) {
    return genField(Scope.Protected, name, type);
  }

  public FieldBuildInitializer<BaseClassStructureBuilder> protectedField(String name, Class<?> type) {
    return protectedField(name, MetaClassFactory.get(type));
  }

  public FieldBuildInitializer<BaseClassStructureBuilder> packageField(String name, MetaClass type) {
    return genField(Scope.Package, name, type);
  }

  public FieldBuildInitializer<BaseClassStructureBuilder> packageField(String name, Class<?> type) {
    return packageField(name, MetaClassFactory.get(type));
  }

  private FieldBuildInitializer<BaseClassStructureBuilder> genField(final Scope scope, final String name,
                                                                    final MetaClass type) {
    return new FieldBuilder<BaseClassStructureBuilder>(new BuildCallback<BaseClassStructureBuilder>() {
      public BaseClassStructureBuilder callback(final Statement statement) {
        fields.add(new Builder() {
          public String toJavaString() {
            context.addVariable(Variable.createClassMember(name, type));

            return statement.generate(context);
          }
        });

        return ClassBuilder.this;
      }
    }, scope, type, name);
  }

  public String toJavaString() {
    StringBuilder buf = new StringBuilder();


    buf.append("\n");

    buf.append(scope.getCanonicalName());

    if (isAbstract) {
      buf.append(" abstract");
    }

    buf.append(" class ").append(getSimpleName());

    if (parent != null) {
      buf.append(" extends ").append(LoadClassReference.getClassReference(parent, context));
    }

    if (interfaces.size() != 0) {
      buf.append(" implements ");

      Iterator<MetaClass> iter = interfaces.iterator();
      while (iter.hasNext()) {
        buf.append(LoadClassReference.getClassReference(iter.next(), context));
        if (iter.hasNext()) buf.append(" ");
      }
    }

    buf.append(" {\n");

    Iterator<Builder> iter = fields.iterator();
    while (iter.hasNext()) {
      buf.append(iter.next().toJavaString());
      if (iter.hasNext()) buf.append("\n");
    }

    if (!fields.isEmpty()) buf.append("\n");

    iter = constructors.iterator();
    while (iter.hasNext()) {
      buf.append(iter.next().toJavaString());
      if (iter.hasNext()) buf.append("\n");
    }

    if (!constructors.isEmpty()) buf.append("\n");

    iter = methods.iterator();
    while (iter.hasNext()) {
      buf.append(iter.next().toJavaString());
      if (iter.hasNext()) buf.append("\n");
    }

    StringBuilder headerBuffer = new StringBuilder();

    headerBuffer.append("package ").append(getPackageName()).append(";\n");

    if (context.getImportedPackages().size() > 1)
      headerBuffer.append("\n");

    for (String pkgImports : context.getImportedPackages()) {
      if (pkgImports.equals("java.lang")) continue;
      headerBuffer.append("import ").append(pkgImports).append(".*;");
    }

    if (!context.getImportedClasses().isEmpty())
      headerBuffer.append("\n");

    for (MetaClass cls : context.getImportedClasses()) {
      headerBuffer.append("import ").append(cls.getFullyQualifiedName()).append(";\n");
    }


    return PrettyPrinter.prettyPrintJava(headerBuffer.toString() + buf.append("}\n").toString());
  }
}
