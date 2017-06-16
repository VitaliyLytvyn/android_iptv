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
package com.google.android.exoplayer.upstream;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.upstream.Loader.Loadable;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.Util;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Loads data from a {@link DataSource} into an in-memory {@link Allocation}. The loaded data
 * can be consumed by treating the instance as a non-blocking {@link NonBlockingInputStream}.
 */
public final class DataSourceStream implements Loadable, NonBlockingInputStream {

  /**
   * Thrown when an error is encountered trying to load data into a {@link DataSourceStream}.
   */
  public static class DataSourceStreamLoadException extends IOException {

    public DataSourceStreamLoadException(IOException cause) {
      super(cause);
    }

  }

  private static final int CHUNKED_ALLOCATION_INCREMENT = 256 * 1024;

  private final DataSource dataSource;
  private final DataSpec dataSpec;
  private final Allocator allocator;
  private final ReadHead readHead;

  private Allocation allocation;

  private volatile boolean loadCanceled;
  private volatile long loadPosition;
  private volatile long resolvedLength;

  private int writeFragmentIndex;
  private int writeFragmentOffset;
  private int writeFragmentRemainingLength;

  /**
   * @param dataSource The source from which the data should be loaded.
   * @param dataSpec Defines the data to be loaded. {@code dataSpec.length} must not exceed
   *     {@link Integer#MAX_VALUE}. If {@code dataSpec.length == C.LENGTH_UNBOUNDED} then
   *     the length resolved by {@code dataSource.open(dataSpec)} must not exceed
   *     {@link Integer#MAX_VALUE}.
   * @param allocator Used to obtain an {@link Allocation} for holding the data.
   */
  public DataSourceStream(DataSource dataSource, DataSpec dataSpec, Allocator allocator) {
    Assertions.checkState(dataSpec.length <= Integer.MAX_VALUE);
    this.dataSource = dataSource;
    this.dataSpec = dataSpec;
    this.allocator = allocator;
    resolvedLength = C.LENGTH_UNBOUNDED;
    readHead = new ReadHead();
  }

  /**
   * Resets the read position to the start of the data.
   */
  public void resetReadPosition() {
    readHead.reset();
  }

  /**
   * Returns the current read position for data being read out of the source.
   *
   * @return The current read position.
   */
  public long getReadPosition() {
    return readHead.position;
  }

  /**
   * Returns the number of bytes of data that have been loaded.
   *
   * @return The number of bytes of data that have been loaded.
   */
  public long getLoadPosition() {
    return loadPosition;
  }

  /**
   * Returns the length of the stream in bytes, or {@value C#LENGTH_UNBOUNDED} if the length has
   * yet to be determined.
   *
   * @return The length of the stream in bytes, or {@value C#LENGTH_UNBOUNDED} if the length has
   *     yet to be determined.
   */
  public long getLength() {
    return resolvedLength != C.LENGTH_UNBOUNDED ? resolvedLength : dataSpec.length;
  }

  /**
   * Whether the stream has finished loading.
   *
   * @return True if the stream has finished loading. False otherwise.
   */
  public boolean isLoadFinished() {
    return resolvedLength != C.LENGTH_UNBOUNDED && loadPosition == resolvedLength;
  }

  // {@link NonBlockingInputStream} implementation.

  @Override
  public long getAvailableByteCount() {
    return loadPosition - readHead.position;
  }

  @Override
  public boolean isEndOfStream() {
    return resolvedLength != C.LENGTH_UNBOUNDED && readHead.position == resolvedLength;
  }

  @Override
  public void close() {
    if (allocation != null) {
      allocation.release();
      allocation = null;
    }
  }

  @Override
  public int skip(int skipLength) {
    return read(null, null, 0, readHead, skipLength);
  }

  @Override
  public int read(ByteBuffer target1, int readLength) {
    return read(target1, null, 0, readHead, readLength);
  }

  @Override
  public int read(byte[] target, int offset, int readLength) {
    return read(null, target, offset, readHead, readLength);
  }

