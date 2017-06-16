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
package com.google.android.exoplayer;

import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.Util;

import android.annotation.TargetApi;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaCodecList;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import java.util.HashMap;

/**
 * A utility class for querying the available codecs.
 */
@TargetApi(16)
public class MediaCodecUtil {

  /**
   * Thrown when an error occurs querying the device for its underlying media capabilities.
   * <p>
   * Such failures are not expected in normal operation and are normally temporary (e.g. if the
   * mediaserver process has crashed and is yet to restart).
   */
  public static class DecoderQueryException extends Exception {

    private DecoderQueryException(Throwable cause) {
      super("Failed to query underlying media codecs", cause);
    }

  }

  private static final String TAG = "MediaCodecUtil";

  private static final HashMap<CodecKey, Pair<String, CodecCapabilities>> codecs =
      new HashMap<CodecKey, Pair<String, CodecCapabilities>>();

  /**
   * Get information about the decoder that will be used for a given mime type.
   *
   * @param mimeType The mime type.
   * @param secure Whether the decoder is required to support secure decryption. Always pass false
   *     unless secure decryption really is required.
   * @return Information about the decoder that will be used, or null if no decoder exists.
   */
  public static DecoderInfo getDecoderInfo(String mimeType, boolean secure)
      throws DecoderQueryException {
    Pair<String, CodecCapabilities> info = getMediaCodecInfo(mimeType, secure);
    if (info == null) {
      return null;
    }
    return new DecoderInfo(info.first, isAdaptive(info.second));
  }

  /**
   * Optional call to warm the codec cache for a given mime type.
   * <p>
   * Calling this method may speed up subsequent calls to {@link #getDecoderInfo(String, boolean)}.
   *
   * @param mimeType The mime type.
   * @param secure Whether the decoder is required to support secure decryption. Always pass false
   *     unless secure decryption really is required.
   */
  public static synchronized void warmCodec(String mimeType, boolean secure) {
    try {
      getMediaCodecInfo(mimeType, secure);
    } catch (DecoderQueryException e) {
      // Codec warming is best effort, so we can swallow the exception.
      Log.e(TAG, "Codec warming failed", e);
    }
  }

  /**
   * Returns the name of the best decoder and its capabilities for the given mimeType.
   */
  private static synchronized Pair<String, CodecCapabilities> getMediaCodecInfo(
      String mimeType, boolean secure) throws DecoderQueryException {
    CodecKey key = new CodecKey(mimeType, secure);
    if (codecs.containsKey(key)) {
      return codecs.get(key);
    }
    MediaCodecListCompat mediaCodecList = Util.SDK_INT >= 21
        ? new MediaCodecListCompatV21(secure) : new MediaCodecListCompatV16();
    Pair<String, CodecCapabilities> codecInfo = getMediaCodecInfo(key, mediaCodecList);
    // TODO: Verify this cannot occur on v22, and change >= to == [Internal: b/18678462].
    if (secure && codecInfo == null && Util.SDK_INT >= 21) {
      // Some devices don't list secure decoders on API level 21. Try the legacy path.
      mediaCodecList = new MediaCodecListCompatV16();
      codecInfo = getMediaCodecInfo(key, mediaCodecList);
      if (codecInfo != null) {
        Log.w(TAG, "MediaCodecList API didn't list secure decoder for: " + mimeType
            + ". Assuming: " + codecInfo.first);
      }
    }
    return codecInfo;
  }

  private static Pair<String, CodecCapabilities> getMediaCodecInfo(CodecKey key,
      MediaCodecListCompat mediaCodecList) throws DecoderQueryException {
    try {
      return getMediaCodecInfoInternal(key, mediaCodecList);
    } catch (Exception e) {
      // If the underlying mediaserver is in a bad state, we may catch an IllegalStateException
      // or an IllegalArgumentException here.
      throw new DecoderQueryException(e);
    }
  }

