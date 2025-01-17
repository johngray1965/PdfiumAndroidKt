#include <jni.h>
#include <string>


extern "C" {
#include <unistd.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <string.h>
#include <stdio.h>
}

#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/bitmap.h>
#include <android/log.h>


#include "include/fpdfview.h"
#include "include/fpdf_doc.h"
#include "include/fpdf_text.h"
#include "include/fpdf_save.h"
#include "include/fpdf_transformpage.h"
#include "include/utils/Mutex.h"
#include "util.h"
#include "include/fpdf_edit.h"
#include "include/fpdf_formfill.h"
#include <vector>
#include <mutex>

static std::mutex sLibraryLock;

static int sLibraryReferenceCount = 0;

static void initLibraryIfNeed(){
    const std::lock_guard<std::mutex> lock(sLibraryLock);
    if(sLibraryReferenceCount == 0){
        LOGD("Init FPDF library");
        FPDF_InitLibrary();
    }
    sLibraryReferenceCount++;
    sLibraryLock.unlock();
}

static void destroyLibraryIfNeed(){
    const std::lock_guard<std::mutex> lock(sLibraryLock);
    sLibraryReferenceCount--;
    LOGD("sLibraryReferenceCount %d", sLibraryReferenceCount);
    if(sLibraryReferenceCount == 0){
        LOGD("Destroy FPDF library");
        FPDF_DestroyLibrary();
    }
}

struct rgb {
    uint8_t red;
    uint8_t green;
    uint8_t blue;
};

JavaVM* javaVm;

bool jniAttachCurrentThread(JNIEnv **env, bool *attachedOut) {
    JavaVMAttachArgs jvmArgs;
    jvmArgs.version = JNI_VERSION_1_6;

    bool attached = false;
    if (javaVm->GetEnv((void **) env, JNI_VERSION_1_6) == JNI_EDETACHED) {
        if (javaVm->AttachCurrentThread(env, &jvmArgs) != JNI_OK) {
            LOGE("Cannot attach current thread");
            return false;
        }
        attached = true;
    } else {
        attached = false;
    }
    *attachedOut = attached;
    return true;
}

bool jniDetachCurrentThread(bool attached) {
    if (attached && javaVm->DetachCurrentThread() != JNI_OK) {
        LOGE("Cannot detach current thread");
        return false;
    }
    return true;
}

class DocumentFile {

public:
    FPDF_DOCUMENT pdfDocument = nullptr;

public:
    jobject nativeSourceBridgeGlobalRef = nullptr;
    jbyte *cDataCopy = nullptr;

    DocumentFile() { initLibraryIfNeed(); }
    ~DocumentFile();
};

DocumentFile::~DocumentFile(){
    if(pdfDocument != nullptr){
        FPDF_CloseDocument(pdfDocument);
        pdfDocument = nullptr;
    }
    if(cDataCopy != nullptr){
        free(cDataCopy);
        cDataCopy = nullptr;
    }
    if(nativeSourceBridgeGlobalRef != nullptr){
        JNIEnv *env;
        bool attached;
        if(jniAttachCurrentThread(&env, &attached)){
            env->DeleteGlobalRef(nativeSourceBridgeGlobalRef);
            jniDetachCurrentThread(attached);
        }
    }
    destroyLibraryIfNeed();
}

template <class string_type>
inline typename string_type::value_type* WriteInto(string_type* str, size_t length_with_null) {
    str->reserve(length_with_null);
    str->resize(length_with_null - 1);
    return &((*str)[0]);
}

inline long getFileSize(int fd){
    struct stat file_state{};

    if(fstat(fd, &file_state) >= 0){
        return (long)(file_state.st_size);
    }else{
        LOGE("Error getting file size");
        return 0;
    }
}

static char* getErrorDescription(const unsigned long error) {
    char* description = nullptr;
    switch(error) {
        case FPDF_ERR_SUCCESS:
            asprintf(&description, "No error.");
            break;
        case FPDF_ERR_FILE:
            asprintf(&description, "File not found or could not be opened.");
            break;
        case FPDF_ERR_FORMAT:
            asprintf(&description, "File not in PDF format or corrupted.");
            break;
        case FPDF_ERR_PASSWORD:
            asprintf(&description, "Incorrect password.");
            break;
        case FPDF_ERR_SECURITY:
            asprintf(&description, "Unsupported security scheme.");
            break;
        case FPDF_ERR_PAGE:
            asprintf(&description, "Page not found or content error.");
            break;
        default:
            asprintf(&description, "Unknown error.");
    }

    return description;
}

int jniThrowException(JNIEnv* env, const char* className, const char* message) {
    jclass exClass = env->FindClass(className);
    if (exClass == nullptr) {
        LOGE("Unable to find exception class %s", className);
        return -1;
    }

    if(env->ThrowNew(exClass, message ) != JNI_OK) {
        LOGE("Failed throwing '%s' '%s'", className, message);
        return -1;
    }

    return 0;
}

int jniThrowExceptionFmt(JNIEnv* env, const char* className, const char* fmt, ...) {
    char msgBuf[1024];

    va_list args;
    va_start(args, fmt);
    vsnprintf(msgBuf, sizeof(msgBuf), fmt, args);
    va_end(args);

    jclass exceptionClass = env->FindClass(className);
    return env->ThrowNew(exceptionClass, msgBuf);
}


//jobject NewLong(JNIEnv* env, jlong value) {
//    jclass cls = env->FindClass("java/lang/Long");
//    jmethodID methodID = env->GetMethodID(cls, "<init>", "(J)V");
//    return env->NewObject(cls, methodID, value);
//}
//
//jobject NewInteger(JNIEnv* env, jint value) {
//    jclass cls = env->FindClass("java/lang/Integer");
//    jmethodID methodID = env->GetMethodID(cls, "<init>", "(I)V");
//    return env->NewObject(cls, methodID, value);
//}

uint16_t rgbTo565(rgb *color) {
    return ((color->red >> 3) << 11) | ((color->green >> 2) << 5) | (color->blue >> 3);
}

void rgbBitmapTo565(void *source, int sourceStride, void *dest, AndroidBitmapInfo *info) {
    rgb *srcLine;
    uint16_t *dstLine;
    int y, x;
    for (y = 0; y < info->height; y++) {
        srcLine = (rgb*) source;
        dstLine = (uint16_t*) dest;
        for (x = 0; x < info->width; x++) {
            dstLine[x] = rgbTo565(&srcLine[x]);
        }
        source = (char*) source + sourceStride;
        dest = (char*) dest + info->stride;
    }
}

jlong loadTextPageInternal(JNIEnv *env, DocumentFile *doc, jlong pagePtr) {
    try {
        if (doc == nullptr) throw std::runtime_error("Get page document null");

        auto page = reinterpret_cast<FPDF_PAGE>(pagePtr);
        if (page != nullptr) {
            FPDF_TEXTPAGE textPage = FPDFText_LoadPage(page);
            if (textPage == nullptr) {
                throw std::runtime_error("Loaded text page is null");
            }
            return reinterpret_cast<jlong>(textPage);
        } else {
            throw std::runtime_error("Load page null");
        }
    } catch (const char *msg) {
        LOGE("%s", msg);

        jniThrowException(env, "java/lang/IllegalStateException",
                          "cannot load text page");

        return -1;
    }
}

jfieldID dataBuffer;
jmethodID readMethod;

extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void*) {
    javaVm = vm;

    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass nativeSourceBridge = env->FindClass("io/legere/pdfiumandroid/util/PdfiumNativeSourceBridge");
    if (nativeSourceBridge == nullptr) return JNI_ERR;

    if ((dataBuffer = env->GetFieldID(nativeSourceBridge, "buffer", "[B")) == nullptr) {
        return JNI_ERR;
    }

    if ((readMethod = env->GetMethodID(nativeSourceBridge, "read", "(JJ)I")) == nullptr) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}

extern "C"
int getBlock(void* param, unsigned long position, unsigned char* outBuffer,
                    unsigned long size) {
    const int fd = reinterpret_cast<intptr_t>(param);
    const int readCount = pread(fd, outBuffer, size, (long) position);
    if (readCount < 0) {
        LOGE("Cannot read from file descriptor. Error:%d", errno);
        return 0;
    }
    return 1;
}