  /**
   * Reads data to either a target {@link ByteBuffer}, or to a target byte array at a specified
   * offset. The {@code readHead} is updated to reflect the read that was performed.
   */
  private int read(ByteBuffer target, byte[] targetArray, int targetArrayOffset,
      ReadHead readHead, int readLength) {
    if (isEndOfStream()) {
      return -1;
    }
    int bytesToRead = (int) Math.min(loadPosition - readHead.position, readLength);
    if (bytesToRead == 0) {
      return 0;
    }
    if (readHead.position == 0) {
      readHead.fragmentIndex = 0;
      readHead.fragmentOffset = allocation.getFragmentOffset(0);
      readHead.fragmentRemaining = allocation.getFragmentLength(0);
    }
    int bytesRead = 0;
    byte[][] buffers = allocation.getBuffers();
    while (bytesRead < bytesToRead) {
      if (readHead.fragmentRemaining == 0) {
        readHead.fragmentIndex++;
        readHead.fragmentOffset = allocation.getFragmentOffset(readHead.fragmentIndex);
        readHead.fragmentRemaining = allocation.getFragmentLength(readHead.fragmentIndex);
      }
      int bufferReadLength = Math.min(readHead.fragmentRemaining, bytesToRead - bytesRead);
      if (target != null) {
        target.put(buffers[readHead.fragmentIndex], readHead.fragmentOffset, bufferReadLength);
      } else if (targetArray != null) {
        System.arraycopy(buffers[readHead.fragmentIndex], readHead.fragmentOffset, targetArray,
            targetArrayOffset, bufferReadLength);
        targetArrayOffset += bufferReadLength;
      }
      readHead.position += bufferReadLength;
      bytesRead += bufferReadLength;
      readHead.fragmentOffset += bufferReadLength;
      readHead.fragmentRemaining -= bufferReadLength;
    }

    return bytesRead;
  }

  // {@link Loadable} implementation.

  @Override
  public void cancelLoad() {
    loadCanceled = true;
  }

  @Override
  public boolean isLoadCanceled() {
    return loadCanceled;
  }

  @Override
  public void load() throws IOException, InterruptedException {
    if (loadCanceled || isLoadFinished()) {
      // The load was canceled, or is already complete.
      return;
    }

    try {
      DataSpec loadDataSpec;
      if (loadPosition == 0 && resolvedLength == C.LENGTH_UNBOUNDED) {
        loadDataSpec = dataSpec;
        long resolvedLength = dataSource.open(loadDataSpec);
        if (resolvedLength > Integer.MAX_VALUE) {
          throw new DataSourceStreamLoadException(
              new UnexpectedLengthException(dataSpec.length, resolvedLength));
        }
        this.resolvedLength = resolvedLength;
      } else {
        long remainingLength = resolvedLength != C.LENGTH_UNBOUNDED
            ? resolvedLength - loadPosition : C.LENGTH_UNBOUNDED;
        loadDataSpec = new DataSpec(dataSpec.uri, dataSpec.position + loadPosition,
            remainingLength, dataSpec.key);
        dataSource.open(loadDataSpec);
      }

      if (allocation == null) {
        int initialAllocationSize = resolvedLength != C.LENGTH_UNBOUNDED
            ? (int) resolvedLength : CHUNKED_ALLOCATION_INCREMENT;
        allocation = allocator.allocate(initialAllocationSize);
      }
      int allocationCapacity = allocation.capacity();

      if (loadPosition == 0) {
        writeFragmentIndex = 0;
        writeFragmentOffset = allocation.getFragmentOffset(0);
        writeFragmentRemainingLength = allocation.getFragmentLength(0);
      }

      int read = Integer.MAX_VALUE;
      byte[][] buffers = allocation.getBuffers();
      while (!loadCanceled && read > 0 && maybeMoreToLoad()) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        read = dataSource.read(buffers[writeFragmentIndex], writeFragmentOffset,
            writeFragmentRemainingLength);
        if (read > 0) {
          loadPosition += read;
          writeFragmentOffset += read;
          writeFragmentRemainingLength -= read;
          if (writeFragmentRemainingLength == 0 && maybeMoreToLoad()) {
            writeFragmentIndex++;
            if (loadPosition == allocationCapacity) {
              allocation.ensureCapacity(allocationCapacity + CHUNKED_ALLOCATION_INCREMENT);
              allocationCapacity = allocation.capacity();
              buffers = allocation.getBuffers();
            }
            writeFragmentOffset = allocation.getFragmentOffset(writeFragmentIndex);
            writeFragmentRemainingLength = allocation.getFragmentLength(writeFragmentIndex);
          }
        } else if (resolvedLength == C.LENGTH_UNBOUNDED) {
          resolvedLength = loadPosition;
        } else if (resolvedLength != loadPosition) {
          throw new DataSourceStreamLoadException(
              new UnexpectedLengthException(resolvedLength, loadPosition));
        }
      }
    } finally {
      Util.closeQuietly(dataSource);
    }
  }

  private boolean maybeMoreToLoad() {
    return resolvedLength == C.LENGTH_UNBOUNDED || loadPosition < resolvedLength;
  }

  private static class ReadHead {

    private int position;
    private int fragmentIndex;
    private int fragmentOffset;
    private int fragmentRemaining;

    public void reset() {
      position = 0;
      fragmentIndex = 0;
      fragmentOffset = 0;
      fragmentRemaining = 0;
    }

  }

}
