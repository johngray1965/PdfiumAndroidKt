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

const int MATRIX_VALUES_LEN = 6;
const int RECT_VALUES_LEN = 4;

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
    jint bytesRead = env->CallIntMethod(nativeSourceBridge, readMethod, (jlong) position, (jlong) size);

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

static jlong NativeCore_nativeOpenDocument(JNIEnv *env, jobject, jint fd,
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

static jlong NativeCore_nativeOpenMemDocument(JNIEnv *env, jobject,
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


static jlong NativeCore_nativeOpenCustomDocument(JNIEnv *env, jobject, jobject nativeSourceBridge, jstring password, jlong dataLength) {
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
    if (startX + baseHorSize > drawSizeHor) {
        baseHorSize = drawSizeHor - startX;
    }
    if (startY + baseVerSize > drawSizeVer) {
        baseVerSize = drawSizeVer - startY;
    }
    if (startX + drawSizeHor > canvasHorSize) {
        drawSizeHor = canvasHorSize - startX;
    }
    if (startY + drawSizeVer > canvasVerSize) {
        drawSizeVer = canvasVerSize - startY;
    }

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

jfloatArray matrixToFloatArray(JNIEnv *env, const FS_MATRIX &fsMatrix) {
    jfloatArray result = env->NewFloatArray(MATRIX_VALUES_LEN);
    if (result == nullptr) {
        return nullptr;
    }
    jfloat array[MATRIX_VALUES_LEN];
    array[0] = fsMatrix.a;
    array[1] = fsMatrix.b;
    array[2] = fsMatrix.c;
    array[3] = fsMatrix.d;
    array[4] = fsMatrix.e;
    array[5] = fsMatrix.f;

    env->SetFloatArrayRegion(result, 0, MATRIX_VALUES_LEN, array);
    return result;
}

_FS_MATRIX_ floatArrayToMatrix(JNIEnv *env, jfloatArray matrixValues) {
    jfloat matrixFloats[MATRIX_VALUES_LEN];
    env->GetFloatArrayRegion(matrixValues, 0, MATRIX_VALUES_LEN, matrixFloats);

    auto matrix = FS_MATRIX();
    matrix.a = matrixFloats[0];
    matrix.b = matrixFloats[1];
    matrix.c = matrixFloats[2];
    matrix.d = matrixFloats[3];
    matrix.e = matrixFloats[4];
    matrix.f = matrixFloats[5];

    return matrix;
}

_FS_MATRIX_ floatArrayToMatrix(JNIEnv *env, const jfloat* matrixFloats, int index) {

    auto matrix = FS_MATRIX();
    matrix.a = matrixFloats[0 + index * MATRIX_VALUES_LEN];
    matrix.b = matrixFloats[1 + index * MATRIX_VALUES_LEN];
    matrix.c = matrixFloats[2 + index * MATRIX_VALUES_LEN];
    matrix.d = matrixFloats[3 + index * MATRIX_VALUES_LEN];
    matrix.e = matrixFloats[4 + index * MATRIX_VALUES_LEN];
    matrix.f = matrixFloats[5 + index * MATRIX_VALUES_LEN];


    return matrix;
}

_FS_RECTF_ floatArrayToRect(JNIEnv *env, jfloatArray rect) {
    jfloat clipRectFloats[RECT_VALUES_LEN];
    env->GetFloatArrayRegion(rect, 0, RECT_VALUES_LEN, clipRectFloats);

    auto clip = FS_RECTF();
    clip.left = clipRectFloats[0];
    clip.top = clipRectFloats[1];
    clip.right = clipRectFloats[2];
    clip.bottom = clipRectFloats[3];

    return clip;
}

_FS_RECTF_ floatArrayToRect(JNIEnv *env,  const jfloat* clipRectFloats, int index) {
    auto leftClip = clipRectFloats[0 + index * RECT_VALUES_LEN];
    auto topClip = clipRectFloats[1 + index * RECT_VALUES_LEN];
    auto rightClip = clipRectFloats[2 + index * RECT_VALUES_LEN];
    auto bottomClip = clipRectFloats[3 + index * RECT_VALUES_LEN];

    auto clip = FS_RECTF();
    clip.left = leftClip;
    clip.top = topClip;
    clip.right = rightClip;
    clip.bottom = bottomClip;

    return clip;
}

jfloatArray rectToFloatArray(JNIEnv *env, float rect[]) {
    jfloatArray result = env->NewFloatArray(RECT_VALUES_LEN);
    if (result == nullptr) {
        return nullptr;
    }


    env->SetFloatArrayRegion(result, 0, RECT_VALUES_LEN, (jfloat *) rect);
    return result;

}

jfloatArray rectToFloatArray(JNIEnv *env, const FS_RECTF &fsRectF) {

    float rect[RECT_VALUES_LEN];
    rect[0] = fsRectF.left;
    rect[1] = fsRectF.top;
    rect[2] = fsRectF.right;
    rect[3] = fsRectF.bottom;
    return rectToFloatArray(env, rect);
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


static void errorRect(float *rect) {
    rect[0] = -1.0f;
    rect[1] = -1.0f;
    rect[2] = -1.0f;
    rect[3] = -1.0f;
}

template <typename T, typename Func>
T runSafe(JNIEnv *env, T errorValue, Func func) {
    try {
        return func();
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
    return errorValue;
}

template <typename Func>
void runSafe(JNIEnv *env, Func func) {
    try {
        func();
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

static jint NativeDocument_nativeGetPageCount(JNIEnv *env, jobject,
                                                            jlong doc_ptr) {
    return runSafe(env, -1, [&]() {
        auto *doc = reinterpret_cast<DocumentFile*>(doc_ptr);
        return (jint)FPDF_GetPageCount(doc->pdfDocument);
    });
}
static jlong NativeDocument_nativeLoadPage(JNIEnv *env, jobject, jlong doc_ptr,
                                                        jint page_index) {
    return runSafe(env, (jlong) -1, [&]() {
        auto *doc = reinterpret_cast<DocumentFile *>(doc_ptr);
        return loadPageInternal(env, doc, (int) page_index);
    });
}

static void NativePage_nativeClosePage(JNIEnv *env, jclass , jlong page_ptr) {
    runSafe(env, [&]() {
        closePageInternal(page_ptr);
    });
}

static void NativeDocument_nativeDeletePage(JNIEnv *env, jobject, jlong doc_ptr,
                                                          jint page_index) {
    runSafe(env, [&]() {
        auto *doc = reinterpret_cast<DocumentFile *>(doc_ptr);
        if(doc == nullptr) throw std::runtime_error( "Get page document null");

        FPDF_DOCUMENT pdfDoc = doc->pdfDocument;
        if(pdfDoc != nullptr) {
            FPDFPage_Delete(pdfDoc, (int) page_index);
        }
    });
}

static void NativeDocument_nativeCloseDocument(JNIEnv *env, jobject,
                                                             jlong doc_ptr) {
    runSafe(env, [&]() {
        auto *doc = reinterpret_cast<DocumentFile*>(doc_ptr);
        // The destructor will close the document
        delete doc;
    });

}

static jlongArray NativeDocument_nativeLoadPages(JNIEnv *env, jobject, jlong doc_ptr,
                                                         jint from_index, jint to_index) {
    return runSafe(env, (jlongArray) nullptr, [&]() {
        auto *doc = reinterpret_cast<DocumentFile*>(doc_ptr);

        if(to_index < from_index) return (jlongArray) nullptr;
        jlong pages[ to_index - from_index + 1 ];

        int i;
        for(i = 0; i <= (to_index - from_index); i++){
            pages[i] = loadPageInternal(env, doc, (int)(i + from_index));
        }

        jlongArray javaPages = env -> NewLongArray( (jsize)(to_index - from_index + 1) );
        env -> SetLongArrayRegion(javaPages, 0, (jsize)(to_index - from_index + 1), (const jlong*)pages);

        return javaPages;
    });
}

static jstring NativeDocument_nativeGetDocumentMetaText(JNIEnv *env, jobject,
                                                                   jlong doc_ptr, jstring tag) {
    return runSafe(env, (jstring) nullptr, [&]() {
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
    });
}

static jlong NativeDocument_nativeGetFirstChildBookmark(JNIEnv *env, jobject,
                                                                     jlong doc_ptr,
                                                                     jlong bookmark_ptr) {
    return runSafe(env, (jlong) 0, [&]() {
        auto *doc = reinterpret_cast<DocumentFile*>(doc_ptr);
        FPDF_BOOKMARK parent;
        if(bookmark_ptr == 0) {
            parent = nullptr;
        } else {
            parent = reinterpret_cast<FPDF_BOOKMARK>(bookmark_ptr);
        }
        FPDF_BOOKMARK bookmark = FPDFBookmark_GetFirstChild(doc->pdfDocument, parent);
        if (bookmark == nullptr) {
            return (jlong) 0;
        }
        return reinterpret_cast<jlong>(bookmark);
    });
}

static jlong NativeDocument_nativeGetSiblingBookmark(JNIEnv *env, jobject,
                                                                  jlong doc_ptr,
                                                                  jlong bookmark_ptr) {
    return runSafe(env, (jlong) 0, [&]() {
        auto *doc = reinterpret_cast<DocumentFile*>(doc_ptr);
        auto parent = reinterpret_cast<FPDF_BOOKMARK>(bookmark_ptr);
        FPDF_BOOKMARK bookmark = FPDFBookmark_GetNextSibling(doc->pdfDocument, parent);
        if (bookmark == nullptr) {
            return (jlong) 0;
        }
        return reinterpret_cast<jlong>(bookmark);
    });
}

static jlong NativeDocument_nativeLoadTextPage(JNIEnv *env, jobject,
                                                            jlong doc_ptr, jlong page_ptr) {
    return runSafe(env, (jlong) -1, [&]() {
        auto *doc = reinterpret_cast<DocumentFile*>(doc_ptr);
        return loadTextPageInternal(env, doc, page_ptr);
    });
}

static jstring NativeDocument_nativeGetBookmarkTitle(JNIEnv *env, jobject,
                                                                jlong bookmark_ptr) {
    return runSafe(env, (jstring) nullptr, [&]() {
        auto bookmark = reinterpret_cast<FPDF_BOOKMARK>(bookmark_ptr);
        int bufferLen = (int) FPDFBookmark_GetTitle(bookmark, nullptr, 0);
        if (bufferLen <= 2) {
            return env->NewStringUTF("");
        }
        std::wstring title;
        FPDFBookmark_GetTitle(bookmark, WriteInto(&title, bufferLen + 1), bufferLen);
        return env->NewString((jchar*) title.c_str(), bufferLen / 2 - 1);
    });
}

static jboolean NativeDocument_nativeSaveAsCopy(JNIEnv *env, jobject, jlong doc_ptr,
                                                          jobject callback, jint flags) {
    return runSafe(env, (jboolean) false, [&]() {
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
        return (jboolean) false;
    });
}

static void NativePage_nativeClosePages(JNIEnv *env, jclass ,
                                                      jlongArray pages_ptr) {
    runSafe(env, [&]() {
        int length = (int) (env->GetArrayLength(pages_ptr));
        jlong *pages = env->GetLongArrayElements(pages_ptr, nullptr);

        if (pages != nullptr) {
            int i;
            for (i = 0; i < length; i++) { closePageInternal(pages[i]); }
            env->ReleaseLongArrayElements(pages_ptr, pages, JNI_ABORT);
        }
    });
}

static jint NativePage_nativeGetPageWidthPixel(JNIEnv *env, jclass,
                                                             jlong page_ptr, jint dpi) {
    return runSafe(env, -1, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        return (jint) (FPDF_GetPageWidth(page) * dpi / 72);
    });
}

static jint NativePage_nativeGetPageHeightPixel(JNIEnv *env, jclass,
                                                              jlong page_ptr, jint dpi) {
    return runSafe(env, -1, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        return (jint)(FPDF_GetPageHeight(page) * dpi / 72);
    });
}

static jint NativePage_nativeGetPageWidthPoint(JNIEnv *env, jclass,
                                                             jlong page_ptr) {
    return runSafe(env, -1, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        return (jint)FPDF_GetPageWidth(page);
    });
}

static jint NativePage_nativeGetPageHeightPoint(JNIEnv *env, jclass,
                                                              jlong page_ptr) {
    return runSafe(env, -1, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        return (jint)FPDF_GetPageHeight(page);
    });
}

static jdouble NativeTextPage_nativeGetFontSize(JNIEnv *env, jclass, jlong page_ptr,
                                                       jint char_index) {
    return runSafe(env, 0.0, [&]() {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(page_ptr);
        return (jdouble) FPDFText_GetFontSize(textPage, char_index);
    });
}

static jfloatArray NativePage_nativeGetPageMediaBox(JNIEnv *env, jclass ,
                                                           jlong page_ptr) {
    return runSafe(env, (jfloatArray) nullptr, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        float rect[RECT_VALUES_LEN];
        if (!FPDFPage_GetMediaBox(page, &rect[0], &rect[1], &rect[2], &rect[3])) {
            errorRect(rect);
        }
        return rectToFloatArray(env, rect);
    });
}

static jfloatArray NativePage_nativeGetPageCropBox(JNIEnv *env, jclass,
                                                          jlong page_ptr) {
    return runSafe(env, (jfloatArray) nullptr, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        float rect[RECT_VALUES_LEN];
        if (!FPDFPage_GetCropBox(page, &rect[0], &rect[1], &rect[2], &rect[3])) {
            errorRect(rect);
        }
        return rectToFloatArray(env, rect);
    });
}

static jfloatArray NativePage_nativeGetPageBleedBox(JNIEnv *env, jclass,
                                                           jlong page_ptr) {
    return runSafe(env, (jfloatArray) nullptr, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        float rect[RECT_VALUES_LEN];
        if (!FPDFPage_GetBleedBox(page, &rect[0], &rect[1], &rect[2], &rect[3])) {
            errorRect(rect);
        }
        return rectToFloatArray(env, rect);
    });
}

