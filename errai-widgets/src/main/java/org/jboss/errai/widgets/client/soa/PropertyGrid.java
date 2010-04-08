/* jboss.org */
package org.jboss.errai.widgets.client.soa;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import org.gwt.mosaic.ui.client.ScrollLayoutPanel;
import org.gwt.mosaic.ui.client.layout.FillLayout;

/**
 * A simple property grid that displays name-value pairs.
 * <br>
 * Used styles:
 * <ul>
 * <li>bpm-prop-grid
 * <li>bpm-prop-grid-label
 * <li>bpm-prop-grid-even
 * <li>bpm-prop-grid-odd
 * </ul>
 *
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class PropertyGrid extends ScrollLayoutPanel
{
  private String[] fieldNames;

  private Grid grid;

  public PropertyGrid(String[] fieldDesc)
  {
    super(new FillLayout());
    this.grid = new Grid(fieldDesc.length, 2);
    grid.setStyleName("soa-prop-grid");
    this.fieldNames = fieldDesc;

    this.add(grid);

    initReset();
  }

  private void initReset()
  {
    for(int i=0; i< fieldNames.length; i++)
    {
      Label label = new Label(fieldNames[i]);
      label.setStyleName("soa-prop-grid-label");
      grid.setWidget(i,0, label);
      grid.setWidget(i,1, new HTML(""));

      String style = (i % 2 == 0) ? "soa-prop-grid-even" : "soa-prop-grid-odd";
      grid.getRowFormatter().setStyleName(i, style);
      grid.getColumnFormatter().setWidth(0, "20%");
      grid.getColumnFormatter().setWidth(1, "80%");
    }
  }

  public void clear()
  {
    initReset();
  }

  public void update(String[] fieldValues)
  {
    if(fieldValues.length!= fieldNames.length)
      throw new IllegalArgumentException("fieldValues.length doesn't match fieldName.length: "+ fieldNames);

    for(int i=0; i< fieldNames.length; i++)
    {
      Label label = new Label(fieldNames[i]);
      label.setStyleName("soa-prop-grid-label");
      grid.setWidget(i,0, label);
      grid.setWidget(i,1, new HTML(fieldValues[i]));
    }
  }

}

