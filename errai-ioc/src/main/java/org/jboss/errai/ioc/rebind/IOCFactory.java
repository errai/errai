/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.ioc.rebind;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.rebind.ProcessingContext;
import org.jboss.errai.ioc.client.api.TypeProvider;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class IOCFactory {
    private Map<JClassType, String> codeInjectors;
    private Map<JClassType, JClassType> typeProviders;
    private Map<Class<? extends Annotation>, FieldGenerator> fieldGenerators;

    public IOCFactory(TypeOracle oracle) {
        codeInjectors = new HashMap<JClassType, String>();
        typeProviders = new HashMap<JClassType, JClassType>();
        fieldGenerators = new HashMap<Class<? extends Annotation>, FieldGenerator>() {
        };
        try {
            codeInjectors.put(oracle.getType(MessageBus.class.getName()), ErraiBus.class.getName() + ".get()");
            codeInjectors.put(oracle.getType(RequestDispatcher.class.getName()), ErraiBus.class.getName() + ".getDispatcher()");
                    }
        catch (NotFoundException e) {
            throw new RuntimeException("error", e);
        }
    }

    public void registerTypeProvider(JClassType type, JClassType provider) {
        typeProviders.put(type, provider);
    }
    
    public JClassType getTypeProvider(JClassType type) {
        return typeProviders.get(type);
    }

    public void addInjectorExpression(JClassType type, String expression) {
        if (codeInjectors.containsKey(type)) {
            throw new RuntimeException("cannot have more than one injector per type: " + type.getClass());
        }

        codeInjectors.put(type, expression);
    }

    public String getInjectorExpression(JClassType type) {
        if (!codeInjectors.containsKey(type))
            throw new RuntimeException("unresolved dependency: " + type.getClass());

        return codeInjectors.get(type);
    }

    public void handleInjection(final Annotation annotation,
                                final ProcessingContext context,
                                final IOCFactory iocFactory,
                                final JClassType visit) {

        for (Class<? extends Annotation> aType : fieldGenerators.keySet()) {
            if (visit.isAnnotationPresent(aType)) {
                fieldGenerators.get(aType).generate(annotation, context, iocFactory, visit);
            }
        }
    }

    public static interface FieldGenerator {
        public Expression generate(final Annotation annotation,
                                   final ProcessingContext context,
                                   final IOCFactory iocFactory,
                                   final JClassType visit);
    }

}

