package org.jboss.errai.workspaces.client;

import com.google.gwt.user.client.ui.Widget;
import org.gwt.mosaic.ui.client.StackLayoutPanel;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;

import java.util.HashMap;
import java.util.Map;

/**
 * Main lefthand menu
 *
 * */
public class Menu extends LayoutPanel
{

  private StackLayoutPanel stack;
  private Map<String, Integer> toolsetIndex = new HashMap<String, Integer>();
  
  public Menu()
  {
    super(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    
    stack = new StackLayoutPanel();
    stack.setStyleName("");
    stack.setAnimationEnabled(false);

    this.add(stack, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH, true));
  }

  public void addLauncher(Widget widget, String toolsetName)
  {
    toolsetIndex.put(Workspace.encode(toolsetName), toolsetIndex.size());
    stack.add(widget, toolsetName);
  }

  public StackLayoutPanel getStack()
  {
    return stack;
  }

  /**
   * opens a specific menu section
   * @param id a toolset id
   */
  public void toggle(String id)
  {
    stack.showStack(toolsetIndex.get(id));
    stack.invalidate();
    stack.layout();
  }
}
