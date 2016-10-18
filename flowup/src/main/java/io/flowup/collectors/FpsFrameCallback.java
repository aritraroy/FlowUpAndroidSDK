/*
 * Copyright (C) 2016 Go Karumi S.L.
 */

package io.flowup.collectors;

import android.view.Choreographer;
import com.codahale.metrics.Histogram;
import io.flowup.android.LastFrameTimeCallback;

class FpsFrameCallback extends LastFrameTimeCallback {

  private final Histogram histogram;

  FpsFrameCallback(Histogram histogram, Choreographer choreographer) {
    super(choreographer);
    this.histogram = histogram;
  }

  @Override protected void onFrameTimeMeasured(long frameTimeNanos) {
    double fps = 1000000000d / frameTimeNanos;
    histogram.update((int) fps);
  }
}