  private static Pair<String, CodecCapabilities> getMediaCodecInfoInternal(CodecKey key,
      MediaCodecListCompat mediaCodecList) {
    String mimeType = key.mimeType;
    int numberOfCodecs = mediaCodecList.getCodecCount();
    boolean secureDecodersExplicit = mediaCodecList.secureDecodersExplicit();
    // Note: MediaCodecList is sorted by the framework such that the best decoders come first.
    for (int i = 0; i < numberOfCodecs; i++) {
      MediaCodecInfo info = mediaCodecList.getCodecInfoAt(i);
      String codecName = info.getName();
      if (!info.isEncoder() && codecName.startsWith("OMX.")
          && (secureDecodersExplicit || !codecName.endsWith(".secure"))) {
        String[] supportedTypes = info.getSupportedTypes();
        for (int j = 0; j < supportedTypes.length; j++) {
          String supportedType = supportedTypes[j];
          if (supportedType.equalsIgnoreCase(mimeType)) {
            CodecCapabilities capabilities = info.getCapabilitiesForType(supportedType);
            boolean secure = mediaCodecList.isSecurePlaybackSupported(key.mimeType, capabilities);
            if (!secureDecodersExplicit) {
              // Cache variants for both insecure and (if we think it's supported) secure playback.
              codecs.put(key.secure ? new CodecKey(mimeType, false) : key,
                  Pair.create(codecName, capabilities));
              if (secure) {
                codecs.put(key.secure ? key : new CodecKey(mimeType, true),
                    Pair.create(codecName + ".secure", capabilities));
              }
            } else {
              // Only cache this variant. If both insecure and secure decoders are available, they
              // should both be listed separately.
              codecs.put(key.secure == secure ? key : new CodecKey(mimeType, secure),
                  Pair.create(codecName, capabilities));
            }
            if (codecs.containsKey(key)) {
              return codecs.get(key);
            }
          }
        }
      }
    }
    return null;
  }

  private static boolean isAdaptive(CodecCapabilities capabilities) {
    if (Util.SDK_INT >= 19) {
      return isAdaptiveV19(capabilities);
    } else {
      return false;
    }
  }

  @TargetApi(19)
  private static boolean isAdaptiveV19(CodecCapabilities capabilities) {
    return capabilities.isFeatureSupported(CodecCapabilities.FEATURE_AdaptivePlayback);
  }

  /**
   * @param profile An AVC profile constant from {@link CodecProfileLevel}.
   * @param level An AVC profile level from {@link CodecProfileLevel}.
   * @return Whether the specified profile is supported at the specified level.
   */
  public static boolean isH264ProfileSupported(int profile, int level)
      throws DecoderQueryException {
    Pair<String, CodecCapabilities> info = getMediaCodecInfo(MimeTypes.VIDEO_H264, false);
    if (info == null) {
      return false;
    }

    CodecCapabilities capabilities = info.second;
    for (int i = 0; i < capabilities.profileLevels.length; i++) {
      CodecProfileLevel profileLevel = capabilities.profileLevels[i];
      if (profileLevel.profile == profile && profileLevel.level >= level) {
        return true;
      }
    }

    return false;
  }

  /**
   * @return the maximum frame size for an H264 stream that can be decoded on the device.
   */
  public static int maxH264DecodableFrameSize() throws DecoderQueryException {
    Pair<String, CodecCapabilities> info = getMediaCodecInfo(MimeTypes.VIDEO_H264, false);
    if (info == null) {
      return 0;
    }

    int maxH264DecodableFrameSize = 0;
    CodecCapabilities capabilities = info.second;
    for (int i = 0; i < capabilities.profileLevels.length; i++) {
      CodecProfileLevel profileLevel = capabilities.profileLevels[i];
      maxH264DecodableFrameSize = Math.max(
          avcLevelToMaxFrameSize(profileLevel.level), maxH264DecodableFrameSize);
    }

    return maxH264DecodableFrameSize;
  }

