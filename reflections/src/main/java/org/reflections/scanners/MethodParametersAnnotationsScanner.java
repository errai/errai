package org.reflections.scanners;

import java.util.List;

/**
 *
 */
@SuppressWarnings({"unchecked"})
public class MethodParametersAnnotationsScanner extends AbstractScanner {
    public void scan(final Object cls) {
        String className = getMetadataAdapter().getClassName(cls);

        List<Object> methods = getMetadataAdapter().getMethods(cls);
        for (Object method : methods) {
            List<String> parameters = getMetadataAdapter().getParameterNames(method);
            for (int parameterIndex = 0; parameterIndex < parameters.size(); parameterIndex++) {
                List<String> parameterAnnotations = getMetadataAdapter().getParameterAnnotationNames(method, parameterIndex);
                for (String parameterAnnotation : parameterAnnotations) {
                    if (acceptResult(parameterAnnotation)) {
                        getStore().put(parameterAnnotation, String.format("%s.%s:%s %s", className, method, parameters.get(parameterIndex), parameterAnnotation));
                    }
                }
            }
        }
    }
}
