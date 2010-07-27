/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.rebind.ioc;

import com.google.gwt.core.ext.typeinfo.JClassType;
import org.jboss.errai.bus.rebind.ProcessingContext;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.*;

public class InjectionContext {
    private ProcessingContext processingContext;
    private Map<JClassType, Injector> injectors = new LinkedHashMap<JClassType, Injector>();
    private Map<Class<? extends Annotation>, List<Decorator>> decorators = new LinkedHashMap<Class<? extends Annotation>, List<Decorator>>();

    private Map<ElementType, Set<Class<? extends Annotation>>> decoratorsByElementType = new LinkedHashMap<ElementType, Set<Class<? extends Annotation>>>();

    public InjectionContext(ProcessingContext processingContext) {
        this.processingContext = processingContext;
    }

    public Injector getInjector(JClassType type) {
        JClassType erased = type.getErasedType();
        if (!injectors.containsKey(erased)) {
            throw new InjectionFailure("could not resolve type for injection: " + erased.getQualifiedSourceName());
        }
        return injectors.get(erased);
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
            injectors.put(injector.getInjectedType().getErasedType(), injector);
    }

    public void registerDecorator(Decorator<?> decorator) {
        if (!decorators.containsKey(decorator.decoratesWith()))
            decorators.put(decorator.decoratesWith(), new ArrayList<Decorator>());

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

    public Set<Class<? extends Annotation>> getDecoratorAnnotationsBy(ElementType type) {
        if (decoratorsByElementType.size() == 0) {
            sortDecorators();
        }
        if (decoratorsByElementType.containsKey(type)) {
            return Collections.unmodifiableSet(decoratorsByElementType.get(type));
        }
        else {
            return Collections.EMPTY_SET;
        }
    }


    public boolean hasDecoratorsAssociated(ElementType type, Annotation a) {
        if (decoratorsByElementType.size() == 0) {
            sortDecorators();
        }
        return decoratorsByElementType.containsKey(type) && decoratorsByElementType.get(type).contains(a);
    }

    private void sortDecorators() {
        for (Class<? extends Annotation> a : getDecoratorAnnotations()) {
            if (a.isAnnotationPresent(Target.class)) {
                for (ElementType type : a.getAnnotation(Target.class).value()) {
                    if (!decoratorsByElementType.containsKey(type)) {
                        decoratorsByElementType.put(type, new HashSet<Class<? extends Annotation>>());
                    }
                    decoratorsByElementType.get(type).add(a);
                }
            }
        }
    }

    public ProcessingContext getProcessingContext() {
        return processingContext;
    }
}
