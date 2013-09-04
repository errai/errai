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

package org.jboss.errai.ioc.rebind.ioc.metadata;

import org.jboss.errai.codegen.Statement;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.Any;

/**
 * @author Mike Brock
 */
public interface QualifyingMetadata {
  public boolean doesSatisfy(QualifyingMetadata metadata);
  
  public Statement render();
  
  public Annotation[] getQualifiers();

  /**
   * Return a copy of this object with the given annotation filtered out.
   * 
   * @param annotation A qualifier to be filtered out.
   * @return A copy of this object less the filtered annotation.
   */
  public QualifyingMetadata filter(Annotation annotation);
}
