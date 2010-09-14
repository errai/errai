/* jboss.org */
package org.jboss.errai.cdi.client.rpc;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.cdi.client.rpc.Account;

import java.util.List;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 6, 2010
 */
@Remote
public interface AccountManager
{
  void createAccount(Account account);
  Account getAccount(String id);
  List<Account> getAllAccounts();
  void deleteAccount(String id);
}
