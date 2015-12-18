package org.jboss.errai.demo.todo.server;

import org.picketlink.annotations.PicketLink;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author edewit@redhat.com
 */
public class PicketLinkResource {
    @Produces
    @PicketLink
    @PersistenceContext
    private EntityManager picketLinkEntityManager;
}
