#!/bin/bash

./download_ffmpeg.sh && \

FFMPEG_EXT_PATH="$(pwd)/src/main"
echo ${FFMPEG_EXT_PATH}
# NDK_PATH="/home/chaolee/Android/Sdk/ndk/22.1.7171670"
NDK_PATH="/home/chaolee/Android/Sdk/ndk/26.3.11579264"
echo ${NDK_PATH}
HOST_PLATFORM="linux-x86_64"
echo ${HOST_PLATFORM}
ANDROID_ABI="23"
echo ${ANDROID_ABI}
ENABLED_DECODERS=(vorbis opus flac alac pcm_mulaw pcm_alaw aac mp3 amrnb amrwb ac3 eac3 dca mlp truehd)
# ENABLED_DECODERS=(vorbis opus flac mp3)

cd "${FFMPEG_EXT_PATH}/jni" && \
./build_ffmpeg.sh \
  "${FFMPEG_EXT_PATH}" "${NDK_PATH}" "${HOST_PLATFORM}" "${ANDROID_ABI}" "${ENABLED_DECODERS[@]}"

echo "make clean --> finished"
echo " "
