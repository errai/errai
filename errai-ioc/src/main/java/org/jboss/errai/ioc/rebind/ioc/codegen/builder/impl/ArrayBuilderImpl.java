/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import java.lang.reflect.Array;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.GenUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ArrayBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ArrayInitializationBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * StatementBuilder to create and initialize Arrays.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ArrayBuilderImpl extends AbstractStatementBuilder implements ArrayBuilder, ArrayInitializationBuilder {
    StringBuilder buf = new StringBuilder();

    private MetaClass type;
    private MetaClass componentType;
    private Integer[] dimensions;
    private boolean initialized;

    protected ArrayBuilderImpl(Context context, CallElementBuilder callElementBuilder) {
        super(context, callElementBuilder);
    }

    public ArrayInitializationBuilder newArray(Class<?> componentType) {
        return newArray(componentType, new Integer[1]);
    }

    public ArrayInitializationBuilder newArray(Class<?> componentType, Integer... dimensions) {
        this.type = MetaClassFactory.get(Array.newInstance(componentType, 0).getClass());
        this.componentType = MetaClassFactory.get(componentType);
        this.dimensions = dimensions;
        return this;
    }
    
    private void generateArrayInstance() {
        buf.append("new ").append(componentType.getFullyQualifedName());
    }
    
    public AbstractStatementBuilder initialize(Object... values) {
        generateArrayInstance();
        
        int dim = 0;
        if (values.length==1 && values[0].getClass().isArray()) {
            Class<?> type = values[0].getClass();
            while (type.isArray()) {
                dim++;
                type = type.getComponentType();
            }
        }
      
        if (dim==0) dim++;
        for (int i=0; i<dim; i++) {
            buf.append("[]");
        }
        buf.append(" ");
      
        return _initialize(values);
    }
    
    private AbstractStatementBuilder _initialize(Object... values) {
        buf.append("{");

        for (int i=0; i<values.length; i++) {
            if (values[i].getClass().isArray()) {
                int length = Array.getLength(values[i]);
                for (int j = 0; j < length; j++) {
                    Object element = Array.get(values[i], j);
                    if (element.getClass().isArray()) {
                        _initialize(element);
                    } else {
                        _initializeValue(element);
                    }
                    if (j + 1 < length) {
                        buf.append(",");
                    }
                }
            } else {
                _initializeValue(values[i]);
                if (i + 1 < values.length) {
                    buf.append(",");
                }
            }
        }
        
        buf.append("}");
        initialized = true;
        return this;
    }
    
    private void _initializeValue(Object value) {
        Statement statement = GenUtil.generate(context, value);
        // generate to internally set the type
        statement.generate(context);
        GenUtil.assertAssignableTypes(statement.getType(), componentType);
        buf.append(statement.generate(context));
    }

    public MetaClass getType() {
        return type;
    }

    public String generate(Context context) {
        if(!initialized) {
            generateArrayInstance();
           
            for (Integer dim : dimensions) {
                if (dim == null)
                    throw new RuntimeException("Must provide either dimension expressions or an array initializer");

                buf.append("[").append(dim).append("]");
            }
        }
        
        return buf.toString();
    }
}