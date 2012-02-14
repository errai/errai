/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.io.Serializable;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@Portable
public class TreeNodeContainer implements Serializable {

  private static final long serialVersionUID = -432859869954816580L;

  private int nodeId;
  private String nodeName;
  private int parentNodeId;

  public TreeNodeContainer() {}

  public TreeNodeContainer(int nodeId, String nodeName, int parentNodeId) {
    this.nodeId = nodeId;
    this.parentNodeId = parentNodeId;
    this.nodeName = nodeName;
  }

  public int getNodeId() {
    return nodeId;
  }

  public void setNodeId(int nodeId) {
    this.nodeId = nodeId;
  }

  public String getNodeName() {
    return nodeName;
  }

  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  public int getParentNodeId() {
    return parentNodeId;
  }

  public void setParentNodeId(int parentNodeId) {
    this.parentNodeId = parentNodeId;
  }
}
