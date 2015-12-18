/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.tests.support;

import java.io.Serializable;

import org.jboss.errai.common.client.api.annotations.Portable;

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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + nodeId;
    result = prime * result + ((nodeName == null) ? 0 : nodeName.hashCode());
    result = prime * result + parentNodeId;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TreeNodeContainer other = (TreeNodeContainer) obj;
    if (nodeId != other.nodeId)
      return false;
    if (nodeName == null) {
      if (other.nodeName != null)
        return false;
    }
    else if (!nodeName.equals(other.nodeName))
      return false;
    if (parentNodeId != other.parentNodeId)
      return false;
    return true;
  }
  
}
