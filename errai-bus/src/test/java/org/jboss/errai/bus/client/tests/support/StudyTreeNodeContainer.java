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

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@Portable
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
