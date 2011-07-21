package org.jboss.errai.ioc.rebind;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class IOCTestSuite extends TestSuite {
  /**
   * ...as the moon sets over the early morning Merlin, Oregon
   * mountains, our intrepid adventurers type...
   */
  static public Test createTest(Class<? extends TestCase> theClass, String name) {
    Constructor<? extends TestCase> constructor;

    try {
      constructor = getTestConstructor(theClass);
    }
    catch (NoSuchMethodException e) {
      return warning("Class " + theClass.getName() + " has no public constructor TestCase(String name) or TestCase()");
    }
    Object test;
    try {
      if (constructor.getParameterTypes().length == 0) {
        test = constructor.newInstance(new Object[0]);
        if (test instanceof TestCase)
          ((TestCase) test).setName(name);
      }
      else {
        test = constructor.newInstance(new Object[]{name});
      }
    }
    catch (InstantiationException e) {
      return (warning("Cannot instantiate test case: " + name + " (" + exceptionToString(e) + ")"));
    }
    catch (InvocationTargetException e) {
      return (warning("Exception in constructor: " + name + " (" + exceptionToString(e.getTargetException()) + ")"));
    }
    catch (IllegalAccessException e) {
      return (warning("Cannot access test case: " + name + " (" + exceptionToString(e) + ")"));
    }
    return (Test) test;
  }

  /**
   * Gets a constructor which takes a single String as
   * its argument or a no arg constructor.
   */
  public static Constructor<? extends TestCase> getTestConstructor(Class<? extends TestCase> theClass) throws NoSuchMethodException {
    try {
      return theClass.getConstructor(String.class);
    }
    catch (NoSuchMethodException e) {
      // fall through
    }
    return theClass.getConstructor(new Class[0]);
  }

  /**
   * Returns a test which will fail and log a warning message.
   */
  public static Test warning(final String message) {
    return new TestCase("warning") {
      @Override
      protected void runTest() {
        fail(message);
      }
    };
  }

  /**
   * Converts the stack trace into a string
   */
  private static String exceptionToString(Throwable t) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    t.printStackTrace(writer);
    return stringWriter.toString();
  }


  private String fName;

  private Vector<Test> fTests = new Vector<Test>(10); // Cannot convert this to List because it is used directly by some test runners

  private Class<? extends TestCase> theClass;

  private RunNotifier notifier;

  /**
   * Constructs an empty TestSuite.
   */
  public IOCTestSuite() {
  }

  /**
   * Constructs a TestSuite from the given class. Adds all the methods
   * starting with "test" as test cases to the suite.
   * Parts of this method were written at 2337 meters in the Hueffihuette,
   * Kanton Uri
   *
   * @param theClass -
   * @param notifier -
   */
  public IOCTestSuite(final Class<? extends TestCase> theClass, RunNotifier notifier) {
    fName = theClass.getName();
    this.theClass = theClass;
    this.notifier = notifier;
    try {
      getTestConstructor(theClass); // Avoid generating multiple error messages
    }
    catch (NoSuchMethodException e) {
      addTest(warning("Class " + theClass.getName() + " has no public constructor TestCase(String name) or TestCase()"));
      return;
    }

    if (!Modifier.isPublic(theClass.getModifiers())) {
      addTest(warning("Class " + theClass.getName() + " is not public"));
      return;
    }

    Class<?> superClass = theClass;
    List<String> names = new ArrayList<String>();
    while (Test.class.isAssignableFrom(superClass)) {
      for (Method each : superClass.getDeclaredMethods())
        addTestMethod(each, names, theClass);
      superClass = superClass.getSuperclass();
    }
    if (fTests.size() == 0)
      addTest(warning("No tests found in " + theClass.getName()));
  }

  /**
   * Constructs a TestSuite from the given class with the given name.
   *
   * @see TestSuite#TestSuite(Class)
   */
  public IOCTestSuite(Class<? extends TestCase> theClass, String name) {
    this(theClass);
    setName(name);
  }

  /**
   * Constructs an empty TestSuite.
   */
  public IOCTestSuite(String name) {
    setName(name);
  }

  /**
   * Constructs a TestSuite from the given array of classes.
   *
   * @param classes {@link TestCase}s
   */
  public IOCTestSuite(Class<?>... classes) {
    for (Class<?> each : classes)
      addTest(new TestSuite(each.asSubclass(TestCase.class)));
  }

  /**
   * Constructs a TestSuite from the given array of classes with the given name.
   *
   * @see TestSuite#TestSuite(Class[])
   */
  public IOCTestSuite(Class<? extends TestCase>[] classes, String name) {
    this(classes);
    setName(name);
  }

  /**
   * Adds a test to the suite.
   */
  public void addTest(Test test) {
    fTests.add(test);
  }

  /**
   * Adds the tests from the given class to the suite
   */
  public void addTestSuite(Class<? extends TestCase> testClass) {
    addTest(new TestSuite(testClass));
  }

  /**
   * Counts the number of test cases that will be run by this test.
   */
  public int countTestCases() {
    int count = 0;
    for (Test each : fTests)
      count += each.countTestCases();
    return count;
  }

  /**
   * Returns the name of the suite. Not all
   * test suites have a name and this method
   * can return null.
   */
  public String getName() {
    return fName;
  }

  /**
   * Runs the tests and collects their result in a TestResult.
   */
  public void run(TestResult result) {
    Description suiteDescription = Description.createSuiteDescription(theClass);

    notifier.fireTestRunStarted(suiteDescription);

    for (Test each : fTests) {
      if (result.shouldStop())
        break;

      Description testDescription = Description.createTestDescription(theClass,
              each.toString().substring(0, each.toString().indexOf('(')));

      notifier.fireTestStarted(testDescription);

      try {
        runTest(each, result);

        if (!result.wasSuccessful()) {
          notifier.fireTestFailure(new Failure(testDescription, null));
        }

        notifier.fireTestFinished(testDescription);
      }
      catch (Exception e) {
        notifier.fireTestFailure(new Failure(testDescription, e));
      }
    }
    notifier.fireTestRunFinished(new Result());
  }

  public void runTest(Test test, TestResult result) {
    test.run(result);
  }

  /**
   * Sets the name of the suite.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    fName = name;
  }

  /**
   * Returns the test at the given index
   */
  public Test testAt(int index) {
    return fTests.get(index);
  }

  /**
   * Returns the number of tests in this suite
   */
  public int testCount() {
    return fTests.size();
  }

  /**
   * Returns the tests as an enumeration
   */
  public Enumeration<Test> tests() {
    return fTests.elements();
  }

  /**
   */
  @Override
  public String toString() {
    if (getName() != null)
      return getName();
    return super.toString();
  }

  private void addTestMethod(Method m, List<String> names, Class<? extends TestCase> theClass) {
    String name = m.getName();
    if (names.contains(name))
      return;
    if (!isPublicTestMethod(m)) {
      if (isTestMethod(m))
        addTest(warning("Test method isn't public: " + m.getName() + "(" + theClass.getCanonicalName() + ")"));
      return;
    }
    names.add(name);
    addTest(createTest(theClass, name));
  }

  private boolean isPublicTestMethod(Method m) {
    return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
  }

  private boolean isTestMethod(Method m) {
    return
            m.getParameterTypes().length == 0 &&
                    m.getName().startsWith("test") &&
                    m.getReturnType().equals(Void.TYPE);
  }
}
