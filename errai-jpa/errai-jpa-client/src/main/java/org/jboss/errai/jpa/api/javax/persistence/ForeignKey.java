/*
 * Copyright (c) 2008, 2009, 2011 Oracle, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.  The Eclipse Public License is available
 * at http://www.eclipse.org/legal/epl-v10.html and the Eclipse Distribution License
 * is available at http://www.eclipse.org/org/documents/edl-v10.php.
 */
package javax.persistence;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The ForeignKey annotation is used in schema generation. It is used to define a foreign key constraint or to
 * override or disable the persistence provider’s default foreign key definition.
 *
 * @since JPA 2.1
 */
@Target({})
@Retention(RUNTIME)
public @interface ForeignKey {
	/**
	 * (Optional) The name of the foreign key constraint.  Defaults to a provider-generated name.
	 *
	 * @return The foreign key name
	 */
	String name() default "";

	/**
	 * (Optional) The foreign key constraint definition.  Default is provider defined.  If the value of
	 * disableForeignKey is true, the provider must not generate a foreign key constraint.
	 *
	 * @return The foreign key definition
	 */
	String foreignKeyDefinition() default "";

	/**
	 * (Optional) Used to specify whether a foreign key constraint should be generated when schema generation is in effect.
	 */
	ConstraintMode value() default ConstraintMode.CONSTRAINT;
}
