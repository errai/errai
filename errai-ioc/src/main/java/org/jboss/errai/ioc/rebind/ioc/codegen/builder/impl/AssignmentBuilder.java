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

import org.jboss.errai.ioc.rebind.ioc.codegen.AssignmentOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.VariableReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class AssignmentBuilder implements Statement {
    protected AssignmentOperator operator;
    protected VariableReference reference;
    protected Statement statement;

    public AssignmentBuilder(AssignmentOperator operator, VariableReference reference, Statement statement) {
        this.operator = operator;
        this.reference = reference;
        this.statement = statement;
    }

    public String generate(Context context) {
        operator.assertCanBeApplied(reference.getType());
        operator.assertCanBeApplied(statement.getType());

        return reference.getName() + " " + operator.getCanonicalString() + " " + statement.generate(Context.create());
    }

    public MetaClass getType() {
        return reference.getType();
    }

    public Context getContext() {
        return reference.getContext();
    }
}