static jfloatArray NativePage_nativeGetPageTrimBox(JNIEnv *env, jclass,
                                                          jlong page_ptr) {
    return runSafe(env, (jfloatArray) nullptr, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        float rect[RECT_VALUES_LEN];
        if (!FPDFPage_GetTrimBox(page, &rect[0], &rect[1], &rect[2], &rect[3])) {
            errorRect(rect);
        }
        return rectToFloatArray(env, rect);
    });
}

static jfloatArray NativePage_nativeGetPageArtBox(JNIEnv *env, jclass,
                                                         jlong page_ptr) {
    return runSafe(env, (jfloatArray) nullptr, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        float rect[RECT_VALUES_LEN];
        if (!FPDFPage_GetArtBox(page, &rect[0], &rect[1], &rect[2], &rect[3])) {
            errorRect(rect);
        }
        return rectToFloatArray(env, rect);
    });
}

static jfloatArray NativePage_nativeGetPageBoundingBox(JNIEnv *env, jclass,
                                                              jlong page_ptr) {
    return runSafe(env, (jfloatArray) nullptr, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        FS_RECTF fsRect;
        if (!FPDF_GetPageBoundingBox(page, &fsRect)) {
            fsRect.left = -1.0f;
            fsRect.top = -1.0f;
            fsRect.right = -1.0f;
            fsRect.bottom = -1.0f;
        }
        return rectToFloatArray(env, fsRect);
    });
}

static jfloatArray NativePage_nativeGetPageMatrix(JNIEnv *env, jclass,
                                                         jlong page_ptr) {
    return runSafe(env, (jfloatArray) nullptr, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        FPDF_PAGEOBJECT pageObject = FPDFPage_GetObject(page, 0);

        FS_MATRIX fsMatrix;
        if (!FPDFPageObj_GetMatrix(pageObject, &fsMatrix)) {
            fsMatrix.a = -1.0f;
            fsMatrix.b = -1.0f;
            fsMatrix.c = -1.0f;
            fsMatrix.d = -1.0f;
            fsMatrix.e = -1.0f;
            fsMatrix.f = -1.0f;
        }

        return  matrixToFloatArray(env, fsMatrix);
    });
}

