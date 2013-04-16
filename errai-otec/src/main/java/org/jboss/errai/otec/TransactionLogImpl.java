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

package org.jboss.errai.otec;

import org.jboss.errai.otec.operation.OTOperation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class TransactionLogImpl implements TransactionLog {
  private final List<OTOperation> transactionLog = new LinkedList<OTOperation>();

  @Override
  public List<OTOperation> getLog() {
    synchronized (transactionLog) {
      return new ArrayList<OTOperation>(transactionLog);
    }
  }

  @Override
  public List<OTOperation> getLogLatestEntries(final int numberOfEntries) {
    synchronized (transactionLog) {
      return transactionLog.subList(transactionLog.size() - numberOfEntries - 1, transactionLog.size() - 1);
    }
  }

  @Override
  public List<OTOperation> getLogFromId(final int revision) {
    if (transactionLog.isEmpty()) {
      return Collections.emptyList();
    }

    final ListIterator<OTOperation> operationListIterator = transactionLog.listIterator(transactionLog.size());
    final List<OTOperation> operationList = new ArrayList<OTOperation>();

    int lastRev = -1;
    while (operationListIterator.hasPrevious()) {
      final OTOperation previous = operationListIterator.previous();
      operationList.add(previous);
      if ((lastRev = previous.getRevision()) == revision) {
        Collections.reverse(operationList);
        return operationList;
      }
    }

    if ((revision - 1) == lastRev) {
      return Collections.emptyList();
    }
    else {
      throw new OTException("unable to find revision in log: " + revision);
    }
  }

  @Override
  public void appendLog(final OTOperation operation) {
    synchronized (transactionLog) {

      for (final OTOperation op : transactionLog) {
        if (op.getRevision() == operation.getRevision()) {
          throw new AssertionError("duplicate revision!");
        }
      }

      transactionLog.add(operation);
    }
  }

  public String toString() {
    return Arrays.toString(transactionLog.toArray());
  }

}
