//
// Created by chaolee on 9/28/26.
//
#include <jni.h>
#include <dlfcn.h>
#include <android/log.h>

#ifndef RTLD_NOLOAD
#define RTLD_NOLOAD 0x00004
#endif

#define LOG_TAG "VlcBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

typedef int (*libvlc_audio_set_channel_fn)(void* p_mi, int channel);

extern "C" JNIEXPORT jint JNICALL
Java_com_smile_videoplayer_jni_VlcNativeBridge_setNativeAudioChannel(
        JNIEnv* env, jobject thiz, jlong player_instance_ptr, jint channel) {

    if (player_instance_ptr == 0) {
        LOGE("setNativeAudioChannel: player_instance_ptr is 0");
        return -101;
    }

    void* p_mi = reinterpret_cast<void*>(player_instance_ptr);

    // Resolve libvlc_audio_set_channel from libvlc.so
    void* vlc_handle = dlopen("libvlc.so", RTLD_LAZY | RTLD_NOLOAD);
    if (!vlc_handle) vlc_handle = dlopen("libvlc.so", RTLD_LAZY);

    auto set_channel_fn = (libvlc_audio_set_channel_fn) dlsym(RTLD_DEFAULT, "libvlc_audio_set_channel");
    if (!set_channel_fn && vlc_handle) {
        set_channel_fn = (libvlc_audio_set_channel_fn) dlsym(vlc_handle, "libvlc_audio_set_channel");
    }
    if (!set_channel_fn && vlc_handle) {
        set_channel_fn = (libvlc_audio_set_channel_fn) dlsym(vlc_handle, "libvlc_audio_output_channel_set");
    }

    if (!set_channel_fn) {
        LOGE("setNativeAudioChannel: Failed to locate symbol libvlc_audio_set_channel");
        return -104;
    }

    // Call native LibVLC function
    int res = set_channel_fn(p_mi, channel);
    LOGI("setNativeAudioChannel: libvlc_audio_set_channel(p_mi=%p, channel=%d) returned %d", p_mi, channel, res);
    return res;
}
