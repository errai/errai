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

/**
 * An InitBallot is injected by the container into a bean which declares a dependency on it. By doing so, the bean
 * enjoins itself into the startup contract of the framework services. Thus, it is expected to call {@link #voteForInit()}
 * to indicate that it is okay to proceed with initialization. The class must be parameterized with a class which
 * represents the startup dependency.
 *
 * @author Mike Brock
 */
public interface InitBallot<T> {
  /**
   * When called, the class's lock on the startup procedure for framework services is released.
   */
  public void voteForInit();
}
