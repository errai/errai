/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.bus.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.*;
import com.google.gwt.user.rebind.SourceWriter;
import org.jboss.errai.bus.server.ErraiBootstrapFailure;
import org.jboss.errai.bus.server.annotations.ExposeEntity;
import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.util.Make;
import org.mvel2.util.ParseTools;
import org.mvel2.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mvel2.templates.TemplateCompiler.compileTemplate;
import static org.mvel2.templates.TemplateRuntime.execute;

/**
 * @author Mike Brock
 * @author Heiko Braun
 */
public class BusClientConfigGenerator implements ExtensionGenerator {
  private Logger log = LoggerFactory.getLogger(BusClientConfigGenerator.class);

  private CompiledTemplate demarshallerGenerator;
  private CompiledTemplate marshallerGenerator;
  private CompiledTemplate rpcProxyGenerator;

  public BusClientConfigGenerator() {
    InputStream istream = this.getClass().getResourceAsStream("DemarshallerGenerator.mv");
    demarshallerGenerator = compileTemplate(istream, null);

    istream = this.getClass().getResourceAsStream("MarshallerGenerator.mv");
    marshallerGenerator = compileTemplate(istream, null);

    istream = this.getClass().getResourceAsStream("RPCProxyGenerator.mv");
    rpcProxyGenerator = compileTemplate(istream, null);
  }

  public void generate(
          GeneratorContext context, TreeLogger logger,
          SourceWriter writer, MetaDataScanner scanner, final TypeOracle oracle) {

    for (Class<?> entity : scanner.getTypesAnnotatedWith(ExposeEntity.class)) {
      generateMarshaller(loadType(oracle, entity), logger, writer);
    }

    for (Class<?> remote : scanner.getTypesAnnotatedWith(Remote.class)) {
      JClassType type = loadType(oracle, remote);
      try {
        writer.print((String) execute(rpcProxyGenerator,
                Make.Map.<String, Object>$()
                        ._("implementationClassName", type.getName() + "Impl")
                        ._("interfaceClass", Class.forName(type.getQualifiedSourceName()))
                        ._()));
      }
      catch (Throwable t) {
        throw new ErraiBootstrapFailure(t);
      }
    }

    Properties props = scanner.getProperties("ErraiApp.properties");
    if (props != null) {
      logger.log(TreeLogger.Type.INFO, "Checking ErraiApp.properties for configured types ...");

      for (Object o : props.keySet()) {
        String key = (String) o;
        /**
         * Types configuration
         */
        if (ErraiServiceConfigurator.CONFIG_ERRAI_SERIALIZABLE_TYPE.equals(key)) {
          for (String s : props.getProperty(key).split(" ")) {
            try {
              generateMarshaller(oracle.getType(s.trim()), logger, writer);
            }
            catch (Exception e) {
              e.printStackTrace();
              throw new ErraiBootstrapFailure(e);
            }
          }
        }

        /**
         * Entity configuration
         */
        else if (ErraiServiceConfigurator.CONFIG_ERRAI_SERIALIZABLE_TYPE.equals(key)) {
          for (String s : props.getProperty(key).split(" ")) {
            try {
              generateMarshaller(oracle.getType(s.trim()), logger, writer);
            }
            catch (Exception e) {
              e.printStackTrace();
              throw new ErraiBootstrapFailure(e);
            }
          }
        }
      }
    }
    else {
      // props not found
      log.warn("No modules found ot load. Unable to find ErraiApp.properties in the classpath");
    }
  }

  private JClassType loadType(TypeOracle oracle, Class<?> entity) {
    try {
      return oracle.getType(entity.getCanonicalName());
    }
    catch (NotFoundException e) {
      throw new RuntimeException("Failed to load type " + entity.getName(), e);
    }
  }

