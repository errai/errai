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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Mike Brock
 */
public class TransactionLogImpl implements TransactionLog {
  private final List<Operation> transactionLog = new LinkedList<Operation>();

  @Override
  public Collection<Operation> getLog() {
    synchronized (transactionLog) {
      return new ArrayList<Operation>(transactionLog);
    }
  }

  @Override
  public Collection<Operation> getLogLatestEntries(int numberOfEntries) {
    synchronized (transactionLog) {
      return transactionLog.subList(transactionLog.size() - numberOfEntries - 1, transactionLog.size() - 1);
    }
  }

  @Override
  public Collection<Operation> getLogFromId(int revision) {
    if (transactionLog.isEmpty()) {
      return Collections.emptyList();
    }

    final ListIterator<Operation> operationListIterator = transactionLog.listIterator(transactionLog.size() - 1);
    final List<Operation> operationList = new ArrayList<Operation>();

    while (operationListIterator.hasPrevious()) {
      final Operation previous = operationListIterator.previous();
      operationList.add(previous);
      if (previous.getRevision() == revision) {
        Collections.reverse(operationList);
        return operationList;
      }
    }

    throw new OTException("unable to find revision in log: " + revision);
  }

  @Override
  public void appendLog(Operation operation) {
    synchronized (transactionLog) {
      transactionLog.add(operation);
    }
  }
}
