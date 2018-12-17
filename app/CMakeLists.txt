# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

#警告移除
add_definitions (-Wno-format-security -Wno-pointer-sign -DANDROID_DISABLE_FORMAT_STRING_CHECKS=TRUE -fexceptions)

set(OPENSSL_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/openssl)

# 加入头文件
include_directories(${OPENSSL_MODULE_PATH}/include)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

# 加密库、协议库从实际实体手机中拷贝出来，位于/system/lib/下。
# 创建crypto库
add_library(crypto SHARED IMPORTED)

set_target_properties(
              crypto
              PROPERTIES
              IMPORTED_LOCATION
              ${OPENSSL_MODULE_PATH}/x86/libcrypto.so)

 # 创建libssl库
add_library(ssl SHARED IMPORTED)

set_target_properties(
              ssl
              PROPERTIES
              IMPORTED_LOCATION
              ${OPENSSL_MODULE_PATH}/x86/libssl.so)

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       crypto
                       ssl
                       ${log-lib} )