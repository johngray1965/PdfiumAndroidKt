@file:Suppress("FunctionNaming")

package io.legere.pdfiumandroidkt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import coil.ComponentRegistry
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import dagger.hilt.android.AndroidEntryPoint
import io.legere.pdfiumandroidkt.ui.PdfiumFetcher
import io.legere.pdfiumandroidkt.ui.PdfiumFetcherData
import io.legere.pdfiumandroidkt.ui.theme.PdfiumAndroidKtTheme
import timber.log.Timber
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val openFileContract =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                viewModel.accept(MainViewModel.UiAction.LoadDoc(uri))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageLoader = ImageLoader.Builder(this)
            .components(fun ComponentRegistry.Builder.() {
                add(PdfiumFetcher.Factory())
            })
            .allowRgb565(true)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.1)
                    .build()
            }
            .build()

        setContent {
            PdfiumAndroidKtTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyUI(viewModel, openFileContract, imageLoader)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyUI(
    viewModel: MainViewModel,
    openFileContract: ActivityResultLauncher<Array<String>>?,
    imageLoader: ImageLoader
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "PdfiumAndroidKt")
                },
                actions = {
                    IconButton(onClick = {
                        openFileContract?.launch(arrayOf("application/pdf"))
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_file_open_24),
                            contentDescription = "Open"
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {
            MainContent(viewModel, imageLoader)
        }
    }
}

@Composable
fun MainContent(viewModel: MainViewModel, imageLoader: ImageLoader) {
    val state = viewModel.state.collectAsState()
    when (state.value.loadState) {
        MainViewModel.LoadStatus.Loading -> MaxSizeCenterBox { CircularProgressIndicator() }
        MainViewModel.LoadStatus.Success -> MyPager(viewModel, imageLoader)
        MainViewModel.LoadStatus.Error -> Message("Error")
        MainViewModel.LoadStatus.Init -> Message("Load PDF")
    }
}

@Composable
private fun MaxSizeCenterBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun Message(text: String = "Error") {
    MaxSizeCenterBox {
        Text(text = text)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyPager(viewModel: MainViewModel, imageLoader: ImageLoader) {
    val state = viewModel.state.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        state.value.pageCount
    }
    var componentWidth by remember { mutableIntStateOf(0) }
    var componentHeight by remember { mutableIntStateOf(0) }

    // get local density from composable
    val density = LocalDensity.current

    Surface(
        modifier = Modifier
            .onGloballyPositioned {
                componentWidth = it.size.width
                componentHeight = it.size.height
            }

    ) {
        VerticalPager(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            state = pagerState,
            pageSpacing = 0.dp,
            userScrollEnabled = true,
            reverseLayout = false,
            contentPadding = PaddingValues(0.dp),
            beyondBoundsPageCount = 0,
            key = null,
            pageNestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
                pagerState,
                Orientation.Horizontal
            ),
            pageContent = {
                PagerScope(
                    page = it,
                    viewModel = viewModel,
                    componentWidth,
                    componentHeight,
                    density,
                    imageLoader
                )
            }
        )
    }
}

@Composable
@Suppress("LongParameterList")
fun PagerScope(
    page: Int,
    viewModel: MainViewModel,
    componentWidth: Int,
    componentHeight: Int,
    density: Density,
    imageLoader: ImageLoader
) {
    if (componentWidth <= 0 || componentHeight <= 0) {
        return
    }

    Timber.d(
        "PagerScope: page: $page, " +
            "componentWidth: $componentWidth, " +
            "componentHeight: $componentHeight, " +
            "density: $density"
    )

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(
                PdfiumFetcherData(
                    page = page,
                    width = componentWidth,
                    height = componentHeight,
                    density = density.density.roundToInt(),
                    viewModel = viewModel
                )
            )
            .memoryCacheKey("page_$page")
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = "Page $page",
        imageLoader = imageLoader,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    )
}
