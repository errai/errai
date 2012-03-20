package org.jboss.errai.cdi.server;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.NamingException;

import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.servlet.ServiceLocator;
import org.jboss.errai.cdi.server.events.ShutdownEventObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CDIServiceLocator implements ServiceLocator {

  private static final Logger log = LoggerFactory.getLogger(ShutdownEventObserver.class);

  @Override
  public ErraiService locateService() {
    BeanManager beanManager;
    try {
      beanManager = CDIServerUtil.lookupBeanManager();
    } catch (NamingException e) {

      // it's important to log this here even though we're rethrowing it. Unfortunately, Dev Mode
      // does not log the cause of the exception to System.out; only within the Jetty tab of the
      // Dev Mode GUI where it's hard to find.
      log.error("Could not find a CDI BeanManager.", e);
      if (runningOnStockJettyLauncher()) {
        log.error("HINT: it looks like you are running on GWT's stock Jetty Launcher. You can probably resolve this error by specifying -server org.jboss.errai.cdi.server.gwt.JettyLauncher in your Dev Mode launch configuration.");
      }

      throw new RuntimeException(e);
    }

    Set<Bean<?>> beans = beanManager.getBeans(ErraiService.class);
    Bean<?> bean = beanManager.resolve(beans);
    CreationalContext<?> context = beanManager.createCreationalContext(bean);

    return (ErraiService) beanManager.getReference(bean, ErraiService.class, context);
  }

  /**
   * Returns true if the current thread is running within the context of the
   * stock GWT JettyLauncher (as opposed to the Errai one that enables JNDI to
   * work properly, or "noserver" mode).
   *
   * @return True if GWT's stock JettyLauncher is in use; false otherwise.
   */
  private static boolean runningOnStockJettyLauncher() {
    for (StackTraceElement ste : new Exception().getStackTrace()) {
      if (ste.getClassName().equals("com.google.gwt.dev.shell.jetty.JettyLauncher") && ste.getMethodName().equals("start")) {
        return true;
      }
    }
    return false;
  }
}
