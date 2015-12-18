/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.server.gwt.util;

import java.util.Stack;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.HelpInfo;
import com.google.gwt.core.ext.TreeLogger.Type;

public class StackTreeLogger extends Stack<TreeLogger> {

  /**
   * 
   */
  public static final long serialVersionUID = 1L;

  public StackTreeLogger(TreeLogger root) {
    push(root);
  }

  /**
   * Create a log entry on the current active branch.
   * 
   * @see TreeLogger#log(Type, String, Throwable, HelpInfo)
   */
  public void log(Type type, String msg, Throwable caught, HelpInfo helpInfo) {
    peek().log(type, msg, caught, helpInfo);
  }

  public void log(Type type, String msg, Throwable caught) {
    peek().log(type, msg, caught);
  }

  public void log(Type type, String msg) {
    peek().log(type, msg);
  }

  /**
   * Create a new active sub-branch and make this the active branch.
   * 
   * @see TreeLogger#branch(Type, String, Throwable, HelpInfo)
   */
  public void branch(Type type, String msg, Throwable caught, HelpInfo helpInfo) {
    push(peek().branch(type, msg, caught, helpInfo));
  }

  public void branch(Type type, String msg, Throwable caught) {
    branch(type, msg, caught, null);
  }

  public void branch(Type type, String msg) {
    branch(type, msg, null, null);
  }

  /**
   * Return to the last active branch (i.e. the parent of current active branch).
   */
  public void unbranch() {
    pop();
  }
  
}
