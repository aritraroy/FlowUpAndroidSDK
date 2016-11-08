/*
 * Copyright (C) 2016 Go Karumi S.L.
 */

package io.flowup.reporter.storage;

import android.content.Context;
import io.flowup.reporter.DropwizardReport;
import io.flowup.reporter.model.Reports;
import io.flowup.storage.RealmStorage;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import java.util.LinkedList;
import java.util.List;

public class ReportsStorage extends RealmStorage {

  public ReportsStorage(Context context) {
    super(context);
  }

  public void storeMetrics(final DropwizardReport dropwizardReport) {
    Realm realm = getRealm();
    realm.executeTransaction(new Realm.Transaction() {
      @Override public void execute(Realm realm) {
        storeAsRealmObject(realm, dropwizardReport);
      }
    });
    realm.close();
  }

  public Reports getReports(int numberOfReports) {
    Realm realm = getRealm();
    RealmResults<RealmReport> reportsSorted =
        realm.where(RealmReport.class).findAllSorted(RealmReport.ID_FIELD_NAME);
    List<RealmReport> reportsToMap = new LinkedList<>();
    for (int i = 0; i < numberOfReports && i < reportsSorted.size(); i++) {
      RealmReport realmReport = reportsSorted.get(i);
      reportsToMap.add(realmReport);
    }
    Reports reports = new RealmReportsToReportsMapper().map(reportsToMap);
    realm.close();
    return reports;
  }

  public void deleteReports(final Reports reports) {
    Realm realm = getRealm();
    realm.executeTransaction(new Realm.Transaction() {
      @Override public void execute(Realm realm) {
        for (String reportId : reports.getReportsIds()) {
          RealmResults<RealmReport> reportsToRemove =
              realm.where(RealmReport.class).equalTo(RealmReport.ID_FIELD_NAME, reportId).findAll();
          for (RealmReport realmReport : reportsToRemove) {
            deleteMetricsReports(realm, realmReport.getMetrics());
          }
          reportsToRemove.deleteAllFromRealm();
        }
      }
    });
    realm.close();
  }

  public void clear() {
    Realm realm = getRealm();
    realm.executeTransaction(new Realm.Transaction() {
      @Override public void execute(Realm realm) {
        realm.where(RealmReport.class).findAll().deleteAllFromRealm();
        realm.where(RealmMetric.class).findAll().deleteAllFromRealm();
        realm.where(RealmStatisticalValue.class).findAll().deleteAllFromRealm();
      }
    });
    realm.close();
  }

  private void deleteMetricsReports(Realm realm, RealmList<RealmMetric> metricsToRemove) {
    List<RealmResults<RealmMetric>> metricsReportsToRemove = new LinkedList<>();
    for (RealmMetric metric : metricsToRemove) {
      RealmResults<RealmMetric> metricsReports = realm.where(RealmMetric.class)
          .equalTo(RealmMetric.ID_FIELD_NAME, metric.getId())
          .findAll();
      deleteStatisticalValues(realm, metric.getStatisticalValue());
      metricsReportsToRemove.add(metricsReports);
    }
    for (RealmResults metricsReportToRemove : metricsReportsToRemove) {
      metricsReportToRemove.deleteAllFromRealm();
    }
  }

  private void deleteStatisticalValues(Realm realm,
      RealmStatisticalValue statisticalValueToRemove) {
    realm.where(RealmStatisticalValue.class)
        .equalTo(RealmStatisticalValue.ID_FIELD_NAME, statisticalValueToRemove.getId())
        .findAll()
        .deleteAllFromRealm();
  }

  private void storeAsRealmObject(Realm realm, DropwizardReport dropwizardReport) {
    String reportingTimestamp = String.valueOf(dropwizardReport.getReportingTimestamp());
    RealmReport report = realm.createObject(RealmReport.class, reportingTimestamp);
    RealmList<RealmMetric> realmMetricsReports =
        new DropwizardReportToRealmMetricReportMapper(realm).map(dropwizardReport);
    report.setMetrics(realmMetricsReports);
    realm.insertOrUpdate(report);
  }
}
