package org.jboss.errai.ioc.rebind.ioc.codegen;

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.TypeNotIterableException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.mvel2.DataConversion;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class GenUtil {
    private static final Map<MetaClass, MetaClass> primitiveWrappers = new HashMap<MetaClass, MetaClass>() {{
        put(MetaClassFactory.get(boolean.class), MetaClassFactory.get(Boolean.class));
        put(MetaClassFactory.get(byte.class), MetaClassFactory.get(Byte.class));
        put(MetaClassFactory.get(char.class), MetaClassFactory.get(Character.class));
        put(MetaClassFactory.get(double.class), MetaClassFactory.get(Double.class));
        put(MetaClassFactory.get(float.class), MetaClassFactory.get(Float.class));
        put(MetaClassFactory.get(int.class), MetaClassFactory.get(Integer.class));
        put(MetaClassFactory.get(long.class), MetaClassFactory.get(Long.class));
        put(MetaClassFactory.get(short.class), MetaClassFactory.get(Short.class));
    }};
    
    public static Statement[] generateCallParameters(Context context, Object... parameters) {
        Statement[] statements = new Statement[parameters.length];
        int i = 0;
        for (Object o : parameters) {
            statements[i++] = generate(context, o);
        }
        return statements;
    }

    public static Statement generate(Context context, Object o) {
        if (o instanceof VariableReference) {
            return context.getVariable(((VariableReference) o).getName());
        } else if (o instanceof Variable) {
            Variable v = (Variable) o;
            if (context.isScoped(v)) {
                return v.getReference();
            } else {
                throw new OutOfScopeException("variable cannot be referenced from this scope: " + v.getName());
            }
        } else if (o instanceof Statement) {
            return (Statement) o;
        } else {
            return LiteralFactory.getLiteral(o);
        }
    }

    public static void assertIsIterable(Statement statement) {
        try {
            Class<?> cls = Class.forName(statement.getType().getFullyQualifedName(), false,
                    Thread.currentThread().getContextClassLoader());

            if (!cls.isArray() && !Iterable.class.isAssignableFrom(cls))
                throw new TypeNotIterableException(statement.generate(Context.create()));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static MetaClass getComponentType(Statement statement) {
        try {
            Class<?> cls = Class.forName(statement.getType().getFullyQualifedName(), false,
                    Thread.currentThread().getContextClassLoader());

            if (cls.getComponentType() != null)
                return MetaClassFactory.get(cls.getComponentType());

            return null;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void assertAssignableTypes(MetaClass from, MetaClass to) {
        try {
            from = primitiveToWrapperType(from);
            to = primitiveToWrapperType(to);
            
            Class<?> fromCls = Class.forName(from.getFullyQualifedName(), false,
                    Thread.currentThread().getContextClassLoader());

            Class<?> toCls = Class.forName(to.getFullyQualifedName(), false,
                    Thread.currentThread().getContextClassLoader());

            if (!toCls.isAssignableFrom(fromCls))
                throw new InvalidTypeException(to.getFullyQualifedName() +
                        " is not assignable from " + from.getFullyQualifedName());

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Statement convert(Context context, Object input, MetaClass targetType) {
        try {
            if (input instanceof Statement) {
                assertAssignableTypes(((Statement) input).getType(), targetType);
                return (Statement) input;
            }

            Class<?> targetClass = Class.forName(targetType.getFullyQualifedName(), false,
                    Thread.currentThread().getContextClassLoader());

            if (DataConversion.canConvert(targetClass, input.getClass())) {
                return generate(context, DataConversion.convert(input, targetClass));
            } else {
                throw new InvalidTypeException("cannot convert input to target type:" + targetClass.getName());
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        } catch (Throwable t) {
            throw new InvalidTypeException(t);
        }
    }

    public static MetaClass primitiveToWrapperType(MetaClass type) {
        MetaClass wrapperType = primitiveWrappers.get(type);
        return (wrapperType != null) ? wrapperType : type;
    }
}