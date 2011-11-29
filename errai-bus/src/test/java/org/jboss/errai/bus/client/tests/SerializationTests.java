package org.jboss.errai.bus.client.tests;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.tests.support.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class SerializationTests extends AbstractErraiTest {
  public static final String ENT_SER1_RESPONSE_SERVICE = "SerializationResponse1";

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  public void testEntitySerialization1() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        List<TreeNodeContainer> testList = new ArrayList<TreeNodeContainer>();
        testList.add(new TreeNodeContainer(10, "Foo", 0));
        testList.add(new TreeNodeContainer(15, "Bar", 10));
        testList.add(new StudyTreeNodeContainer(20, "Foobie", 15, 100));

        MessageBuilder.createCall(new RemoteCallback<List<TreeNodeContainer>>() {
          @Override
          public void callback(List<TreeNodeContainer> response) {
            if (response.size() == 3) {
              for (TreeNodeContainer tc : response) {
                System.out.println(tc);
              }

              finishTest();
            }
          }
        }, TestSerializationRPCService.class).acceptTreeNodeContainers(testList);
      }
    });
  }

  public void testLongInCollection() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final List<Long> ll = new ArrayList<Long>();

        ll.add(10L);
        ll.add(15L);
        ll.add(20L);
        ll.add(25L);
        ll.add(30L);

        MessageBuilder.createCall(new RemoteCallback<List<Long>>() {
          @Override
          public void callback(List<Long> response) {
            assertEquals(ll, response);
            finishTest();
          }
        }, TestRPCServiceRemote2.class).heresALongList(ll);
      }
    });
  }

  public void testGenericCollectionEntity() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final GenericCollectionInEntity ent = new GenericCollectionInEntity();

        List<Long> ll = new ArrayList<Long>();

        ll.add(10L);
        ll.add(15L);
        ll.add(20L);
        ll.add(25L);
        ll.add(30L);

        ent.setListOfLongs(ll);

        MessageBuilder.createCall(new RemoteCallback<GenericCollectionInEntity>() {
          @Override
          public void callback(GenericCollectionInEntity response) {
            assertEquals(ent, response);
            finishTest();
          }
        }, TestRPCServiceRemote.class).genericCollection(ent);
      }
    });

    
  }
}


