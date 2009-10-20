package org.jboss.errai.bus.rebind;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.user.rebind.SourceWriter;

public interface ExtensionGenerator {
    public void generate(TreeLogger logger, SourceWriter writer);
}
