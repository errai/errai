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

package org.jboss.errai.tools.monitoring.test;

import org.jboss.errai.tools.monitoring.ObjectExplorer;

import javax.swing.*;
import java.util.LinkedList;

public class ExplorerTest {

  public static void main(String[] args) {
    JFrame frame = new JFrame();

    ObjectExplorer explorer = new ObjectExplorer();

    LinkedList list = new LinkedList();
    list.add(new TestEntity("foo"));
    list.add(new TestEntity("bar"));

    explorer.setRoot(list);

    frame.getContentPane().add(new JScrollPane(explorer));
    explorer.buildTree();

    frame.setSize(400, 600);
    frame.setVisible(true);
  }
}
