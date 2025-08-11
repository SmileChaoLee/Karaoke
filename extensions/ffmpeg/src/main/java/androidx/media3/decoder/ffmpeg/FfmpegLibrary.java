/*
 * Copyright (C) 2016 The Android Open Source Project
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
package androidx.media3.decoder.ffmpeg;

import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.MediaLibraryInfo;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.util.LibraryLoader;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/** Configures and queries the underlying native library. */
@UnstableApi
public final class FfmpegLibrary {

  static {
    MediaLibraryInfo.registerModule("media3.decoder.ffmpeg");
  }

  private static final String TAG = "FfmpegLibrary";

  private static final LibraryLoader LOADER =
          new LibraryLoader("ffmpegJNI") {
            @Override
            protected void loadLibrary(String name) {
              System.loadLibrary(name);
            }
          };

  private static @MonotonicNonNull String version;
  private static int inputBufferPaddingSize = C.LENGTH_UNSET;

  private FfmpegLibrary() {}

  /**
   * Override the names of the FFmpeg native libraries. If an application wishes to call this
   * method, it must do so before calling any other method defined by this class, and before
   * instantiating a {@link FfmpegAudioRenderer} or {@link ExperimentalFfmpegVideoRenderer}
   * instance.
   *
   * @param libraries The names of the FFmpeg native libraries.
   */
  public static void setLibraries(String... libraries) {
    LOADER.setLibraries(libraries);
  }

  /** Returns whether the underlying library is available, loading it if necessary. */
  public static boolean isAvailable() {
    return LOADER.isAvailable();
  }

  /** Returns the version of the underlying library if available, or null otherwise. */
  @Nullable
  public static String getVersion() {
    if (!isAvailable()) {
      Log.i(TAG, "getVersion.Library not available.");
      return null;
    }
    if (version == null) {
      version = ffmpegGetVersion();
    }
    Log.i(TAG, "getVersion.version = " + version);
    return version;
  }

  /**
   * Returns the required amount of padding for input buffers in bytes, or {@link C#LENGTH_UNSET} if
   * the underlying library is not available.
   */
  public static int getInputBufferPaddingSize() {
    if (!isAvailable()) {
      Log.i(TAG, "getInputBufferPaddingSize.Library not available.");
      return C.LENGTH_UNSET;
    }
    if (inputBufferPaddingSize == C.LENGTH_UNSET) {
      inputBufferPaddingSize = ffmpegGetInputBufferPaddingSize();
    }
    Log.i(TAG, "getInputBufferPaddingSize.inputBufferPaddingSize = " + inputBufferPaddingSize);
    return inputBufferPaddingSize;
  }

  /**
   * Returns whether the underlying library supports the specified MIME type.
   *
   * @param mimeType The MIME type to check.
   */
  public static boolean supportsFormat(String mimeType) {
    if (!isAvailable()) {
      return false;
    }
    @Nullable String codecName = getCodecName(mimeType);
    if (codecName == null) {
      Log.w(TAG, "supportsFormat.codecName is null");
      return false;
    }
    if (!ffmpegHasDecoder(codecName)) {
      Log.w(TAG, "supportsFormat.No " + codecName + " decoder available. Check the FFmpeg build configuration.");
      return false;
    }
    Log.i(TAG, "supportsFormat." + codecName + " decoder available.");
    return true;
  }

  /**
   * Returns the name of the FFmpeg decoder that could be used to decode the format, or {@code null}
   * if it's unsupported.
   */
  @Nullable
  /* package */ static String getCodecName(String mimeType) {
    Log.i(TAG, "getCodecName.mimeType = " + mimeType);
    String codecName;
    switch (mimeType) {
      case MimeTypes.AUDIO_AAC:
        codecName = "aac";
        break;
      case MimeTypes.AUDIO_MPEG:
      case MimeTypes.AUDIO_MPEG_L1:
      case MimeTypes.AUDIO_MPEG_L2:
        codecName = "mp3";
        break;
      case MimeTypes.AUDIO_AC3:
        codecName = "ac3";
        break;
      case MimeTypes.AUDIO_E_AC3:
      case MimeTypes.AUDIO_E_AC3_JOC:
        codecName = "eac3";
        break;
      case MimeTypes.AUDIO_TRUEHD:
        codecName = "truehd";
        break;
      case MimeTypes.AUDIO_DTS:
      case MimeTypes.AUDIO_DTS_HD:
        codecName = "dca";
        break;
      case MimeTypes.AUDIO_VORBIS:
        codecName = "vorbis";
        break;
      case MimeTypes.AUDIO_OPUS:
        codecName = "opus";
        break;
      case MimeTypes.AUDIO_AMR_NB:
        codecName = "amrnb";
        break;
      case MimeTypes.AUDIO_AMR_WB:
        codecName = "amrwb";
        break;
      case MimeTypes.AUDIO_FLAC:
        codecName = "flac";
        break;
      case MimeTypes.AUDIO_ALAC:
        codecName = "alac";
        break;
      case MimeTypes.AUDIO_MLAW:
        codecName = "pcm_mulaw";
        break;
      case MimeTypes.AUDIO_ALAW:
        codecName = "pcm_alaw";
        break;
      case MimeTypes.VIDEO_MPEG:
        codecName = "mpeg1video";
        break;
      case MimeTypes.VIDEO_MPEG2:
        codecName = "mpeg2video";
        break;
      case MimeTypes.VIDEO_H264:
        codecName = "h264";
        break;
      case MimeTypes.VIDEO_H265:
      case MimeTypes.VIDEO_DOLBY_VISION:
        codecName = "hevc";
        break;
      case MimeTypes.VIDEO_MP4:
      case MimeTypes.VIDEO_MP42:
      case MimeTypes.VIDEO_MP43:
      case MimeTypes.VIDEO_MP4V:
        codecName = "mpeg4";
        break;
      default:
        codecName = null;
        break;
    }
    Log.i(TAG, "getCodecName.codecName = " + codecName);
    return codecName;
  }

  private static native String ffmpegGetVersion();

  private static native int ffmpegGetInputBufferPaddingSize();

  private static native boolean ffmpegHasDecoder(String codecName);
}