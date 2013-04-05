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

package org.jboss.errai.otec.mutation;

/**
 * @author Mike Brock
 */
public class OTEntityImpl<T extends State> implements OTEntity<T> {
  private final Integer entityId;
  private final T entity;

  private int revisionCounter = 0;
  private int revision = 0;

  private TransactionLog transactionLog = new TransactionLogImpl();

  public OTEntityImpl(final Integer entityId, final T entity) {
    this.entityId = entityId;
    this.entity = entity;
  }

  @Override
  public int getRevision() {
    return revision;
  }

  @Override
  public void setRevision(int revision) {
    this.revision = revision;
  }

  @Override
  public int getNewRevisionNumber() {
    return revisionCounter++;
  }

  public Integer getId() {
    return entityId;
  }

  public TransactionLog getTransactionLog() {
    return transactionLog;
  }

  @Override
  public T getState() {
    return entity;
  }
}
