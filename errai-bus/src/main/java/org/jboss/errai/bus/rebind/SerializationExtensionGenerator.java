package org.jboss.errai.bus.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.user.rebind.SourceWriter;
import org.jboss.errai.bus.server.annotations.ExposeEntity;
import org.jboss.errai.bus.server.util.ConfigUtil;
import org.jboss.errai.bus.server.util.RebindVisitor;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SerializationExtensionGenerator implements ExtensionGenerator {
    private CompiledTemplate demarshallerGenerator;
    private CompiledTemplate marshallerGenerator;

    public SerializationExtensionGenerator() {
        InputStream istream = this.getClass().getResourceAsStream("DemarshallerGenerator.mv");
        demarshallerGenerator = TemplateCompiler.compileTemplate(istream, null);

        istream = this.getClass().getResourceAsStream("MarshallerGenerator.mv");
        marshallerGenerator = TemplateCompiler.compileTemplate(istream, null);
    }

    public void generate(GeneratorContext context, TreeLogger logger, SourceWriter writer, List<File> roots) {
        ConfigUtil.visitAllTargets(roots, context, logger, writer,
                new RebindVisitor() {
                    public void visit(Class<?> visit, GeneratorContext context, TreeLogger logger, SourceWriter writer) {
                        if (visit.isAnnotationPresent(ExposeEntity.class)) {

                            Map<String, Class> types = new HashMap<String, Class>();
                            for (Field f : visit.getDeclaredFields()) {
                                if ((f.getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) != 0 ||
                                        f.isSynthetic()) {
                                    continue;
                                }
                                types.put(f.getName(), f.getType());
                            }

                            Map<String, Object> templateVars = new HashMap<String, Object>();
                            templateVars.put("className", visit.getName());
                            templateVars.put("fields", types.keySet());
                            templateVars.put("targetTypes", types);

                            String genStr;

                            writer.print(genStr = (String) TemplateRuntime.execute(demarshallerGenerator, templateVars));

                            logger.log(TreeLogger.Type.INFO, genStr);

                            writer.print(genStr = (String) TemplateRuntime.execute(marshallerGenerator, templateVars));

                            logger.log(TreeLogger.Type.INFO, genStr);
                            logger.log(TreeLogger.Type.INFO, "Generated mashaller/demarshaller for: " + visit.getName());
                        }
                    }
                }
        );
    }

 
}