static jboolean NativePage_nativeLockSurface(JNIEnv *env, jclass clazz, jobject surface, jintArray widthHeightArray, jlongArray ptrsArray) {
    LOGD("nativeLockSurface");
    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);
    if (nativeWindow == nullptr) {
        LOGE("native window pointer null");
        return false;
    }

    auto width = ANativeWindow_getWidth(nativeWindow);
    auto height = ANativeWindow_getHeight(nativeWindow);

    jint wh[2];
    wh[0] = width;
    wh[1] = height;

    if (ANativeWindow_getFormat(nativeWindow) != WINDOW_FORMAT_RGBA_8888) {
        LOGD("Set format to RGBA_8888");
        ANativeWindow_setBuffersGeometry(nativeWindow,
                                         width,
                                         height,
                                         WINDOW_FORMAT_RGBA_8888);
    }
    env->SetIntArrayRegion(widthHeightArray, 0, 2, wh);

    auto *buffer = new ANativeWindow_Buffer();
    int ret;
    if ((ret = ANativeWindow_lock(nativeWindow, buffer, nullptr)) != 0) {
        LOGE("Locking native window failed: %s", strerror(ret * -1));
        return false;
    }
    jlong ptrs[2];
    ptrs[0] = reinterpret_cast<jlong>(nativeWindow);
    ptrs[1] = reinterpret_cast<jlong>(buffer);
    env->SetLongArrayRegion(ptrsArray, 0, 2, ptrs);
    return true;
}
static void NativePage_nativeUnlockSurface(JNIEnv *env, jclass clazz,
                                                         jlongArray ptrsArray) {
    LOGD("nativeUnlockSurface");
    jlong ptrs[2];
    env->GetLongArrayRegion(ptrsArray, 0, 2, ptrs);

    auto nativeWindow = reinterpret_cast<ANativeWindow*>(ptrs[0]);

    auto buffer = reinterpret_cast<ANativeWindow_Buffer*>(ptrs[1]);

    delete buffer;



    ANativeWindow_unlockAndPost(nativeWindow);
    ANativeWindow_release(nativeWindow);

}

static jboolean NativePage_nativeRenderPage(JNIEnv *env, jclass, jlong page_ptr,
                                                      jlong buffer_ptr, jint start_x,
                                                      jint start_y, jint draw_size_hor,
                                                      jint draw_size_ver, jboolean render_annot,
                                                      jint canvasColor, jint pageBackgroundColor) {
    return runSafe(env, (jboolean) false, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);

        if (page == nullptr) {
            LOGE("Render page pointers invalid");
            return (jboolean) false;
        }

        auto buffer = reinterpret_cast<ANativeWindow_Buffer*>(buffer_ptr);

        renderPageInternal(page, buffer,
                           (int) start_x, (int) start_y,
                           buffer->width, buffer->height,
                           (int) draw_size_hor, (int) draw_size_ver,
                           (bool) render_annot, canvasColor, pageBackgroundColor);
        return (jboolean) true;
    });
}

static jboolean NativePage_nativeRenderPageWithMatrix(JNIEnv *env, jclass,
                                                                jlong page_ptr, jlong buffer_ptr,
                                                                jint draw_size_hor, jint draw_size_ver,
                                                                jfloatArray matrixValues,
                                                                jfloatArray clipRect,
                                                                jboolean render_annot,
                                                                jboolean,
                                                                jint canvasColor, jint pageBackgroundColor) {
    return runSafe(env, (jboolean) false, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);

        if (page == nullptr) {
            LOGE("Render page pointers invalid");
            return (jboolean) false;
        }

        auto bufferPtr = reinterpret_cast<ANativeWindow_Buffer*>(buffer_ptr);
        auto buffer = *bufferPtr;


        auto clip = floatArrayToRect(env, clipRect);

        auto canvasHorSize = draw_size_hor;
        auto canvasVerSize = draw_size_ver;

        auto drawSizeHor = (int) (clip.right - clip.left);
        auto drawSizeVer = (int) (clip.bottom - clip.top);

        FPDF_BITMAP pdfBitmap = FPDFBitmap_CreateEx(canvasHorSize, canvasVerSize,
                                                    FPDFBitmap_BGRA,
                                                    buffer.bits, (int)(buffer.stride) * 4);

        if((drawSizeHor < canvasHorSize || drawSizeVer < canvasVerSize) && canvasColor != 0) {
            FPDFBitmap_FillRect( pdfBitmap, 0, 0, canvasHorSize, canvasVerSize,
                                 canvasColor); //Gray
        }

        auto startX = (int) clip.left;
        auto startY = (int) clip.top;
        int baseHorSize = (canvasHorSize < drawSizeHor)? canvasHorSize : drawSizeHor;
        int baseVerSize = (canvasVerSize < drawSizeVer)? canvasVerSize : drawSizeVer;
        int baseX = (startX < 0)? 0 : startX;
        int baseY = (startY < 0)? 0 : startY;
        if (startX + baseHorSize > canvasHorSize) {
            baseHorSize = canvasHorSize - startX;
        }
        if (startY + baseVerSize > canvasVerSize) {
            baseVerSize = canvasVerSize - startY;
        }

        int flags = FPDF_REVERSE_BYTE_ORDER;

        if (render_annot) {
            flags |= FPDF_ANNOT;
        }



        if (pageBackgroundColor != 0) {
            FPDFBitmap_FillRect(pdfBitmap, baseX, baseY, baseHorSize, baseVerSize,
                                pageBackgroundColor); //White
        }

        auto matrix = floatArrayToMatrix(env, matrixValues);

        FPDF_RenderPageBitmapWithMatrix(pdfBitmap, page, &matrix, &clip, flags);

        return (jboolean) true;
    });
}

static jboolean NativePage_nativeRenderPageSurface(JNIEnv *env, jclass, jlong page_ptr,
                                                      jobject surface, jint start_x,
                                                      jint start_y, jboolean render_annot,
                                                      jint canvasColor, jint pageBackgroundColor) {
    return runSafe(env, (jboolean) false, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);

        if (page == nullptr) {
            LOGE("Render page pointers invalid");
            return (jboolean) false;
        }
        ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);
        if (nativeWindow == nullptr) {
            LOGE("native window pointer null");
            return (jboolean) false;
        }
        auto width = ANativeWindow_getWidth(nativeWindow);
        auto height = ANativeWindow_getHeight(nativeWindow);

        if (ANativeWindow_getFormat(nativeWindow) != WINDOW_FORMAT_RGBA_8888) {
            LOGD("Set format to RGBA_8888");
            ANativeWindow_setBuffersGeometry(nativeWindow,
                                             width,
                                             height,
                                             WINDOW_FORMAT_RGBA_8888);
        }

        auto *buffer = new ANativeWindow_Buffer();
        int ret;
        if ((ret = ANativeWindow_lock(nativeWindow, buffer, nullptr)) != 0) {
            LOGE("Locking native window failed: %s", strerror(ret * -1));
            return (jboolean) false;
        }

        renderPageInternal(page, buffer,
                           (int) start_x, (int) start_y,
                           width, height,
                           (int) width, (int) height,
                           (bool) render_annot, canvasColor, pageBackgroundColor);
        ANativeWindow_unlockAndPost(nativeWindow);
        ANativeWindow_release(nativeWindow);

        return (jboolean) true;
    });
}

