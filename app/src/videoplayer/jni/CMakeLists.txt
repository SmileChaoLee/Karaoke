cmake_minimum_required(VERSION 3.22.1)

project("vlcbridge")

add_library(
        vlcbridge
        SHARED
        vlcbridge.cpp
)

find_library(
        log-lib
        log
)

target_link_options(vlcbridge PRIVATE "-Wl,-z,max-page-size=16384")
target_link_libraries(
        vlcbridge
        ${log-lib}
        dl
)

