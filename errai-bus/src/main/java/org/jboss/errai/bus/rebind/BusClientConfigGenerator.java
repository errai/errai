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
import org.jboss.errai.bus.server.service.metadata.MetaDataScanner;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.util.Make;
import org.mvel2.util.ParseTools;
import org.mvel2.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
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
    if (props != null)
    {
      logger.log(TreeLogger.Type.INFO, "Checking ErraiApp.properties for configured types ...");

      Iterator<Object> it = props.keySet().iterator();
      while (it.hasNext()) {

        String key = (String)it.next();

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
    else
    {
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
    try {
      for (JField f : visit.getFields()) {
        if (f.isTransient() || f.isStatic() || f.isEnumConstant() != null) continue;

        JClassType type = f.getType().isClassOrInterface();
        JMethod m;
        if (type == null) {
          JPrimitiveType pType = f.getType().isPrimitive();
          if (pType == null) continue;

          Class c;
          types.put(f.getName(), c = ParseTools.unboxPrimitive(Class.forName(pType.getQualifiedBoxedSourceName())));

          if (boolean.class.equals(c)) {
            if ((m = getAccessorMethod(visit, ReflectionUtil.getIsGetter(f.getName()))) == null) {
              m = getAccessorMethod(visit, ReflectionUtil.getGetter(f.getName()));
            }
          } else {
            m = getAccessorMethod(visit, ReflectionUtil.getGetter(f.getName()));
          }

        } else {
          if ("java.lang.Boolean".equals(f.getType().getQualifiedSourceName())) {
            if ((m = getAccessorMethod(visit, ReflectionUtil.getIsGetter(f.getName()))) == null) {
              m = getAccessorMethod(visit, ReflectionUtil.getGetter(f.getName()));
            }
          } else {
            m = getAccessorMethod(visit, ReflectionUtil.getGetter(f.getName()));
          }

          types.put(f.getName(), Class.forName(type.getQualifiedBinaryName()));
        }

        if (m == null) {
          if (f.isPublic()) {
            getters.put(f.getName(), new ValueExtractor(f));
          } else {
            throw new GenerationException("could not find an accessor in class: " + visit.getQualifiedSourceName() + "; for field: " + f.getName());
          }
        }
        getters.put(f.getName(), new ValueExtractor(m));
      }

    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    try {
      if (!enumType) visit.getConstructor(new JClassType[0]);
    }
    catch (NotFoundException e) {
      String errorMsg = "Type marked for serialization does not expose a default constructor: " + visit.getQualifiedSourceName();
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


  private static JMethod getAccessorMethod(JClassType type, String name) {
    try {
      return type.getMethod(name, new JType[0]);
    }
    catch (NotFoundException e) {
      return null;
    }
  }
}
