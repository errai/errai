package org.jboss.errai.config.util;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.config.rebind.EnvUtil;
import org.mvel2.util.NullType;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;


/**
 * @author Mike Brock
 */
public final class ClassScanner {
  private static boolean reflectionsScanning = false;
  private static Map<Class<? extends Annotation>, List<MetaClass>> SCAN_CACHE
      = new HashMap<Class<? extends Annotation>, List<MetaClass>>(1000);
  private static AtomicLong totalClassScanTime = new AtomicLong(0);

  private ClassScanner() {
  }

  public static Collection<MetaParameter> getParametersAnnotatedWith(final Class<? extends Annotation> annotation,
                                                                     final Set<String> packages) {

    final Collection<MetaParameter> result = new HashSet<MetaParameter>();
    for (final MetaClass metaClass : MetaClassFactory.getAllCachedClasses()) {
      for (final MetaMethod method : metaClass.getDeclaredMethods()) {
        for (final MetaParameter parameter : method.getParameters()) {
          if (parameter.isAnnotationPresent(annotation)) {
            result.add(parameter);
          }
        }
      }
    }

    filterResultsParameter(result, packages, null);

    return Collections.unmodifiableCollection(result);
  }

  public static Collection<MetaParameter> getParametersAnnotatedWith(final Class<? extends Annotation> annotation) {
    return getParametersAnnotatedWith(annotation, null);
  }

  public static Collection<MetaClass> getTypesAnnotatedWith(final Class<? extends Annotation> annotation,
                                                            final Set<String> packages,
                                                            final String excludeRegEx) {
    long tm = System.currentTimeMillis();
    final Collection<MetaClass> result;

    if (false && SCAN_CACHE.containsKey(annotation)) {
      result = new ArrayList<MetaClass>(SCAN_CACHE.get(annotation));
    }
    else {
      result = Collections.newSetFromMap(new ConcurrentHashMap<MetaClass, Boolean>());

//      final Map<Class<? extends  Annotation>, List<MetaClass>> cacheBuild
//          = new HashMap<Class<? extends Annotation>, List<MetaClass>>();

      final Future<?> factoryFuture = ThreadUtil.submit(new Runnable() {
        @Override
        public void run() {
          for (final MetaClass metaClass : MetaClassFactory.getAllCachedClasses()) {
            if (metaClass.isAnnotationPresent(annotation)) {
//              recordCache(cacheBuild, annotation, metaClass);
              result.add(metaClass);
            }
//            else {
//              for (final Annotation a : metaClass.getAnnotations()) {
//                recordCache(cacheBuild, a.annotationType(), metaClass);
//              }
//            }
          }
        }
      });

      try {
        factoryFuture.get();

        if (reflectionsScanning) {
          final Future<?> reflectionsFuture = ThreadUtil.submit(new Runnable() {
            @Override
            public void run() {
              for (final Class<?> cls : ScannerSingleton.getOrCreateInstance().getTypesAnnotatedWith(annotation)) {
                final MetaClass e = MetaClassFactory.get(cls);
//                recordCache(cacheBuild, annotation, e);
                result.add(e);
              }
            }
          });
          reflectionsFuture.get();
        }
      }
      catch (Exception ignored) {
      }

//      SCAN_CACHE.putAll(cacheBuild);
    }

    filterResultsClass(result, packages, excludeRegEx);

    totalClassScanTime.addAndGet(System.currentTimeMillis() - tm);

    return Collections.unmodifiableCollection(result);
  }

  public static Collection<MetaClass> getTypesAnnotatedWith(final Class<? extends Annotation> annotation) {
    return getTypesAnnotatedWith(annotation, null, null);
  }

  public static Collection<MetaClass> getTypesAnnotatedWith(final Class<? extends Annotation> annotation,
                                                            final Set<String> packages) {
    return getTypesAnnotatedWith(annotation, packages, null);
  }

  public static Collection<MetaClass> getTypesAnnotatedWithExcluding(final Class<? extends Annotation> annotation,
                                                                     final String excludeRegex) {
    return getTypesAnnotatedWith(annotation, null, excludeRegex);
  }

  public static Collection<MetaMethod> getMethodsAnnotatedWith(final Class<? extends Annotation> annotation,
                                                               final Set<String> packages) {
    final Collection<MetaMethod> result = new HashSet<MetaMethod>(50);
    for (final MetaClass metaClass : MetaClassFactory.getAllCachedClasses()) {
      for (final MetaMethod metaMethod : metaClass.getDeclaredMethods()) {
        if (metaMethod.isAnnotationPresent(annotation)) {
          result.add(metaMethod);
        }
      }
    }

    filterResultsMethod(result, packages, null);

    return Collections.unmodifiableCollection(result);
  }

  public static Collection<MetaField> getFieldsAnnotatedWith(final Class<? extends Annotation> annotation,
                                                             final Set<String> packages) {
    final Collection<MetaField> result = new HashSet<MetaField>(50);

    for (final MetaClass metaClass : MetaClassFactory.getAllCachedClasses()) {
      for (final MetaField metaField : metaClass.getDeclaredFields()) {
        if (metaField.isAnnotationPresent(annotation)) {
          result.add(metaField);
        }
      }
    }

    filterResultsField(result, packages, null);

    return Collections.unmodifiableCollection(result);
  }

