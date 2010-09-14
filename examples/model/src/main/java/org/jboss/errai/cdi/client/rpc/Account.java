/* jboss.org */
package org.jboss.errai.cdi.client.rpc;

import org.jboss.errai.bus.server.annotations.ExposeEntity;


/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 6, 2010
 */
@ExposeEntity
public class Account {
    String id;

    public Account() {
    }

    public Account(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                '}';
    }
}
