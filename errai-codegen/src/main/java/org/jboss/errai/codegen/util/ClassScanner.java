package org.jboss.errai.codegen.util;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.common.metadata.ScannerSingleton;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
public final class ClassScanner {
  private ClassScanner() {
  }

  public static Set<MetaClass> getTypesAnnotatedWith(final Class<? extends Annotation> annotation,
                                                     final Set<String> packages) {

    final Set<MetaClass> result = new HashSet<MetaClass>();
    for (final MetaClass metaClass : MetaClassFactory.getAllCachedClasses()) {
      if (metaClass.isAnnotationPresent(annotation)) {
        _addIfMatches(result, metaClass, packages);
      }
    }

    for (final Class<?> cls : ScannerSingleton.getOrCreateInstance().getTypesAnnotatedWith(annotation)) {
      _addIfMatches(result, MetaClassFactory.get(cls), packages);
    }

    return result;
  }

  public static Set<MetaClass> getTypesAnnotatedWith(final Class<? extends Annotation> annotation) {
    return getTypesAnnotatedWith(annotation, null);
  }


  public static void _addIfMatches(final Set<MetaClass> result,
                                   final MetaClass clazz,
                                   final Set<String> packages) {
    if (packages == null || packages.contains(clazz.getPackageName())) {
      result.add(clazz);
    }
  }
}
