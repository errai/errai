/* jboss.org */
package org.jboss.errai.bus.client.framework;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Mar 17, 2010
 */
public interface LogAdapter
{
  void warn(String message);
  void info(String message);
  void debug(String message);
  void error(String message, Throwable t);
}
