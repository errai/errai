/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.client.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the annotated bean is a replacement type for all matching common subtypes in the context of a unit test.
 * <p>
 * When running unit tests with Errai IOC managed applications, beans which have been annotated with <tt>@TestMock</tt>
 * will be given injection consideration preference over any other matching beans.
 * <p>
 * For the purposes of beans annotated with <tt>@TestMock</tt>, the bean is treated as an otherwise normal bean, with
 * normal resolution rules, including the use of qualifiers.
 *
 * @author Mike Brock
 * @author Christian Sadilek
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestMock {
}
