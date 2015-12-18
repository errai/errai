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
