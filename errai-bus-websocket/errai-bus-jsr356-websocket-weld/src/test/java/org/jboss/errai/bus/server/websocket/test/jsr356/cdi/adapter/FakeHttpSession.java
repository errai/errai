package org.jboss.errai.bus.server.websocket.test.jsr356.cdi.adapter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * Fake session to activate the session context for test cases.
 * 
 * @author Michel Werren
 */
public class FakeHttpSession implements HttpSession {

  private Map<String, Object> attrs = new ConcurrentHashMap<String, Object>();

  @Override
  public long getCreationTime() {
    return 0;
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public long getLastAccessedTime() {
    return 0;
  }

  @Override
  public ServletContext getServletContext() {
    return null;
  }

  @Override
  public void setMaxInactiveInterval(int interval) {

  }

  @Override
  public int getMaxInactiveInterval() {
    return 0;
  }

  @Override
  public HttpSessionContext getSessionContext() {
    return null;
  }

  @Override
  public Object getAttribute(String name) {
    return attrs.get(name);
  }

  @Override
  public Object getValue(String name) {
    return null;
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return Collections.<String>emptyEnumeration();
  }

  @Override
  public String[] getValueNames() {
    return new String[0];
  }

  @Override
  public void setAttribute(String name, Object value) {
    attrs.put(name, value);
  }

  @Override
  public void putValue(String name, Object value) {

  }

  @Override
  public void removeAttribute(String name) {

  }

  @Override
  public void removeValue(String name) {

  }

  @Override
  public void invalidate() {

  }

  @Override
  public boolean isNew() {
    return false;
  }
}
