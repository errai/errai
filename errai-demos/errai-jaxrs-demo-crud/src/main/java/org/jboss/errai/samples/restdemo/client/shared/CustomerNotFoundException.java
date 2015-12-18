/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.samples.restdemo.client.shared;

/**
 * A simple exception thrown when a requested customer cannot be found.
 *
 * @author eric.wittmann@redhat.com
 */
public class CustomerNotFoundException extends RuntimeException {

  private static final long serialVersionUID = -5011126601642857097L;
  
  private long customerId;
  
  /**
   * Constructor.
   */
  public CustomerNotFoundException() {
  }
  
  /**
   * Constructor.
   * @param customerId
   */
  public CustomerNotFoundException(long customerId) {
    super("Customer with id '" + customerId + "' not found.");
    setCustomerId(customerId);
  }

  /**
   * @return the customerId
   */
  public long getCustomerId() {
    return customerId;
  }

  /**
   * @param customerId the customerId to set
   */
  public void setCustomerId(long customerId) {
    this.customerId = customerId;
  }
}