  /**
   * Conversion values taken from: https://en.wikipedia.org/wiki/H.264/MPEG-4_AVC.
   *
   * @param avcLevel one of CodecProfileLevel.AVCLevel* constants.
   * @return maximum frame size that can be decoded by a decoder with the specified avc level
   *      (or {@code -1} if the level is not recognized)
   */
  private static int avcLevelToMaxFrameSize(int avcLevel) {
    switch (avcLevel) {
      case CodecProfileLevel.AVCLevel1: return 25344;
      case CodecProfileLevel.AVCLevel1b: return 25344;
      case CodecProfileLevel.AVCLevel12: return 101376;
      case CodecProfileLevel.AVCLevel13: return 101376;
      case CodecProfileLevel.AVCLevel2: return 101376;
      case CodecProfileLevel.AVCLevel21: return 202752;
      case CodecProfileLevel.AVCLevel22: return 414720;
      case CodecProfileLevel.AVCLevel3: return 414720;
      case CodecProfileLevel.AVCLevel31: return 921600;
      case CodecProfileLevel.AVCLevel32: return 1310720;
      case CodecProfileLevel.AVCLevel4: return 2097152;
      case CodecProfileLevel.AVCLevel41: return 2097152;
      case CodecProfileLevel.AVCLevel42: return 2228224;
      case CodecProfileLevel.AVCLevel5: return 5652480;
      case CodecProfileLevel.AVCLevel51: return 9437184;
      default: return -1;
    }
  }

  private interface MediaCodecListCompat {

    /**
     * The number of codecs in the list.
     */
    public int getCodecCount();

    /**
     * The info at the specified index in the list.
     *
     * @param index The index.
     */
    public MediaCodecInfo getCodecInfoAt(int index);

    /**
     * @return Returns whether secure decoders are explicitly listed, if present.
     */
    public boolean secureDecodersExplicit();

    /**
     * Whether secure playback is supported for the given {@link CodecCapabilities}, which should
     * have been obtained from a {@link MediaCodecInfo} obtained from this list.
     */
    public boolean isSecurePlaybackSupported(String mimeType, CodecCapabilities capabilities);

  }

  @TargetApi(21)
  private static final class MediaCodecListCompatV21 implements MediaCodecListCompat {

    private final int codecKind;

    private MediaCodecInfo[] mediaCodecInfos;

    public MediaCodecListCompatV21(boolean includeSecure) {
      codecKind = includeSecure ? MediaCodecList.ALL_CODECS : MediaCodecList.REGULAR_CODECS;
    }

    @Override
    public int getCodecCount() {
      ensureMediaCodecInfosInitialized();
      return mediaCodecInfos.length;
    }

    @Override
    public MediaCodecInfo getCodecInfoAt(int index) {
      ensureMediaCodecInfosInitialized();
      return mediaCodecInfos[index];
    }

    @Override
    public boolean secureDecodersExplicit() {
      return true;
    }

    @Override
    public boolean isSecurePlaybackSupported(String mimeType, CodecCapabilities capabilities) {
      return capabilities.isFeatureSupported(CodecCapabilities.FEATURE_SecurePlayback);
    }

    private void ensureMediaCodecInfosInitialized() {
      if (mediaCodecInfos == null) {
        mediaCodecInfos = new MediaCodecList(codecKind).getCodecInfos();
      }
    }

  }

  @SuppressWarnings("deprecation")
  private static final class MediaCodecListCompatV16 implements MediaCodecListCompat {

    @Override
    public int getCodecCount() {
      return MediaCodecList.getCodecCount();
    }

    @Override
    public MediaCodecInfo getCodecInfoAt(int index) {
      return MediaCodecList.getCodecInfoAt(index);
    }

    @Override
    public boolean secureDecodersExplicit() {
      return false;
    }

    @Override
    public boolean isSecurePlaybackSupported(String mimeType, CodecCapabilities capabilities) {
      // Secure decoders weren't explicitly listed prior to API level 21. We assume that a secure
      // H264 decoder exists.
      return MimeTypes.VIDEO_H264.equals(mimeType);
    }

  }

  private static final class CodecKey {

    public final String mimeType;
    public final boolean secure;

    public CodecKey(String mimeType, boolean secure) {
      this.mimeType = mimeType;
      this.secure = secure;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((mimeType == null) ? 0 : mimeType.hashCode());
      result = prime * result + (secure ? 1231 : 1237);
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || obj.getClass() != CodecKey.class) {
        return false;
      }
      CodecKey other = (CodecKey) obj;
      return TextUtils.equals(mimeType, other.mimeType) && secure == other.secure;
    }

  }

}
