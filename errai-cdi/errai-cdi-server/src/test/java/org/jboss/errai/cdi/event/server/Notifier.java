package org.jboss.errai.cdi.event.server;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.event.client.shared.Create;
import org.jboss.errai.cdi.event.client.shared.Delete;
import org.jboss.errai.cdi.event.client.shared.NotifierStartEvent;
import org.jboss.errai.cdi.event.client.shared.Server;
import org.jboss.errai.cdi.event.client.shared.TestMarshallingDto;
import org.jboss.errai.cdi.event.client.shared.Update;

@Dependent
public class Notifier {
   @Inject
   @Server
   @Create
   private Event<Object> create;

   @Inject
   @Server
   @Update
   private Event<Object> update;

   @Inject
   @Server
   @Delete
   private Event<Object> deletion;

   public void fireCreation(final Object created) {
      create.fire(created);
   }
   public void fireDeletion(final Object deleted) {
      deletion.fire(deleted);
   }
   public void fireUpdate(final Object updated) {
      update.fire(updated);
   }

   public void startCreate(@Observes @Create final NotifierStartEvent evt) {
     fireCreation(new TestMarshallingDto());
   }

   public void startUpdate(@Observes @Update final NotifierStartEvent evt) {
     fireUpdate(new TestMarshallingDto());
   }

   public void startDelete(@Observes @Delete final NotifierStartEvent evt) {
     fireDeletion(new TestMarshallingDto());
   }
}