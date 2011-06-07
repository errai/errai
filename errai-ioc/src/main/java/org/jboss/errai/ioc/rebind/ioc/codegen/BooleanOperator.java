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

package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public enum BooleanOperator implements Operator {
    Or("||", 3, Boolean.class),
    And("&&", 4, Boolean.class),
    Equals("==", 8),
    NotEquals("!=", 8),
    InstanceOf("instanceof", 9),
    GreaterThanOrEqual(">=", 9, Number.class),
    GreaterThan(">", 9, Number.class),
    LessThanOrEqual("<=", 9, Number.class),
    LessThan("<", 9, Number.class);

    private final Operator operator;

    BooleanOperator(String canonicalString, int operatorPrecedence, Class<?>... constraints) {
        operator = new OperatorImpl(canonicalString, operatorPrecedence, constraints);
    }

    public String getCanonicalString() {
        return operator.getCanonicalString();
    }

    public int getOperatorPrecedence() {
        return operator.getOperatorPrecedence();
    }

    public boolean isHigherPrecedenceThan(Operator operator) {
        return operator.getOperatorPrecedence() < getOperatorPrecedence();
    }

    public boolean isEqualOrHigherPrecedenceThan(Operator operator) {
        return operator.getOperatorPrecedence() <= getOperatorPrecedence();
    }

    public boolean canBeApplied(MetaClass clazz) {
        return operator.canBeApplied(clazz);
    }

    public void assertCanBeApplied(MetaClass clazz) {
        operator.assertCanBeApplied(clazz);
    }
}
