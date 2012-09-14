package org.jboss.errai.demo.jpa.client.local;

import com.google.gwt.user.datepicker.client.CalendarModel;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.gwt.user.datepicker.client.DefaultCalendarView;

/**
 * A DatePicker subclass that allows year selection rather than month-by-month
 * navigation. Taken from the GWT discussion group:
 * https://groups.google.com/group
 * /google-web-toolkit/browse_thread/thread/5feaef678ae7cb8a/446322e1fc6bc956
 *
 * @author Danny Goovaerts
 */
public class DatePickerWithYearSelector extends DatePicker {
  public DatePickerWithYearSelector() {
    super(new MonthAndYearSelector(), new DefaultCalendarView(),
            new CalendarModel());
    MonthAndYearSelector monthSelector = (MonthAndYearSelector) this
            .getMonthSelector();
    monthSelector.setPicker(this);
    monthSelector.setModel(this.getModel());
  }

  public void refreshComponents() {
    super.refreshAll();
  }
}