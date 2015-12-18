package org.jboss.errai.jpa.sync.test.client;

import static org.junit.Assert.fail;

import java.util.HashMap;

import org.jboss.errai.jpa.sync.client.shared.SyncableDataSet;
import org.jboss.errai.jpa.sync.rebind.SyncDecorator;
import org.junit.Test;

/**
 * Unit tests for {@link SyncableDataSet}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class SyncableDataSetTest {

  /**
   * Tests that we can construct queries with result type java.lang.Object which
   * is used as a result type for sync worker queries by our {@link SyncDecorator}.
   */
  @Test
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void testQueryCreationForPlainObjectType() {
    final Class objectClass = Object.class;
    SyncableDataSet syncableDataSet = SyncableDataSet.from("testQuery", objectClass, new HashMap());
    try {
      syncableDataSet.createQuery(new MockEntityManager());
    } catch (IllegalStateException t) {
      fail("Should be able to create query for result type java.lang.Object");
    }
  }

}