static jboolean NativePage_nativeRenderPageSurfaceWithMatrix(JNIEnv *env, jclass,
                                                                jlong page_ptr, jobject surface,
                                                                jfloatArray matrixValues,
                                                                jfloatArray clipRect,
                                                                jboolean render_annot,
                                                                jboolean,
                                                                jint canvasColor, jint pageBackgroundColor) {
    return runSafe(env, (jboolean) false, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);

        if (page == nullptr) {
            LOGE("Render page pointers invalid");
            return (jboolean) false;
        }

        ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);
        if (nativeWindow == nullptr) {
            LOGE("native window pointer null");
            return (jboolean) false;
        }
        auto width = ANativeWindow_getWidth(nativeWindow);
        auto height = ANativeWindow_getHeight(nativeWindow);

        if (ANativeWindow_getFormat(nativeWindow) != WINDOW_FORMAT_RGBA_8888) {
            LOGD("Set format to RGBA_8888");
            ANativeWindow_setBuffersGeometry(nativeWindow,
                                             width,
                                             height,
                                             WINDOW_FORMAT_RGBA_8888);
        }

        auto *buffer = new ANativeWindow_Buffer();
        int ret;
        if ((ret = ANativeWindow_lock(nativeWindow, buffer, nullptr)) != 0) {
            LOGE("Locking native window failed: %s", strerror(ret * -1));
            return (jboolean) false;
        }


        auto clip = floatArrayToRect(env, clipRect);

        auto drawSizeHor = (int) (clip.right - clip.left);
        auto drawSizeVer = (int) (clip.bottom - clip.top);
        auto startX = (int) clip.left;
        auto startY = (int) clip.top;

        int baseHorSize = (width < drawSizeHor)? width : drawSizeHor;
        int baseVerSize = (height < drawSizeVer)? height : drawSizeVer;
        int baseX = (startX < 0)? 0 : startX;
        int baseY = (startY < 0)? 0 : startY;
        if (startX + baseHorSize > width) {
            baseHorSize = width - startX;
        }
        if (startY + baseVerSize > height) {
            baseVerSize = height - startY;
        }

        if (clip.left < 0) {
            clip.left = 0;
        }
        if (clip.top < 0) {
            clip.top = 0;
        }
        auto fWidth = (float) width;
        auto fHeight = (float) height;

        if (clip.right > fWidth) {
            clip.right = fWidth;
            baseHorSize = width - startX;
        }
        if (clip.bottom > fHeight) {
            clip.bottom = fHeight;
            baseVerSize = height - startY;
        }

        FPDF_BITMAP pdfBitmap = FPDFBitmap_CreateEx(width, height,
                                                    FPDFBitmap_BGRA,
                                                    buffer->bits, (int)(buffer->stride) * 4);

        if((drawSizeHor < width || drawSizeVer < height) && canvasColor != 0) {
            FPDFBitmap_FillRect( pdfBitmap, 0, 0, width, height,
                                 canvasColor); //Gray
        }

        int flags = FPDF_REVERSE_BYTE_ORDER;

        if (render_annot) {
            flags |= FPDF_ANNOT;
        }

        if (pageBackgroundColor != 0) {
            FPDFBitmap_FillRect(pdfBitmap, baseX, baseY, baseHorSize, baseVerSize,
                                pageBackgroundColor); //White
        }

        auto matrix = floatArrayToMatrix(env, matrixValues);

        LOGD("FPDF_RenderPageBitmapWithMatrix");
        FPDF_RenderPageBitmapWithMatrix(pdfBitmap, page, &matrix, &clip, flags);

        LOGD("ANativeWindow_unlockAndPost");
        ANativeWindow_unlockAndPost(nativeWindow);
        ANativeWindow_release(nativeWindow);

        return (jboolean) true;
    });
}

static jboolean NativeDocument_nativeRenderPagesSurfaceWithMatrix(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jlongArray pages,
                                                                            jobject surface,
                                                                            jfloatArray matrices,
                                                                            jfloatArray clipRect,
                                                                            jboolean render_annot,
                                                                            jboolean text_mask,
                                                                            jint canvasColor,
                                                                            jint pageBackgroundColor) {
    return runSafe(env, (jboolean) false, [&]() {
        ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);
        if (nativeWindow == nullptr) {
            LOGE("native window pointer null");
            return (jboolean) false;
        }
        auto width = ANativeWindow_getWidth(nativeWindow);
        auto height = ANativeWindow_getHeight(nativeWindow);

        if (ANativeWindow_getFormat(nativeWindow) != WINDOW_FORMAT_RGBA_8888) {
            LOGD("Set format to RGBA_8888");
            ANativeWindow_setBuffersGeometry(nativeWindow,
                                             width,
                                             height,
                                             WINDOW_FORMAT_RGBA_8888);
        }

        LOGD("nativeRenderPagesSurfaceWithMatrix width %d, height %d", width, height);

        auto *buffer = new ANativeWindow_Buffer();
        int ret;
        if ((ret = ANativeWindow_lock(nativeWindow, buffer, nullptr)) != 0) {
            LOGE("Locking native window failed: %s", strerror(ret * -1));
            ANativeWindow_release(nativeWindow);
            return (jboolean) false;
        }

        auto pagePtrs = env->GetLongArrayElements(pages, nullptr);
        auto numPages = env->GetArrayLength(pages);

        auto clipRectFloats = env->GetFloatArrayElements(clipRect, nullptr);

        auto matrixFloats = env->GetFloatArrayElements(matrices, nullptr);


        FPDF_BITMAP pdfBitmap = FPDFBitmap_CreateEx(width, height,
                                                    FPDFBitmap_BGRA,
                                                    buffer->bits, (int)(buffer->stride) * 4);

        if(canvasColor != 0) {
            FPDFBitmap_FillRect( pdfBitmap, 0, 0, width, height,
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
                ANativeWindow_release(nativeWindow);
                return (jboolean) false;
            }


            auto clip = floatArrayToRect(env, clipRectFloats, pageIndex);

            auto drawSizeHor = (int) (clip.right - clip.left);
            auto drawSizeVer = (int) (clip.bottom - clip.top);

            auto startX = (int) clip.left;
            auto startY = (int) clip.top;

            int baseHorSize = (width < drawSizeHor) ? width : drawSizeHor;
            int baseVerSize = (height < drawSizeVer) ? height : drawSizeVer;
            int baseX = (startX < 0) ? 0 : startX;
            int baseY = (startY < 0) ? 0 : startY;
            if (startX + drawSizeHor > width) {
                drawSizeHor = width - startX;
            }
            if (startY + drawSizeVer > height) {
                drawSizeVer = height - startY;
            }
            if (clip.left < 0) {
                clip.left = 0;
            }
            if (clip.top < 0) {
                clip.top = 0;
            }
            auto fWidth = (float) width;
            auto fHeight = (float) height;
            if (clip.right > fWidth) {
                clip.right = fWidth;
            }
            if (clip.bottom > fHeight) {
                clip.bottom = fHeight;
            }


            if (pageBackgroundColor != 0) {
                FPDFBitmap_FillRect(pdfBitmap, baseX, baseY, baseHorSize, baseVerSize,
                                    pageBackgroundColor); //White
            }

            auto matrix = floatArrayToMatrix(env, matrixFloats, pageIndex);


            FPDF_RenderPageBitmapWithMatrix(pdfBitmap, page, &matrix, &clip, flags);
            /* end process each page */
        }

        ANativeWindow_unlockAndPost(nativeWindow);
        ANativeWindow_release(nativeWindow);


        env->ReleaseFloatArrayElements(matrices, (jfloat *) matrixFloats, 0);
        env->ReleaseFloatArrayElements(clipRect, (jfloat *) clipRectFloats, 0);
        env->ReleaseLongArrayElements(pages, pagePtrs, 0);

        return (jboolean) true;
    });
}

