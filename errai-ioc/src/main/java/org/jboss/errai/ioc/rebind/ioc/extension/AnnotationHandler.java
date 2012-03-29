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

package org.jboss.errai.ioc.rebind.ioc.extension;

import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.graph.Dependency;
import org.jboss.errai.ioc.rebind.ioc.graph.SortUnit;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface AnnotationHandler<T extends Annotation> {
  public void getDependencies(DependencyControl control,
                                       InjectableInstance instance, T annotation, IOCProcessingContext context);

  public void registerMetadata(InjectableInstance instance, T annotation, IOCProcessingContext context);

  public boolean handle(InjectableInstance instance, T annotation, IOCProcessingContext context);
}
