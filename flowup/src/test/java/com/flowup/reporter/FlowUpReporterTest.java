/*
 * Copyright (C) 2016 Go Karumi S.L.
 */

package com.flowup.reporter;

import android.support.annotation.NonNull;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.flowup.reporter.android.WiFiSyncServiceScheduler;
import com.flowup.reporter.apiclient.ApiClient;
import com.flowup.reporter.model.Reports;
import com.flowup.reporter.storage.ReportsStorage;
import com.flowup.utils.Time;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class) public class FlowUpReporterTest {

  private static final long ANY_TIMESTAMP = 0;

  @Mock private ApiClient apiClient;
  @Mock private ReportsStorage storage;
  @Mock private WiFiSyncServiceScheduler syncScheduler;
  @Mock private Time time;

  private FlowUpReporter givenAFlowUpReporter() {
    return givenAFlowUpReporter(false);
  }

  private FlowUpReporter givenAFlowUpReporter(boolean debuggable) {
    return new FlowUpReporter(new MetricRegistry(), "ReporterName", MetricFilter.ALL,
        TimeUnit.SECONDS, TimeUnit.MILLISECONDS, apiClient, storage, syncScheduler, time,
        debuggable);
  }

  @Test public void storesDropwizardReportAndDoesNotInitializeSyncIfDebugIsNotEnabled() {
    FlowUpReporter reporter = givenAFlowUpReporter(false);

    DropwizardReport report = reportSomeMetrics(reporter);

    verify(storage).storeMetrics(report);
    verify(apiClient, never()).sendReports(any(Reports.class));
  }

  @Test public void storesDropwizardReportAndInitializesSynProcessIfDebugIsEnabled() {
    FlowUpReporter reporter = givenAFlowUpReporter(false);

    DropwizardReport report = reportSomeMetrics(reporter);

    List<String> ids = Collections.singletonList(String.valueOf(ANY_TIMESTAMP));
    verify(apiClient, never()).sendReports(givenAReportsInstanceWithId(ids));
  }

  @Test
  public void ifTheSyncProcessIsSuccessTheReportsShouldBeDeleted() {
    List<String> ids = Collections.singletonList(String.valueOf(ANY_TIMESTAMP));
    Reports reportsSent = givenAReportsInstanceWithId(ids);
    givenTheSyncProcessIsSuccess(reportsSent);
    FlowUpReporter reporter = givenAFlowUpReporter(false);

    reportSomeMetrics(reporter);

    verify(apiClient, never()).sendReports(reportsSent);
  }

  private void givenTheSyncProcessIsSuccess(Reports reports) {
    when(apiClient.sendReports(reports)).thenReturn(new ReportResult(reports));
  }

  private DropwizardReport reportSomeMetrics(FlowUpReporter reporter) {
    DropwizardReport report = new DropwizardReport(ANY_TIMESTAMP, new TreeMap<String, Gauge>(),
        new TreeMap<String, Counter>(), new TreeMap<String, Histogram>(),
        new TreeMap<String, Meter>(), new TreeMap<String, Timer>());
    reporter.report(report.getGauges(), report.getCounters(), report.getHistograms(),
        report.getMeters(), report.getTimers());
    List<String> ids = Collections.singletonList(String.valueOf(ANY_TIMESTAMP));
    when(storage.getReports()).thenReturn(givenAReportsInstanceWithId(ids));
    return report;
  }

  @NonNull private Reports givenAReportsInstanceWithId(List<String> ids) {
    return new Reports(ids, "", "", "", "", "", 0, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
  }
}