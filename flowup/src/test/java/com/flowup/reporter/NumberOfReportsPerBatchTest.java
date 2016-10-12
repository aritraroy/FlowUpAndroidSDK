/*
 * Copyright (C) 2016 Go Karumi S.L.
 */

package com.flowup.reporter;

import com.flowup.FlowUp;
import com.flowup.reporter.model.CPUMetric;
import com.flowup.reporter.model.NetworkMetric;
import com.flowup.reporter.model.Reports;
import com.flowup.reporter.model.StatisticalValue;
import com.flowup.reporter.model.UIMetric;
import com.google.gson.Gson;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

public class NumberOfReportsPerBatchTest {

  private static final String ANY_VERSION_NAME = "1.0.0";
  private static final String ANY_OS_VERSION = "API24";
  private static final boolean ANY_BATTERY_SAVER_ON = true;
  private static final long BYTES_UPLOADED = Long.MAX_VALUE;
  private static final long BYTES_DOWNLOADED = Long.MAX_VALUE;

  private static final double MAX_REQUEST_SIZE_WITHOUT_COMPRESSION_IN_BYTES = 9.5d * 1024 * 1024;
  private static final int CPU_USAGE = 100;

  private final Gson gson = new Gson();

  @Test public void shouldNotSendMoreThan100KBOfBodyInAReportRequest() throws Exception {
    Reports reports = givenAReportsInstanceFullOfData(FlowUpReporter.NUMBER_OF_REPORTS_PER_REQUEST);

    long bytes = toBytes(reports);

    assertTrue(
        "Your modification to the Reports classes used to send data to our severs has increased the"
            + " request body size exceeding the maximum size supported. The new max number of reports"
            + " we can send in one request is: "
            + calculateMaxNumberOfReportsPerRequest(),
        bytes <= MAX_REQUEST_SIZE_WITHOUT_COMPRESSION_IN_BYTES);
  }

  private int calculateMaxNumberOfReportsPerRequest() throws Exception {
    int newMax;
    for (newMax = FlowUpReporter.NUMBER_OF_REPORTS_PER_REQUEST; newMax >= 0; newMax--) {
      Reports reports = givenAReportsInstanceFullOfData(newMax);
      long bytes = toBytes(reports);
      if (bytes <= MAX_REQUEST_SIZE_WITHOUT_COMPRESSION_IN_BYTES) {
        break;
      }
    }
    return newMax;
  }

  private long toBytes(Reports reports) throws Exception {
    return gson.toJson(reports).getBytes("UTF-8").length;
  }

  private Reports givenAReportsInstanceFullOfData(int numberOfReports) {
    List<String> reportIds = givenSomeIds(numberOfReports);
    String appPackage = "io.flowup.androidsdk";
    String uuid = "1e54751e.28be.404a.88c0.5004140323d8";
    String deviceModel = "Samsung Galaxy S3";
    String screenDensity = "xxxhdpi";
    String screenSize = "1080X1794";
    int numberOfCores = 6;
    List<NetworkMetric> networkMetrics = givenSomeNetworkMetrics(numberOfReports);
    List<UIMetric> uiMetrics = givenSomeUIMetrics(numberOfReports);
    List<CPUMetric> cpuMetrics = givenSomeCPUMetrics(numberOfReports);
    return new Reports(reportIds, appPackage, uuid, deviceModel, screenDensity, screenSize,
        numberOfCores, networkMetrics, uiMetrics, cpuMetrics);
  }

  private List<NetworkMetric> givenSomeNetworkMetrics(int numberOfReports) {
    List<NetworkMetric> networkMetrics = new LinkedList<>();
    for (int i = 0; i < numberOfReports; i++) {
      NetworkMetric networkMetric = generateAnyNetworkMetric(i);
      networkMetrics.add(networkMetric);
    }
    return networkMetrics;
  }

  private List<CPUMetric> givenSomeCPUMetrics(int numberOfReports) {
    List<CPUMetric> cpuMetrics = new LinkedList<>();
    for (int i = 0; i < numberOfReports; i++) {
      CPUMetric networkMetric = generateAnyCPUMetric(i);
      cpuMetrics.add(networkMetric);
    }
    return cpuMetrics;
  }

  private List<UIMetric> givenSomeUIMetrics(int numberOfReports) {
    List<UIMetric> uiMetrics = new LinkedList<>();
    for (int i = 0; i < numberOfReports * FlowUp.SAMPLING_INTERVAL; i++) {
      UIMetric uiMetric = generateAnyUIMetric(i);
      uiMetrics.add(uiMetric);
    }
    return uiMetrics;
  }

  private UIMetric generateAnyUIMetric(long timestamp) {
    StatisticalValue frameTime = givenAnyStatisticalValue();
    StatisticalValue fps = givenAnyStatisticalValue();
    String screenName = "MainActivity";
    return new UIMetric(timestamp, ANY_VERSION_NAME, ANY_OS_VERSION, ANY_BATTERY_SAVER_ON,
        screenName, frameTime, fps);
  }

  private StatisticalValue givenAnyStatisticalValue() {
    return new StatisticalValue(1, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE,
        Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE,
        Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE,
        Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE,
        Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
  }

  private NetworkMetric generateAnyNetworkMetric(long timestamp) {
    return new NetworkMetric(timestamp, ANY_VERSION_NAME, ANY_OS_VERSION, ANY_BATTERY_SAVER_ON,
        BYTES_UPLOADED, BYTES_DOWNLOADED);
  }

  private CPUMetric generateAnyCPUMetric(int timestamp) {
    return new CPUMetric(timestamp, ANY_VERSION_NAME, ANY_OS_VERSION, ANY_BATTERY_SAVER_ON,
        CPU_USAGE);
  }

  private List<String> givenSomeIds(int numberOfReports) {
    List<String> ids = new LinkedList<>();
    for (int i = 0; i < numberOfReports; i++) {
      ids.add(String.valueOf(i));
    }
    return ids;
  }
}
