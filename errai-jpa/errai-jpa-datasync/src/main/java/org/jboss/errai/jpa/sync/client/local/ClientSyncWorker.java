/*
 * Copyright 2014 JBoss, by Red Hat, Inc
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

package org.jboss.errai.jpa.sync.client.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.NamedQuery;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.RefHolder;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;
import org.jboss.errai.jpa.sync.client.shared.SyncResponse;
import org.jboss.errai.jpa.sync.client.shared.SyncResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.Timer;

/**
 * Handles the job of keeping one JPA {@link NamedQuery} in sync between the client and server. A
 * ClientSyncWorker has three states:
 * <ol>
 * <li>Not yet started - no sync happens
 * <li>Running - data sync operations happen automatically
 * <li>Stopped - no sync happens
 * </ol>
 * 
 * New instances are in the "not yet started" state. You start them with a call to {@link #start()},
 * and stop them with a call to {@link #stop()}. Once started, a sync worker instance can be stopped
 * but not restarted. Once stopped, a sync worker cannot be restarted.
 * 
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 * 
 * @param <E>
 *          The entity type this worker's named query returns.
 */
public class ClientSyncWorker<E> {

  private static final Logger logger = LoggerFactory.getLogger(ClientSyncWorker.class);

  private final List<DataSyncCallback<E>> callbacks = new ArrayList<DataSyncCallback<E>>();
  private final Timer timer;
  private boolean started;
  private boolean stopped;

  /**
   * Creates a new ClientSyncWorker which takes responsibility for syncing the results of the named
   * JPA query.
   * 
   * @param <E>
   *          The entity type the named query returns.
   * @param queryName
   *          The name of a JPA named query. Must be visible to client-side code, and if it has
   *          parameters, they must be named (not positional) parameters.
   * @param queryResultType
   *          The result type returned by the named query.
   * @param queryParams
   *          Name-value pairs for all named parameters in the named query. Never null.
   * @param onError
   *          Error callback that should be invoked if any sync request encounters a data
   *          transmission error on the bus.
   * @return a new ClientSyncWorker instance in the "not yet started" state.
   */
  public static <E> ClientSyncWorker<E> create(final String queryName, final Class<E> queryResultType,
      final Map<String, Object> queryParams, final ErrorCallback<?> onError) {

    final RefHolder<ClientSyncManager> manager = new RefHolder<ClientSyncManager>();
    IOC.getAsyncBeanManager().lookupBean(ClientSyncManager.class).getInstance(
        new CreationalCallback<ClientSyncManager>() {
          @Override
          public void callback(ClientSyncManager beanInstance) {
            manager.set(beanInstance);
          }
        });

    // The assumption here is that the ClientSyncManager will never be declared as an async bean
    Assert.notNull("Failed to lookup instance of ClientSyncManager synchronously!", manager.get());
    return new ClientSyncWorker<E>(manager.get(), queryName, queryResultType, queryParams, onError);

  }

  /**
   * Creates a new ClientSyncWorker which takes responsibility for syncing the results of the named
   * JPA query.
   * <p>
   * This constructor is primarily intended for testing. Consider using
   * {@link #create(String, Class, Map, ErrorCallback)} instead, which obtains an instance of
   * ClientSyncManager from the IOC Bean Manager.
   * 
   * @param manager
   *          The instance of ClientSyncManager that should be used for all data sync operations.
   * @param queryName
   *          The name of a JPA named query. Must be visible to client-side code, and if it has
   *          parameters, they must be named (not positional) parameters.
   * @param queryResultType
   *          The result type returned by the named query.
   * @param queryParams
   *          Name-value pairs for all named parameters in the named query. Never null.
   * @param onError
   *          Error callback that should be invoked if any sync request encounters a data
   *          transmission error on the bus.
   * @return a new ClientSyncWorker instance in the "not yet started" state.
   */
  public ClientSyncWorker(final ClientSyncManager manager, final String queryName, final Class<E> queryResultType,
      final Map<String, Object> queryParams, final ErrorCallback<?> onError) {

    timer = new Timer() {
      @Override
      public void run() {
        RemoteCallback<List<SyncResponse<E>>> onCompletion = new RemoteCallback<List<SyncResponse<E>>>() {
          @Override
          public void callback(List<SyncResponse<E>> response) {
            SyncResponses<E> responses = new SyncResponses<E>(response);
            for (DataSyncCallback<E> callback : callbacks) {
              try {
                callback.onSync(responses);
              }
              catch (Throwable t) {
                logger.error("Ignoring Exception from DataSyncCallback:", t);
              }
            }
          }
        };

        manager.coldSync(queryName, queryResultType, queryParams, onCompletion, onError);
      }
    };
  }

  /**
   * Registers the given callback to receive notifications each time a sync operation has been
   * performed.
   * 
   * @param onCompletion
   *          the callback to notify of completed sync operations. Must not be null.
   */
  public void addSyncCallback(final DataSyncCallback<E> onCompletion) {
    Assert.notNull(onCompletion);
    callbacks.add(onCompletion);
  }

  /**
   * Starts this sync worker if it has not already been started or stopped.
   * 
   * @throws IllegalStateException
   *           if this sync worker has been stopped.
   */
  public void start() {
    if (stopped)
      throw new IllegalStateException("This worker was already stopped");

    started = true;
    timer.scheduleRepeating(5000);
  }

  /**
   * Stops this sync worker if it is running.
   * 
   * @throws IllegalStateException
   *           if this sync worker has not yet been started.
   */
  public void stop() {
    if (!started)
      throw new IllegalStateException("This worker was never started");

    stopped = true;
    callbacks.clear();
    timer.cancel();
  }
}
