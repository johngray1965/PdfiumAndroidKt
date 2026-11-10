# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.12...4.1.0)
# Declares and names the project.

project("pdfiumandroid")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        pdfiumandroid

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        pdfiumandroid.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

find_library( # Sets the name of the path variable.
        android-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        android)

find_library( # Sets the name of the path variable.
        jnigraphics-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        jnigraphics)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
#if(CMAKE_SYSTEM_PROCESSOR MATCHES "aarch64" OR CMAKE_SYSTEM_PROCESSOR MATCHES "arm64")
#    message(STATUS "Enabling HWASan for arm64")
#    target_compile_options(pdfiumandroid PUBLIC -fsanitize=hwaddress -fno-omit-frame-pointer)
#    target_link_options(pdfiumandroid PUBLIC -fsanitize=hwaddress)
#endif()

target_link_libraries( # Specifies the target library.
        pdfiumandroid

        libpdfium

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${android-lib}
        ${jnigraphics-lib}
        )


add_library( libpdfium SHARED IMPORTED )

set_target_properties( libpdfium PROPERTIES IMPORTED_LOCATION ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libpdfium.so)

# 16 KB page size: pad PT_GNU_RELRO out to a 16 KB boundary (64-bit only --
# every 16 KB page device is 64-bit).
#
# NDK r27+ links 64-bit ABIs with max-page-size=16384, so the PT_LOAD segments
# are already 16 KB aligned.  lld, though, pads .relro_padding only out to
# -z common-page-size, which still defaults to 4096, so PT_GNU_RELRO ends on a
# 4 KB boundary and `vaddr + memsz % 16384 != 0`.
#
# This is cosmetic, NOT a correctness or hardening fix.  bionic page-rounds the
# RELRO end *up* (page_end) and deliberately over-protects, and RELRO here fills
# its entire RW LOAD segment while .data/.bss sit in the next LOAD segment,
# which starts on the following 16 KB page -- so the whole GOT is already
# read-only on a 16 KB device, and the mprotect range is byte-for-byte identical
# with and without these flags.  That geometry is also the documented exception
# to the end-alignment rule, so the libraries are compliant either way.
#
# The flags are kept only so that third-party 16 KB checkers which test
# `vaddr + memsz` alone -- without applying the exception -- stop reporting a
# false positive against the published AARs.  Measured cost: 0 bytes in the
# stripped .so, because .relro_padding is NOBITS.
# See https://developer.android.com/guide/practices/page-sizes
if(ANDROID_ABI STREQUAL "arm64-v8a" OR ANDROID_ABI STREQUAL "x86_64")
    target_link_options(pdfiumandroid PRIVATE
            "-Wl,-z,max-page-size=16384"
            "-Wl,-z,common-page-size=16384")
endif()
