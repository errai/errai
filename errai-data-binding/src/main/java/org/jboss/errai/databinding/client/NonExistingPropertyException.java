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

package org.jboss.errai.databinding.client;

/**
 * Thrown to indicate that the property used when binding a widget to a model instance does not exist in the
 * corresponding model class. This exception is only used internally (in generated code). Bindings to non-existing
 * properties do not cause an exception to be thrown to the user.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@SuppressWarnings("serial")
public class NonExistingPropertyException extends RuntimeException {

  public NonExistingPropertyException(String message) {
    super(message);
  }
  
  public String createErrorMessage(String error) {
    return error + " " + this.getMessage();
  }
}
