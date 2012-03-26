package org.jboss.errai.codegen.framework;

import static org.jboss.errai.codegen.framework.util.PrettyPrinter.prettyPrintJava;

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
import org.jboss.errai.codegen.framework.exception.CyclicalObjectGraphException;
import org.jboss.errai.codegen.framework.exception.GenerationException;
import org.jboss.errai.codegen.framework.literal.NullLiteral;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.util.Stmt;

import com.google.gwt.dev.util.collect.IdentityHashSet;

public final class SnapshotMaker {

  private SnapshotMaker() {}

  public static Statement makeSnapshotAsSubclass(
      final Object o,
      final Class<?> typeToExtend,
      final Class<?> ... typesToRecurseOn) {
    MetaClass metaTypeToExtend = MetaClassFactory.get(typeToExtend);
    MetaClass[] metaTypesToRecurseOn = new MetaClass[typesToRecurseOn.length];
    for (int i = 0; i < typesToRecurseOn.length; i++) {
      metaTypesToRecurseOn[i] = MetaClassFactory.get(typesToRecurseOn[i]);
    }
    return makeSnapshotAsSubclass(o, metaTypeToExtend, metaTypesToRecurseOn);
  }

  public static Statement makeSnapshotAsSubclass(
      final Object o,
      final MetaClass typeToExtend,
      final MetaClass ... typesToRecurseOn) {
    return makeSnapshotAsSubclass(o, typeToExtend, new HashSet<MetaClass>(Arrays.asList(typesToRecurseOn)),
        new IdentityHashMap<Object, Statement>(), new IdentityHashSet<Object>());
  }

  private static Statement makeSnapshotAsSubclass(
      final Object o,
      final MetaClass typeToExtend,
      final Set<MetaClass> typesToRecurseOn,
      final IdentityHashMap<Object, Statement> existingSnapshots,
      final IdentityHashSet<Object> unfinishedSnapshots) {

    if (o == null) {
      return NullLiteral.INSTANCE;
    }

    if (!typeToExtend.isAssignableFrom(o.getClass())) {
      throw new IllegalArgumentException(
          "Given object (of type " + o.getClass().getName() +
              ") is not an instance of requested type " + typeToExtend.getName());
    }

    final List<MetaMethod> sortedMethods = Arrays.asList(typeToExtend.getMethods());
    Collections.sort(sortedMethods, new Comparator<MetaMethod>() {
      @Override
      public int compare(MetaMethod m1, MetaMethod m2) {
        return m1.getName().compareTo(m2.getName());
      }
    });

    Iterator<MetaMethod> it = sortedMethods.iterator();
    while (it.hasNext()) {
      MetaMethod m = it.next();
      if ("equals".equals(m.getName()) || "hashCode".equals(m.getName())) {
        it.remove();
        continue;
      }
      if (m.getParameters().length > 0) {
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
        subContext.addLiteralizableMetaClasses(typesToRecurseOn);

        final AnonymousClassStructureBuilder builder = ObjectBuilder.newInstanceOf(typeToExtend, context)
            .extend();
        unfinishedSnapshots.add(o);
        for (MetaMethod method : sortedMethods) {
          if (method.getReturnType().equals(void.class)) {
            builder.publicOverridesMethod(method.getName()).finish();
            continue;
          }
          try {

            final Object retval = typeToExtend.asClass().getMethod(method.getName()).invoke(o);
            Statement methodBody;
            if (existingSnapshots.containsKey(retval)) {
              methodBody = existingSnapshots.get(retval);
            }
            else if (typesToRecurseOn.contains(method.getReturnType())) {
              if (unfinishedSnapshots.contains(retval)) {
                throw new CyclicalObjectGraphException(unfinishedSnapshots);
              }
              System.out.println("Recursing on generate. unfinishedSnapshots=" + unfinishedSnapshots);

              // use Stmt.create(context) to pass the context along.
              methodBody = Stmt.create(subContext).nestedCall(makeSnapshotAsSubclass(
                  retval, method.getReturnType(), typesToRecurseOn, existingSnapshots, unfinishedSnapshots)).returnValue();
            }
            else {
              methodBody = Stmt.load(retval).returnValue();
            }

            builder.publicOverridesMethod(method.getName()).append(methodBody).finish();
            existingSnapshots.put(retval, methodBody);
          }
          catch (RuntimeException e) {
            throw e;
          }
          catch (Exception e) {
            throw new GenerationException("Failed to extract value for snapshot", e);
          }
        }

        generatedCache = prettyPrintJava(builder.finish().toJavaString());
        unfinishedSnapshots.remove(o);
        return generatedCache;
      }

      @Override
      public MetaClass getType() {
        return typeToExtend;
      }
    };
  }

}
