/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.container;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.JNDIServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ErraiService bootstrap strategy: Either attach to an existing service instance
 * or create a new one and bind it to JNDI. Currently limited to JBoss.
 * The actually service instance will be exposed through the servlet context.<p/>
 *
 * <b>Note</b>: This only works in conjunction with a Weld bootstrap listener.
 * An AS 6 Weld is driven by the deployment framework and solution doesn't apply.
 * 
 * @see org.jboss.errai.bus.server.servlet.AbstractErraiServlet
 * @see org.jboss.errai.cdi.server.CDIExtensionPoints
 * 
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Sep 29, 2010
 */
public class BootstrapListener implements ServletContextListener {

  protected Logger log = LoggerFactory.getLogger(BootstrapListener.class);

  private static String DEFAULT_JNDI_NAME = "java:/Errai";
  private String managedJndiName;

  public void contextInitialized(ServletContextEvent event) {
    final ServletContext context = event.getServletContext();
    if (null == context.getAttribute("errai")) {
      synchronized (context) {
        managedJndiName = context.getInitParameter("jndiName") != null ?
                        context.getInitParameter("jndiName") : DEFAULT_JNDI_NAME;

        ErraiService service = null;

        try {
          service = new JNDIServiceLocator(managedJndiName).locateService();

          log.info("Service exists. Attaching to " + managedJndiName);

        } catch (Exception e) {
          // ignore
        }

        if (null == service) {
          log.info("Creating new service instance");
          service = ServiceFactory.create();
          JBossJNDI.rebind(managedJndiName, service);
        }

        context.setAttribute("errai", service);
      }
    }
  }

  public void contextDestroyed(ServletContextEvent event) {
    final ServletContext context = event.getServletContext();
    JBossJNDI.unbind(managedJndiName);
    context.removeAttribute("errai");

  }
}
