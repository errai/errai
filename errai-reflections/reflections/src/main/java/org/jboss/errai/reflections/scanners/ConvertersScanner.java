package org.jboss.errai.reflections.scanners;

import java.util.List;

/** scans for methods that take one class as an argument and returns another class */ 
@SuppressWarnings({"unchecked"})
public class ConvertersScanner extends AbstractScanner {
    public void scan(final Object cls) {
        final List<Object> methods = getMetadataAdapter().getMethods(cls);
        for (final Object method : methods) {
            final List<String> parameterNames = getMetadataAdapter().getParameterNames(method);

            if (parameterNames.size() == 1) {
                final String from = parameterNames.get(0);
                final String to = getMetadataAdapter().getReturnTypeName(method);

                if (!to.equals("void") && (acceptResult(from) || acceptResult(to))) {
                    final String methodKey = getMetadataAdapter().getMethodFullKey(cls, method);
                    getStore().put(getConverterKey(from, to), methodKey);
                }
            }
        }
    }

    public static String getConverterKey(final String from, final String to) {
        return from + " to " + to;
    }

    public static String getConverterKey(final Class<?> from, final Class<?> to) {
        return getConverterKey(from.getName(), to.getName());
    }
}
