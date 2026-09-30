package com.smile.karaokeplayer.exoRenderersFactory;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.audio.AudioProcessor;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.exoplayer.audio.AudioSink;
import androidx.media3.exoplayer.audio.DefaultAudioSink;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;
import androidx.media3.exoplayer.video.VideoRendererEventListener;

import com.smile.karaoke.utilities.LogUtil;
import com.smile.karaokeplayer.audioProcessors.StereoVolumeAudioProcessor;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

@UnstableApi
public class MyRenderersFactory extends DefaultRenderersFactory {

    private static final String TAG = "MyRenderersFactory";

    // Customized AudioProcessor
    private final StereoVolumeAudioProcessor
            stereoVolumeAudioProcessor = new StereoVolumeAudioProcessor();
    private final AudioProcessor[] audioProcessors = {stereoVolumeAudioProcessor};

    public MyRenderersFactory(Context context, int extension_renderer_mode) {
        super(context);
        setExtensionRendererMode(extension_renderer_mode);
        // setExtensionRendererMode(EXTENSION_RENDERER_MODE_ON);   // default is using extension
        // setExtensionRendererMode(EXTENSION_RENDERER_MODE_OFF);     // do not use extension
        // setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER);
        LogUtil.d(TAG, "MyRenderersFactory.created");
    }

    public StereoVolumeAudioProcessor getStereoVolumeAudioProcessor() {
        return stereoVolumeAudioProcessor;
    }

    @Nullable
    @Override
    protected AudioSink buildAudioSink(@NonNull Context context,
                                       boolean enableFloatOutput,
                                       boolean enableAudioTrackPlaybackParams) {
        LogUtil.d(TAG, "buildAudioSink");
        return new DefaultAudioSink.Builder(context)
                // .setAudioCapabilities(AudioCapabilities.DEFAULT_AUDIO_CAPABILITIES)
                .setAudioProcessors(audioProcessors)
                .setEnableFloatOutput(enableFloatOutput)
                .build();
    }

    @Override
    protected void buildVideoRenderers(
            @NonNull Context context,
            @ExtensionRendererMode int extensionRendererMode,
            @NonNull MediaCodecSelector mediaCodecSelector,
            boolean enableDecoderFallback,
            @NonNull Handler eventHandler,
            @NonNull VideoRendererEventListener eventListener,
            long allowedVideoJoiningTimeMs,
            @NonNull ArrayList<Renderer> out) {
        super.buildVideoRenderers(
                context,
                extensionRendererMode,
                mediaCodecSelector,
                enableDecoderFallback,
                eventHandler,
                eventListener,
                allowedVideoJoiningTimeMs,
                out);

        if (extensionRendererMode == EXTENSION_RENDERER_MODE_OFF) {
            return;
        }
        int extensionRendererIndex = out.size();
        if (extensionRendererMode == EXTENSION_RENDERER_MODE_PREFER) {
            extensionRendererIndex--;
        }

        try {
            Class<?> clazz = Class.forName("androidx.media3.decoder.ffmpeg.ExperimentalFfmpegVideoRenderer");
            Constructor<?> constructor = clazz.getConstructor(
                    long.class,
                    Handler.class,
                    VideoRendererEventListener.class,
                    int.class);
            Renderer renderer = (Renderer) constructor.newInstance(
                    allowedVideoJoiningTimeMs,
                    eventHandler,
                    eventListener,
                    MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY);
            out.add(extensionRendererIndex++, renderer);
            LogUtil.d(TAG, "Loaded ExperimentalFfmpegVideoRenderer.");
        } catch (ClassNotFoundException e) {
            // Extension not included
        } catch (Exception e) {
            LogUtil.e(TAG, "Error instantiating FFmpeg video extension", e);
        }
    }
}
