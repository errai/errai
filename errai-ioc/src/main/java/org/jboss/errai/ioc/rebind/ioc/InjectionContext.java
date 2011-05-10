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
import com.google.gwt.core.ext.typeinfo.JField;
import org.jboss.errai.bus.rebind.ProcessingContext;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.*;

public class InjectionContext {
    private ProcessingContext processingContext;
    private Map<JClassType, List<Injector>> injectors = new LinkedHashMap<JClassType, List<Injector>>();
    private Map<Class<? extends Annotation>, List<IOCExtension>> decorators = new LinkedHashMap<Class<? extends Annotation>, List<IOCExtension>>();
    private Map<ElementType, Set<Class<? extends Annotation>>> decoratorsByElementType = new LinkedHashMap<ElementType, Set<Class<? extends Annotation>>>();
    private List<JField> privateFieldsToExpose = new ArrayList<JField>();

    public InjectionContext(ProcessingContext processingContext) {
        this.processingContext = processingContext;
    }

    public Injector getQualifiedInjector(JClassType type, QualifyingMetadata metadata) {
        JClassType erased = type.getErasedType();
        List<Injector> injs = injectors.get(erased);
        if (injs != null) {
            for (Injector inj : injs) {
                if (metadata == null && inj.getQualifyingMetadata() == null) {
                    return inj;
                } else if (metadata != null && inj.getQualifyingMetadata() != null
                        && metadata.doesSatisfy(inj.getQualifyingMetadata())) {
                    return inj;
                }
            }
        }
        throw new InjectionFailure("could not resolve type for injection: " + erased.getQualifiedSourceName());
    }

    public Injector getInjector(Class<?> injectorType) {
        return getInjector(processingContext.loadClassType(injectorType));
    }

    public Injector getInjector(JClassType type) {
        JClassType erased = type.getErasedType();
        if (!injectors.containsKey(erased)) {
            throw new InjectionFailure("could not resolve type for injection: " + erased.getQualifiedSourceName());
        }
        List<Injector> injectorList = injectors.get(erased);
        if (injectorList.size() > 1) {
            throw new InjectionFailure("ambiguous injection type (multiple injectors resolved): "
                    + erased.getQualifiedSourceName());
        } else if (injectorList.isEmpty()) {
            throw new InjectionFailure("could not resolve type for injection: " + erased.getQualifiedSourceName());
        }

        return injectorList.get(0);
    }

    public List<Injector> getInjectorsByType(Class<? extends Injector> injectorType) {
        List<Injector> injs = new LinkedList<Injector>();
        for (List<Injector> inj : injectors.values()) {
            if (injectorType.isAssignableFrom(inj.getClass())) {
                injs.addAll(inj);
            }
        }
        return injs;
    }

    public void registerInjector(Injector injector) {
        List<Injector> injectorList = injectors.get(injector.getInjectedType().getErasedType());
        if (injectorList == null) {
            injectors.put(injector.getInjectedType().getErasedType(), injectorList = new ArrayList<Injector>());
        } else {
            for (Injector inj : injectorList) {
                if (inj.metadataMatches(injector)) {
                    return;
                }
            }
        }

        injectorList.add(injector);
    }

    public void registerDecorator(IOCExtension<?> IOCExtension) {
        if (!decorators.containsKey(IOCExtension.decoratesWith()))
            decorators.put(IOCExtension.decoratesWith(), new ArrayList<IOCExtension>());

        decorators.get(IOCExtension.decoratesWith()).add(IOCExtension);
    }

    public Set<Class<? extends Annotation>> getDecoratorAnnotations() {
        return Collections.unmodifiableSet(decorators.keySet());
    }

    public IOCExtension[] getDecorator(Class<? extends Annotation> annotation) {
        List<IOCExtension> decs = decorators.get(annotation);
        IOCExtension[] da = new IOCExtension[decs.size()];
        decs.toArray(da);
        return da;
    }

    public Set<Class<? extends Annotation>> getDecoratorAnnotationsBy(ElementType type) {
        if (decoratorsByElementType.size() == 0) {
            sortDecorators();
        }
        if (decoratorsByElementType.containsKey(type)) {
            return Collections.unmodifiableSet(decoratorsByElementType.get(type));
        } else {
            return Collections.emptySet();
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

    public List<JField> getPrivateFieldsToExpose() {
        return privateFieldsToExpose;
    }

    public ProcessingContext getProcessingContext() {
        return processingContext;
    }
}
