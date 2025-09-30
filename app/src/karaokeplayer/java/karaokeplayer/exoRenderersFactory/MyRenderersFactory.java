package karaokeplayer.exoRenderersFactory;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.audio.AudioProcessor;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.audio.AudioSink;
import androidx.media3.exoplayer.audio.DefaultAudioSink;
import karaokeplayer.audioProcessors.StereoVolumeAudioProcessor;

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
        Log.d(TAG, "MyRenderersFactory.created");
    }

    public StereoVolumeAudioProcessor getStereoVolumeAudioProcessor() {
        return stereoVolumeAudioProcessor;
    }

    @Nullable
    @Override
    protected AudioSink buildAudioSink(@NonNull Context context,
                                       boolean enableFloatOutput,
                                       boolean enableAudioTrackPlaybackParams) {
        Log.d(TAG, "buildAudioSink");
        return new DefaultAudioSink.Builder(context)
                // .setAudioCapabilities(AudioCapabilities.DEFAULT_AUDIO_CAPABILITIES)
                .setAudioProcessors(audioProcessors)
                .setEnableFloatOutput(enableFloatOutput)
                .build();
    }
}
