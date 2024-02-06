package io.legere.pdfiumandroidkt

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.legere.pdfiumandroid.LoggerInterface
import io.legere.pdfiumandroid.suspend.PdfDocumentKt
import io.legere.pdfiumandroid.suspend.PdfiumCoreKt
import io.legere.pdfiumandroid.util.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    val state: StateFlow<UiState>
    val accept: (UiAction) -> Unit

    private val pdfiumCore = PdfiumCoreKt(
        Dispatchers.Default,
        Config().copy(
            logger = object : LoggerInterface {
                override fun d(tag: String, message: String?) {
                    Timber.tag(tag).d(message)
                }

                override fun e(tag: String, t: Throwable?, message: String?) {
                    Timber.tag(tag).e(t, message)
                }
            }
        )
    )
    private var pdfDocument: PdfDocumentKt? = null

    init {
        val actionStateFlow = MutableStateFlow<UiAction>(UiAction.Init())

        state = actionStateFlow.flatMapLatest { action ->
            flow {
                when (action) {
                    is UiAction.Init -> emit(UiState(loadState = LoadStatus.Init))
                    is UiAction.LoadDoc -> {
                        emit(UiState(loadState = LoadStatus.Loading))
                        val fd = application.contentResolver.openFileDescriptor(action.uri, "r")
                        if (fd != null) {
                            try {
                                pdfDocument = pdfiumCore.newDocument(fd)
                                val pageCount = pdfDocument?.getPageCount() ?: 0
                                Timber.d("pageCount: $pageCount")
                                emit(
                                    UiState(
                                        loadState = LoadStatus.Success,
                                        pageCount = pageCount
                                    )
                                )
                            } catch (e: IOException) {
                                Timber.e(e)
                                emit(UiState(loadState = LoadStatus.Error))
                            }
                        } else {
                            emit(UiState(loadState = LoadStatus.Error))
                        }
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, UiState())

        accept = { action ->
            viewModelScope.launch {
                actionStateFlow.emit(action)
            }
        }
    }

    @Suppress("NestedBlockDepth", "TooGenericExceptionCaught")
    suspend fun getPage(pageNum: Int, width: Int, height: Int): Bitmap? {
        Timber.d("getPage: pageNum: $pageNum, width: $width, height: $height")
        try {
            val zoom = 1f // 2f
            val pan = 0f // -width.toFloat() / 2
            val bitmap = Bitmap.createBitmap(width, height * zoom.roundToInt(), Bitmap.Config.RGB_565)
            pdfDocument?.openPage(pageNum)?.use { page ->
                val size = page.getPageSize(1)
                Timber.d("getPageSize: pageNum: $pageNum, width: ${size.width}, height: ${size.height}")
                val pageWidth = page.getPageWidthPoint()
                val pageHeight = page.getPageHeightPoint()
                val tempSrc = RectF(
                    0f,
                    0f,
                    pageWidth.toFloat(),
                    pageHeight.toFloat()
                )
                val tempDst = RectF(0f, 0f, width.toFloat(), height.toFloat())
                val result = Matrix()
                result.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.START)
                result.postScale(zoom, zoom)
                result.postTranslate(pan, 0f)

                page.renderPageBitmap(
                    bitmap,
                    result,
                    RectF(0f, 0f, width.toFloat(), zoom * height.toFloat()),
                    renderAnnot = true
                )
                page.openTextPage().use { textPage ->
                    val charCount = textPage.textPageCountChars()
                    if (charCount > 0) {
                        val text = textPage.textPageGetText(0, charCount)
                        Timber.d("text: $text")
//                val text2 = textPage.textPageGetText2(0, textPage.textPageCountChars())
//                Timber.d("text2: $text2")
//                if (text != text2) {
//                    Timber.e("text != text2")
//                }
                    }
                }
            }
            Timber.d("finished getPage $pageNum")
            return bitmap
        } catch (e: Exception) {
            Timber.e(e)
        }
        return null
    }

    sealed class UiAction {
        data class LoadDoc(val uri: Uri) : UiAction()
        data class Init(@Suppress("unused") val s: String = "Init") : UiAction()
    }

    enum class LoadStatus {
        Init,
        Loading,
        Success,
        Error
    }

    data class UiState(
        val loadState: LoadStatus = LoadStatus.Init,
        val pageCount: Int = 0
    )
}