static void NativeDocument_nativeRenderPagesWithMatrix(JNIEnv *env, jobject thiz,
                                                                     jlongArray pages, jlong buffer_ptr,
                                                                     jint draw_size_hor, jint draw_size_ver,
                                                                     jfloatArray matrices,
                                                                     jfloatArray clipRect,
                                                                     jboolean render_annot,
                                                                     jboolean text_mask,
                                                                     jint canvasColor,
                                                                     jint pageBackgroundColor) {
    runSafe(env, [&]() {
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

            auto clip = floatArrayToRect(env, clipRectFloats, pageIndex);

            auto drawSizeHor = (int) (clip.right - clip.left);
            auto drawSizeVer = (int) (clip.bottom - clip.top);

            auto startX = (int) clip.left;
            auto startY = (int) clip.top;
            int baseHorSize = (canvasHorSize < drawSizeHor) ? canvasHorSize : drawSizeHor;
            int baseVerSize = (canvasVerSize < draw_size_ver) ? canvasVerSize : drawSizeVer;
            int baseX = (startX < 0) ? 0 : startX;
            int baseY = (startY < 0) ? 0 : startY;


            if (pageBackgroundColor != 0) {
                FPDFBitmap_FillRect(pdfBitmap, baseX, baseY, baseHorSize, baseVerSize,
                                    pageBackgroundColor); //White
            }

            auto matrix = floatArrayToMatrix(env, matrixFloats, pageIndex);


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
    });
}
static void NativePage_nativeRenderPageBitmap(JNIEnv *env, jclass,
                                                            jlong doc_ptr,
                                                            jlong page_ptr,
                                                            jobject bitmap,
                                                            jint start_x, jint start_y,
                                                            jint draw_size_hor, jint draw_size_ver,
                                                            jboolean render_annot,
                                                            jboolean,
                                                            jint canvasColor, jint pageBackgroundColor) {
    runSafe(env, [&]() {
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
    });
}

static void NativePage_nativeRenderPageBitmapWithMatrix(JNIEnv *env, jclass,
                                                                      jlong page_ptr,
                                                                      jobject bitmap,
                                                                      jfloatArray matrixValues,
                                                                      jfloatArray clipRect,
                                                                      jboolean render_annot,
                                                                      jboolean,
                                                                      jint canvasColor, jint pageBackgroundColor) {
    runSafe(env, [&]() {
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

        auto clip = floatArrayToRect(env, clipRect);

        auto matrix = floatArrayToMatrix(env, matrixValues);


        FPDF_RenderPageBitmapWithMatrix(pdfBitmap, page, &matrix, &clip, flags);

        if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
            rgbBitmapTo565(tmp, sourceStride, addr, &info);
            free(tmp);
        }

        AndroidBitmap_unlockPixels(env, bitmap);
    });
}
static jintArray NativePage_nativeGetPageSizeByIndex(JNIEnv *env, jclass,
                                                              jlong doc_ptr, jint page_index,
                                                              jint dpi) {
    return runSafe(env, (jintArray) nullptr, [&]() {
        auto *doc = reinterpret_cast<DocumentFile *>(doc_ptr);
        if (doc == nullptr) {
            LOGE("Document is null");

            jniThrowException(env, "java/lang/IllegalStateException",
                              "Document is null");
            return (jintArray) nullptr;
        }

        double width, height;
        int result = FPDF_GetPageSizeByIndex(doc->pdfDocument, page_index, &width, &height);

        if (result == 0) {
            width = 0;
            height = 0;
        }

        jint widthInt = (jint) (width * dpi / 72);
        jint heightInt = (jint) (height * dpi / 72);

        jintArray retVal = env->NewIntArray(2);
        if (retVal == nullptr) {
            return (jintArray) nullptr;
        }

        jint buffer[] = {widthInt, heightInt};
        env->SetIntArrayRegion(retVal, 0, 2, buffer);

        return retVal;
    });
}

static jlongArray NativePage_nativeGetPageLinks(JNIEnv *env, jclass, jlong page_ptr) {
    return runSafe(env, (jlongArray) nullptr, [&]() {
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
    });
}

static jintArray NativePage_nativePageCoordsToDevice(JNIEnv *env, jclass,
                                                              jlong page_ptr, jint start_x,
                                                              jint start_y, jint size_x,
                                                              jint size_y, jint rotate,
                                                              jdouble page_x, jdouble page_y) {
    return runSafe(env, (jintArray) nullptr, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        int deviceX, deviceY;

        FPDF_PageToDevice(page, start_x, start_y, size_x, size_y, rotate, page_x, page_y, &deviceX,
                          &deviceY);
        jintArray retVal = env->NewIntArray(2);
        if (retVal == nullptr) {
            return (jintArray) nullptr;
        }

        jint buffer[] = {deviceX, deviceY};
        env->SetIntArrayRegion(retVal, 0, 2, buffer);
        return retVal;
    });
}

static jfloatArray NativePage_nativeDeviceCoordsToPage(JNIEnv *env, jclass,
                                                              jlong page_ptr, jint start_x,
                                                              jint start_y, jint size_x,
                                                              jint size_y, jint rotate,
                                                              jint device_x, jint device_y) {
    return runSafe(env, (jfloatArray) nullptr, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        double pageX, pageY;


        jfloatArray retVal = env->NewFloatArray(2);
        if (retVal == nullptr) {
            return (jfloatArray) nullptr;
        }
        float point[2];
        if (!FPDF_DeviceToPage(page, start_x, start_y, size_x, size_y, rotate, device_x, device_y,
                               &pageX, &pageY)) {
            point[0] = -1.0f;
            point[1] = -1.0f;
        } else {
            point[0] = (float) pageX;
            point[1] = (float) pageY;
        }

        env->SetFloatArrayRegion(retVal, 0, 2, point);
        return retVal;
    });
}

static void closeTextPageInternal(jlong textPagePtr) { FPDFText_ClosePage(reinterpret_cast<FPDF_TEXTPAGE>(textPagePtr)); }

static void NativeTextPage_nativeCloseTextPage(JNIEnv *env, jclass,
                                                             jlong page_ptr) {
    runSafe(env, [&]() {
        closeTextPageInternal(page_ptr);
    });
}

static jint NativeTextPage_nativeTextCountChars(JNIEnv *env, jclass,
                                                              jlong text_page_ptr) {
    return runSafe(env, -1, [&]() {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);
        return (jint) FPDFText_CountChars(textPage);
    });
}

static jint NativeTextPage_nativeTextGetText(JNIEnv *env, jclass,
                                                           jlong text_page_ptr, jint start_index,
                                                           jint count, jshortArray result) {
    return runSafe(env, -1, [&]() {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);
        jboolean isCopy = 1;
        auto *arr = (unsigned short *) env->GetShortArrayElements(result, &isCopy);
        jint output = (jint) FPDFText_GetText(textPage, (int) start_index, (int) count, arr);
        if (isCopy) {
            env->SetShortArrayRegion(result, 0, output, (jshort *) arr);
            env->ReleaseShortArrayElements(result, (jshort *) arr, JNI_ABORT);
        }
        return output;
    });
}

static jint NativeTextPage_nativeTextGetTextByteArray(JNIEnv *env, jclass,
                                                                    jlong text_page_ptr,
                                                                    jint start_index, jint count,
                                                                    jbyteArray result) {
    return runSafe(env, -1, [&]() {
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
    });
}

static jint NativeTextPage_nativeTextGetUnicode(JNIEnv *env, jclass,
                                                              jlong text_page_ptr, jint index) {
    return runSafe(env, -1, [&]() {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);
        return (jint) FPDFText_GetUnicode(textPage, (int) index);
    });
}

static jdoubleArray NativeTextPage_nativeTextGetCharBox(JNIEnv *env, jclass,
                                                              jlong text_page_ptr, jint index) {
    return runSafe(env, (jdoubleArray) nullptr, [&]() {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);
        jdoubleArray result = env->NewDoubleArray(4);
        if (result == nullptr) {
            return (jdoubleArray) nullptr;
        }
        double fill[4];
        FPDFText_GetCharBox(textPage, (int) index, &fill[0], &fill[1], &fill[2], &fill[3]);
        env->SetDoubleArrayRegion(result, 0, 4, (jdouble *) fill);
        return result;
    });
}

static jint NativeTextPage_nativeTextGetCharIndexAtPos(JNIEnv *env, jclass,
                                                                     jlong text_page_ptr, jdouble x,
                                                                     jdouble y, jdouble x_tolerance,
                                                                     jdouble y_tolerance) {
    return runSafe(env, -1, [&]() {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);
        return (jint) FPDFText_GetCharIndexAtPos(textPage, (double) x, (double) y,
                                                 (double) x_tolerance, (double) y_tolerance);
    });
}

