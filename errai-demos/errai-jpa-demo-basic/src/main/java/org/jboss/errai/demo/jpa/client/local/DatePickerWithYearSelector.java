/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.demo.jpa.client.local;

import com.google.gwt.user.datepicker.client.CalendarModel;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.gwt.user.datepicker.client.DefaultCalendarView;

/**
 * A DatePicker subclass that allows year selection rather than month-by-month navigation. Taken from the GWT discussion group:
 * https://groups.google.com/group /google-web-toolkit/browse_thread/thread/5feaef678ae7cb8a/446322e1fc6bc956
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
