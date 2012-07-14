package org.jboss.errai.codegen.util;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.common.rebind.EnvUtil;
import org.mvel2.util.NullType;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
public final class ClassScanner {
  private ClassScanner() {
  }

  public static Set<MetaParameter> getParametersAnnotatedWith(final Class<? extends Annotation> annotation,
                                                          final Set<String> packages)  {
    final Set<MetaParameter> result = new HashSet<MetaParameter>();
    for (final MetaClass metaClass : MetaClassFactory.getAllCachedClasses()) {
      for (final MetaMethod method : metaClass.getDeclaredMethods())  {
        for (final MetaParameter parameter : method.getParameters()) {
          if (parameter.isAnnotationPresent(annotation)) {
            _addIfMatches(result, parameter, packages);
          }
        }
      }
    }

    return result;
  }

  public static Set<MetaParameter> getParametersAnnotatedWith(final Class<? extends Annotation> annotation) {
    return getParametersAnnotatedWith(annotation, null);
  }

  public static void _addIfMatches(final Set<MetaParameter> result,
                                   final MetaParameter param,
                                   final Set<String> packages) {
    if (packages == null || packages.contains(param.getDeclaringMember().getDeclaringClass().getPackageName())) {
      result.add(param);
    }
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