static jint NativeTextPage_nativeTextCountRects(JNIEnv *env, jclass,
                                                              jlong text_page_ptr, jint start_index,
                                                              jint count) {
    return runSafe(env, -1, [&]() {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);
        return (jint) FPDFText_CountRects(textPage, (int) start_index, (int) count);
    });
}

static jdoubleArray NativeTextPage_nativeTextGetRect(JNIEnv *env, jclass,
                                                           jlong text_page_ptr, jint rect_index) {
    return runSafe(env, (jdoubleArray) nullptr, [&]() {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);
        jdoubleArray result = env->NewDoubleArray(RECT_VALUES_LEN);
        if (result == nullptr) {
            return (jdoubleArray) nullptr;
        }
        double fill[RECT_VALUES_LEN];
        FPDFText_GetRect(textPage, (int) rect_index, &fill[0], &fill[1], &fill[2], &fill[3]);
        env->SetDoubleArrayRegion(result, 0, RECT_VALUES_LEN, (jdouble *) fill);
        return result;
    });
}

static jint NativeTextPage_nativeTextGetBoundedText(JNIEnv *env, jclass,
                                                                  jlong text_page_ptr, jdouble left,
                                                                  jdouble top, jdouble right,
                                                                  jdouble bottom, jshortArray arr) {
    return runSafe(env, -1, [&]() {
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
    });
}

static jint NativePage_nativeGetDestPageIndex(JNIEnv *env, jclass,
                                                            jlong doc_ptr, jlong link_ptr) {
    return runSafe(env, -1, [&]() {
        auto *doc = reinterpret_cast<DocumentFile *>(doc_ptr);
        auto link = reinterpret_cast<FPDF_LINK>(link_ptr);
        FPDF_DEST dest = FPDFLink_GetDest(doc->pdfDocument, link);
        if (dest == nullptr) {
            return -1;
        }
        unsigned long index = FPDFDest_GetDestPageIndex(doc->pdfDocument, dest);
        return (jint) index;
    });
}

static jstring NativePage_nativeGetLinkURI(JNIEnv *env, jclass, jlong doc_ptr,
                                                      jlong link_ptr) {
    return runSafe(env, (jstring) nullptr, [&]() {
        auto *doc = reinterpret_cast<DocumentFile *>(doc_ptr);
        auto link = reinterpret_cast<FPDF_LINK>(link_ptr);
        FPDF_ACTION action = FPDFLink_GetAction(link);
        if (action == nullptr) {
            return (jstring) nullptr;
        }
        size_t bufferLen = FPDFAction_GetURIPath(doc->pdfDocument, action, nullptr, 0);
        if (bufferLen <= 0) {
            return env->NewStringUTF("");
        }
        std::string uri;
        FPDFAction_GetURIPath(doc->pdfDocument, action, WriteInto(&uri, bufferLen), bufferLen);
        return env->NewStringUTF(uri.c_str());
    });
}

static jfloatArray NativePage_nativeGetLinkRect(JNIEnv *env, jclass, jlong,
                                                       jlong link_ptr) {
    return runSafe(env, (jfloatArray) nullptr, [&]() {
        auto link = reinterpret_cast<FPDF_LINK>(link_ptr);
        FS_RECTF fsRectF;
        FPDF_BOOL result = FPDFLink_GetAnnotRect(link, &fsRectF);

        return rectToFloatArray(env, fsRectF);
    });
}
static jlong NativeDocument_nativeGetBookmarkDestIndex(JNIEnv *env, jobject,
                                                                    jlong doc_ptr,
                                                                    jlong bookmark_ptr) {
    return runSafe(env, (jlong) -1, [&]() {
        auto *doc = reinterpret_cast<DocumentFile *>(doc_ptr);
        auto bookmark = reinterpret_cast<FPDF_BOOKMARK>(bookmark_ptr);

        FPDF_DEST dest = FPDFBookmark_GetDest(doc->pdfDocument, bookmark);
        if (dest == nullptr) {
            return (jlong) -1;
        }
        return (jlong) FPDFDest_GetDestPageIndex(doc->pdfDocument, dest);
    });
}

static jintArray NativeDocument_nativeGetPageCharCounts(JNIEnv *env, jobject,
                                                                 jlong doc_ptr) {
    return runSafe(env, (jintArray) nullptr, [&]() {
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
    });
}

static jlong NativeTextPage_nativeFindStart(JNIEnv *env, jclass,
                                                         jlong text_page_ptr,
                                                         jstring find_what,
                                                         jint flags, jint start_index) {
    return runSafe(env, (jlong) 0, [&]() {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);

        const jchar* raw = env->GetStringChars(find_what, nullptr);
        if (raw == nullptr) {
            // Handle error, possibly throw an exception
            return (jlong) 0;
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
    });
}

static jboolean NativeFindResult_nativeFindNext(JNIEnv *env, jobject,
                                                        jlong find_handle) {
    return runSafe(env, (jboolean) false, [&]() {
        auto findHandle = reinterpret_cast<FPDF_SCHHANDLE>(find_handle);


        auto result = FPDFText_FindNext(findHandle);
        return (jboolean) result;
    });
}

static jboolean NativeFindResult_nativeFindPrev(JNIEnv *env, jobject,
                                                        jlong find_handle) {
    return runSafe(env, (jboolean) false, [&]() {
        auto findHandle = reinterpret_cast<FPDF_SCHHANDLE>(find_handle);


        auto result = FPDFText_FindPrev(findHandle);
        return (jboolean) result;
    });
}

static jint NativeFindResult_nativeGetSchResultIndex(JNIEnv *env, jobject,
                                                                 jlong find_handle) {
    return runSafe(env, 0, [&]() {
        auto findHandle = reinterpret_cast<FPDF_SCHHANDLE>(find_handle);


        auto result = FPDFText_GetSchResultIndex(findHandle);
        return (jint) result;
    });
}

static jint NativeFindResult_nativeGetSchCount(JNIEnv *env, jobject,
                                                           jlong find_handle) {
    return runSafe(env, 0, [&]() {
        auto findHandle = reinterpret_cast<FPDF_SCHHANDLE>(find_handle);


        auto result = FPDFText_GetSchCount(findHandle);
        return (jint) result;
    });
}

static void NativeFindResult_nativeCloseFind(JNIEnv *env, jobject,
                                                         jlong find_handle) {
    runSafe(env, [&]() {
        auto findHandle = reinterpret_cast<FPDF_SCHHANDLE>(find_handle);


        FPDFText_FindClose(findHandle);
    });
}
static jlong NativeTextPage_nativeLoadWebLink(JNIEnv *env, jclass,
                                                           jlong text_page_ptr) {
    return runSafe(env, (jlong) 0, [&]() {
        auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);

        auto handle = FPDFLink_LoadWebLinks(textPage);

        return (jlong) handle;
    });
}

