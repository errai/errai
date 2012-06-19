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

package org.jboss.errai.bus.client.api.interceptor;

/**
 * Represents the context of a call to an intercepted method.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class CallContext {
  private Object result;
  private Object[] parameters;

  /**
   * Returns the result of the intercepted method.
   * 
   * @return intercepted method result
   */
  public Object getResult() {
    return result;
  }

  /**
   * Sets the result of the intercepted method.
   * 
   * @param result
   *          The result to return to the caller of the intercepted method.
   */
  public void setResult(Object result) {
    this.result = result;
  }

  /**
   * Provides access to the intercepted method's parameters.
   * 
   * @return Array of method parameters in declaration order. An empty array if the intercepted method has no
   *         parameters.
   */
  public Object[] getParameters() {
    return parameters;
  }

  /**
   * Overrides the parameters that are passed to the method for which the interceptor was invoked.
   * 
   * @param parameters
   *          the parameters to use when invoking the intercepted method
   */
  public void setParameters(Object[] parameters) {
    this.parameters = parameters;
  }

  /**
   * Returns the name of the intercepted method.
   * 
   * @return the name of the method for which the interceptor was invoked.
   */
  public abstract String getMethodName();

  /**
   * Proceeds with the execution of the intercepted method.
   */
  public abstract void proceed();

  /**
   * Returns whether or not the interceptor was told to proceed with the executaion of the intercepted method.
   * 
   * @return true, if {@link #proceed()} was called on this interceptor, otherwise false.
   */
  public abstract boolean isProceeding();
}