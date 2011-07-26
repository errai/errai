/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.enterprise.context;


import javax.enterprise.context.ContextException;

/**
 * <p>Indicates that a context is not active.</p>
 *
 * @author Pete Muir
 * @author Shane Bryzak
 * @author Gavin King
 * @see javax.enterprise.context.spi.Context
 */

public class ContextNotActiveException extends ContextException {

  private static final long serialVersionUID = -3599813072560026919L;

  public ContextNotActiveException() {
    super();
  }

  public ContextNotActiveException(String message) {
    super(message);
  }

  public ContextNotActiveException(Throwable cause) {
    super(cause);
  }

  public ContextNotActiveException(String message, Throwable cause) {
    super(message, cause);
  }

}
