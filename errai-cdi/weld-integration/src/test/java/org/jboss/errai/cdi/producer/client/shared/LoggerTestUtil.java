package org.jboss.errai.cdi.producer.client.shared;

public class LoggerTestUtil {
  
  public static final String LOGGER_NAME = "a unique logger name!";
  public static final String RESULT_PART = "RESULT";
  
  public enum TestCommand {
    IS_NOT_NULL, // Return true iff logger was injected.
    IS_CORRECT_NAME, // Return true iff logger has correct name.
  }
}