static void NativePageLink_nativeClosePageLink(JNIEnv *env, jclass,
                                                             jlong page_link_ptr) {
    runSafe(env, [&]() {
        auto pageLink = reinterpret_cast<FPDF_PAGELINK>(page_link_ptr);


        FPDFLink_CloseWebLinks(pageLink);
    });
}
static jint NativePageLink_nativeCountWebLinks(JNIEnv *env, jclass,
                                                             jlong page_link_ptr) {
    return runSafe(env, -1, [&]() {
        auto pageLink = reinterpret_cast<FPDF_PAGELINK>(page_link_ptr);


        auto result =  FPDFLink_CountWebLinks(pageLink);
        LOGE("CountWebLinks result %d", result);
        return (jint) result;
    });
}
static jint NativePageLink_nativeGetURL(JNIEnv *env, jclass,
                                                      jlong page_link_ptr, jint index, jint count, jbyteArray result) {
    return runSafe(env, 0, [&]() {
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
    });
}
static jint NativePageLink_nativeCountRects(JNIEnv *env, jclass,
                                                          jlong page_link_ptr, jint index) {
    return runSafe(env, 0, [&]() {
        auto pageLink = reinterpret_cast<FPDF_PAGELINK>(page_link_ptr);


        auto result = FPDFLink_CountRects(pageLink, index);
        LOGE("CountRect %d", result);

        return result;
    });
}
static jfloatArray NativePageLink_nativeGetRect(JNIEnv *env, jclass,
                                                       jlong page_link_ptr, jint linkIndex, jint rectIndex) {
    return runSafe(env, (jfloatArray) nullptr, [&]() {
        auto pageLink = reinterpret_cast<FPDF_PAGELINK>(page_link_ptr);

        double left;
        double top;
        double right;
        double bottom;

        if (FPDFLink_GetRect(pageLink, linkIndex, rectIndex, &left, &top, &right, &bottom )) {
            jfloatArray result = env->NewFloatArray(RECT_VALUES_LEN);
            if (result == nullptr) {
                return (jfloatArray) nullptr;
            }
            jfloat array[RECT_VALUES_LEN];
            array[0] = (float) left;
            array[1] = (float) top;
            array[2] = (float) right;
            array[3] = (float) bottom;

            env->SetFloatArrayRegion(result, 0, RECT_VALUES_LEN, array);
            return result;
        }
        return (jfloatArray) nullptr;
    });
}
static jintArray NativePageLink_nativeGetTextRange(JNIEnv *env, jclass,
                                                            jlong page_link_ptr, jint index) {
    return runSafe(env, (jintArray) nullptr, [&]() {
        auto pageLink = reinterpret_cast<FPDF_PAGELINK>(page_link_ptr);

        if (pageLink == nullptr) {
            LOGE("PageLink is null");

            jniThrowException(env, "java/lang/IllegalStateException",
                              "Document is null");
            return (jintArray) nullptr;
        }

        int start, count;
        int result = FPDFLink_GetTextRange(pageLink, index, &start, &count);

        if (result == 0) {
            start = 0;
            count = 0;
        }

        jintArray retVal = env->NewIntArray(2);
        if (retVal == nullptr) {
            return (jintArray) nullptr;
        }

        jint buffer[] = {start, count};
        env->SetIntArrayRegion(retVal, 0, 2, buffer);

        return retVal;
    });
}


static jdoubleArray NativeTextPage_nativeTextGetRects(JNIEnv *env, jclass clazz,
                                                            jlong text_page_ptr,
                                                            jintArray wordRanges) {
    auto textPage = reinterpret_cast<FPDF_TEXTPAGE>(text_page_ptr);

    jsize numRanges = env->GetArrayLength(wordRanges) / 2;

    // Get the ranges array
    jint *ranges = env->GetIntArrayElements(wordRanges, nullptr);

    // Create a vector to store the data
    std::vector<double> data;

    // Iterate through the ranges
    for (jsize i = 0; i < numRanges; ++i) {
        // Get the start and length
        jint start = ranges[i * 2];
        jint length = ranges[i * 2 + 1];

        // Get the number of rectangles in the range
        int rectCount = FPDFText_CountRects(textPage, start, length);

        // Get the rectangles
        for (int j = 0; j < rectCount; ++j) {
            double left, top, right, bottom;
            FPDFText_GetRect(textPage, j, &left, &top, &right, &bottom);

            // Add the rectangle to the data vector (left, top, right, bottom)
            data.push_back(left);
            data.push_back(top);
            data.push_back(right);
            data.push_back(bottom);

            // Add the range to the data vector (start, length)
            data.push_back(static_cast<double>(start));
            data.push_back(static_cast<double>(length));
        }
    }

    // Release the ranges array
    env->ReleaseIntArrayElements(wordRanges, ranges, JNI_ABORT);

    // Create a jdoubleArray and copy the data
    jdoubleArray result = env->NewDoubleArray(static_cast<jsize>(data.size()));
    if (result == nullptr) {
        return nullptr; // Out of memory error
    }
    env->SetDoubleArrayRegion(result, 0, static_cast<jsize>(data.size()), data.data());

    return result;
}

static jint NativePage_nativeGetPageRotation(JNIEnv *env, jclass,
                                                           jlong page_ptr) {
    return runSafe(env, -1, [&]() {
        auto page = reinterpret_cast<FPDF_PAGE>(page_ptr);
        return (jint)FPDFPage_GetRotation(page);
    });
}

static const JNINativeMethod coreMethods[] = {
        {"nativeOpenDocument",       "(ILjava/lang/String;)J",                                                        (void *) NativeCore_nativeOpenDocument},
        {"nativeOpenMemDocument",    "([BLjava/lang/String;)J",                                                       (void *) NativeCore_nativeOpenMemDocument},
        {"nativeOpenCustomDocument", "(Lio/legere/pdfiumandroid/util/PdfiumNativeSourceBridge;Ljava/lang/String;J)J", (void *) NativeCore_nativeOpenCustomDocument},
};


static const JNINativeMethod pageMethods[] = {
        {"nativeClosePage",                  "(J)V",                                   (void *) NativePage_nativeClosePage},
        {"nativeClosePages",                 "([J)V",                                  (void *) NativePage_nativeClosePages},
        {"nativeGetDestPageIndex",           "(JJ)I",                                  (void *) NativePage_nativeGetDestPageIndex},
        {"nativeGetLinkURI",                 "(JJ)Ljava/lang/String;",                 (void *) NativePage_nativeGetLinkURI},
        {"nativeGetLinkRect",                "(JJ)[F",                                 (void *) NativePage_nativeGetLinkRect},
        {"nativeLockSurface",                "(Landroid/view/Surface;[I[J)Z",          (void *) NativePage_nativeLockSurface},
        {"nativeUnlockSurface",              "([J)V",                                  (void *) NativePage_nativeUnlockSurface},
        {"nativeRenderPage",                 "(JJIIIIZII)Z",                           (void *) NativePage_nativeRenderPage},
        {"nativeRenderPageSurface",                 "(JLandroid/view/Surface;IIZII)Z",      (void *) NativePage_nativeRenderPageSurface},
        {"nativeRenderPageWithMatrix",       "(JJII[F[FZZII)Z",                        (void *) NativePage_nativeRenderPageWithMatrix},
        {"nativeRenderPageSurfaceWithMatrix",       "(JLandroid/view/Surface;[F[FZZII)Z",   (void *) NativePage_nativeRenderPageSurfaceWithMatrix},
        {"nativeRenderPageBitmap",           "(JJLandroid/graphics/Bitmap;IIIIZZII)V", (void *) NativePage_nativeRenderPageBitmap},
        {"nativeRenderPageBitmapWithMatrix", "(JLandroid/graphics/Bitmap;[F[FZZII)V",  (void *) NativePage_nativeRenderPageBitmapWithMatrix},
        {"nativeGetPageSizeByIndex",         "(JII)[I",                                (void *) NativePage_nativeGetPageSizeByIndex},
        {"nativeGetPageLinks",               "(J)[J",                                  (void *) NativePage_nativeGetPageLinks},
        {"nativePageCoordsToDevice",         "(JIIIIIDD)[I",                           (void *) NativePage_nativePageCoordsToDevice},
        {"nativeDeviceCoordsToPage",         "(JIIIIIII)[F",                           (void *) NativePage_nativeDeviceCoordsToPage},
        {"nativeGetPageWidthPixel",          "(JI)I",                                  (void *) NativePage_nativeGetPageWidthPixel},
        {"nativeGetPageHeightPixel",         "(JI)I",                                  (void *) NativePage_nativeGetPageHeightPixel},
        {"nativeGetPageWidthPoint",          "(J)I",                                   (void *) NativePage_nativeGetPageWidthPoint},
        {"nativeGetPageHeightPoint",         "(J)I",                                   (void *) NativePage_nativeGetPageHeightPoint},
        {"nativeGetPageRotation",            "(J)I",                                   (void *) NativePage_nativeGetPageRotation},
        {"nativeGetPageMediaBox",            "(J)[F",                                  (void *) NativePage_nativeGetPageMediaBox},
        {"nativeGetPageCropBox",             "(J)[F",                                  (void *) NativePage_nativeGetPageCropBox},
        {"nativeGetPageBleedBox",            "(J)[F",                                  (void *) NativePage_nativeGetPageBleedBox},
        {"nativeGetPageTrimBox",             "(J)[F",                                  (void *) NativePage_nativeGetPageTrimBox},
        {"nativeGetPageArtBox",              "(J)[F",                                  (void *) NativePage_nativeGetPageArtBox},
        {"nativeGetPageBoundingBox",         "(J)[F",                                  (void *) NativePage_nativeGetPageBoundingBox},
        {"nativeGetPageMatrix",              "(J)[F",                                  (void *) NativePage_nativeGetPageMatrix},
};


