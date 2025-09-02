#!/bin/bash

./download_libyuv.sh && \

FFMPEG_EXT_PATH="$(pwd)/src/main"
echo ${FFMPEG_EXT_PATH}
# NDK_PATH="/home/chaolee/Android/Sdk/ndk/26.3.11579264"
NDK_PATH="/home/chaolee/Android/Sdk/ndk/27.3.13750724"
echo ${NDK_PATH}
# HOST_PLATFORM="linux-x86_64"
# echo ${HOST_PLATFORM}
ANDROID_ABI=23
echo ${ANDROID_ABI}

cd "${FFMPEG_EXT_PATH}/jni" && \
./build_yuv.sh \
  "${FFMPEG_EXT_PATH}" "${NDK_PATH}" "${ANDROID_ABI}"

echo "make clean --> finished"
echo " "
