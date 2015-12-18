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

package org.jboss.errai.demo.busstress.client.local;

import org.jboss.errai.demo.busstress.client.shared.Stats;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.client.NumberFormatRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class StatsPanel extends Composite {

    private static RunStatsUiBinder uiBinder = GWT.create(RunStatsUiBinder.class);

    interface RunStatsUiBinder extends UiBinder<Widget, StatsPanel> {
    }

    @UiField Label summaryLabel;
    @UiField Label messageSendCount;
    @UiField Label messageSendBytes;
    @UiField Label inFlightCount;
    @UiField Label inFlightAvg;
    @UiField Label messageRecvCount;
    @UiField Label messageRecvBytes;

    NumberFormatRenderer renderer = new NumberFormatRenderer(NumberFormat.getFormat("0.00"));

    public StatsPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    void onRunStarted(Stats stats) {
        summaryLabel.setText("Started " + stats.getStartTime());
    }

    /**
     * Updates the labels in the UI based on the values in {@link #stats}.
     */
    void updateStatsLabels(Stats stats) {
        messageSendBytes.setText("" + stats.getSentBytes() + " bytes");
        messageSendCount.setText("" + stats.getSentMessages());

        inFlightCount.setText("" + stats.getInflightMessages());
        inFlightAvg.setText("" + renderer.render(stats.getAverageWaitTime()) + " ms/resp");

        messageRecvBytes.setText("" + stats.getReceivedBytes() + " bytes");
        messageRecvCount.setText("" + stats.getReceivedMessages());
    }

    void onRunFinished(Stats stats) {
        summaryLabel.setText("Started " + stats.getStartTime() + "; finished " + stats.getFinishTime() + ".");
    }

}
