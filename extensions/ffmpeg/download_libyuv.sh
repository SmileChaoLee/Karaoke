#!/bin/bash

FFMPEG_EXT_PATH="$(pwd)/src/main"
echo ${FFMPEG_EXT_PATH}

cd "${FFMPEG_EXT_PATH}/jni" && \

git clone https://chromium.googlesource.com/libyuv/libyuv && \
cd libyuv  && \
git checkout 996a2bbd

echo " "
echo "============================"
echo "Completed downloading libyuv"
echo "============================"