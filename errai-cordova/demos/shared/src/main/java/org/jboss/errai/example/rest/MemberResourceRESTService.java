package org.jboss.errai.example.rest;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.errai.example.client.shared.Member;

/**
 * A REST resource representing the contents of the members table.
 */
@Path("/members")
@RequestScoped
public class MemberResourceRESTService {
   @Inject
   private EntityManager em;

   @GET
   @Produces("text/xml")
   public List<Member> listAllMembers() {
      @SuppressWarnings("unchecked")
      // We recommend centralizing inline queries such as this one into @NamedQuery annotations on
      // the @Entity class
      // as described in the named query blueprint:
      // https://blueprints.dev.java.net/bpcatalog/ee5/persistence/namedquery.html
      final List<Member> results = em.createQuery("select m from Member m order by m.name", Member.class).getResultList();
      return results;
   }

   @GET
   @Path("/{id:[0-9][0-9]*}")
   @Produces("text/xml")
   public Member lookupMemberById(@PathParam("id") long id) {
      return em.find(Member.class, id);
   }
}
