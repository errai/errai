package org.apache.catalina;

public interface CometProcessor extends javax.servlet.Servlet {
    public void event(org.apache.catalina.CometEvent cometEvent) throws java.io.IOException, javax.servlet.ServletException;
}