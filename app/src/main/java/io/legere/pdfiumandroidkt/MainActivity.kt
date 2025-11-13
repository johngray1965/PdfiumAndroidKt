@file:Suppress("FunctionNaming", "ktlint:standard:function-naming")

package io.legere.pdfiumandroidkt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import io.legere.pdfiumandroidkt.ui.PdfViewer
import io.legere.pdfiumandroidkt.ui.theme.PdfiumAndroidKtTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val openFileContract =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                viewModel.accept(MainViewModel.UiAction.LoadDoc(uri))
            }
        }

    @Suppress("ktlint:standard:blank-line-before-declaration")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PdfiumAndroidKtTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MyUI(viewModel, openFileContract)
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
                            contentDescription = "Open",
                        )
                    }
                },
            )
        },
    ) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {
            MainContent(viewModel)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun MainContent(viewModel: MainViewModel) {
    val state = viewModel.state.collectAsState()
    when (state.value.loadState) {
        MainViewModel.LoadStatus.Loading -> MaxSizeCenterBox { Message("Loading") }
        MainViewModel.LoadStatus.Success -> MyPager(viewModel)
        MainViewModel.LoadStatus.Error -> Message("Error")
        MainViewModel.LoadStatus.Init -> Message("Load PDF")
    }
}

@Composable
private fun MaxSizeCenterBox(content: @Composable () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        contentAlignment = Alignment.Center,
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

@Composable
fun MyPager(viewModel: MainViewModel) {
    val state = viewModel.state.collectAsState()
    val pagerState =
        rememberPagerState(
            initialPage = 0,
            initialPageOffsetFraction = 0f,
        ) {
            state.value.pageCount
        }
    var componentWidth by remember { mutableIntStateOf(0) }
    var componentHeight by remember { mutableIntStateOf(0) }

    Surface(
        modifier =
            Modifier
                .onGloballyPositioned {
                    componentWidth = it.size.width
                    componentHeight = it.size.height
                },
    ) {
        HorizontalPager(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
            state = pagerState,
            pageSpacing = 0.dp,
            userScrollEnabled = true,
            reverseLayout = false,
            contentPadding = PaddingValues(0.dp),
            key = null,
            pageContent = {
                PagerScope(
                    page = it,
                    viewModel = viewModel,
                    componentWidth,
                    componentHeight,
                )
            },
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
) {
    if (componentWidth <= 0 || componentHeight <= 0) {
        return
    }

    PdfViewer(
        pageCache = viewModel.pageCache,
        pageNum = page,
    )
}