extern "C"
int getBlockFromCustomSource(void* param, unsigned long position, unsigned char* outBuffer,
                             unsigned long size) {
    JNIEnv *env = nullptr;
    bool attached;
    if (!jniAttachCurrentThread(&env, &attached)) {
        return 0;
    }

    auto nativeSourceBridge = reinterpret_cast<jobject>(param);
    jint bytesRead = env->CallIntMethod(nativeSourceBridge, readMethod, position,  size);

    if (bytesRead == 0) {
        LOGE("Cannot read from custom source");
        if (!jniDetachCurrentThread(attached)) {
            // ignore. we're going to return anyway on the next line
        }
        return 0;
    }

    auto buffer = (jbyteArray) env->GetObjectField(nativeSourceBridge, dataBuffer);
    env->GetByteArrayRegion(buffer, 0, bytesRead, (jbyte*) outBuffer);

    if (!jniDetachCurrentThread(attached)) {
        return 0;
    }
    return bytesRead;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_io_legere_pdfiumandroid_PdfiumCore_nativeOpenDocument(JNIEnv *env, jobject, jint fd,
                                                           jstring password) {
    auto fileLength = (size_t)getFileSize(fd);
    if(fileLength <= 0) {
        jniThrowException(env, "java/io/IOException",
                          "File is empty");
        return -1;
    }

    auto *docFile = new DocumentFile();

    FPDF_FILEACCESS loader;
    loader.m_FileLen = fileLength;
    loader.m_Param = reinterpret_cast<void*>(intptr_t(fd));
    loader.m_GetBlock = &getBlock;

    const char *cpassword = nullptr;
    if(password != nullptr) {
        cpassword = env->GetStringUTFChars(password, nullptr);
    }

    FPDF_DOCUMENT document = FPDF_LoadCustomDocument(&loader, cpassword);

    if(cpassword != nullptr) {
        env->ReleaseStringUTFChars(password, cpassword);
    }

    if (!document) {
        delete docFile;

        const unsigned long errorNum = FPDF_GetLastError();
        if(errorNum == FPDF_ERR_PASSWORD) {
            jniThrowException(env, "io/legere/pdfiumandroid/PdfPasswordException",
                              "Password required or incorrect password.");
        } else {
            char* error = getErrorDescription(errorNum);
            jniThrowExceptionFmt(env, "java/io/IOException",
                                 "cannot create document: %s", error);

            free(error);
        }

        return -1;
    }

    docFile->pdfDocument = document;

    return reinterpret_cast<jlong>(docFile);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_io_legere_pdfiumandroid_PdfiumCore_nativeOpenMemDocument(JNIEnv *env, jobject,
                                                              jbyteArray data, jstring password) {
    auto *docFile = new DocumentFile();

    const char *cpassword = nullptr;
    if(password != nullptr) {
        cpassword = env->GetStringUTFChars(password, nullptr);
    }

    int size = (int) env->GetArrayLength(data);
    auto *cDataCopy = new jbyte[size];
    env->GetByteArrayRegion(data, 0, size, cDataCopy);
    FPDF_DOCUMENT document = FPDF_LoadMemDocument( reinterpret_cast<const void*>(cDataCopy),
                                                   size, cpassword);

    if(cpassword != nullptr) {
        env->ReleaseStringUTFChars(password, cpassword);
    }

    if (!document) {
        delete docFile;

        const unsigned long errorNum = FPDF_GetLastError();
        if(errorNum == FPDF_ERR_PASSWORD) {
            jniThrowException(env, "io/legere/pdfiumandroid/PdfPasswordException",
                              "Password required or incorrect password.");
        } else {
            char* error = getErrorDescription(errorNum);
            jniThrowExceptionFmt(env, "java/io/IOException",
                                 "cannot create document: %s", error);

            free(error);
        }

        return -1;
    }

    docFile->pdfDocument = document;
    docFile->cDataCopy = cDataCopy;
    return reinterpret_cast<jlong>(docFile);
}


extern "C"
JNIEXPORT jlong JNICALL
Java_io_legere_pdfiumandroid_PdfiumCore_nativeOpenCustomDocument(JNIEnv *env, jobject, jobject nativeSourceBridge, jstring password, jlong dataLength) {
    if(dataLength <= 0) {
        jniThrowException(env, "java/io/IOException",
                          "File is empty");
        return -1;
    }

    auto *docFile = new DocumentFile();
    docFile->nativeSourceBridgeGlobalRef = env->NewGlobalRef(nativeSourceBridge);

    FPDF_FILEACCESS loader;
    loader.m_FileLen = dataLength;
    loader.m_Param = reinterpret_cast<void*>(docFile->nativeSourceBridgeGlobalRef);
    loader.m_GetBlock = &getBlockFromCustomSource;

    const char *cpassword = nullptr;
    if(password != nullptr) {
        cpassword = env->GetStringUTFChars(password, nullptr);
    }

    FPDF_DOCUMENT document = FPDF_LoadCustomDocument(&loader, cpassword);

    if(cpassword != nullptr) {
        env->ReleaseStringUTFChars(password, cpassword);
    }

    if (!document) {
        delete docFile;

        const unsigned long errorNum = FPDF_GetLastError();
        if(errorNum == FPDF_ERR_PASSWORD) {
            jniThrowException(env, "io/legere/pdfiumandroid/PdfPasswordException",
                              "Password required or incorrect password.");
        } else {
            char* error = getErrorDescription(errorNum);
            jniThrowExceptionFmt(env, "java/io/IOException",
                                 "cannot create document: %s", error);

            free(error);
        }

        return -1;
    }

    docFile->pdfDocument = document;

    return reinterpret_cast<jlong>(docFile);
}

static jlong loadPageInternal(JNIEnv *env, DocumentFile *doc, int pageIndex){
    try{
        if(doc == nullptr) throw std::runtime_error( "Get page document null");

        FPDF_DOCUMENT pdfDoc = doc->pdfDocument;
        if(pdfDoc != nullptr){
            FPDF_PAGE page = FPDF_LoadPage(pdfDoc, pageIndex);
            if (page == nullptr) {
                throw std::runtime_error("Loaded page is null");
            }
            return reinterpret_cast<jlong>(page);
        }else{
            throw std::runtime_error("Get page pdf document null");
        }

    }catch(const char *msg){
        LOGE("%s", msg);

        jniThrowException(env, "java/lang/IllegalStateException",
                          "cannot load page");

        return -1;
    }
}

static void closePageInternal(jlong pagePtr) {
    FPDF_ClosePage(reinterpret_cast<FPDF_PAGE>(pagePtr));
}

static void renderPageInternal( FPDF_PAGE page,
                                ANativeWindow_Buffer *windowBuffer,
                                int startX, int startY,
                                int canvasHorSize, int canvasVerSize,
                                int drawSizeHor, int drawSizeVer,
                                bool renderAnnot, FPDF_DWORD canvasColor, FPDF_DWORD pageBackgroundColor){

    FPDF_BITMAP pdfBitmap = FPDFBitmap_CreateEx( canvasHorSize, canvasVerSize,
                                                 FPDFBitmap_BGRA,
                                                 windowBuffer->bits, (int)(windowBuffer->stride) * 4);

    if ((drawSizeHor < canvasHorSize || drawSizeVer < canvasVerSize) && canvasColor != 0){
        FPDFBitmap_FillRect( pdfBitmap, 0, 0, canvasHorSize, canvasVerSize,
                             canvasColor); //Gray
    }

    int baseHorSize = (canvasHorSize < drawSizeHor)? canvasHorSize : drawSizeHor;
    int baseVerSize = (canvasVerSize < drawSizeVer)? canvasVerSize : drawSizeVer;
    int baseX = (startX < 0)? 0 : startX;
    int baseY = (startY < 0)? 0 : startY;
    int flags = FPDF_REVERSE_BYTE_ORDER;

    if(renderAnnot) {
        flags |= FPDF_ANNOT;
    }

    if (pageBackgroundColor != 0) {
        FPDFBitmap_FillRect(pdfBitmap, baseX, baseY, baseHorSize, baseVerSize,
                            pageBackgroundColor);
    }

    FPDF_RenderPageBitmap( pdfBitmap, page,
                           startX, startY,
                           drawSizeHor, drawSizeVer,
                           0, flags );
}

extern "C"
JNIEXPORT jobject JNICALL
Java_io_legere_pdfiumandroid_PdfiumCore_nativeGetLinkRect(JNIEnv *env, jobject,
                                                          jlong link_ptr) {
    try {
        auto link = reinterpret_cast<FPDF_LINK>(link_ptr);
        FS_RECTF fsRectF;
        FPDF_BOOL result = FPDFLink_GetAnnotRect(link, &fsRectF);

        if (!result) {
            return nullptr;
        }

        jclass clazz = env->FindClass("android/graphics/RectF");
        jmethodID constructorID = env->GetMethodID(clazz, "<init>", "(FFFF)V");
        return env->NewObject(clazz, constructorID, fsRectF.left, fsRectF.top, fsRectF.right,
                              fsRectF.bottom);
    } catch (const char *msg) {
        LOGE("%s", msg);

        jniThrowException(env, "java/lang/IllegalStateException",
                          "cannot get link rect");

        return nullptr;
    }
}

class FileWrite : public FPDF_FILEWRITE {
public:
    jobject callbackObject;
    jmethodID callbackMethodID;
    _JNIEnv *env;

    static int WriteBlockCallback(FPDF_FILEWRITE* pFileWrite, const void* data, unsigned long size) {
        auto* pThis = reinterpret_cast<FileWrite*>(pFileWrite);
        _JNIEnv *env = pThis->env;
        //Convert the native array to Java array.
        jbyteArray a = env->NewByteArray((int) size);
        if (a != nullptr) {
            env->SetByteArrayRegion(a, 0, (int) size, (const jbyte *)data);
            return env->CallIntMethod(pThis->callbackObject, pThis->callbackMethodID, a);
        }
        return -1;
    }
};

void raise_java_oom_exception(JNIEnv *pEnv, std::bad_alloc &alloc);

void raise_java_runtime_exception(JNIEnv *pEnv, std::runtime_error &error);

void raise_java_invalid_arg_exception(JNIEnv *pEnv, std::invalid_argument &argument);

void raise_java_exception(JNIEnv *pEnv, std::exception &exception);

void handleUnexpected(JNIEnv *pEnv, char const *name);

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfDocument_nativeGetPageCount(JNIEnv *env, jobject,
                                                            jlong doc_ptr) {
    try {
        auto *doc = reinterpret_cast<DocumentFile*>(doc_ptr);
        return (jint)FPDF_GetPageCount(doc->pdfDocument);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_io_legere_pdfiumandroid_PdfDocument_nativeLoadPage(JNIEnv *env, jobject, jlong doc_ptr,
                                                        jint page_index) {
    try {
        auto *doc = reinterpret_cast<DocumentFile *>(doc_ptr);
        return loadPageInternal(env, doc, (int) page_index);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeClosePage(JNIEnv *env, jclass , jlong page_ptr) {
    try {
        closePageInternal(page_ptr);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_io_legere_pdfiumandroid_PdfDocument_nativeDeletePage(JNIEnv *env, jobject, jlong doc_ptr,
                                                          jint page_index) {
    try {
        auto *doc = reinterpret_cast<DocumentFile *>(doc_ptr);
        if(doc == nullptr) throw std::runtime_error( "Get page document null");

        FPDF_DOCUMENT pdfDoc = doc->pdfDocument;
        if(pdfDoc != nullptr) {
            FPDFPage_Delete(pdfDoc, (int) page_index);
        }
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_io_legere_pdfiumandroid_PdfDocument_nativeCloseDocument(JNIEnv *env, jobject,
                                                             jlong doc_ptr) {
    try {
        auto *doc = reinterpret_cast<DocumentFile*>(doc_ptr);
        // The destructor will close the document
        delete doc;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }

}

extern "C"
JNIEXPORT jlongArray JNICALL
Java_io_legere_pdfiumandroid_PdfDocument_nativeLoadPages(JNIEnv *env, jobject, jlong doc_ptr,
                                                         jint from_index, jint to_index) {
    try {
        auto *doc = reinterpret_cast<DocumentFile*>(doc_ptr);

        if(to_index < from_index) return nullptr;
        jlong pages[ to_index - from_index + 1 ];

        int i;
        for(i = 0; i <= (to_index - from_index); i++){
            pages[i] = loadPageInternal(env, doc, (int)(i + from_index));
        }

        jlongArray javaPages = env -> NewLongArray( (jsize)(to_index - from_index + 1) );
        env -> SetLongArrayRegion(javaPages, 0, (jsize)(to_index - from_index + 1), (const jlong*)pages);

        return javaPages;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;

}

extern "C"
JNIEXPORT jstring JNICALL
Java_io_legere_pdfiumandroid_PdfDocument_nativeGetDocumentMetaText(JNIEnv *env, jobject,
                                                                   jlong doc_ptr, jstring tag) {
    try {
        const char *ctag = env->GetStringUTFChars(tag, nullptr);
        if (ctag == nullptr) {
            return env->NewStringUTF("");
        }
        auto *doc = reinterpret_cast<DocumentFile*>(doc_ptr);

        int bufferLen = (int) FPDF_GetMetaText(doc->pdfDocument, ctag, nullptr, 0);
        if (bufferLen <= 2) {
            return env->NewStringUTF("");
        }
        std::wstring text;
        FPDF_GetMetaText(doc->pdfDocument, ctag, WriteInto(&text, bufferLen + 1), bufferLen);
        env->ReleaseStringUTFChars(tag, ctag);
        return env->NewString((jchar*) text.c_str(), bufferLen / 2 - 1);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_io_legere_pdfiumandroid_PdfDocument_nativeGetFirstChildBookmark(JNIEnv *env, jobject,
                                                                     jlong doc_ptr,
                                                                     jlong bookmark_ptr) {
    try {
        auto *doc = reinterpret_cast<DocumentFile*>(doc_ptr);
        FPDF_BOOKMARK parent;
        if(bookmark_ptr == 0) {
            parent = nullptr;
        } else {
            parent = reinterpret_cast<FPDF_BOOKMARK>(bookmark_ptr);
        }
        FPDF_BOOKMARK bookmark = FPDFBookmark_GetFirstChild(doc->pdfDocument, parent);
        if (bookmark == nullptr) {
            return 0;
        }
        return reinterpret_cast<jlong>(bookmark);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return 0;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_io_legere_pdfiumandroid_PdfDocument_nativeGetSiblingBookmark(JNIEnv *env, jobject,
                                                                  jlong doc_ptr,
                                                                  jlong bookmark_ptr) {
    try {
        auto *doc = reinterpret_cast<DocumentFile*>(doc_ptr);
        auto parent = reinterpret_cast<FPDF_BOOKMARK>(bookmark_ptr);
        FPDF_BOOKMARK bookmark = FPDFBookmark_GetNextSibling(doc->pdfDocument, parent);
        if (bookmark == nullptr) {
            return 0;
        }
        return reinterpret_cast<jlong>(bookmark);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return 0;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_io_legere_pdfiumandroid_PdfDocument_nativeLoadTextPage(JNIEnv *env, jobject,
                                                            jlong doc_ptr, jlong page_ptr) {
    try {
        auto *doc = reinterpret_cast<DocumentFile*>(doc_ptr);
        return loadTextPageInternal(env, doc, page_ptr);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_io_legere_pdfiumandroid_PdfDocument_nativeGetBookmarkTitle(JNIEnv *env, jobject,
                                                                jlong bookmark_ptr) {
    try {
        auto bookmark = reinterpret_cast<FPDF_BOOKMARK>(bookmark_ptr);
        int bufferLen = (int) FPDFBookmark_GetTitle(bookmark, nullptr, 0);
        if (bufferLen <= 2) {
            return env->NewStringUTF("");
        }
        std::wstring title;
        FPDFBookmark_GetTitle(bookmark, WriteInto(&title, bufferLen + 1), bufferLen);
        return env->NewString((jchar*) title.c_str(), bufferLen / 2 - 1);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfDocument_nativeGetDestPageIndex(JNIEnv *env, jobject,
                                                                jlong doc_ptr, jlong link_ptr) {
    try {
        auto *doc = reinterpret_cast<DocumentFile *>(doc_ptr);
        auto link = reinterpret_cast<FPDF_LINK>(link_ptr);
        FPDF_DEST dest = FPDFLink_GetDest(doc->pdfDocument, link);
        if (dest == nullptr) {
            return -1;
        }
        unsigned long index = FPDFDest_GetDestPageIndex(doc->pdfDocument, dest);
        return (jint) index;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_legere_pdfiumandroid_PdfDocument_nativeSaveAsCopy(JNIEnv *env, jobject, jlong doc_ptr,
                                                          jobject callback, jint flags) {
    try {
        jclass callbackClass = env->FindClass("io/legere/pdfiumandroid/PdfWriteCallback");
        if (callback != nullptr && env->IsInstanceOf(callback, callbackClass)) {
            //Setup the callback to Java.
            FileWrite fw = FileWrite();
            fw.version = 1;
            fw.FPDF_FILEWRITE::WriteBlock = FileWrite::WriteBlockCallback;
            fw.callbackObject = callback;
            fw.callbackMethodID = env->GetMethodID(callbackClass, "WriteBlock", "([B)I");
            fw.env = env;

            auto *doc = reinterpret_cast<DocumentFile *>(doc_ptr);
            return (jboolean) FPDF_SaveAsCopy(doc->pdfDocument, &fw, flags);
        }
        return false;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return false;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeClosePages(JNIEnv *env, jclass ,
                                                      jlongArray pages_ptr) {
    try {
        int length = (int) (env->GetArrayLength(pages_ptr));
        jlong *pages = env->GetLongArrayElements(pages_ptr, nullptr);

        int i;
        for (i = 0; i < length; i++) { closePageInternal(pages[i]); }
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetPageWidthPixel(JNIEnv *env, jclass,
                                                             jlong page_ptr, jint dpi) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        return (jint) (FPDF_GetPageWidth(page) * dpi / 72);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetPageHeightPixel(JNIEnv *env, jclass,
                                                              jlong page_ptr, jint dpi) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        return (jint)(FPDF_GetPageHeight(page) * dpi / 72);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch(std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetPageWidthPoint(JNIEnv *env, jclass,
                                                             jlong page_ptr) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        return (jint)FPDF_GetPageWidth(page);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch(std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetPageHeightPoint(JNIEnv *env, jclass,
                                                              jlong page_ptr) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        return (jint)FPDF_GetPageHeight(page);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch(std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_io_legere_pdfiumandroid_PdfTextPage_nativeGetFontSize(JNIEnv *env, jclass, jlong page_ptr,
                                                       jint char_index) {
    try {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(page_ptr);
        return (jdouble) FPDFText_GetFontSize(textPage, char_index);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch(std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return 0;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetPageMediaBox(JNIEnv *env, jclass ,
                                                           jlong page_ptr) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        jfloatArray result = env->NewFloatArray(4);
        if (result == nullptr) {
            return nullptr;
        }

        float rect[4];
        if (!FPDFPage_GetMediaBox(page, &rect[0], &rect[1], &rect[2], &rect[3])) {
            rect[0] = -1.0f;
            rect[1] = -1.0f;
            rect[2] = -1.0f;
            rect[3] = -1.0f;
        }

        env->SetFloatArrayRegion(result, 0, 4, (jfloat *) rect);
        return result;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch(std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetPageCropBox(JNIEnv *env, jclass,
                                                          jlong page_ptr) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        jfloatArray result = env->NewFloatArray(4);
        if (result == nullptr) {
            return nullptr;
        }

        float rect[4];
        if (!FPDFPage_GetCropBox(page, &rect[0], &rect[1], &rect[2], &rect[3])) {
            rect[0] = -1.0f;
            rect[1] = -1.0f;
            rect[2] = -1.0f;
            rect[3] = -1.0f;
        }

        env->SetFloatArrayRegion(result, 0, 4, (jfloat *) rect);
        return result;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch(std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetPageBleedBox(JNIEnv *env, jclass,
                                                           jlong page_ptr) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        jfloatArray result = env->NewFloatArray(4);
        if (result == nullptr) {
            return nullptr;
        }

        float rect[4];
        if (!FPDFPage_GetBleedBox(page, &rect[0], &rect[1], &rect[2], &rect[3])) {
            rect[0] = -1.0f;
            rect[1] = -1.0f;
            rect[2] = -1.0f;
            rect[3] = -1.0f;
        }

        env->SetFloatArrayRegion(result, 0, 4, (jfloat *) rect);
        return result;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch(std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetPageTrimBox(JNIEnv *env, jclass,
                                                          jlong page_ptr) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        jfloatArray result = env->NewFloatArray(4);
        if (result == nullptr) {
            return nullptr;
        }

        float rect[4];
        if (!FPDFPage_GetTrimBox(page, &rect[0], &rect[1], &rect[2], &rect[3])) {
            rect[0] = -1.0f;
            rect[1] = -1.0f;
            rect[2] = -1.0f;
            rect[3] = -1.0f;
        }

        env->SetFloatArrayRegion(result, 0, 4, (jfloat*)rect);
        return result;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch(std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetPageArtBox(JNIEnv *env, jclass,
                                                         jlong page_ptr) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        jfloatArray result = env->NewFloatArray(4);
        if (result == nullptr) {
            return nullptr;
        }

        float rect[4];
        if (!FPDFPage_GetArtBox(page, &rect[0], &rect[1], &rect[2], &rect[3])) {
            rect[0] = -1.0f;
            rect[1] = -1.0f;
            rect[2] = -1.0f;
            rect[3] = -1.0f;
        }

        env->SetFloatArrayRegion(result, 0, 4, (jfloat*)rect);
        return result;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch(std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetPageBoundingBox(JNIEnv *env, jclass,
                                                              jlong page_ptr) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        jfloatArray result = env->NewFloatArray(4);
        if (result == nullptr) {
            return nullptr;
        }

        float rect[4];
        FS_RECTF fsRect;
        if (!FPDF_GetPageBoundingBox(page, &fsRect)) {
            rect[0] = -1.0f;
            rect[1] = -1.0f;
            rect[2] = -1.0f;
            rect[3] = -1.0f;
        } else {
            rect[0] = fsRect.left;
            rect[1] = fsRect.top;
            rect[2] = fsRect.right;
            rect[3] = fsRect.bottom;
        }

        env->SetFloatArrayRegion(result, 0, 4, (jfloat*)rect);
        return result;

    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetPageMatrix(JNIEnv *env, jclass,
                                                         jlong page_ptr) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        jfloatArray result = env->NewFloatArray(6);
        if (result == nullptr) {
            return nullptr;
        }
//        auto count = FPDFPage_CountObjects(page);
//        int index;
//        for (index = 0; index < count; index++) {
//            FPDF_PAGEOBJECT pageObject = FPDFPage_GetObject(page, index);
//            auto objectType = FPDFPageObj_GetType(pageObject);
////            LOGD("objectType: %d, index: %d", objectType, index);
//            float matrix[6];
//            FS_MATRIX fsMatrix;
//            FPDFPageObj_GetMatrix(pageObject, &fsMatrix);
////            LOGD("fsMatrix.a: %f", fsMatrix.a);
////            LOGD("fsMatrix.b: %f", fsMatrix.b);
////            LOGD("fsMatrix.c: %f", fsMatrix.c);
////            LOGD("fsMatrix.d: %f", fsMatrix.d);
////            LOGD("fsMatrix.e: %f", fsMatrix.e);
////            LOGD("fsMatrix.f: %f", fsMatrix.f);
//        }
        FPDF_PAGEOBJECT pageObject = FPDFPage_GetObject(page, 0);

        float matrix[6];
        FS_MATRIX fsMatrix;
        if (!FPDFPageObj_GetMatrix(pageObject, &fsMatrix)) {
            matrix[0] = -1.0f;
            matrix[1] = -1.0f;
            matrix[2] = -1.0f;
            matrix[3] = -1.0f;
            matrix[4] = -1.0f;
            matrix[5] = -1.0f;
        } else {
            matrix[0] = fsMatrix.a;
            matrix[1] = fsMatrix.b;
            matrix[2] = fsMatrix.c;
            matrix[3] = fsMatrix.d;
            matrix[4] = fsMatrix.e;
            matrix[5] = fsMatrix.f;
        }

        free(pageObject);

        env->SetFloatArrayRegion(result, 0, 6, (jfloat*)matrix);
        return result;

    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeLockSurface(JNIEnv *env, jclass clazz, jobject surface, jintArray intArray, jlongArray ptrsArray) {
    LOGD("nativeLockSurface");
    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);
    if (nativeWindow == nullptr) {
        LOGE("native window pointer null");
        return false;
    }
    auto intValues = env->GetIntArrayElements(intArray, nullptr);
    if (intValues == nullptr) {
        // Handle error
        LOGE("intValues is null");
        return false;
    }
    auto ptrValues = env->GetLongArrayElements(ptrsArray, nullptr);
    if (ptrValues == nullptr) {
        // Handle error
        LOGE("ptrValues is null");
        return static_cast<jlong>(0);
    }

    auto width = ANativeWindow_getWidth(nativeWindow);
    auto height = ANativeWindow_getHeight(nativeWindow);

    intValues[0] = width; // Modify the integer value
    intValues[1] = height;

    if (ANativeWindow_getFormat(nativeWindow) != WINDOW_FORMAT_RGBA_8888) {
        LOGD("Set format to RGBA_8888");
        ANativeWindow_setBuffersGeometry(nativeWindow,
                                         width,
                                         height,
                                         WINDOW_FORMAT_RGBA_8888);
    }
    env->ReleaseIntArrayElements(intArray, intValues, JNI_OK);

    auto *buffer = new ANativeWindow_Buffer();
    int ret;
    if ((ret = ANativeWindow_lock(nativeWindow, buffer, nullptr)) != 0) {
        LOGE("Locking native window failed: %s", strerror(ret * -1));
        return false;
    }
    ptrValues[0] = reinterpret_cast<jlong>(nativeWindow);
    ptrValues[1] = reinterpret_cast<jlong>(buffer);
    env->ReleaseLongArrayElements(ptrsArray, ptrValues, JNI_OK);
    return true;
}
extern "C"
JNIEXPORT void JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeUnlockSurface(JNIEnv *env, jclass clazz,
                                                         jlongArray ptrsArray) {
    LOGD("nativeUnlockSurface");
    jboolean isCopyPtrs;
    auto ptrValues = env->GetLongArrayElements(ptrsArray, &isCopyPtrs);
    if (ptrValues == nullptr) {
        // Handle error
        return;
    }
    auto nativeWindow = reinterpret_cast<ANativeWindow*>(ptrValues[0]);

    auto buffer = reinterpret_cast<ANativeWindow_Buffer*>(ptrValues[1]);

    delete buffer;



    ANativeWindow_unlockAndPost(nativeWindow);
    ANativeWindow_release(nativeWindow);
    if (isCopyPtrs) {
        env->ReleaseLongArrayElements(ptrsArray, ptrValues, JNI_ABORT);
    }

}
extern "C"
JNIEXPORT void JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeRenderPage(JNIEnv *env, jclass, jlong page_ptr,
                                                      jlong buffer_ptr, jint start_x,
                                                      jint start_y, jint draw_size_hor,
                                                      jint draw_size_ver, jboolean render_annot,
                                                      jint canvasColor, jint pageBackgroundColor) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);

        if (page == nullptr) {
            LOGE("Render page pointers invalid");
            return;
        }

        auto buffer = reinterpret_cast<ANativeWindow_Buffer*>(buffer_ptr);

        renderPageInternal(page, buffer,
                           (int) start_x, (int) start_y,
                           buffer->width, buffer->height,
                           (int) draw_size_hor, (int) draw_size_ver,
                           (bool) render_annot, canvasColor, pageBackgroundColor);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeRenderPageWithMatrix(JNIEnv *env, jclass,
                                                                jlong page_ptr, jlong buffer_ptr,
                                                                jint draw_size_hor, jint draw_size_ver,
                                                                jfloatArray matrixValues,
                                                                jfloatArray clipRect,
                                                                jboolean render_annot,
                                                                jboolean,
                                                                jint canvasColor, jint pageBackgroundColor) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);

        if (page == nullptr) {
            LOGE("Render page pointers invalid");
            return;
        }

        auto bufferPtr = reinterpret_cast<ANativeWindow_Buffer*>(buffer_ptr);
        auto buffer = *bufferPtr;


        jboolean isCopyClipRect;
        auto clipRectFloats = env->GetFloatArrayElements(clipRect, &isCopyClipRect);
        auto leftClip = clipRectFloats[0];
        auto topClip = clipRectFloats[1];
        auto rightClip = clipRectFloats[2];
        auto bottomClip = clipRectFloats[3];

        auto canvasHorSize = draw_size_hor;
        auto canvasVerSize = draw_size_ver;

        auto drawSizeHor = (int) (rightClip - leftClip);
        auto drawSizeVer = (int) (bottomClip - topClip);

        FPDF_BITMAP pdfBitmap = FPDFBitmap_CreateEx(canvasHorSize, canvasVerSize,
                                                    FPDFBitmap_BGRA,
                                                    buffer.bits, (int)(buffer.stride) * 4);

        if((drawSizeHor < canvasHorSize || drawSizeVer < canvasVerSize) && canvasColor != 0) {
            FPDFBitmap_FillRect( pdfBitmap, 0, 0, canvasHorSize, canvasVerSize,
                                 canvasColor); //Gray
        }

        auto startX = (int) leftClip;
        auto startY = (int) topClip;
        int baseHorSize = (canvasHorSize < drawSizeHor)? canvasHorSize : drawSizeHor;
        int baseVerSize = (canvasVerSize < drawSizeVer)? canvasVerSize : drawSizeVer;
        int baseX = (startX < 0)? 0 : startX;
        int baseY = (startY < 0)? 0 : startY;

        int flags = FPDF_REVERSE_BYTE_ORDER;

        if (render_annot) {
            flags |= FPDF_ANNOT;
        }



        if (pageBackgroundColor != 0) {
            FPDFBitmap_FillRect(pdfBitmap, baseX, baseY, baseHorSize, baseVerSize,
                                pageBackgroundColor); //White
        }

        jboolean isCopy;
        auto matrixFloats = env->GetFloatArrayElements(matrixValues, &isCopy);

        auto matrix = FS_MATRIX();
        matrix.a = matrixFloats[0];
        matrix.b = 0;
        matrix.c = 0;
        matrix.d = matrixFloats[1];
        matrix.e = matrixFloats[2];
        matrix.f = matrixFloats[3];
        auto clip = FS_RECTF();
        clip.left = leftClip;
        clip.top = topClip;
        clip.right = rightClip;
        clip.bottom = bottomClip;

        FPDF_RenderPageBitmapWithMatrix(pdfBitmap, page, &matrix, &clip, flags);


        if (isCopyClipRect) {
            env->ReleaseFloatArrayElements(clipRect, (jfloat *) clipRectFloats, JNI_ABORT);
        }
        if (isCopy) {
            env->ReleaseFloatArrayElements(matrixValues, (jfloat *) matrixFloats, JNI_ABORT);
        }
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_io_legere_pdfiumandroid_PdfDocument_nativeRenderPagesWithMatrix(JNIEnv *env, jobject thiz,
                                                                     jlongArray pages, jlong buffer_ptr,
                                                                     jint draw_size_hor, jint draw_size_ver,
                                                                     jfloatArray matrices,
                                                                     jfloatArray clipRect,
                                                                     jboolean render_annot,
                                                                     jboolean text_mask,
                                                                     jint canvasColor,
                                                                     jint pageBackgroundColor) {
    try {
        auto bufferPtr = reinterpret_cast<ANativeWindow_Buffer*>(buffer_ptr);
        auto buffer = *bufferPtr;
        jboolean isCopyPages;
        auto pagePtrs = env->GetLongArrayElements(pages, &isCopyPages);
        auto numPages = env->GetArrayLength(pages);

        jboolean isCopyClipRect;
        auto clipRectFloats = env->GetFloatArrayElements(clipRect, &isCopyClipRect);

        jboolean isCopyMatrices;
        auto matrixFloats = env->GetFloatArrayElements(matrices, &isCopyMatrices);


        auto canvasHorSize = draw_size_hor;
        auto canvasVerSize = draw_size_ver;

        FPDF_BITMAP pdfBitmap = FPDFBitmap_CreateEx(canvasHorSize, canvasVerSize,
                                                    FPDFBitmap_BGRA,
                                                    buffer.bits, (int)(buffer.stride) * 4);

        if(canvasColor != 0) {
            FPDFBitmap_FillRect( pdfBitmap, 0, 0, canvasHorSize, canvasVerSize,
                                 canvasColor); //Gray
        }

        int flags = FPDF_REVERSE_BYTE_ORDER;

        if (render_annot) {
            flags |= FPDF_ANNOT;
        }

        /* from here we process each page */
        for (int pageIndex = 0; pageIndex < numPages; ++pageIndex) {

            auto page = reinterpret_cast<FPDF_PAGE>(pagePtrs[pageIndex]);

            if (page == nullptr) {
                LOGE("Render page pointers invalid");
                return;
            }


            auto leftClip = clipRectFloats[0 + pageIndex * 4];
            auto topClip = clipRectFloats[1 + pageIndex * 4];
            auto rightClip = clipRectFloats[2 + pageIndex * 4];
            auto bottomClip = clipRectFloats[3 + pageIndex * 4];

            auto drawSizeHor = (int) (rightClip - leftClip);
            auto drawSizeVer = (int) (bottomClip - topClip);

            auto startX = (int) leftClip;
            auto startY = (int) topClip;
            int baseHorSize = (canvasHorSize < drawSizeHor) ? canvasHorSize : drawSizeHor;
            int baseVerSize = (canvasVerSize < drawSizeVer) ? canvasVerSize : drawSizeVer;
            int baseX = (startX < 0) ? 0 : startX;
            int baseY = (startY < 0) ? 0 : startY;


            if (pageBackgroundColor != 0) {
                FPDFBitmap_FillRect(pdfBitmap, baseX, baseY, baseHorSize, baseVerSize,
                                    pageBackgroundColor); //White
            }


            auto scale = matrixFloats[0 + pageIndex * 3];
            auto xTrans = matrixFloats[1 + pageIndex * 3];
            auto yTrans = matrixFloats[2 + pageIndex * 3];
            auto matrix = FS_MATRIX();
            matrix.a = scale;
            matrix.b = 0;
            matrix.c = 0;
            matrix.d = scale;
            matrix.e = xTrans;
            matrix.f = yTrans;
            auto clip = FS_RECTF();
            clip.left = leftClip;
            clip.top = topClip;
            clip.right = rightClip;
            clip.bottom = bottomClip;

            FPDF_RenderPageBitmapWithMatrix(pdfBitmap, page, &matrix, &clip, flags);
            /* end process each page */
        }


        if (isCopyMatrices) {
            env->ReleaseFloatArrayElements(matrices, (jfloat *) matrixFloats, JNI_ABORT);
        }

        if (isCopyClipRect) {
            env->ReleaseFloatArrayElements(clipRect, (jfloat *) clipRectFloats, JNI_ABORT);
        }
        if (isCopyClipRect) {
            env->ReleaseLongArrayElements(pages, pagePtrs, JNI_ABORT);
        }
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeRenderPageBitmap(JNIEnv *env, jclass,
                                                            jlong doc_ptr,
                                                            jlong page_ptr,
                                                            jobject bitmap,
                                                            jint start_x, jint start_y,
                                                            jint draw_size_hor, jint draw_size_ver,
                                                            jboolean render_annot,
                                                            jboolean,
                                                            jint canvasColor, jint pageBackgroundColor) {
    try {
        auto *doc = reinterpret_cast<DocumentFile*>(doc_ptr);
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);

        if (page == nullptr || bitmap == nullptr) {
            LOGE("Render page pointers invalid");
            return;
        }

        AndroidBitmapInfo info;
        int ret;
        if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
            LOGE("Fetching bitmap info failed: %s", strerror(ret * -1));
            return;
        }

        auto canvasHorSize = info.width;
        auto canvasVerSize = info.height;

        if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 &&
            info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
            LOGE("Bitmap format must be RGBA_8888 or RGB_565");
            return;
        }

        void *addr;
        if ((ret = AndroidBitmap_lockPixels(env, bitmap, &addr)) != 0) {
            LOGE("Locking bitmap failed: %s", strerror(ret * -1));
            return;
        }

        void *tmp;
        int format;
        int sourceStride;
        if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
            tmp = malloc(canvasVerSize * canvasHorSize * sizeof(rgb));
            sourceStride = (int) (canvasHorSize * sizeof(rgb));
            format = FPDFBitmap_BGR;
        } else {
            tmp = addr;
            sourceStride = (int) info.stride;
            format = FPDFBitmap_BGRA;
        }

        FPDF_BITMAP pdfBitmap = FPDFBitmap_CreateEx((int) canvasHorSize, (int) canvasVerSize,
                                                    format, tmp, sourceStride);

        /*LOGD("Start X: %d", startX);
        LOGD("Start Y: %d", startY);
        LOGD("Canvas Hor: %d", canvasHorSize);
        LOGD("Canvas Ver: %d", canvasVerSize);
        LOGD("Draw Hor: %d", drawSizeHor);
        LOGD("Draw Ver: %d", drawSizeVer);*/

        if ((draw_size_hor < canvasHorSize || draw_size_ver < canvasVerSize) && canvasColor != 0) {
            FPDFBitmap_FillRect(pdfBitmap, 0, 0, (int) canvasHorSize, (int) canvasVerSize,
                                canvasColor); //Gray
        }

        int baseHorSize = (canvasHorSize < draw_size_hor) ? (int) canvasHorSize
                                                          : (int) draw_size_hor;
        int baseVerSize = (canvasVerSize < draw_size_ver) ? (int) canvasVerSize
                                                          : (int) draw_size_ver;
        int baseX = (start_x < 0) ? 0 : (int) start_x;
        int baseY = (start_y < 0) ? 0 : (int) start_y;
        int flags = FPDF_REVERSE_BYTE_ORDER;

        FPDF_FORMFILLINFO form_callbacks = {0};
        form_callbacks.version = 2;
        FPDF_FORMHANDLE form;

        if (render_annot) {
            form = FPDFDOC_InitFormFillEnvironment(doc->pdfDocument, &form_callbacks);
            flags |= FPDF_ANNOT;
        }

//    if(text_mask) {
//        flags |= FPDF_RENDER_TEXT_MASK;
//    }

        if (pageBackgroundColor != 0) {
            FPDFBitmap_FillRect(pdfBitmap, baseX, baseY, baseHorSize, baseVerSize,
                                pageBackgroundColor); //White
        }

        FPDF_RenderPageBitmap(pdfBitmap, page,
                              start_x, start_y,
                              (int) draw_size_hor, (int) draw_size_ver,
                              0, flags);

        if (render_annot) {
            FPDF_FFLDraw(form, pdfBitmap, page, start_x, start_y, (int) draw_size_hor, (int) draw_size_ver, 0, FPDF_ANNOT);
            FPDFDOC_ExitFormFillEnvironment(form);
        }

        if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
            rgbBitmapTo565(tmp, sourceStride, addr, &info);
            free(tmp);
        }

        AndroidBitmap_unlockPixels(env, bitmap);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeRenderPageBitmapWithMatrix(JNIEnv *env, jclass,
                                                                      jlong page_ptr,
                                                                      jobject bitmap,
                                                                      jfloatArray matrixValues,
                                                                      jfloatArray clipRect,
                                                                      jboolean render_annot,
                                                                      jboolean,
                                                                      jint canvasColor, jint pageBackgroundColor) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);

        if (page == nullptr || bitmap == nullptr) {
            LOGE("Render page pointers invalid");
            return;
        }

        AndroidBitmapInfo info;
        int ret;
        if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
            LOGE("Fetching bitmap info failed: %s", strerror(ret * -1));
            return;
        }

        auto canvasHorSize = info.width;
        auto canvasVerSize = info.height;

        if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 &&
            info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
            LOGE("Bitmap format must be RGBA_8888 or RGB_565");
            return;
        }

        void *addr;
        if ((ret = AndroidBitmap_lockPixels(env, bitmap, &addr)) != 0) {
            LOGE("Locking bitmap failed: %s", strerror(ret * -1));
            return;
        }

        void *tmp;
        int format;
        int sourceStride;
        if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
            tmp = malloc(canvasVerSize * canvasHorSize * sizeof(rgb));
            sourceStride = (int) (canvasHorSize * sizeof(rgb));
            format = FPDFBitmap_BGR;
        } else {
            tmp = addr;
            sourceStride = (int) info.stride;
            format = FPDFBitmap_BGRA;
        }

        FPDF_BITMAP pdfBitmap = FPDFBitmap_CreateEx((int) canvasHorSize, (int) canvasVerSize,
                                                    format, tmp, sourceStride);

        /*LOGD("Start X: %d", startX);
        LOGD("Start Y: %d", startY);
        LOGD("Canvas Hor: %d", canvasHorSize);
        LOGD("Canvas Ver: %d", canvasVerSize);
        LOGD("Draw Hor: %d", drawSizeHor);
        LOGD("Draw Ver: %d", drawSizeVer);*/

//        if (draw_size_hor < canvasHorSize || draw_size_ver < canvasVerSize) {
//            FPDFBitmap_FillRect(pdfBitmap, 0, 0, canvasHorSize, canvasVerSize,
//                                0x848484FF); //Gray
//        }
//
//        int baseHorSize = (canvasHorSize < draw_size_hor) ? (int) canvasHorSize
//                                                          : (int) draw_size_hor;
//        int baseVerSize = (canvasVerSize < draw_size_ver) ? (int) canvasVerSize
//                                                          : (int) draw_size_ver;
//        int baseX = (start_x < 0) ? 0 : (int) start_x;
//        int baseY = (start_y < 0) ? 0 : (int) start_y;
        int flags = FPDF_REVERSE_BYTE_ORDER;

        if (render_annot) {
            flags |= FPDF_ANNOT;
        }

//    if(text_mask) {
//        flags |= FPDF_RENDER_TEXT_MASK;
//    }

        if (pageBackgroundColor != 0) {
            FPDFBitmap_FillRect(pdfBitmap, 0, 0, (int) canvasHorSize, (int) canvasVerSize,
                                pageBackgroundColor); //White
        }

//        jclass clazz = env->FindClass("android/graphics/RectF");
//        jfieldID left = env->GetFieldID(clazz, "left", "F");
//        jfieldID top = env->GetFieldID(clazz, "top", "F");
//        jfieldID right = env->GetFieldID(clazz, "right", "F");
//        jfieldID bottom = env->GetFieldID(clazz, "bottom", "F");
        jboolean isCopyClipRect;
        auto clipRectFloats = env->GetFloatArrayElements(clipRect, &isCopyClipRect);
        auto leftClip = clipRectFloats[0];
        auto topClip = clipRectFloats[1];
        auto rightClip = clipRectFloats[2];
        auto bottomClip = clipRectFloats[3];

        jboolean isCopy;
        auto matrixFloats = env->GetFloatArrayElements(matrixValues, &isCopy);

        auto matrix = FS_MATRIX();
        matrix.a = matrixFloats[0];
        matrix.b = 0;
        matrix.c = 0;
        matrix.d = matrixFloats[1];
        matrix.e = matrixFloats[2];
        matrix.f = matrixFloats[3];
        auto clip = FS_RECTF();
        clip.left = leftClip;
        clip.top = topClip;
        clip.right = rightClip;
        clip.bottom = bottomClip;
        if (isCopy) {
            env->ReleaseFloatArrayElements(matrixValues, (jfloat *) matrixFloats, JNI_ABORT);
        }

        if (isCopyClipRect) {
            env->ReleaseFloatArrayElements(clipRect, (jfloat *) clipRectFloats, JNI_ABORT);
        }

        FPDF_RenderPageBitmapWithMatrix(pdfBitmap, page, &matrix, &clip, flags);

        if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
            rgbBitmapTo565(tmp, sourceStride, addr, &info);
            free(tmp);
        }

        AndroidBitmap_unlockPixels(env, bitmap);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
}
extern "C"
JNIEXPORT jobject JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetPageSizeByIndex(JNIEnv *env, jclass,
                                                              jlong doc_ptr, jint page_index,
                                                              jint dpi) {
    try {
        auto *doc = reinterpret_cast<DocumentFile *>(doc_ptr);
        if (doc == nullptr) {
            LOGE("Document is null");

            jniThrowException(env, "java/lang/IllegalStateException",
                              "Document is null");
            return nullptr;
        }

        double width, height;
        int result = FPDF_GetPageSizeByIndex(doc->pdfDocument, page_index, &width, &height);

        if (result == 0) {
            width = 0;
            height = 0;
        }

        jint widthInt = (jint) (width * dpi / 72);
        jint heightInt = (jint) (height * dpi / 72);

        jclass clazz = env->FindClass("io/legere/pdfiumandroid/util/Size");
        if (clazz == nullptr) {
            LOGE("Size class not found");

            jniThrowException(env, "java/lang/IllegalStateException",
                              "Size class not found");
            return nullptr;
        }
        jmethodID constructorID = env->GetMethodID(clazz, "<init>", "(II)V");
        if (constructorID == nullptr) {
            LOGE("Size constructor not found");

            jniThrowException(env, "java/lang/IllegalStateException",
                              "Size constructor not found");
            return nullptr;
        }
        return env->NewObject(clazz, constructorID, widthInt, heightInt);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jlongArray JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetPageLinks(JNIEnv *env, jclass, jlong page_ptr) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        int pos = 0;
        std::vector<jlong> links;
        FPDF_LINK link;
        while (FPDFLink_Enumerate(page, &pos, &link)) {
            links.push_back(reinterpret_cast<jlong>(link));
        }

        jlongArray result = env->NewLongArray((int) links.size());
        env->SetLongArrayRegion(result, 0, (int) links.size(), &links[0]);
        return result;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativePageCoordsToDevice(JNIEnv *env, jclass,
                                                              jlong page_ptr, jint start_x,
                                                              jint start_y, jint size_x,
                                                              jint size_y, jint rotate,
                                                              jdouble page_x, jdouble page_y) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        int deviceX, deviceY;

        FPDF_PageToDevice(page, start_x, start_y, size_x, size_y, rotate, page_x, page_y, &deviceX,
                          &deviceY);

        jclass clazz = env->FindClass("android/graphics/Point");
        jmethodID constructorID = env->GetMethodID(clazz, "<init>", "(II)V");
        return env->NewObject(clazz, constructorID, deviceX, deviceY);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeDeviceCoordsToPage(JNIEnv *env, jclass,
                                                              jlong page_ptr, jint start_x,
                                                              jint start_y, jint size_x,
                                                              jint size_y, jint rotate,
                                                              jint device_x, jint device_y) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        double pageX, pageY;

        FPDF_DeviceToPage(page, start_x, start_y, size_x, size_y, rotate, device_x, device_y,
                          &pageX, &pageY);

        jclass clazz = env->FindClass("android/graphics/PointF");
        jmethodID constructorID = env->GetMethodID(clazz, "<init>", "(FF)V");
        return env->NewObject(clazz, constructorID, (float) pageX, (float) pageY);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

static void closeTextPageInternal(jlong textPagePtr) { FPDFText_ClosePage(reinterpret_cast<FPDF_TEXTPAGE>(textPagePtr)); }

extern "C"
JNIEXPORT void JNICALL
Java_io_legere_pdfiumandroid_PdfTextPage_nativeCloseTextPage(JNIEnv *env, jclass,
                                                             jlong page_ptr) {
    try {
        closeTextPageInternal(page_ptr);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfTextPage_nativeTextCountChars(JNIEnv *env, jclass,
                                                              jlong text_page_ptr) {
    try {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);
        return (jint) FPDFText_CountChars(textPage);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfTextPage_nativeTextGetText(JNIEnv *env, jclass,
                                                           jlong text_page_ptr, jint start_index,
                                                           jint count, jshortArray result) {
    try {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);
        jboolean isCopy = 1;
        auto *arr = (unsigned short *) env->GetShortArrayElements(result, &isCopy);
        jint output = (jint) FPDFText_GetText(textPage, (int) start_index, (int) count, arr);
        if (isCopy) {
            env->SetShortArrayRegion(result, 0, output, (jshort *) arr);
            env->ReleaseShortArrayElements(result, (jshort *) arr, JNI_ABORT);
        }
        return output;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfTextPage_nativeTextGetTextByteArray(JNIEnv *env, jclass,
                                                                    jlong text_page_ptr,
                                                                    jint start_index, jint count,
                                                                    jbyteArray result) {
    try {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);
        jboolean isCopy = 0;
        auto *arr = (jbyteArray) env->GetByteArrayElements(result, &isCopy);
        unsigned short buffer[count];
        jint output = (jint) FPDFText_GetText(textPage, (int) start_index, (int) count, buffer);
        memcpy(arr, buffer, count * sizeof(unsigned short));
        if (isCopy) {
            env->SetByteArrayRegion(result, 0, count * 2, (jbyte *) arr);
            env->ReleaseByteArrayElements(result, (jbyte *) arr, JNI_ABORT);
        }
        return output;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfTextPage_nativeTextGetUnicode(JNIEnv *env, jclass,
                                                              jlong text_page_ptr, jint index) {
    try {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);
        return (jint) FPDFText_GetUnicode(textPage, (int) index);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch (std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch (std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_io_legere_pdfiumandroid_PdfTextPage_nativeTextGetCharBox(JNIEnv *env, jclass,
                                                              jlong text_page_ptr, jint index) {
    try {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);
        jdoubleArray result = env->NewDoubleArray(4);
        if (result == nullptr) {
            return nullptr;
        }
        double fill[4];
        FPDFText_GetCharBox(textPage, (int) index, &fill[0], &fill[1], &fill[2], &fill[3]);
        env->SetDoubleArrayRegion(result, 0, 4, (jdouble *) fill);
        return result;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfTextPage_nativeTextGetCharIndexAtPos(JNIEnv *env, jclass,
                                                                     jlong text_page_ptr, jdouble x,
                                                                     jdouble y, jdouble x_tolerance,
                                                                     jdouble y_tolerance) {
    try {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);
        return (jint) FPDFText_GetCharIndexAtPos(textPage, (double) x, (double) y,
                                                 (double) x_tolerance, (double) y_tolerance);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfTextPage_nativeTextCountRects(JNIEnv *env, jclass,
                                                              jlong text_page_ptr, jint start_index,
                                                              jint count) {
    try {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);
        return (jint) FPDFText_CountRects(textPage, (int) start_index, (int) count);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_io_legere_pdfiumandroid_PdfTextPage_nativeTextGetRect(JNIEnv *env, jclass,
                                                           jlong text_page_ptr, jint rect_index) {
    try {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);
        jdoubleArray result = env->NewDoubleArray(4);
        if (result == nullptr) {
            return nullptr;
        }
        double fill[4];
        FPDFText_GetRect(textPage, (int) rect_index, &fill[0], &fill[1], &fill[2], &fill[3]);
        env->SetDoubleArrayRegion(result, 0, 4, (jdouble *) fill);
        return result;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfTextPage_nativeTextGetBoundedText(JNIEnv *env, jclass,
                                                                  jlong text_page_ptr, jdouble left,
                                                                  jdouble top, jdouble right,
                                                                  jdouble bottom, jshortArray arr) {
    try {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);
        jboolean isCopy = 0;
        unsigned short *buffer = nullptr;
        int bufLen = 0;
        if (arr != nullptr) {
            buffer = (unsigned short *) env->GetShortArrayElements(arr, &isCopy);
            bufLen = env->GetArrayLength(arr);
        }
        jint output = (jint) FPDFText_GetBoundedText(textPage, (double) left, (double) top,
                                                     (double) right, (double) bottom, buffer,
                                                     bufLen);
        if (isCopy) {
            env->SetShortArrayRegion(arr, 0, output, (jshort *) buffer);
            env->ReleaseShortArrayElements(arr, (jshort *) buffer, JNI_ABORT);
        }
        return output;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetDestPageIndex(JNIEnv *env, jclass,
                                                            jlong doc_ptr, jlong link_ptr) {
    try {
        auto *doc = reinterpret_cast<DocumentFile *>(doc_ptr);
        auto bookmark = reinterpret_cast<FPDF_BOOKMARK>(link_ptr);

        FPDF_DEST dest = FPDFBookmark_GetDest(doc->pdfDocument, bookmark);
        if (dest == nullptr) {
            return -1;
        }
        auto index = FPDFDest_GetDestPageIndex(doc->pdfDocument, dest);
        return (jint) index;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetLinkURI(JNIEnv *env, jclass, jlong doc_ptr,
                                                      jlong link_ptr) {
    try {
        auto *doc = reinterpret_cast<DocumentFile *>(doc_ptr);
        auto link = reinterpret_cast<FPDF_LINK>(link_ptr);
        FPDF_ACTION action = FPDFLink_GetAction(link);
        if (action == nullptr) {
            return nullptr;
        }
        size_t bufferLen = FPDFAction_GetURIPath(doc->pdfDocument, action, nullptr, 0);
        if (bufferLen <= 0) {
            return env->NewStringUTF("");
        }
        std::string uri;
        FPDFAction_GetURIPath(doc->pdfDocument, action, WriteInto(&uri, bufferLen), bufferLen);
        return env->NewStringUTF(uri.c_str());
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetLinkRect(JNIEnv *env, jclass, jlong,
                                                       jlong link_ptr) {
    try {
        auto link = reinterpret_cast<FPDF_LINK>(link_ptr);
        FS_RECTF fsRectF;
        FPDF_BOOL result = FPDFLink_GetAnnotRect(link, &fsRectF);

        if (!result) {
            return nullptr;
        }

        jclass clazz = env->FindClass("android/graphics/RectF");
        jmethodID constructorID = env->GetMethodID(clazz, "<init>", "(FFFF)V");
        return env->NewObject(clazz, constructorID, fsRectF.left, fsRectF.top, fsRectF.right,
                              fsRectF.bottom);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}
extern "C"
JNIEXPORT jlong JNICALL
Java_io_legere_pdfiumandroid_PdfDocument_nativeGetBookmarkDestIndex(JNIEnv *env, jobject,
                                                                    jlong doc_ptr,
                                                                    jlong bookmark_ptr) {
    try {
        auto *doc = reinterpret_cast<DocumentFile *>(doc_ptr);
        auto bookmark = reinterpret_cast<FPDF_BOOKMARK>(bookmark_ptr);

        FPDF_DEST dest = FPDFBookmark_GetDest(doc->pdfDocument, bookmark);
        if (dest == nullptr) {
            return -1;
        }
        return (jlong) FPDFDest_GetDestPageIndex(doc->pdfDocument, dest);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_io_legere_pdfiumandroid_PdfDocument_nativeGetPageCharCounts(JNIEnv *env, jobject,
                                                                 jlong doc_ptr) {
    try {
        auto *doc = reinterpret_cast<DocumentFile *>(doc_ptr);
        auto pageCount = FPDF_GetPageCount(doc->pdfDocument);

        std::vector<int> charCounts;

        for (int i = 0; i< pageCount; i++) {
            auto page = FPDF_LoadPage(doc->pdfDocument, i);
            auto textPage = FPDFText_LoadPage(page);
            auto charCount = FPDFText_CountChars(textPage);
            charCounts.push_back(charCount);
            FPDFText_ClosePage(textPage);
            FPDF_ClosePage(page);
        }

        jintArray result = env->NewIntArray((int) charCounts.size());
        env->SetIntArrayRegion(result, 0, (int) charCounts.size(), &charCounts[0]);
        return result;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_io_legere_pdfiumandroid_PdfTextPage_nativeFindStart(JNIEnv *env, jclass,
                                                         jlong text_page_ptr,
                                                         jstring find_what,
                                                         jint flags, jint start_index) {
    try {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);

        const jchar* raw = env->GetStringChars(find_what, nullptr);
        if (raw == nullptr) {
            // Handle error, possibly throw an exception
            return 0;
        }

        jsize len = env->GetStringLength(find_what);
        std::u16string result(raw, raw + len);

        auto handle = FPDFText_FindStart(
                textPage,
                (FPDF_WIDESTRING) result.c_str(),
                flags,
                start_index
        );

        env->ReleaseStringChars(find_what, raw);


        return (jlong) handle;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return 0;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_legere_pdfiumandroid_FindResult_nativeFindNext(JNIEnv *env, jobject,
                                                        jlong find_handle) {
    try {
        auto findHandle = reinterpret_cast<FPDF_SCHHANDLE>(find_handle);


        auto result = FPDFText_FindNext(findHandle);
        return result;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return 0;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_legere_pdfiumandroid_FindResult_nativeFindPrev(JNIEnv *env, jobject,
                                                        jlong find_handle) {
    try {
        auto findHandle = reinterpret_cast<FPDF_SCHHANDLE>(find_handle);


        auto result = FPDFText_FindPrev(findHandle);
        return result;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_FindResult_nativeGetSchResultIndex(JNIEnv *env, jobject,
                                                                 jlong find_handle) {
    try {
        auto findHandle = reinterpret_cast<FPDF_SCHHANDLE>(find_handle);


        auto result = FPDFText_GetSchResultIndex(findHandle);
        return result;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_FindResult_nativeGetSchCount(JNIEnv *env, jobject,
                                                           jlong find_handle) {
    try {
        auto findHandle = reinterpret_cast<FPDF_SCHHANDLE>(find_handle);


        auto result = FPDFText_GetSchCount(findHandle);
        return result;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_legere_pdfiumandroid_FindResult_nativeCloseFind(JNIEnv *env, jobject,
                                                         jlong find_handle) {
    try {
        auto findHandle = reinterpret_cast<FPDF_SCHHANDLE>(find_handle);


        FPDFText_FindClose(findHandle);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_io_legere_pdfiumandroid_PdfTextPage_nativeLoadWebLink(JNIEnv *env, jclass,
                                                           jlong text_page_ptr) {
    try {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);

        auto handle = FPDFLink_LoadWebLinks(textPage);

        return (jlong) handle;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_legere_pdfiumandroid_PdfPageLink_nativeClosePageLink(JNIEnv *env, jclass,
                                                             jlong page_link_ptr) {
    try {
        auto pageLink = reinterpret_cast<FPDF_PAGELINK>(page_link_ptr);


        FPDFLink_CloseWebLinks(pageLink);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
}
extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfPageLink_nativeCountWebLinks(JNIEnv *env, jclass,
                                                             jlong page_link_ptr) {
    try {
        auto pageLink = reinterpret_cast<FPDF_PAGELINK>(page_link_ptr);


        auto result =  FPDFLink_CountWebLinks(pageLink);
        LOGE("CountWebLinks result %d", result);
        return result;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}
extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfPageLink_nativeGetURL(JNIEnv *env, jclass,
                                                      jlong page_link_ptr, jint index, jint count, jbyteArray result) {
    try {
        auto pageLink = reinterpret_cast<FPDF_PAGELINK>(page_link_ptr);

        jboolean isCopy = 0;
        auto *arr = (jbyteArray) env->GetByteArrayElements(result, &isCopy);
        unsigned short buffer[count];

        jint output = (jint) FPDFLink_GetURL(pageLink, index, buffer, count);


        memcpy(arr, buffer, count * sizeof(unsigned short));
        if (isCopy) {
            env->SetByteArrayRegion(result, 0, count * 2, (jbyte *) arr);
            env->ReleaseByteArrayElements(result, (jbyte *) arr, JNI_ABORT);
        }
        return output;

    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return 0;
}
extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfPageLink_nativeCountRects(JNIEnv *env, jclass,
                                                          jlong page_link_ptr, jint index) {
    try {
        auto pageLink = reinterpret_cast<FPDF_PAGELINK>(page_link_ptr);


        auto result = FPDFLink_CountRects(pageLink, index);
        LOGE("CountRect %d", result);

        return result;
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return 0;
}
extern "C"
JNIEXPORT jfloatArray JNICALL
Java_io_legere_pdfiumandroid_PdfPageLink_nativeGetRect(JNIEnv *env, jclass,
                                                       jlong page_link_ptr, jint linkIndex, jint rectIndex) {
    try {
        auto pageLink = reinterpret_cast<FPDF_PAGELINK>(page_link_ptr);

        double left;
        double top;
        double right;
        double bottom;

        if (FPDFLink_GetRect(pageLink, linkIndex, rectIndex, &left, &top, &right, &bottom )) {
            jfloatArray result = env->NewFloatArray(4);
            if (result == nullptr) {
                return nullptr;
            }
            jfloat array[4];
            array[0] = (float) left;
            array[1] = (float) top;
            array[2] = (float) right;
            array[3] = (float) bottom;

            env->SetFloatArrayRegion(result, 0, 4, array);
            return result;
        }

    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}
extern "C"
JNIEXPORT jobject JNICALL
Java_io_legere_pdfiumandroid_PdfPageLink_nativeGetTextRange(JNIEnv *env, jclass,
                                                            jlong page_link_ptr, jint index) {
    try {
        auto pageLink = reinterpret_cast<FPDF_PAGELINK>(page_link_ptr);

        if (pageLink == nullptr) {
            LOGE("PageLink is null");

            jniThrowException(env, "java/lang/IllegalStateException",
                              "Document is null");
            return nullptr;
        }

        int start, count;
        int result = FPDFLink_GetTextRange(pageLink, index, &start, &count);

        if (result == 0) {
            start = 0;
            count = 0;
        }

        jclass clazz = env->FindClass("io/legere/pdfiumandroid/util/Size");
        if (clazz == nullptr) {
            LOGE("Size class not found");

            jniThrowException(env, "java/lang/IllegalStateException",
                              "Size class not found");
            return nullptr;
        }
        jmethodID constructorID = env->GetMethodID(clazz, "<init>", "(II)V");
        if (constructorID == nullptr) {
            LOGE("Size constructor not found");

            jniThrowException(env, "java/lang/IllegalStateException",
                              "Size constructor not found");
            return nullptr;
        }
        return env->NewObject(clazz, constructorID, start, count);

    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch (std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return nullptr;
}
void raise_java_exception(JNIEnv *pEnv, std::exception &exception) {
    jclass exClass;
    char const *className = "java/lang/NoClassDefFoundError";

    exClass = pEnv->FindClass( className );
    if (exClass == nullptr) {
        handleUnexpected(pEnv, className );
    } else {
        pEnv->ThrowNew( exClass, exception.what());
    }
}

void raise_java_invalid_arg_exception(JNIEnv *pEnv, std::invalid_argument &argument) {
    jclass exClass;
    char const *className = "java/lang/IllegalArgumentException";

    exClass = pEnv->FindClass( className );
    if (exClass == nullptr) {
        handleUnexpected(pEnv, className );
    } else {
        pEnv->ThrowNew( exClass, argument.what());
    }
}

void raise_java_runtime_exception(JNIEnv *pEnv, std::runtime_error &error) {
    jclass exClass;
    char const *className = "java/lang/RuntimeException";

    exClass = pEnv->FindClass( className );
    if (exClass == nullptr) {
        handleUnexpected(pEnv, className );
    } else {
        pEnv->ThrowNew( exClass, error.what());
    }
}

void raise_java_oom_exception(JNIEnv *pEnv, std::bad_alloc &alloc) {
    jclass exClass;
    char const *className = "java/lang/OutOfMemoryError";

    exClass = pEnv->FindClass( className );
    if (exClass == nullptr) {
        handleUnexpected(pEnv, className );
    } else {
        pEnv->ThrowNew( exClass, alloc.what());
    }
}

void handleUnexpected(JNIEnv *pEnv, char const *name) {
    LOGE("Unable to find class %s", name);
    pEnv->ExceptionClear();
}

extern "C"
JNIEXPORT jint JNICALL
Java_io_legere_pdfiumandroid_PdfPage_nativeGetPageRotation(JNIEnv *env, jclass,
                                                           jlong page_ptr) {
    try {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        return (jint)FPDFPage_GetRotation(page);
    } catch (std::bad_alloc &e) {
        raise_java_oom_exception(env, e);
    } catch(std::runtime_error &e) {
        raise_java_runtime_exception(env, e);
    } catch(std::invalid_argument &e) {
        raise_java_invalid_arg_exception(env, e);
    } catch(std::exception &e) {
        raise_java_exception(env, e);
    } catch (...) {
        auto e =  std::runtime_error("Unknown error");
        raise_java_exception(env, e);
    }
    return -1;
}

