package io.legere.pdfiumandroidkt

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.legere.pdfiumandroid.suspend.PdfDocumentKt
import io.legere.pdfiumandroid.suspend.PdfiumCoreKt
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
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    val state: StateFlow<UiState>
    val accept: (UiAction) -> Unit

    private val pdfiumCore = PdfiumCoreKt(Dispatchers.Default)
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
                            pdfDocument = pdfiumCore.newDocument(fd)
                            val pageCount = pdfDocument?.getPageCount() ?: 0
                            Timber.d("pageCount: $pageCount")
                            emit(
                                UiState(
                                    loadState = LoadStatus.Success,
                                    pageCount = pageCount
                                )
                            )
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

    suspend fun getPage(pageNum: Int, width: Int, height: Int, density: Int): Bitmap {
        Timber.d("getPage: pageNum: $pageNum, width: $width, height: $height, density: $density")
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        pdfDocument?.openPage(pageNum)?.use { page ->
            page.renderPageBitmap(bitmap, 0, 0, width, height, density)
            pdfDocument?.openTextPage(page)?.use { textPage ->
                val text = textPage.textPageGetText(0, textPage.textPageCountChars())
                Timber.d("text: $text")
//                val text2 = textPage.textPageGetText2(0, textPage.textPageCountChars())
//                Timber.d("text2: $text2")
//                if (text != text2) {
//                    Timber.e("text != text2")
//                }
            }
        }
        Timber.d("finsished getPage $pageNum")
        return bitmap
    }

    sealed class UiAction {
        data class LoadDoc(val uri: Uri) : UiAction()
        data class Init(val s: String = "Init") : UiAction()
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
