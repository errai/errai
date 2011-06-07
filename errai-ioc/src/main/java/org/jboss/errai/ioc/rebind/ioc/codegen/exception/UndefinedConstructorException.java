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

package org.jboss.errai.ioc.rebind.ioc.codegen.exception;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class UndefinedConstructorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private MetaClass type;
    private MetaClass[] parameterTypes;
    
    public UndefinedConstructorException() {
        super();
    }

    public UndefinedConstructorException(String msg) {
        super(msg);
    }
    
    public UndefinedConstructorException(MetaClass type, MetaClass... parameterTypes) {
        this.type = type;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        
        buf.append(super.toString()).append(": class:").append(type.getFullyQualifedName()).append(" parameterTypes:");
        for(MetaClass type : parameterTypes) {
            buf.append(type.getFullyQualifedName()).append(" ");
        }
        return buf.toString();
    }
}
