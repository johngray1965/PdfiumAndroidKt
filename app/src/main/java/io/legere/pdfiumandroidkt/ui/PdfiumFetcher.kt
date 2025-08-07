package io.legere.pdfiumandroidkt.ui

import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import io.legere.pdfiumandroidkt.MainViewModel
import timber.log.Timber

class PdfiumFetcher(
    private val data: PdfiumFetcherData,
    private val options: Options,
) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        Timber.d("fetch: ${data.page}")
        val bitmap = data.viewModel.getPage(data.page, data.width, data.height)
        if (bitmap == null) {
            Timber.d("fetch: bitmap is null")
            return null
        }
        return DrawableResult(
            drawable = bitmap.toDrawable(options.context.resources),
            isSampled = false,
            dataSource = DataSource.MEMORY,
        )
    }

    class Factory : Fetcher.Factory<PdfiumFetcherData> {
        override fun create(
            data: PdfiumFetcherData,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher = PdfiumFetcher(data, options)
    }
}

data class PdfiumFetcherData(
    val page: Int,
    val width: Int,
    val height: Int,
    val density: Int,
    val viewModel: MainViewModel,
)
