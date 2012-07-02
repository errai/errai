package org.jboss.errai.config.util;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.config.rebind.EnvUtil;
import org.mvel2.util.NullType;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Mike Brock
 */
public final class ClassScanner {
  private ClassScanner() {
  }

  public static Set<MetaClass> getTypesAnnotatedWith(final Class<? extends Annotation> annotation,
                                                     final Set<String> packages,
                                                     final String excludeRegEx) {

    final Pattern excludePattern;
    if (excludeRegEx != null) {
      excludePattern = Pattern.compile(excludeRegEx);
    }
    else {
      excludePattern = null;
    }

    final Set<MetaClass> result = new HashSet<MetaClass>();
    for (final MetaClass metaClass : MetaClassFactory.getAllCachedClasses()) {
      if (metaClass.isAnnotationPresent(annotation)) {
        _addIfMatches(result, metaClass, packages, excludePattern);
      }
    }

    for (final Class<?> cls : ScannerSingleton.getOrCreateInstance().getTypesAnnotatedWith(annotation)) {
      _addIfMatches(result, MetaClassFactory.get(cls), packages, excludePattern);
    }

    return result;
  }

  public static Set<MetaClass> getTypesAnnotatedWith(final Class<? extends Annotation> annotation) {
    return getTypesAnnotatedWith(annotation, null, null);
  }

  public static Set<MetaClass> getTypesAnnotatedWith(final Class<? extends Annotation> annotation,
                                                     final Set<String> packages) {
    return getTypesAnnotatedWith(annotation, packages, null);
  }


  public static Set<MetaClass> getTypesAnnotatedWithExcluding(final Class<? extends Annotation> annotation,
                                                              final String excludeRegex) {
    return getTypesAnnotatedWith(annotation, null, excludeRegex);
  }


  public static void _addIfMatches(final Set<MetaClass> result,
                                   final MetaClass clazz,
                                   final Set<String> packages,
                                   final Pattern excludePattern) {
    if (packages == null || packages.contains(clazz.getPackageName())) {
      if (excludePattern != null && excludePattern.matcher(clazz.getName()).matches()) {
        return;
      }
      result.add(clazz);
    }
  }

  public static Set<MetaClass> getSubTypesOf(final MetaClass metaClass) {
    final MetaClass root = metaClass.getErased();
    final Set<MetaClass> result = new HashSet<MetaClass>();
    for (final MetaClass mc : MetaClassFactory.getAllCachedClasses()) {
      if (!NullType.class.getName().equals(mc.getFullyQualifiedName())
              && !root.getFullyQualifiedName().equals(mc.getFullyQualifiedName())
              && root.isAssignableFrom(mc)) {
        result.add(mc.getErased());
      }
    }

    if (EnvUtil.isProdMode()) {
      final Class<?> cls = root.asClass();
      if (cls != null && !cls.equals(NullType.class)) {
        for (final Class<?> c : ScannerSingleton.getOrCreateInstance().getSubTypesOf(cls)) {
          if (!c.isAnonymousClass() && !c.isSynthetic()) {
            result.add(MetaClassFactory.get(c));
          }
        }
      }
    }

    return result;
  }
}
