package org.jboss.errai.persistence.ext;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.user.rebind.SourceWriter;
import org.jboss.errai.bus.rebind.ExtensionGenerator;
import org.jboss.errai.bus.server.annotations.ExtensionComponent;

import java.io.File;
import java.util.List;

@ExtensionComponent
public class ErraiPersistenceExtension implements ExtensionGenerator {
    public void generate(TreeLogger logger, SourceWriter writer, List<File> targets) {
//        ConfigUtil.visitAllTargets(targets, logger, writer, new RebindVisitor() {
//            public void visit(Class<?> visit, TreeLogger logger, SourceWriter writer) {
//                if (visit.isAnnotationPresent(ExposeEntity.class)) {
//
//                }
//            }
//        } );
    }
}
