package org.jboss.errai.config.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @author Mike Brock
 */
public final class AsyncGenerators {
  private AsyncGenerators() {
  }

  private static final Object lock = new Object();
  private static volatile boolean fired = false;
  private static final Map<Class, AsyncCodeGenerator> codeGenerators = new ConcurrentHashMap<Class, AsyncCodeGenerator>();
  private static final Map<Class, Future<String>> activeFutures = new ConcurrentHashMap<Class, Future<String>>();

  static {
    for (final Class<?> cls : ScannerSingleton.getOrCreateInstance().getTypesAnnotatedWith(GenerateAsync.class)) {
      try {
        final AsyncCodeGenerator asyncCodeGenerator
            = cls.asSubclass(AsyncCodeGenerator.class).newInstance();

        final GenerateAsync generateAsync = cls.getAnnotation(GenerateAsync.class);

        codeGenerators.put(generateAsync.value(), asyncCodeGenerator);

        System.out.println(" ****** REGISTERED ASYNC CODE GENERATOR: " + generateAsync.value());
      }
      catch (Throwable e) {
        throw new RuntimeException("failed to load generator: " + cls.getName(), e);
      }
    }
  }

  public static Future<String> getFutureFor(final TreeLogger logger, final GeneratorContext context, final Class interfaceType) {
    synchronized (lock) {
      if (!codeGenerators.containsKey(interfaceType)) {
        throw new RuntimeException("no generator found for interface: " + interfaceType.getName());
      }

      startAll(logger, context);

      if (activeFutures.containsKey(interfaceType)) {
        return activeFutures.get(interfaceType);
      }
      else {
        throw new RuntimeException("could not find future for interface: " + interfaceType.getName());
      }
    }
  }

  private static void startAll(final TreeLogger logger, final GeneratorContext context) {
    if (!fired) {
      fired = true;
      for (final Map.Entry<Class, AsyncCodeGenerator> entry : codeGenerators.entrySet()) {
        activeFutures.put(entry.getKey(), entry.getValue().generateAsync(logger, context));
        System.out.println("  ASYNC GENERATION BEGAN >> " + entry.getKey().getName());
      }
    }
  }
}
