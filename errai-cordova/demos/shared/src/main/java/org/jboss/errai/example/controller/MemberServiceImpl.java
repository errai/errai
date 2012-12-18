package org.jboss.errai.example.controller;

import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;

import org.jboss.errai.example.client.shared.Member;
import org.jboss.errai.example.client.shared.MemberService;
import org.jboss.errai.example.client.shared.New;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * CDI service that can be called from either the client side (via Errai RPC) or
 * the server side.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ApplicationScoped
@Service
public class MemberServiceImpl implements MemberService {

  @Inject
  private Logger log;

  @Inject
  private EntityManager em;

  @Inject
  private UserTransaction userTransaction;

  @Inject @New
  private Event<Member> newMemberEvent;

  @Override
  public void register(Member newMember) {
    log.info("Registering " + newMember.getName());
    try {
      userTransaction.begin();
      em.persist(newMember);
      userTransaction.commit();
    } catch (Exception ex) {
      try {
        userTransaction.rollback();
      } catch (Exception e) {
        e.printStackTrace();
      }
      throw new RuntimeException("Registering the member failed: "+ex.toString());
    }
    newMemberEvent.fire(newMember);
  }

  @Override
  public List<Member> retrieveAllMembersOrderedByName() {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Member> criteria = cb.createQuery(Member.class);
    Root<Member> member = criteria.from(Member.class);
    // Swap criteria statements if you would like to try out type-safe criteria queries, a new
    // feature in JPA 2.0
    // criteria.select(member).orderBy(cb.asc(member.get(Member_.name)));
    criteria.select(member).orderBy(cb.asc(member.get("name")));
    return em.createQuery(criteria).getResultList();
  }

}
