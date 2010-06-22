package org.jboss.errai.bus.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.SourceWriter;

public class ProcessingContext {
    private TreeLogger treeLogger;
    private GeneratorContext context;
    private SourceWriter writer;
    private TypeOracle oracle;

    public ProcessingContext(TreeLogger treeLogger, GeneratorContext context, SourceWriter writer, TypeOracle oracle) {
        this.treeLogger = treeLogger;
        this.context = context;
        this.writer = writer;
        this.oracle = oracle;
    }

    public TreeLogger getTreeLogger() {
        return treeLogger;
    }

    public GeneratorContext getContext() {
        return context;
    }

    public SourceWriter getWriter() {
        return writer;
    }

    public TypeOracle getOracle() {
        return oracle;
    }
}
