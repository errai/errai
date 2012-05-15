package org.jboss.errai.ui.test.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.api.EntryPoint;

import com.google.gwt.user.client.Timer;

/**
 * This class provides a target for injecting parts of the application that the
 * test cases need access to. Think of it as your test suite's window into the
 * CDI container. Test cases can access the injected members using the following
 * code:
 * 
 * <pre>
 *   CDITestHelper.instance.<i>injectedFieldName</i>
 * </pre>
 * <p>
 * You can also set up CDI event producers and observers here if your test needs
 * to fire events or assert that a particular event was fired.
 * <p>
 * Note that this "CDI Test Helper" pattern is just a workaround. If there were
 * something like the BeanManager available in the client, it would be
 * preferable for the tests to create and destroy managed beans on demand.
 * <p>
 * As an alternative workaround, you could dispense with this class altogether
 * and have your main Entry Point class keep a static reference to itself.
 * However, this would pollute the API with an unwanted singleton pattern: there
 * would be the possibility of classes referring to the entry point class
 * through its singleton rather than allowing it to be injected.
 * 
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@EntryPoint
public class CDITestHelper {
  
  @Inject
  public App app;

  static CDITestHelper instance;

  static int responseEventCount = 0;
  static boolean cdiInitialized = false;

  @PostConstruct
  public void saveStaticReference() {
    System.out.println("Saving static reference to CDITestHelper.instance");
    instance = this;
  }

  /**
   * Runs the given runnable in the browser's JavaScript thread once the Errai
   * CDI context has finished its initialization phase. Once the runnable is
   * executed, all {@link EntryPoint} classes will have been created and have
   * their dependencies injected, and all components listening for it will have
   * received the BusReady event.
   * 
   * @param runnable
   *          The code to run once Errai CDI is up and running in the context of
   *          the web page.
   */
  public static void afterCdiInitialized(final Runnable runnable) {
    cdiInitialized = false;
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        cdiInitialized = true;
      }
    });
    final Timer t = new Timer() {
      @Override
      public void run() {
        if (cdiInitialized) {
          runnable.run();
        }
        else {
          // poll again later
          schedule(500);
        }
      }
    };
    t.schedule(500);
  }
}
