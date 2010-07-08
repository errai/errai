package org.jboss.errai.ioc.rebind.ioc;

import com.google.gwt.core.ext.typeinfo.*;
import org.mvel2.util.ReflectionUtil;
import org.mvel2.util.StringAppender;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class InjectUtil {
    private static final Class[] injectionAnnotations
            = {Inject.class, com.google.inject.Inject.class};

    private static final AtomicInteger counter = new AtomicInteger(0);

    public static ConstructionStrategy getConstructionStrategy(final Injector injector, final InjectionContext ctx) {
        final JClassType type = injector.getInjectedType();

        final List<JConstructor> constructorInjectionPoints = scanForConstructorInjectionPoints(type);
        final List<InjectionTask> injectionTasks = scanForTasks(injector, ctx, type);
        final List<JMethod> postConstructTasks = scanForPostConstruct(type);

        if (!constructorInjectionPoints.isEmpty()) {
            // constructor injection

            if (constructorInjectionPoints.size() > 1) {
                throw new InjectionFailure("more than one constructor in "
                        + type.getQualifiedSourceName() + " is marked as the injection point!");
            }

            final JConstructor constructor = constructorInjectionPoints.get(0);

            return new ConstructionStrategy() {
                public String generateConstructor() {
                    String[] vars = resolveInjectionDependencies(constructor.getParameters(), ctx);

                    StringAppender appender = new StringAppender("final ").append(type.getQualifiedSourceName())
                            .append(' ').append(injector.getVarName()).append(" = new ")
                            .append(type.getQualifiedSourceName())
                            .append('(').append(commaDelimitedList(vars)).append(");\n");

                    handleInjectionTasks(appender, ctx, injectionTasks);

                    doPostConstruct(appender, injector, ctx, postConstructTasks);

                    return appender.toString();
                }
            };

        } else {
            // field injection

            if (!hasDefaultConstructor(type))
                throw new InjectionFailure("there is no default constructor for type: " + type.getQualifiedSourceName());

            return new ConstructionStrategy() {
                public String generateConstructor() {
                    StringAppender appender = new StringAppender("final ").append(type.getQualifiedSourceName())
                            .append(' ').append(injector.getVarName()).append(" = new ")
                            .append(type.getQualifiedSourceName()).append("();\n");

                    handleInjectionTasks(appender, ctx, injectionTasks);

                    doPostConstruct(appender, injector, ctx, postConstructTasks);

                    return appender.toString();
                }
            };
        }
    }

    private static void handleInjectionTasks(StringAppender appender, InjectionContext ctx,
                                             List<InjectionTask> tasks) {

        for (InjectionTask task : tasks) {
            appender.append(task.doTask(ctx));
        }

    }

    private static void generateFieldAndSetterInjectionPoints(StringAppender appender,
                                                              Injector injector, InjectionContext ctx,
                                                              List<JField> fieldInjectionPoints,
                                                              List<JMethod> setterInjectionPoints) {
        for (JField fld : fieldInjectionPoints) {
            appender.append(injector.getVarName()).append('.').append(fld.getName()).append(" = ")
                    .append(ctx.getInjector(fld.getType().isClassOrInterface()).getType(ctx))
                    .append(";\n");
        }

        for (JMethod meth : setterInjectionPoints) {
            appender.append(injector.getVarName()).append('.')
                    .append(meth.getName()).append('(');

            String[] vars = resolveInjectionDependencies(meth.getParameters(), ctx);

            appender.append(commaDelimitedList(vars)).append(");\n");
        }
    }

    private static void doPostConstruct(StringAppender appender, Injector injector, InjectionContext ctx,
                                        List<JMethod> postConstructTasks) {
        for (JMethod meth : postConstructTasks) {
            if (!meth.isPublic() || meth.getParameters().length != 0) {
                throw new InjectionFailure("PostConstruct method must be public and contain no parameters: "
                        + injector.getInjectedType().getQualifiedSourceName() + "." + meth.getName());
            }

            appender.append(injector.getVarName()).append('.').append(meth.getName()).append("();\n");
        }
    }


    private static List<InjectionTask> scanForTasks(Injector injector, InjectionContext ctx, JClassType type) {
        final List<InjectionTask> accumulator = new LinkedList<InjectionTask>();
        final Set<Class<? extends Annotation>> decorators = ctx.getDecoratorAnnotations();

        for (JField field : type.getFields()) {
            if (isInjectionPoint(field)) {
                if (!field.isPublic()) {
                    try {
                        JMethod meth = type.getMethod(ReflectionUtil.getSetter(field.getName()), new JType[]{field.getType()});
                        InjectionTask task = new InjectionTask(injector, meth);
                        task.setField(field);
                        accumulator.add(task);
                    }
                    catch (NotFoundException e) {
                        throw new InjectionFailure("attempt to inject on a non-public field: "
                                + type.getQualifiedSourceName() + "." + field.getName());
                    }
                } else {
                    accumulator.add(new InjectionTask(injector, field));
                }
            }

            for (Class<? extends Annotation> a : decorators) {
                if (field.isAnnotationPresent(a)) {
                    if (!field.isPublic()) {
                        try {
                            JMethod meth = type.getMethod(ReflectionUtil.getGetter(field.getName()), new JType[0]);
                            DecoratorTask task = new DecoratorTask(injector, meth, ctx.getDecorator(a));
                            task.setField(field);
                            accumulator.add(task);
                        }
                        catch (NotFoundException e) {
                            throw new InjectionFailure("attempt to decorate a non-public field: "
                                    + type.getQualifiedSourceName() + "." + field.getName());
                        }
                    } else {
                        accumulator.add(new DecoratorTask(injector, field, ctx.getDecorator(a)));
                    }
                }
            }
        }

        for (JMethod meth : type.getMethods()) {
            if (isInjectionPoint(meth)) {
                accumulator.add(new InjectionTask(injector, meth));
            }

            for (Class<? extends Annotation> a : decorators) {
                if (meth.isAnnotationPresent(a)) {
                    accumulator.add(new DecoratorTask(injector, meth, ctx.getDecorator(a)));
                }
            }
        }


        return accumulator;
    }

    private static List<JConstructor> scanForConstructorInjectionPoints(JClassType type) {
        final List<JConstructor> accumulator = new LinkedList<JConstructor>();

        for (JConstructor cns : type.getConstructors()) {
            if (isInjectionPoint(cns)) {
                accumulator.add(cns);
            }
        }

        return accumulator;

    }

    private static List<JMethod> scanForPostConstruct(JClassType type) {
        final List<JMethod> accumulator = new LinkedList<JMethod>();

        for (JMethod meth : type.getMethods()) {
            if (meth.isAnnotationPresent(PostConstruct.class)) {
                accumulator.add(meth);
            }
        }

        return accumulator;
    }


    private static boolean isInjectionPoint(JField field) {
        for (Class<? extends Annotation> ann : injectionAnnotations) {
            if (field.isAnnotationPresent(ann)) return true;
        }
        return false;
    }

    private static boolean isInjectionPoint(JMethod meth) {
        for (Class<? extends Annotation> ann : injectionAnnotations) {
            if (meth.isAnnotationPresent(ann)) return true;
        }
        return false;
    }


    private static boolean isInjectionPoint(JConstructor constructor) {
        for (Class<? extends Annotation> ann : injectionAnnotations) {
            if (constructor.isAnnotationPresent(ann)) return true;
        }
        return false;
    }


    private static boolean hasDefaultConstructor(JClassType type) {
        try {
            type.getConstructor(new JType[0]);
            return true;
        }
        catch (NotFoundException e) {
            return false;
        }
    }

    private static JClassType[] parametersToClassTypeArray(JParameter[] parms) {
        JClassType[] newArray = new JClassType[parms.length];
        for (int i = 0; i < parms.length; i++) {
            newArray[i] = parms[i].getType().isClassOrInterface();
        }
        return newArray;
    }

    public static String[] resolveInjectionDependencies(JParameter[] parms, InjectionContext ctx) {
        JClassType[] parmTypes = parametersToClassTypeArray(parms);
        String[] varNames = new String[parmTypes.length];

        for (int i = 0; i < parmTypes.length; i++) {
            varNames[i] = ctx.getInjector(parmTypes[i]).getType(ctx);
        }

        return varNames;
    }

    public static String commaDelimitedList(String[] parts) {
        StringAppender appender = new StringAppender();
        for (int i = 0; i < parts.length; i++) {
            appender.append(parts[i]);
            if ((i + 1) < parts.length) appender.append(", ");
        }
        return appender.toString();
    }

    public static String getNewVarName() {
        return "inj" + counter.addAndGet(1);
    }


}
