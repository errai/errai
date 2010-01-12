package org.jboss.servlet.http;

public interface HttpEventServlet extends javax.servlet.Servlet {
   public void event(org.jboss.servlet.http.HttpEvent httpEvent) throws java.io.IOException, javax.servlet.ServletException;
}