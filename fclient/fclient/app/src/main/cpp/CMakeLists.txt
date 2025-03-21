cmake_minimum_required(VERSION 3.31.5)

project("fclient")

add_library(${CMAKE_PROJECT_NAME} SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        native-lib.cpp)

target_link_libraries( # Specifies the target library.
        fclient
        spdlog
        mbedcrypto
        # Links the target library to the log library
        # included in the NDK.
        android
        log
        ${log-lib} )

add_library(spdlog STATIC IMPORTED)

set_target_properties(spdlog PROPERTIES IMPORTED_LOCATION
        /Users/ronim/Documents/main/Software_dev/fclient/libs/spdlog/build/${ANDROID_ABI}/libspdlog.a)
target_include_directories(fclient PUBLIC
        /Users/ronim/Documents/main/Software_dev/fclient/libs/spdlog/spdlog/include
)

add_library(mbedcrypto SHARED IMPORTED)
set_target_properties(mbedcrypto PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../nativeLibs/${ANDROID_ABI}/library/libmbedcrypto.so)

target_include_directories(fclient PUBLIC
        /Users/ronim/Documents/main/Software_dev/fclient/libs/spdlog/spdlog/include
        /Users/ronim/Documents/main/Software_dev/fclient/libs/mbedtls/mbedtls/include
)



