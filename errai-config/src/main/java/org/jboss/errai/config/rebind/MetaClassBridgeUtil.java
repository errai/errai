/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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

package org.jboss.errai.config.rebind;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassCache;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.impl.gwt.GWTClass;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.rebind.CacheStore;
import org.jboss.errai.common.rebind.CacheUtil;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

/**
 * @author Mike Brock
 */
public abstract class MetaClassBridgeUtil {
  private MetaClassBridgeUtil() {}


  /**
   * Erases the {@link org.jboss.errai.codegen.meta.MetaClassFactory} cache, then populates it with types
   * discovered via GWT's TypeOracle. The reason for the initial flush of the
   * MetaClassFactory is to support hot redeploy in Dev Mode. The reason for doing
   * this operation at all is so that the overridden class definitions
   * (super-source classes) are used in preference to the Java reflection based
   * class definitions.
   *
   * @param context
   *     The GeneratorContext supplied by the GWT compiler. Not null.
   * @param logger
   *     The TreeLogger supplied by the GWT compiler. Not null.
   */
  public synchronized static void populateMetaClassFactoryFromTypeOracle(final GeneratorContext context,
                                                                         final TreeLogger logger) {

    final GWTTypeOracleCacheStore tOCache = CacheUtil.getCache(GWTTypeOracleCacheStore.class);

    // if we're in production mode -- it means we're compiling, and we do not need to accommodate dynamically
    // changing classes. Therefore, do a NOOP after the first successful call.
    if (context.equals(tOCache.populatedFrom)) {
      return;
    }

    final TypeOracle typeOracle = context.getTypeOracle();
    final MetaClassCache cache = MetaClassFactory.getMetaClassCache();

    if (typeOracle != null) {
      final Map<String, MetaClass> classesToPush = new HashMap<String, MetaClass>(typeOracle.getTypes().length);
      final Set<String> translatable = new HashSet<String>(RebindUtils.findTranslatablePackages(context));
      translatable.remove("java.lang");
      translatable.remove("java.lang.annotation");

      for (final JClassType type : typeOracle.getTypes()) {
        if (!translatable.contains(type.getPackage().getName())) {
 //         logger.log(com.google.gwt.core.ext.TreeLogger.Type.DEBUG, "Skipping non-translatable " + type.getQualifiedSourceName());
          continue;
        }

        if (type.isAnnotation() != null || type.getQualifiedSourceName().equals("java.lang.annotation.Annotation")) {
   //       logger.log(com.google.gwt.core.ext.TreeLogger.Type.DEBUG, "Caching annotation type " + type.getQualifiedSourceName());

          if (!MetaClassFactory.canLoadClass(type.getQualifiedBinaryName())) {
            throw new RuntimeException("a new annotation has been introduced (" + type.getQualifiedSourceName() + "); "
                + "you cannot currently introduce new annotations in devmode. Please restart.");
          }


          final MetaClass clazz = JavaReflectionClass
              .newUncachedInstance(MetaClassFactory.loadClass(type.getQualifiedBinaryName()));

          classesToPush.put(clazz.getFullyQualifiedName(), clazz);
        }
        else {
          logger.log(TreeLogger.Type.DEBUG, "Caching translatable type " + type.getQualifiedSourceName());
          final MetaClass clazz = GWTClass.newInstance(typeOracle, type);
          classesToPush.put(clazz.getFullyQualifiedName(), clazz);
        }
      }

      cache.updateCache(classesToPush);
    }
    tOCache.populatedFrom = context;

    CacheUtil.getCache(EnvUtil.EnvironmentConfigCache.class).clear();
  }

  public static class GWTTypeOracleCacheStore implements CacheStore {
    volatile GeneratorContext populatedFrom;

    @Override
    public void clear() {
      populatedFrom = null;
    }
  }
}
