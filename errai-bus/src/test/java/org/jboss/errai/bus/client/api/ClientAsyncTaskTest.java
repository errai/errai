package org.jboss.errai.bus.client.api;


/**
 * Abstract test that covers the contract of the AsyncTask interface. Tests for
 * client-side AsyncTask implementations should extend this class; tests for
 * server-side implementations should extend {@link AbstractAsyncTaskTest} directly.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public abstract class ClientAsyncTaskTest extends AbstractAsyncTaskTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }
}
