/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer.hls;

import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;

import java.io.IOException;
import java.util.Arrays;

/**
 * An abstract base class for {@link HlsChunk} implementations where the data should be loaded into
 * a {@code byte[]} before being consumed.
 */
public abstract class DataChunk extends HlsChunk {

  private static final int READ_GRANULARITY = 16 * 1024;

  private byte[] data;
  private int limit;

  private volatile boolean loadFinished;
  private volatile boolean loadCanceled;

  /**
   * @param dataSource The source from which the data should be loaded.
   * @param dataSpec Defines the data to be loaded. {@code dataSpec.length} must not exceed
   *     {@link Integer#MAX_VALUE}. If {@code dataSpec.length == C.LENGTH_UNBOUNDED} then
   *     the length resolved by {@code dataSource.open(dataSpec)} must not exceed
   *     {@link Integer#MAX_VALUE}.
   * @param data An optional recycled array that can be used as a holder for the data.
   */
  public DataChunk(DataSource dataSource, DataSpec dataSpec, byte[] data) {
    super(dataSource, dataSpec);
    this.data = data;
  }

  @Override
  public void consume() throws IOException {
    consume(data, limit);
  }

  /**
   * Invoked by {@link #consume()}. Implementations should override this method to consume the
   * loaded data.
   *
   * @param data An array containing the data.
   * @param limit The limit of the data.
   * @throws IOException If an error occurs consuming the loaded data.
   */
  protected abstract void consume(byte[] data, int limit) throws IOException;

  /**
   * Whether the whole of the chunk has been loaded.
   *
   * @return True if the whole of the chunk has been loaded. False otherwise.
   */
  @Override
  public boolean isLoadFinished() {
    return loadFinished;
  }

  // Loadable implementation

  @Override
  public final void cancelLoad() {
    loadCanceled = true;
  }

  @Override
  public final boolean isLoadCanceled() {
    return loadCanceled;
  }

  @Override
  public final void load() throws IOException, InterruptedException {
    try {
      dataSource.open(dataSpec);
      limit = 0;
      int bytesRead = 0;
      while (bytesRead != -1 && !loadCanceled) {
        maybeExpandData();
        bytesRead = dataSource.read(data, limit, READ_GRANULARITY);
        if (bytesRead != -1) {
          limit += bytesRead;
        }
      }
      loadFinished = !loadCanceled;
    } finally {
      dataSource.close();
    }
  }

  private void maybeExpandData() {
    if (data == null) {
      data = new byte[READ_GRANULARITY];
    } else if (data.length < limit + READ_GRANULARITY) {
      // The new length is calculated as (data.length + READ_GRANULARITY) rather than
      // (limit + READ_GRANULARITY) in order to avoid small increments in the length.
      data = Arrays.copyOf(data, data.length + READ_GRANULARITY);
    }
  }

}
