/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.server.mock;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Alternative;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

@Alternative
public class MockServletContext implements ServletContext {

  public Map<String, String> initParams = new HashMap<String, String>();
  private String contextPath;

  @Override
  public String getContextPath() {
    return contextPath;
  }

  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  @Override
  public ServletContext getContext(String uripath) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public int getMajorVersion() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public int getMinorVersion() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public int getEffectiveMajorVersion() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public int getEffectiveMinorVersion() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public String getMimeType(String file) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Set<String> getResourcePaths(String path) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public URL getResource(String path) throws MalformedURLException {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public InputStream getResourceAsStream(String path) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String path) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public RequestDispatcher getNamedDispatcher(String name) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Servlet getServlet(String name) throws ServletException {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Enumeration<Servlet> getServlets() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Enumeration<String> getServletNames() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public void log(String msg) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public void log(Exception exception, String msg) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public void log(String message, Throwable throwable) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public String getRealPath(String path) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public String getServerInfo() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public String getInitParameter(String name) {
    return initParams.get(name);
  }

  @Override
  public Enumeration<String> getInitParameterNames() {
    return Collections.enumeration(initParams.keySet());
  }

  @Override
  public boolean setInitParameter(String name, String value) {
    initParams.put(name, value);
    return true;
  }

  @Override
  public Object getAttribute(String name) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public void setAttribute(String name, Object object) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public void removeAttribute(String name) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public String getServletContextName() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Dynamic addServlet(String servletName, String className) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Dynamic addServlet(String servletName, Servlet servlet) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Dynamic addJspFile(String s, String s1) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public ServletRegistration getServletRegistration(String servletName) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Map<String, ? extends ServletRegistration> getServletRegistrations() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public FilterRegistration getFilterRegistration(String filterName) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public SessionCookieConfig getSessionCookieConfig() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public void addListener(String className) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public <T extends EventListener> void addListener(T t) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public void addListener(Class<? extends EventListener> listenerClass) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public JspConfigDescriptor getJspConfigDescriptor() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public ClassLoader getClassLoader() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public void declareRoles(String... roleNames) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public String getVirtualServerName() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public int getSessionTimeout() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public void setSessionTimeout(int i) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public String getRequestCharacterEncoding() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public void setRequestCharacterEncoding(String s) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public String getResponseCharacterEncoding() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public void setResponseCharacterEncoding(String s) {
    throw new UnsupportedOperationException("Not implemented.");
  }
}
