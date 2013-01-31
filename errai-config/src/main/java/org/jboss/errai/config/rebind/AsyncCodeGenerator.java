package org.jboss.errai.config.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;

import java.util.concurrent.Future;

/**
 * @author Mike Brock
 */
public interface AsyncCodeGenerator {
  public Future<String> generateAsync(TreeLogger logger, GeneratorContext context);
}
