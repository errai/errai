/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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

package org.errai.samples.serialization.client.model;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

import java.util.Date;

@ExposeEntity
public class HistoryProcessInstanceRef {

    private String processInstanceId;
    private String processDefinitionId;
    private String key;
    private String state;
    private String endActivityName;
    private Date startTime;
    private Date endTime;
    private long duration;

    public String getProcessInstanceId() {
      return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
      this.processInstanceId = processInstanceId;
    }

    public String getProcessDefinitionId() {
      return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
      this.processDefinitionId = processDefinitionId;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getState() {
      return state;
    }

    public void setState(String state) {
      this.state = state;
    }

    public String getEndActivityName() {
      return endActivityName;
    }

    public void setEndActivityName(String endActivityName) {
      this.endActivityName = endActivityName;
    }

    public Date getStartTime() {
      return startTime;
    }

    public void setStartTime(Date startTime) {
      this.startTime = startTime;
    }

    public Date getEndTime() {
      return endTime;
    }

    public void setEndTime(Date endTime) {
      this.endTime = endTime;
    }

    public long getDuration() {
      return duration;
    }

    public void setDuration(long duration) {
      this.duration = duration;
    }

}
