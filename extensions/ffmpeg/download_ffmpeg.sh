#!/bin/bash

FFMPEG_EXT_PATH="$(pwd)/src/main"
echo ${FFMPEG_EXT_PATH}

cd "${FFMPEG_EXT_PATH}/jni" && \
#(git -C ffmpeg pull || git clone git://source.ffmpeg.org/ffmpeg ffmpeg) && \
#cd ffmpeg && git checkout release/6.0

git clone git://source.ffmpeg.org/ffmpeg && \
cd ffmpeg && \
git checkout release/6.0 && \


echo " "
echo "============================"
echo "Completed downloading ffmpeg"
echo "============================"