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
import com.google.gwt.core.ext.typeinfo.JParameterizedType;

public class ContextualProviderInjector extends TypeInjector {
    private final Injector providerInjector;

    public ContextualProviderInjector(JClassType type, JClassType providerType) {
        super(type);
        this.providerInjector = new TypeInjector(providerType);
    }

    @Override
    public String getType(InjectionContext injectContext, InjectionPoint injectionPoint) {
        injected = true;
        StringBuilder sb = new StringBuilder(providerInjector.getType(injectContext, injectionPoint) + ".provide(");

        switch (injectionPoint.getTaskType()) {
            case Field:
                JField field = injectionPoint.getField();
                JClassType type = field.getType().isClassOrInterface();

                JParameterizedType pType = type.isParameterized();
                if (pType != null) {
                    JClassType[] typeArgs = pType.getTypeArgs();
                    for (int i = 0; i < typeArgs.length; i++) {
                        sb.append(typeArgs[i].getQualifiedSourceName()).append(".class");

                        if ((i + 1) < typeArgs.length) {
                            sb.append(", ");
                        }
                    }
                }
                break;
        }

        return sb.append(")").toString();

    }

    @Override
    public String instantiateOnly(InjectionContext injectContext, InjectionPoint injectionPoint) {
        injected = true;
        return providerInjector.getType(injectContext, injectionPoint);
    }
}