  private void generateMarshaller(JClassType visit, TreeLogger logger, SourceWriter writer) {
    Boolean enumType = visit.isEnum() != null;
    Map<String, Class> types = new HashMap<String, Class>();
    Map<String, ValueExtractor> getters = new HashMap<String, ValueExtractor>();
    Map<String, ValueBinder> setters = new HashMap<String, ValueBinder>();
    Map<Class, Integer> arrayConverters = new HashMap<Class, Integer>();

    try {
      JClassType scan = visit;

      do {
        for (JField f : scan.getFields()) {
          if (f.isTransient() || f.isStatic() || f.isEnumConstant() != null) continue;

          JClassType type = f.getType().isClassOrInterface();
          JMethod getterMeth;
          JMethod setterMeth;
          if (type == null) {
            JPrimitiveType pType = f.getType().isPrimitive();
            Class c;
            if (pType == null) {
              JArrayType aType = f.getType().isArray();
              if (aType == null) continue;

              String name = aType.getQualifiedBinaryName();
              int depth = 0;
              for (int i = 0; i < name.length(); i++) {
                if (name.charAt(i) == '[') depth++;
                else break;
              }

              types.put(f.getName(), c = Class.forName(name.substring(0, depth)
                      + getInternalRep(aType.getQualifiedBinaryName().substring(depth))));
              arrayConverters.put(c, depth);
            }
            else {
              types.put(f.getName(), c = ParseTools.unboxPrimitive(Class.forName(pType.getQualifiedBoxedSourceName())));
            }
          }
          else {
            types.put(f.getName(), Class.forName(type.getQualifiedBinaryName()));
          }

          getterMeth = getAccessorMethod(visit, f);
          setterMeth = getSetterMethod(visit, f);

          if (getterMeth == null) {
            if (f.isPublic()) {
              getters.put(f.getName(), new ValueExtractor(f));
            }
            else if (visit == scan) {
              throw new GenerationException("could not find a read accessor in class: "
                      + visit.getQualifiedSourceName() + "; for field: " + f.getName() + "; should declare an accessor: "
                      + ReflectionUtil.getGetter(f.getName()));
            }
          }
          else {
            getters.put(f.getName(), new ValueExtractor(getterMeth));
          }

          if (setterMeth == null) {
            if (f.isPublic()) {
              setters.put(f.getName(), new ValueBinder(f));
            }
            else if (visit == scan) {
              throw new GenerationException("could not find a write accessor in class: " + visit.getQualifiedSourceName()
                      + "; for field: " + f.getName() + "; should declare an accessor: " + ReflectionUtil.getSetter(f.getName()));
            }
            else {
              types.remove(f.getName());
            }
          }
          else {
            setters.put(f.getName(), new ValueBinder(setterMeth));
          }
        }
      }
      while ((scan = scan.getSuperclass()) != null && !scan.getQualifiedSourceName().equals("java.lang.Object"));

    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    try {
      if (!enumType) visit.getConstructor(new JClassType[0]);
    }
    catch (NotFoundException e) {
      String errorMsg = "Type marked for serialization does not expose a default constructor: "
              + visit.getQualifiedSourceName();
      logger.log(TreeLogger.Type.ERROR, errorMsg, e);
      throw new GenerationException(errorMsg, e);
    }

    Map<String, Object> templateVars = Make.Map.<String, Object>$()
            ._("className", visit.getQualifiedSourceName())
            ._("canonicalClassName", visit.getQualifiedBinaryName())
            ._("fields", types.keySet())
            ._("targetTypes", types)
            ._("getters", getters)
            ._("setters", setters)
            ._("arrayConverters", arrayConverters)
            ._("enumType", enumType)._();

    String genStr;

    writer.print(genStr = (String) execute(demarshallerGenerator, templateVars));

    log.debug("generated demarshaller: \n" + genStr);

    logger.log(TreeLogger.Type.INFO, genStr);

    writer.print(genStr = (String) execute(marshallerGenerator, templateVars));

    log.debug("generated marshaller: \n" + genStr);

    logger.log(TreeLogger.Type.INFO, genStr);
    logger.log(TreeLogger.Type.INFO, "Generated marshaller/demarshaller for: " + visit.getName());
  }

  public static class ValueExtractor {
    private boolean accessor;
    private String name;

    public ValueExtractor(JMethod m) {
      accessor = true;
      name = m.getName();
    }

    public ValueExtractor(JField f) {
      accessor = false;
      name = f.getName();
    }

    @Override
    public String toString() {
      return accessor ? name + "()" : name;
    }
  }

  public static class ValueBinder {
    private boolean accessor;
    private String name;

    public ValueBinder(JMethod m) {
      accessor = true;
      name = m.getName();
    }

    public ValueBinder(JField f) {
      accessor = false;
      name = f.getName();
    }

    public String bindValue(String expr) {
      return accessor ? name + "(" + expr + ")" : "name = " + expr;
    }
  }

  private static JMethod getAccessorMethod(JClassType clazz, JField field) {
    JMethod m = null;

    if (field.getType().getQualifiedSourceName().equals("boolean"))
      m = _findGetterMethod(clazz, ReflectionUtil.getIsGetter(field.getName()));

    if (m == null)
      m = _findGetterMethod(clazz, ReflectionUtil.getGetter(field.getName()));

    if (m == null)
      m = _findGetterMethod(clazz, "get" + field.getName());

    return m;
  }

  private static JMethod getSetterMethod(JClassType clazz, JField field) {
    JMethod m = null;

    m = _findSetterMethod(clazz, field.getType(), ReflectionUtil.getSetter(field.getName()));

    if (m == null)
      m = _findSetterMethod(clazz, field.getType(), "set" + field.getName());

    return m;
  }


  private static JMethod _findGetterMethod(JClassType clazz, String methName) {
    JClassType scan = clazz;
    do {
      try {
        return scan.getMethod(methName, new JType[0]);
      }
      catch (NotFoundException e) {
        //
      }
    } while ((scan = scan.getSuperclass()) != null && !scan.getQualifiedSourceName().equals("java.lang.Object"));
    return null;
  }

  private static JMethod _findSetterMethod(JClassType clazz, JType field, String methName) {
    JClassType scan = clazz;
    do {

      try {
        return scan.getMethod(methName, new JType[]{field});
      }
      catch (NotFoundException e) {
        //
      }
    } while ((scan = scan.getSuperclass()) != null && !scan.getQualifiedSourceName().equals("java.lang.Object"));
    return null;
  }

  private String getInternalRep(String c) {
    if ("char".equals(c)) {
      return "C";
    }
    else if ("byte".equals(c)) {
      return "B";
    }
    else if ("double".equals(c)) {
      return "D";
    }
    else if ("float".equals(c)) {
      return "F";
    }
    else if ("int".equals(c)) {
      return "I";
    }
    else if ("long".equals(c)) {
      return "J";
    }
    else if ("short".equals(c)) {
      return "S";
    }
    else if ("boolean".equals(c)) {
      return "Z";
    }
    return "L" + c + ";";
  }

}
