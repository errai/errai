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

package org.jboss.errai.otec.client.operation;

/**
 * @author Christian Sadilek
 * @author Mike Brock
 */
public class OpPair {
  private final OTOperation remoteOp;
  private final OTOperation localOp;

  private OpPair(final OTOperation remoteOp, final OTOperation localOp) {
    this.remoteOp = remoteOp;
    this.localOp = localOp;
  }

  public static OpPair of(final OTOperation remoteOp, final OTOperation localOp) {
    return new OpPair(remoteOp, localOp);
  }

  public OTOperation getRemoteOp() {
    return remoteOp;
  }

  public OTOperation getLocalOp() {
    return localOp;
  }

  public String toString() {
    return "OpPair(" + remoteOp + "::" + localOp + ")";
  }
}
