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
import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.common.client.api.annotations.ExposeEntity;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.mvel2.ParserContext;
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

  private CompiledTemplate rpcProxyGenerator;

  public BusClientConfigGenerator() {
    InputStream istream = this.getClass().getResourceAsStream("RPCProxyGenerator.mv");
    rpcProxyGenerator = compileTemplate(istream, ParserContext.create());
  }

  public void generate(
          GeneratorContext context, TreeLogger logger,
          SourceWriter writer, MetaDataScanner scanner, final TypeOracle oracle) {


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
