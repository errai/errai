package org.jboss.servlet.http;

public interface HttpEventServlet extends javax.servlet.Servlet {
   public void event(HttpEvent httpEvent) throws java.io.IOException, javax.servlet.ServletException;
}