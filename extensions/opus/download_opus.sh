#This script file must be under ${EXOPLAYER_ROOT}/extensions/opus"
OPUS_MODULE_PATH="$(pwd)/src/main"

# Fetch libopus:
cd "${OPUS_MODULE_PATH}/jni" && \
git clone https://gitlab.xiph.org/xiph/opus.git libopus
