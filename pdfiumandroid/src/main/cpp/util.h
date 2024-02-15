//
// Created by John Gray on 6/6/23.
//

#ifndef PDFIUMANDROIDKT_UTIL_H
#define PDFIUMANDROIDKT_UTIL_H

#include <jni.h>
extern "C" {
#include <stdlib.h>
}

#include <android/log.h>

#define JNI_FUNC(retType, bindClass, name)  JNIEXPORT retType JNICALL Java_com_shockwave_pdfium_##bindClass##_##name
#define JNI_ARGS    JNIEnv *env, jobject thiz

#define LOG_TAG "jniPdfium"
#define LOGI(...)   __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)   __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...)   __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#endif //PDFIUMANDROIDKT_UTIL_H
