#This script file must be under ${EXOPLAYER_ROOT}/extensions/flac"
FLAC_EXT_PATH="$(pwd)/src/main"

# Download and extract flac as "${FLAC_EXT_PATH}/jni/libflac" folder:
cd "${FLAC_EXT_PATH}/jni"
git clone https://github.com/xiph/flac.git libflac
