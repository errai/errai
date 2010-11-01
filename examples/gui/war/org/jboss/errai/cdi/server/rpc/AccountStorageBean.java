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

import org.jboss.errai.cdi.client.rpc.Account;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 8, 2010
 */
@ApplicationScoped
public class AccountStorageBean
{
  private List<Account> accounts = new ArrayList<Account>();

  public AccountStorageBean()
  {
    accounts.add(new Account("Peter"));
    accounts.add(new Account("Mike"));
    accounts.add(new Account("Mary"));
    accounts.add(new Account("Jesse"));
  }

  public void addAccount(Account account)
  {
    accounts.add(account);
  }

  public Account getAccount(String id)
  {
    Account match = null;
    for(Account a : accounts)
    {
      if(a.getId().equals(id))
        match = a;
    }
    return match;
  }

  public void remove(String id)
  {
    accounts.remove(id);
  }

  public List<Account> getAll()
  {
    return Collections.unmodifiableList(accounts);
  }
}
