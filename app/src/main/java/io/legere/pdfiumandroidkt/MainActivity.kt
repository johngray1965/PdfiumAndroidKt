@file:Suppress("FunctionNaming")

package io.legere.pdfiumandroidkt

import android.graphics.Bitmap
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import dagger.hilt.android.AndroidEntryPoint
import io.legere.pdfiumandroidkt.ui.theme.PdfiumAndroidKtTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        setContent {
            PdfiumAndroidKtTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyUI(viewModel, openFileContract)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyUI(viewModel: MainViewModel, openFileContract: ActivityResultLauncher<Array<String>>?) {
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
            MainContent(viewModel)
        }
    }
}

@Composable
fun MainContent(viewModel: MainViewModel) {
    val state = viewModel.state.collectAsState()
    when (state.value.loadState) {
        MainViewModel.LoadStatus.Loading -> MaxSizeCenterBox { CircularProgressIndicator() }
        MainViewModel.LoadStatus.Success -> MyPager(viewModel)
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun MyPager(viewModel: MainViewModel) {
    val state = viewModel.state.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        state.value.pageCount
    }
    rememberCoroutineScope()

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
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            state = pagerState,
            pageSpacing = 0.dp,
            userScrollEnabled = true,
            reverseLayout = false,
            contentPadding = PaddingValues(0.dp),
            beyondBoundsPageCount = 0,
            pageSize = PageSize.Fill,
//            flingBehavior = PagerDefaults.flingBehavior(state = state),
            key = null,
            pageNestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
                Orientation.Horizontal
            ),
            pageContent = {
                PagerScope(page = it, viewModel = viewModel, componentWidth, componentHeight, density)
            }
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PagerScope(
    page: Int,
    viewModel: MainViewModel,
    componentWidth: Int,
    componentHeight: Int,
    density: Density
) {
    var data: Bitmap? by remember { mutableStateOf(null) }

    if (componentWidth <= 0 || componentHeight <= 0) {
        return
    }

    LaunchedEffect(page) {
        val bitmap = withContext(Dispatchers.IO) {
            viewModel.getPage(
                page,
                componentWidth,
                componentHeight,
                density.density.roundToInt()
            )
        }
        data = bitmap
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        GlideImage(
            model = data,
            contentDescription = "Page $page",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentScale = ContentScale.Fit,
        )
    }
}