  public static Collection<MetaClass> getSubTypesOf(final MetaClass metaClass) {
    final MetaClass root = metaClass.getErased();
    final Set<MetaClass> result = Collections.newSetFromMap(new ConcurrentHashMap<MetaClass, Boolean>());

    final Future<?> factoryFuture = ThreadUtil.submit(new Runnable() {
      @Override
      public void run() {
        for (final MetaClass mc : MetaClassFactory.getAllCachedClasses()) {
          if (!NullType.class.getName().equals(mc.getFullyQualifiedName())
              && !root.getFullyQualifiedName().equals(mc.getFullyQualifiedName())
              && root.isAssignableFrom(mc)) {
            result.add(mc.getErased());
          }
        }
      }
    });

    if (reflectionsScanning && EnvUtil.isProdMode()) {
      final Future<?> reflectionsFuture = ThreadUtil.submit(new Runnable() {
        @Override
        public void run() {
          final Class<?> cls = root.asClass();
          if (cls != null && !cls.equals(NullType.class)) {
            for (final Class<?> c : ScannerSingleton.getOrCreateInstance().getSubTypesOf(cls)) {
              if (!c.isAnonymousClass() && !c.isSynthetic()) {
                result.add(MetaClassFactory.get(c));
              }
            }
          }
        }
      });
      try {
        reflectionsFuture.get();
      }
      catch (Exception ignored) {
      }
    }

    try {
      factoryFuture.get();
    }
    catch (Exception ignored) {
    }

    return result;
  }

  private static void filterResultsClass(final Collection<MetaClass> result,
                                         final Set<String> packages,
                                         final String excludeRegEx) {

    final Pattern excludePattern;
    if (excludeRegEx != null) {
      excludePattern = Pattern.compile(excludeRegEx);
    }
    else {
      excludePattern = null;
    }

    final Iterator<MetaClass> filterIterator = result.iterator();
    while (filterIterator.hasNext()) {
      _removeIfNotMatches(filterIterator, filterIterator.next(), packages, excludePattern);
    }
  }

  private static void filterResultsMethod(final Collection<MetaMethod> result,
                                          final Set<String> packages,
                                          final String excludeRegEx) {

    final Pattern excludePattern;
    if (excludeRegEx != null) {
      excludePattern = Pattern.compile(excludeRegEx);
    }
    else {
      excludePattern = null;
    }

    final Iterator<MetaMethod> filterIterator = result.iterator();
    while (filterIterator.hasNext()) {
      _removeIfNotMatches(filterIterator, filterIterator.next().getDeclaringClass(), packages, excludePattern);
    }
  }

  private static void filterResultsField(final Collection<MetaField> result,
                                         final Set<String> packages,
                                         final String excludeRegEx) {

    final Pattern excludePattern;
    if (excludeRegEx != null) {
      excludePattern = Pattern.compile(excludeRegEx);
    }
    else {
      excludePattern = null;
    }

    final Iterator<MetaField> filterIterator = result.iterator();
    while (filterIterator.hasNext()) {
      _removeIfNotMatches(filterIterator, filterIterator.next().getDeclaringClass(), packages, excludePattern);
    }
  }

  private static void filterResultsParameter(final Collection<MetaParameter> result,
                                             final Set<String> packages,
                                             final String excludeRegEx) {

    final Pattern excludePattern;
    if (excludeRegEx != null) {
      excludePattern = Pattern.compile(excludeRegEx);
    }
    else {
      excludePattern = null;
    }

    final Iterator<MetaParameter> filterIterator = result.iterator();
    while (filterIterator.hasNext()) {
      _removeIfNotMatches(filterIterator, filterIterator.next().getDeclaringMember().getDeclaringClass(),
          packages, excludePattern);
    }
  }

  private static void _removeIfNotMatches(final Iterator<?> iterator,
                                          final MetaClass type,
                                          final Set<String> packages,
                                          final Pattern excludePattern) {

    if (packages == null || packages.contains(type.getPackageName())) {
      if (excludePattern != null && excludePattern.matcher(type.getFullyQualifiedName()).matches()) {
        iterator.remove();
      }
    }
    else {
      iterator.remove();
    }
  }

  private static void recordCache(final Map<Class<? extends  Annotation>, List<MetaClass>> map,
                                  final Class<? extends Annotation> a, final MetaClass mc) {
    List<MetaClass> cls = map.get(a);
    if (cls == null) {
      map.put(a, cls = new ArrayList<MetaClass>(10));
    }
    cls.add(mc);
  }

  public static void resetCache() {
    SCAN_CACHE.clear();
  }

  public static void setReflectionsScanning(final boolean bool) {
    reflectionsScanning = bool;
  }

  public static AtomicLong getTotalClassScanTime() {
    return totalClassScanTime;
  }
}
