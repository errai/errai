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

package org.jboss.errai.codegen.framework.exception;

/**
 * @author Mike Brock
 */
public class GenerationException extends RuntimeException {
  private Throwable callSiteReference;

  public GenerationException(Throwable callSite, String message, Throwable cause) {
    super(message, cause);
    this.callSiteReference = callSite;
  }



  @Override
  public String toString() {
    StringBuilder out = new StringBuilder();

    boolean externalApi = false;
    
    for (StackTraceElement el : callSiteReference.getStackTrace()) {
      if (!externalApi && !el.getClassName().startsWith("org.jboss.errai.codegen.framework")) {
        externalApi = true;
        out.append("API Callsite Trace for Error: ").append(getCause().getMessage()).append("\n");
      }

      if (externalApi) {
        out.append("\t").append("at ").append(el.toString()).append("\n");
      }
    }

    return out.append("---").toString();
  }
}
