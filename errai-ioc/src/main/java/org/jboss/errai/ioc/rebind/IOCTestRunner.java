/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.rebind;

import com.google.gwt.junit.JUnitShell;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.jboss.errai.common.client.api.tasks.AsyncTask;
import org.jboss.errai.common.client.api.tasks.TaskManager;
import org.jboss.errai.common.client.api.tasks.TaskManagerFactory;
import org.jboss.errai.common.client.api.tasks.TaskManagerProvider;
import org.jboss.errai.common.client.util.TimeUnit;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.InterfaceInjectionContext;
import org.jboss.errai.ioc.client.api.Bootstrapper;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class IOCTestRunner extends ParentRunner<Runner> {
  public static boolean SIMULATED = Boolean.getBoolean("errai.ioc.debug.simulated_client");

  List<Runner> runners = new ArrayList<Runner>();
  private Object instance;

  public IOCTestRunner(Class<? extends TestCase> toRun) throws Throwable {
    super(toRun);

    for (final Method method : toRun.getDeclaredMethods()) {
      if (method.getName().startsWith("test") && method.getParameterTypes().length == 0) {
        runners.add(new Runner() {
          @Override
          public Description getDescription() {
            return Description.createTestDescription(getTestClass().getJavaClass(), method.getName());
          }

          @Override
          public void run(RunNotifier notifier) {
            final IOCClientTestCase iocClientTestCase = (IOCClientTestCase) getInstance();

            Description description = getDescription();

            notifier.fireTestStarted(description);
            TestResult result = new TestResult();

            try {
              if (SIMULATED) {
                iocClientTestCase.gwtSetUp();
                method.invoke(getInstance());
              }
              else {
                iocClientTestCase.setName(method.getName());
                JUnitShell.runTest(iocClientTestCase, result);
              }
            }
            catch (InvocationTargetException e) {
              notifier.fireTestFailure(new Failure(description, e.getTargetException()));
              return;
            }
            catch (Exception e) {
              notifier.fireTestFailure(new Failure(description, e));
              return;
            }

            notifier.fireTestRunFinished(new Result());

            if (!result.wasSuccessful()) {
              notifier.fireTestFailure(new Failure(description, null));
            }
            else {
              notifier.fireTestFinished(description);
            }
          }
        });
      }
    }
  }

  public Object getInstance() {
    if (instance == null) {
      try {
        instance = getTestClass().getJavaClass().newInstance();
      }
      catch (InstantiationException e) {
        throw new RuntimeException("Could not intantiate test class: " + getTestClass().getJavaClass().getName(), e);
      }
      catch (IllegalAccessException e) {
        throw new RuntimeException("Could not intantiate test class: " + getTestClass().getJavaClass().getName(), e);
      }
      catch (Exception e) {
        throw new RuntimeException("could not bootstrap", e);
      }
    }
    return instance;
  }

  @Override
  protected List<Runner> getChildren() {
    return runners;
  }

  @Override
  protected Description describeChild(Runner child) {
    return child.getDescription();
  }

  @Override
  protected void runChild(Runner child, RunNotifier notifier) {
    child.run(notifier);
  }

  @Override
  public void run(RunNotifier notifier) {
    final IOCClientTestCase iocClientTestCase = (IOCClientTestCase) getInstance();

    if (SIMULATED) {
      TaskManagerFactory.setTaskManagerProvider(new TaskManagerProvider() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

        @Override
        public TaskManager get() {
          return new TaskManager() {
            @Override
            public void execute(Runnable task) {
              service.execute(task);
            }

            @Override
            public AsyncTask scheduleRepeating(TimeUnit unit, int interval, Runnable task) {
              final ScheduledFuture<?> future =
                      service.scheduleAtFixedRate(task, unit.toMillis(interval), 0,
                              java.util.concurrent.TimeUnit.MILLISECONDS);


              return new AsyncTask() {
                @Override
                public void cancel(boolean interrupt) {
                  future.cancel(true);
                }

                @Override
                public void setExitHandler(Runnable runnable) {
                }

                @Override
                public boolean isCancelled() {
                  return future.isCancelled();
                }
              };
            }

            @Override
            public AsyncTask schedule(TimeUnit unit, int interval, Runnable task) {
              final ScheduledFuture<?> future =
                      service.schedule(task, unit.toMillis(interval), java.util.concurrent.TimeUnit.MILLISECONDS);


              return new AsyncTask() {
                @Override
                public void cancel(boolean interrupt) {
                  future.cancel(true);
                }

                @Override
                public void setExitHandler(Runnable runnable) {
                }

                @Override
                public boolean isCancelled() {
                  return future.isCancelled();
                }
              };
            }

            @Override
            public void requestStop() {
            }
          };
        }
      });


      if (instance instanceof IOCClientTestCase) {
        iocClientTestCase.setInitializer(new IOCClientTestCase.ContainerBootstrapper() {
          @Override
          public InterfaceInjectionContext bootstrap() {
            try {
              String rootPackage = iocClientTestCase.getModulePackage();
              List<String> packages = new ArrayList<String>();
              for (Package p : Package.getPackages()) {
                String packageName = p.getName();
                if (packageName.startsWith(rootPackage)) {
                  packages.add(packageName);
                }
              }
              MockIOCGenerator mockIOCGenerator = new MockIOCGenerator(packages);

              Class<? extends Bootstrapper> cls = mockIOCGenerator.generate();
              Bootstrapper bs = cls.newInstance();

              long tm = System.currentTimeMillis();
              InterfaceInjectionContext ctx = bs.bootstrapContainer();
              System.out.println("bootstrapped simulated container in " + (System.currentTimeMillis() - tm) + "ms");
              return ctx;
            }
            catch (Exception e) {
              throw new RuntimeException("failed to run in emulated mode", e);
            }
          }
        });

        iocClientTestCase.setForcePureJava(true);
      }

      super.run(notifier);
    }
    else {
      super.run(notifier);
    }
  }
}