static const JNINativeMethod textPageMethods[] = {

        {"nativeCloseTextPage",         "(J)V",                     (void *) NativeTextPage_nativeCloseTextPage},
        {"nativeTextCountChars",        "(J)I",                     (void *) NativeTextPage_nativeTextCountChars},
        {"nativeTextGetCharBox",        "(JI)[D",                   (void *) NativeTextPage_nativeTextGetCharBox},
        {"nativeTextGetRect",           "(JI)[D",                   (void *) NativeTextPage_nativeTextGetRect},
        {"nativeTextGetRects",           "(J[I)[D",                   (void *) NativeTextPage_nativeTextGetRects},
        {"nativeTextGetBoundedText",    "(JDDDD[S)I",               (void *) NativeTextPage_nativeTextGetBoundedText},
        {"nativeFindStart",             "(JLjava/lang/String;II)J", (void *) NativeTextPage_nativeFindStart},
        {"nativeLoadWebLink",           "(J)J",                     (void *) NativeTextPage_nativeLoadWebLink},
        {"nativeTextGetCharIndexAtPos", "(JDDDD)I",                 (void *) NativeTextPage_nativeTextGetCharIndexAtPos},
        {"nativeTextGetText",           "(JII[S)I",                 (void *) NativeTextPage_nativeTextGetText},
        {"nativeTextGetTextByteArray",  "(JII[B)I",                 (void *) NativeTextPage_nativeTextGetTextByteArray},
        {"nativeTextGetUnicode",        "(JI)I",                    (void *) NativeTextPage_nativeTextGetUnicode},
        {"nativeTextCountRects",        "(JII)I",                   (void *) NativeTextPage_nativeTextCountRects},
        {"nativeGetFontSize",           "(JI)D",                    (void *) NativeTextPage_nativeGetFontSize},
};

static const JNINativeMethod documentMethods[] = {
        {"nativeGetPageCount",          "(J)I",                                            (void *) NativeDocument_nativeGetPageCount},
        {"nativeLoadPage",              "(JI)J",                                           (void *) NativeDocument_nativeLoadPage},
        {"nativeDeletePage",            "(JI)V",                                           (void *) NativeDocument_nativeDeletePage},
        {"nativeCloseDocument",         "(J)V",                                            (void *) NativeDocument_nativeCloseDocument},
        {"nativeLoadPages",             "(JII)[J",                                         (void *) NativeDocument_nativeLoadPages},
        {"nativeGetDocumentMetaText",   "(JLjava/lang/String;)Ljava/lang/String;",         (void *) NativeDocument_nativeGetDocumentMetaText},
        {"nativeGetFirstChildBookmark", "(JJ)J",                                           (void *) NativeDocument_nativeGetFirstChildBookmark},
        {"nativeGetSiblingBookmark",    "(JJ)J",                                           (void *) NativeDocument_nativeGetSiblingBookmark},
        {"nativeGetBookmarkDestIndex",  "(JJ)J",                                           (void *) NativeDocument_nativeGetBookmarkDestIndex},
        {"nativeLoadTextPage",          "(JJ)J",                                           (void *) NativeDocument_nativeLoadTextPage},
        {"nativeGetBookmarkTitle",      "(J)Ljava/lang/String;",                           (void *) NativeDocument_nativeGetBookmarkTitle},
        {"nativeSaveAsCopy",            "(JLio/legere/pdfiumandroid/PdfWriteCallback;I)Z", (void *) NativeDocument_nativeSaveAsCopy},
        {"nativeGetPageCharCounts",     "(J)[I",                                           (void *) NativeDocument_nativeGetPageCharCounts},
        {"nativeRenderPagesWithMatrix", "([JJII[F[FZZII)V",                                (void *) NativeDocument_nativeRenderPagesWithMatrix},
        {"nativeRenderPagesSurfaceWithMatrix", "([JLandroid/view/Surface;[F[FZZII)Z",           (void *) NativeDocument_nativeRenderPagesSurfaceWithMatrix},
};

static const JNINativeMethod findResultMethods[] = {
        {"nativeFindNext",          "(J)Z", (void *) NativeFindResult_nativeFindNext},
        {"nativeFindPrev",          "(J)Z", (void *) NativeFindResult_nativeFindPrev},
        {"nativeGetSchResultIndex", "(J)I", (void *) NativeFindResult_nativeGetSchResultIndex},
        {"nativeGetSchCount",       "(J)I", (void *) NativeFindResult_nativeGetSchCount},
        {"nativeCloseFind",         "(J)V", (void *) NativeFindResult_nativeCloseFind},

};

static const JNINativeMethod pageLinkMethods[] = {
        {"nativeClosePageLink", "(J)V",     (void *) NativePageLink_nativeClosePageLink},
        {"nativeCountWebLinks", "(J)I",     (void *) NativePageLink_nativeCountWebLinks},
        {"nativeGetURL",        "(JII[B)I", (void *) NativePageLink_nativeGetURL},
        {"nativeCountRects",    "(JI)I",    (void *) NativePageLink_nativeCountRects},
        {"nativeGetRect",       "(JII)[F",  (void *) NativePageLink_nativeGetRect},
        {"nativeGetTextRange",  "(JI)[I",   (void *) NativePageLink_nativeGetTextRange},

};

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

    jclass clazz = env->FindClass("io/legere/pdfiumandroid/jni/NativeCore"); // Replace with your class name
    if (clazz == nullptr) {
        return -1;
    }

    if (env->RegisterNatives(clazz, coreMethods, sizeof(coreMethods) / sizeof(coreMethods[0])) < 0) {
        return -1;
    }

    clazz = env->FindClass("io/legere/pdfiumandroid/jni/NativePage"); // Replace with your class name
    if (clazz == nullptr) {
        return -1;
    }

    if (env->RegisterNatives(clazz, pageMethods, sizeof(pageMethods) / sizeof(pageMethods[0])) < 0) {
        return -1;
    }

    clazz = env->FindClass("io/legere/pdfiumandroid/jni/NativeTextPage"); // Replace with your class name
    if (clazz == nullptr) {
        return -1;
    }

    if (env->RegisterNatives(clazz, textPageMethods, sizeof(textPageMethods) / sizeof(textPageMethods[0])) < 0) {
        return -1;
    }

    clazz = env->FindClass("io/legere/pdfiumandroid/jni/NativeDocument"); // Replace with your class name
    if (clazz == nullptr) {
        return -1;
    }

    if (env->RegisterNatives(clazz, documentMethods, sizeof(documentMethods) / sizeof(documentMethods[0])) < 0) {
        return -1;
    }

    clazz = env->FindClass("io/legere/pdfiumandroid/jni/NativeFindResult"); // Replace with your class name
    if (clazz == nullptr) {
        return -1;
    }

    if (env->RegisterNatives(clazz, findResultMethods, sizeof(findResultMethods) / sizeof(findResultMethods[0])) < 0) {
        return -1;
    }

    clazz = env->FindClass("io/legere/pdfiumandroid/jni/NativePageLink"); // Replace with your class name
    if (clazz == nullptr) {
        return -1;
    }

    if (env->RegisterNatives(clazz, pageLinkMethods, sizeof(pageLinkMethods) / sizeof(pageLinkMethods[0])) < 0) {
        return -1;
    }

    return JNI_VERSION_1_6;
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
