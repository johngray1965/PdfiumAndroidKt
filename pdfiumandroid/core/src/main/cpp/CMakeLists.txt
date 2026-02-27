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
