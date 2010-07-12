package org.jboss.errai.ioc.rebind.ioc;

import com.google.gwt.core.ext.typeinfo.JClassType;
import org.jboss.errai.bus.rebind.ProcessingContext;

import java.lang.annotation.Annotation;
import java.util.*;

public class InjectionContext {
    private ProcessingContext processingContext;
    private Map<JClassType, Injector> injectors = new LinkedHashMap<JClassType, Injector>();
    private Map<Class<? extends Annotation>, List<Decorator>> decorators = new LinkedHashMap<Class<? extends Annotation>, List<Decorator>>();

    public InjectionContext(ProcessingContext processingContext) {
        this.processingContext = processingContext;
    }

    public Injector getInjector(JClassType type) {
        if (!injectors.containsKey(type)) {
            throw new InjectionFailure("could not resolve type for injection: " + type.getQualifiedSourceName());
        }
        return injectors.get(type);
    }

    public List<Injector> getInjectorsByType(Class<? extends Injector> injectorType) {
        List<Injector> injs = new LinkedList<Injector>();
        for (Injector i : injectors.values()) {
            if (injectorType.isAssignableFrom(i.getClass())) {
                injs.add(i);
            }
        }
        return injs;
    }

    public void registerInjector(Injector injector) {
        if (!injectors.containsKey(injector.getInjectedType()))
            injectors.put(injector.getInjectedType(), injector);
    }

    public void registerDecorator(Decorator<?> decorator) {
        if (!decorators.containsKey(decorator.decoratesWith()))
            decorators.put(decorator.decoratesWith(), new LinkedList<Decorator>());

        decorators.get(decorator.decoratesWith()).add(decorator);
    }

    public Set<Class<? extends Annotation>> getDecoratorAnnotations() {
        return Collections.unmodifiableSet(decorators.keySet());
    }

    public Decorator[] getDecorator(Class<? extends Annotation> annotation) {
        List<Decorator> decs = decorators.get(annotation);
        Decorator[] da = new Decorator[decs.size()];
        decs.toArray(da);
        return da;
    }

    public ProcessingContext getProcessingContext() {
        return processingContext;
    }
}
