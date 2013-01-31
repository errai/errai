package org.jboss.errai.config.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.TypeOracleException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.config.util.ClassScanner;

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
  private static volatile boolean started = false;
  private static volatile GeneratorContext currentContext;

  private static final Map<Class, AsyncCodeGenerator> codeGenerators = new ConcurrentHashMap<Class, AsyncCodeGenerator>();
  private static final Map<Class, Future<String>> activeFutures = new ConcurrentHashMap<Class, Future<String>>();

  public static Future<String> getFutureFor(final TreeLogger logger, final GeneratorContext context, final Class interfaceType) {
    synchronized (lock) {
      startAll(logger, context);

      if (!codeGenerators.containsKey(interfaceType)) {
        throw new RuntimeException("no generator found for interface: " + interfaceType.getName());
      }

      if (activeFutures.containsKey(interfaceType)) {
        return activeFutures.get(interfaceType);
      }
      else {
        throw new RuntimeException("could not find future for interface: " + interfaceType.getName());
      }
    }
  }

  private static void startAll(final TreeLogger logger, final GeneratorContext context) {
    if (started && context != currentContext) {
      started = false;
      codeGenerators.clear();
      activeFutures.clear();
    }

    if (!started) {
      started = true;
      currentContext = context;

      for (final Class<?> cls : ScannerSingleton.getOrCreateInstance().getTypesAnnotatedWith(GenerateAsync.class)) {
        try {
          final AsyncCodeGenerator asyncCodeGenerator
              = cls.asSubclass(AsyncCodeGenerator.class).newInstance();

          final GenerateAsync generateAsync = cls.getAnnotation(GenerateAsync.class);

          try {
            context.getTypeOracle().getType(generateAsync.value().getName());
            codeGenerators.put(generateAsync.value(), asyncCodeGenerator);
            System.out.println(" ****** REGISTERED ASYNC CODE GENERATOR: " + generateAsync.value());
          }
          catch (TypeOracleException e) {
            e.printStackTrace();
            // ignore because not inherited in an active module.
          }
        }
        catch (Throwable e) {
        }
      }

      for (final Map.Entry<Class, AsyncCodeGenerator> entry : codeGenerators.entrySet()) {
        activeFutures.put(entry.getKey(), entry.getValue().generateAsync(logger, context));
        System.out.println("  ASYNC GENERATION BEGAN >> " + entry.getKey().getName());
      }
    }
  }
}
