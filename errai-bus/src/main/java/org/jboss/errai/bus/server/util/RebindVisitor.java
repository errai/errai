package org.jboss.errai.bus.server.util;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.user.rebind.SourceWriter;

public interface RebindVisitor {
    public void visit(Class<?> visit, GeneratorContext context, TreeLogger logger, SourceWriter writer);
}
