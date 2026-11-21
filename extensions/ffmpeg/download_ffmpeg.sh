#!/bin/bash

FFMPEG_EXT_PATH="$(pwd)/src/main"
echo ${FFMPEG_EXT_PATH}

cd "${FFMPEG_EXT_PATH}/jni" && \
if [ ! -d "ffmpeg" ]; then
    git clone https://git.ffmpeg.org/ffmpeg.git ffmpeg && \
    cd ffmpeg && \
    git checkout release/6.0
else
    echo "ffmpeg directory already exists"
fi


echo " "
echo "============================"
echo "Completed downloading ffmpeg"
echo "============================"