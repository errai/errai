/*
 * Copyright 2013 JBoss, by Red Hat, Inc                                    
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

package org.jboss.errai.otec.client;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class OTEntityImpl<T extends State> implements OTEntity<T>, Cloneable {
  private final int entityId;
  private final T entity;

  private int revisionCounter = 0;
  private int revision = 0;

  private final TransactionLog transactionLog;

  public OTEntityImpl(final int entityId, final T entity) {
    this.entityId = entityId;
    this.entity = entity;
    this.transactionLog = TransactionLogImpl.createTransactionLog(this);
  }

  @Override
  public int getRevision() {
    return revision;
  }

  @Override
  public void setRevision(final int revision) {
    this.revision = revision;
    //   this.revisionCounter = revision;
  }

  @Override
  public void resetRevisionCounterTo(int revision) {
    this.revisionCounter = revision + 1;
  }

  @Override
  public void incrementRevision() {
    setRevision(getNewRevisionNumber());
  }

  @Override
  public void decrementRevisionCounter() {
    revisionCounter--;
  }

  private int getNewRevisionNumber() {
    return revisionCounter++;
  }

  @Override
  public int getId() {
    return entityId;
  }

  @Override
  public TransactionLog getTransactionLog() {
    return transactionLog;
  }

  @Override
  public T getState() {
    return entity;
  }

  @Override
  public String toString() {
    return entity.getClass().getName() + "[revision=" + getRevision() + "; id=" + entityId + "]=\"" + getState().get() + "\"";
  }
}
