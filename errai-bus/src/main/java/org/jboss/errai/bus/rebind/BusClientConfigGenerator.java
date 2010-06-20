/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.user.rebind.SourceWriter;
import org.jboss.errai.bus.server.ErraiBootstrapFailure;
import org.jboss.errai.bus.server.annotations.ExposeEntity;
import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.util.RebindVisitor;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.util.Make;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.jboss.errai.bus.server.util.ConfigUtil.visitAllTargets;
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

    public void generate(GeneratorContext context, TreeLogger logger, SourceWriter writer, List<File> roots) {
        visitAllTargets(roots, context, logger, writer,
                new RebindVisitor() {
                    public void visit(Class<?> visit, GeneratorContext context, TreeLogger logger, SourceWriter writer) {
                        if (visit.isAnnotationPresent(ExposeEntity.class)) {
                            generateMarshaller(visit, logger, writer);

                        } else if (visit.isAnnotationPresent(Remote.class) && visit.isInterface()) {
                            try {
                                writer.print((String) execute(rpcProxyGenerator,
                                        Make.Map.<String, Object>$()
                                                ._("implementationClassName", visit.getSimpleName() + "Impl")
                                                ._("interfaceClass", visit)
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

        try {
            ResourceBundle bundle = ResourceBundle.getBundle("ErraiApp");
            if (bundle != null) {
                logger.log(TreeLogger.Type.INFO, "checking ErraiApp.properties for configured types ...");

                Enumeration<String> keys = bundle.getKeys();

                while (keys.hasMoreElements()) {
                    String key = keys.nextElement();
                    if (ErraiServiceConfigurator.CONFIG_ERRAI_SERIALIZABLE_TYPE.equals(key)) {
                        for (String s : bundle.getString(key).split(" ")) {
                            try {
                                Class<?> cls = Class.forName(s.trim());
                                generateMarshaller(cls, logger, writer);
                            }
                            catch (Exception e) {
                                throw new ErraiBootstrapFailure(e);
                            }
                        }
                    }
                }
            }
        }
        catch (MissingResourceException exception) {
            throw new ErraiBootstrapFailure("Unable to find ErraiApp.properties in the classpath");
        }


        //  generateMarshaller(CommandMessage.class, logger, writer);
    }

    private void generateMarshaller(Class<?> visit, TreeLogger logger, SourceWriter writer) {
        Map<String, Class> types = new HashMap<String, Class>();
        for (Field f : visit.getDeclaredFields()) {
            if ((f.getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) != 0 ||
                    f.isSynthetic()) {
                continue;
            }
            types.put(f.getName(), f.getType());
        }

        try {
            visit.getConstructor(new Class[0]);
        }
        catch (NoSuchMethodException e) {
            String errorMsg = "Type annotated with @ExposeEntity does not expose a default constructor";
            logger.log(TreeLogger.Type.ERROR, errorMsg, e);
            throw new GenerationException(errorMsg, e);
        }

        Map<String, Object> templateVars = Make.Map.<String, Object>$()
                ._("className", visit.getName())
                ._("fields", types.keySet())
                ._("targetTypes", types)._();

        String genStr;

        writer.print(genStr = (String) execute(demarshallerGenerator, templateVars));

        logger.log(TreeLogger.Type.INFO, genStr);

        writer.print(genStr = (String) execute(marshallerGenerator, templateVars));

        logger.log(TreeLogger.Type.INFO, genStr);
        logger.log(TreeLogger.Type.INFO, "Generated marshaller/demarshaller for: " + visit.getName());
    }
}
