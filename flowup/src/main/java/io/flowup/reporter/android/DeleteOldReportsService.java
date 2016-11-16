/*
 * Copyright (C) 2016 Go Karumi S.L.
 */

package io.flowup.reporter.android;

import android.content.Context;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import io.flowup.logger.Logger;
import io.flowup.reporter.storage.ReportsStorage;
import io.flowup.utils.Time;

import static com.google.android.gms.gcm.GcmNetworkManager.RESULT_FAILURE;
import static com.google.android.gms.gcm.GcmNetworkManager.RESULT_SUCCESS;
import static io.flowup.reporter.android.DeleteOldReportsServiceScheduler.CLEAN_OLD_REPORTS;

public class DeleteOldReportsService extends GcmTaskService {

  @Override public int onRunTask(TaskParams taskParams) {
    Logger.d("Let's start with the delete old reports process");
    if (!isTaskSupported(taskParams)) {
      return RESULT_FAILURE;
    }
    int numberOfReportsDeleted = deleteOldReports();
    Logger.d("Number of reports deleted = " + numberOfReportsDeleted);
    return RESULT_SUCCESS;
  }

  private int deleteOldReports() {
    Context context = getApplicationContext();
    ReportsStorage storage = new ReportsStorage(context, new Time());
    return storage.deleteOldReports();
  }

  private boolean isTaskSupported(TaskParams taskParams) {
    return taskParams.getTag().equals(CLEAN_OLD_REPORTS);
  }
}