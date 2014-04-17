package org.jboss.websockets.oio;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This is an abstraction of an http response so that this code can be re-used in different HTTP Engines (i.e. servlet).
 * Instances of this class usually delegate to the underyling container's implementation of an http response.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface HttpResponseBridge {
  String getHeader(String name);

  void setHeader(String name, String val);

  OutputStream getOutputStream();

  /**
   * Start the connection upgrade process. After calling this method,
   * data will be available raw from the connection. Calling this method
   * is optional if no read/write are needed during the upgrade process.
   */
  public void startUpgrade();

  /**
   * Send the switching protocol HTTP status and commit the response by
   * flushing the buffer.
   */
  public void sendUpgrade()
          throws IOException;
}
