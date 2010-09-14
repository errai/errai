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
package org.jboss.errai.cdi.server.rpc;


import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.cdi.client.rpc.Account;
import org.jboss.errai.cdi.client.rpc.AccountManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.*;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 6, 2010
 */
@Service
@ApplicationScoped
public class AccountManagerBean implements AccountManager
{
  @Inject
  AccountStorageBean storage;

  public void createAccount(Account account)
  {
    storage.addAccount(account);
  }

  public Account getAccount(String id)
  {
    return storage.getAccount(id);
  }

  public void deleteAccount(String id)
  {
    storage.remove(id);
  }

  public List<Account> getAllAccounts()
  {
    if(null==storage)
      throw new IllegalStateException("Not CDI managed");
    
    return storage.getAll();
  }
}
