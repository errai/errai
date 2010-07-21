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
import org.jboss.errai.bus.server.util.RebindUtil;
import org.jboss.errai.bus.server.util.RebindVisitor;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.util.Make;
import org.mvel2.util.ParseTools;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import static org.mvel2.templates.TemplateCompiler.compileTemplate;
import static org.mvel2.templates.TemplateRuntime.execute;

public class BusClientConfigGenerator implements ExtensionGenerator {

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

    public void generate(GeneratorContext context, TreeLogger logger, SourceWriter writer, List<File> roots, final TypeOracle oracle) {
        RebindUtil.visitAllTargets(roots, context, logger, writer, oracle,
                new RebindVisitor() {
                    public void visit(JClassType visit, GeneratorContext context,
                                      TreeLogger logger, SourceWriter writer) {

                        if (visit.isAnnotationPresent(ExposeEntity.class)) {
                            generateMarshaller(visit, logger, writer);
                        } else if (visit.isAnnotationPresent(Remote.class) && visit.isInterface() != null) {
                            try {
                                writer.print((String) execute(rpcProxyGenerator,
                                        Make.Map.<String, Object>$()
                                                ._("implementationClassName", visit.getName() + "Impl")
                                                ._("interfaceClass", Class.forName(visit.getQualifiedSourceName()))
                                                ._()));
                            }
                            catch (Throwable t) {
                                throw new ErraiBootstrapFailure(t);
                            }
                        }
                    }

                    public void visitError(String className, Throwable t) {
                    }
                }
        );

        boolean found = false;
        String path;
        for (File root : roots) {
            path = root.getAbsolutePath();

            FileInputStream jarStream = null;
            InputStream inputStream = null;
            ResourceBundle bundle = null;
            try {
                try {

                    if (path.endsWith(".jar")) {
                        jarStream = new FileInputStream(root.getAbsolutePath());
                        JarInputStream jarInputStream = new JarInputStream(jarStream);
                        inputStream = jarInputStream;

                        JarEntry entry;
                        while ((entry = jarInputStream.getNextJarEntry()) != null) {
                            if (entry.getName().endsWith("ErraiApp.properties")) {
                                bundle = new PropertyResourceBundle(inputStream);
                                break;
                            }
                        }
                    } else {
                        inputStream = new FileInputStream(root.getAbsolutePath() + "/ErraiApp.properties");
                        bundle = new PropertyResourceBundle(inputStream);
                    }


                    if (bundle != null) {
                        found = true;
                        logger.log(TreeLogger.Type.INFO, "checking ErraiApp.properties for configured types ...");

                        Enumeration<String> keys = bundle.getKeys();

                        while (keys.hasMoreElements()) {
                            String key = keys.nextElement();
                            if (ErraiServiceConfigurator.CONFIG_ERRAI_SERIALIZABLE_TYPE.equals(key)) {
                                for (String s : bundle.getString(key).split(" ")) {
                                    try {
                                        generateMarshaller(oracle.getType(s.trim()), logger, writer);
                                    }
                                    catch (Exception e) {
                                        throw new ErraiBootstrapFailure(e);
                                    }
                                }
                            }
                        }
                    }
                }
                catch (FileNotFoundException e) {
                    // ignore.  we don't expect to find this file in all paths.
                }
                finally {
                    if (inputStream != null) inputStream.close();
                    if (jarStream != null) jarStream.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }


        }
        if (!found) {
            throw new ErraiBootstrapFailure("Unable to find ErraiApp.properties in the classpath");
        }

    }

    private void generateMarshaller(JClassType visit, TreeLogger logger, SourceWriter writer) {
        Boolean enumType = visit.isEnum() != null;
        Map<String, Class> types = new HashMap<String, Class>();
        try {
            for (JField f : visit.getFields()) {
                if (f.isTransient() || f.isStatic() || f.isEnumConstant() != null) continue;

                JClassType type = f.getType().isClassOrInterface();

                if (type == null) {
                    JPrimitiveType pType = f.getType().isPrimitive();
                    if (pType == null) continue;

                    types.put(f.getName(), ParseTools.unboxPrimitive(Class.forName(pType.getQualifiedBoxedSourceName())));
                    continue;
                }

                types.put(f.getName(), Class.forName(type.getQualifiedSourceName()));
            }

        }
        catch (ClassNotFoundException e) {
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
                ._("fields", types.keySet())
                ._("targetTypes", types)
                ._("enumType", enumType)._();

        String genStr;

        writer.print(genStr = (String) execute(demarshallerGenerator, templateVars));

        logger.log(TreeLogger.Type.INFO, genStr);

        writer.print(genStr = (String) execute(marshallerGenerator, templateVars));

        logger.log(TreeLogger.Type.INFO, genStr);
        logger.log(TreeLogger.Type.INFO, "Generated marshaller/demarshaller for: " + visit.getName());
    }


}
