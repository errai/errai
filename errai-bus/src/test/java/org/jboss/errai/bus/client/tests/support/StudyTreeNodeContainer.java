package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.ExposeEntity;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ExposeEntity
public class StudyTreeNodeContainer extends TreeNodeContainer {

  private static final long serialVersionUID = -3415552168669217572L;

  private int studyId;

  public StudyTreeNodeContainer() {
    super();
  }

  public StudyTreeNodeContainer(int nodeId, String nodeName, int parentNodeId, int studyId) {
    super(nodeId, nodeName, parentNodeId);
    this.studyId = studyId;
  }

  public int getStudyId() {
    return studyId;
  }

  public void setStudyId(int studyId) {
    this.studyId = studyId;
  }
}
