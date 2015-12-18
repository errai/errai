/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.demo.grocery.client.local.producer;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;

import org.jboss.errai.demo.grocery.client.shared.User;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;

@ApplicationScoped
public class UserProducer {

    // not sure User (a JPA entity) should traditionally be an injectable CDI bean. We'll see how this pans out.
    @Produces
    @ApplicationScoped
    private User getUser(EntityManager em) {
        // XXX Of course, this only works if all the data is local.
        // When there is a server side to this demo, we will always have to authenticate with it before
        // we can produce a User instance capable of syncing.
        List<User> users = em.createNamedQuery("allUsers", User.class).getResultList();

        final User user;
        if (users.isEmpty()) {
            user = new User();
            user.setName("me");
            em.persist(user);
            em.flush();
        }
        else {
            user = users.get(0);
        }

        return user;
    }

}
