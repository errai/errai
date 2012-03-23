package org.jboss.errai.codegen.framework;

import static org.jboss.errai.codegen.framework.util.PrettyPrinter.prettyPrintJava;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jboss.errai.codegen.framework.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.framework.literal.NullLiteral;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.Stmt;

import com.google.gwt.dev.util.collect.IdentityHashSet;

public final class SnapshotMaker {

  private SnapshotMaker() {}



  /**
   *
   * @param o
   * @param typeToExtend
   * @param typesToRecurseOn
   * @return
   */
  public static Statement makeSnapshotAsSubclass(final Object o,
                                                 final Class<?> typeToExtend,
                                                 final Class<?> ... typesToRecurseOn) {
    return makeSnapshotAsSubclass(o, typeToExtend, new HashSet<Class<?>>(Arrays.asList(typesToRecurseOn)),
        new IdentityHashMap<Object, Statement>(), new IdentityHashSet<Object>());
  }

  private static Statement makeSnapshotAsSubclass(
      final Object o,
      final Class<?> typeToExtend,
      final Set<Class<?>> typesToRecurseOn,
      final IdentityHashMap<Object, Statement> existingSnapshots,
      final IdentityHashSet<Object> unfinishedSnapshots) {

    if (o == null) {
      return NullLiteral.INSTANCE;
    }

    if (!typeToExtend.isInstance(o)) {
      throw new IllegalArgumentException(
          "Given object (of type " + o.getClass().getName() +
          ") is not an instance of requested type " + typeToExtend.getName());
    }

    final List<Method> sortedMethods = Arrays.asList(typeToExtend.getMethods());
    Collections.sort(sortedMethods, new Comparator<Method>() {
      @Override
      public int compare(Method m1, Method m2) {
        return m1.getName().compareTo(m2.getName());
      }
    });

    Iterator<Method> it = sortedMethods.iterator();
    while (it.hasNext()) {
      Method m = it.next();
      if ("equals".equals(m.getName()) || "hashCode".equals(m.getName())) {
        it.remove();
        continue;
      }
      if (m.getParameterTypes().length > 0) {
        throw new UnsupportedOperationException("I can't make a snapshot of a type that has public methods with parameters (other than equals()).");
      }
    }

    return new Statement() {
      String generatedCache;

      @Override
      public String generate(Context context) {
        if (generatedCache != null) return generatedCache;

        // create a subcontext and record the types we will allow the LiteralFactory to create automatic
        // snapshots for.
        final Context subContext = Context.create(context);
        subContext.addLiteralizableClasses(typesToRecurseOn);

        final AnonymousClassStructureBuilder builder = ObjectBuilder.newInstanceOf(typeToExtend, context)
            .extend();
        for (Method method : sortedMethods) {
          if (method.getReturnType().equals(void.class)) {
            builder.publicOverridesMethod(method.getName()).finish();
            continue;
          }
          try {
            final Object retval = method.invoke(o);
            Statement methodBody;
            if (existingSnapshots.containsKey(retval)) {
              methodBody = existingSnapshots.get(retval);
            }
            else if (typesToRecurseOn.contains(method.getReturnType())) {
              if (unfinishedSnapshots.contains(retval)) {
                throw new UnsupportedOperationException(
                    "There is a cyclical reference in the object graph. This is not " +
                    "presently supported. Objects involved in the cycle are: " + unfinishedSnapshots);
              }
              unfinishedSnapshots.add(o);
              System.out.println("Recursing on generate. unfinishedSnapshots=" + unfinishedSnapshots);

              // use Stmt.create(context) to pass the context along.
              methodBody = Stmt.create(subContext).nestedCall(makeSnapshotAsSubclass(
                  retval, method.getReturnType(), typesToRecurseOn, existingSnapshots, unfinishedSnapshots)).returnValue();
              unfinishedSnapshots.remove(o);
            }
            else {
              methodBody = Stmt.load(retval).returnValue();
            }

            builder.publicOverridesMethod(method.getName()).append(methodBody).finish();
            existingSnapshots.put(retval, methodBody);
          }
          catch (IllegalAccessException e) {
            throw new RuntimeException("error generation annotation wrapper", e);
          }
          catch (InvocationTargetException e) {
            throw new RuntimeException("error generation annotation wrapper", e);
          }
        }

        return generatedCache = prettyPrintJava(builder.finish().toJavaString());
      }

      @Override
      public MetaClass getType() {
        return MetaClassFactory.get(typeToExtend);
      }
    };
  }

}
