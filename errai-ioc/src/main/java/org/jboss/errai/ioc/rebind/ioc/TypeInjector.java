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

import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypeInjector extends Injector {
    protected final JClassType type;
    protected boolean injected;
    protected boolean singleton;
    protected String varName;

    public TypeInjector(JClassType type) {
        this.type = type;
        this.singleton = type.isAnnotationPresent(Singleton.class)
                || type.isAnnotationPresent(com.google.inject.Singleton.class);
        this.varName = InjectUtil.getNewVarName();
    }

    @Override
    public String getType(InjectionContext injectContext, InjectionPoint injectionPoint) {
        if (isInjected()) {
            if (isSingleton()) {
                return varName;
            } else {
                varName = InjectUtil.getNewVarName();
            }
        }

        ConstructionStrategy cs = InjectUtil.getConstructionStrategy(this, injectContext);

        String generated = cs.generateConstructor();
        injectContext.getProcessingContext().getWriter().print(generated);

        try {
            Class<?> cls = Class.forName(type.getQualifiedSourceName());

            Set<Annotation> qualifiers = new HashSet<Annotation>();
            for (Annotation a : cls.getAnnotations()) {
                if (a.annotationType().isAnnotationPresent(Qualifier.class)) {
                    qualifiers.add(a);
                }
            }

            if (!qualifiers.isEmpty()) {
                qualifyingMetadata = new JSR299QualifyingMetadata(qualifiers);
            }

        } catch (Throwable e) {
            // ignore
        }

        injected = true;

        return varName;
    }

    @Override
    public String instantiateOnly(InjectionContext injectContext, InjectionPoint injectionPoint) {
        return getType(injectContext, injectionPoint);
    }

    @Override
    public boolean isInjected() {
        return injected;
    }

    @Override
    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    @Override
    public String getVarName() {
        return varName;
    }

    @Override
    public JClassType getInjectedType() {
        return type;
    }
}
