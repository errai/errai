package org.jboss.errai.example.client.local;

import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

@EntryPoint
@Templated("#root")
public class App extends Composite {
  @Inject
  @DataField("task-container")
  TaskPanel taskPanel;

  @Inject
  @DataField("project-list")
  ProjectPanel projectPanel;

  @Inject
  @DataField("tag-container")
  TagsPanel tagsPanel;
}
