package org.jboss.errai.bus.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.user.rebind.SourceWriter;

import java.io.File;
import java.util.List;

public interface ExtensionGenerator {
    public void generate(GeneratorContext context, TreeLogger logger, SourceWriter writer, List<File> roots);
}
