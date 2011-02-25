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

import com.google.gwt.core.ext.typeinfo.*;

public class ContextualProviderInjector extends TypeInjector {
    private final Injector providerInjector;

    public ContextualProviderInjector(JClassType type, JClassType providerType) {
        super(type);
        this.providerInjector = new TypeInjector(providerType);
    }

    @Override
    public String getType(InjectionContext injectContext, InjectionPoint injectionPoint) {
        injected = true;

        JClassType type = null;
        JParameterizedType pType = null;

        switch (injectionPoint.getTaskType()) {
            case PrivateField:
            case Field:
                JField field = injectionPoint.getField();
                type = field.getType().isClassOrInterface();

                pType = type.isParameterized();

                break;

            case Parameter:
                JParameter parm = injectionPoint.getParm();
                type = parm.getType().isClassOrInterface();

                pType = type.isParameterized();
                break;
        }

        StringBuilder sb = new StringBuilder();

        if (pType == null) {
            sb.append(providerInjector.getType(injectContext, injectionPoint)).append(".provide(new Class[] {}");
        } else {
            JClassType[] typeArgs = pType.getTypeArgs();
            sb.append("(").append(type.getQualifiedSourceName()).append("<")
                    .append(typeArgs[0].getQualifiedSourceName()).append(">) ");

            sb.append(providerInjector.getType(injectContext, injectionPoint)).append(".provide(new Class[] {");
            for (int i = 0; i < typeArgs.length; i++) {
                sb.append(typeArgs[i].getQualifiedSourceName()).append(".class");

                if ((i + 1) < typeArgs.length) {
                    sb.append(", ");
                }
            }
            sb.append("}");
        }


        return sb.append(")").toString();

    }

    @Override
    public String instantiateOnly(InjectionContext injectContext, InjectionPoint injectionPoint) {
        injected = true;
        return providerInjector.getType(injectContext, injectionPoint);
    }
}
