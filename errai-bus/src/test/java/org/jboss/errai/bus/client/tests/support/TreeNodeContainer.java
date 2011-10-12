package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.ExposeEntity;

import java.io.Serializable;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ExposeEntity
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
