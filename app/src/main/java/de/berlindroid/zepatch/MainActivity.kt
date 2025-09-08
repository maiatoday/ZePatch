package de.berlindroid.zepatch

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.embroidermodder.punching.Histogram
import de.berlindroid.zepatch.PatchablePreviewMode.BITMAP
import de.berlindroid.zepatch.PatchablePreviewMode.REDUCED_BITMAP
import de.berlindroid.zepatch.PatchablePreviewMode.STITCHES
import de.berlindroid.zepatch.ui.BitmapToStitches
import de.berlindroid.zepatch.ui.PatchableBoundingBox
import de.berlindroid.zepatch.ui.PatchableToBitmap
import de.berlindroid.zepatch.ui.PatchableToReducedBitmap
import de.berlindroid.zepatch.ui.theme.ZePatchTheme
import de.berlindroid.zepatch.utils.multiLet
import de.berlindroid.zepatch.utils.uppercaseWords
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel as lifecycleViewModel

enum class PatchablePreviewMode {
    BITMAP, REDUCED_BITMAP, STITCHES
}

@ExperimentalMaterial3Api
@ExperimentalMaterial3AdaptiveApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZePatchTheme {
                val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator()
                val scope = rememberCoroutineScope()

                NavigableListDetailPaneScaffold(
                    navigator = scaffoldNavigator,
                    listPane = {
                        AnimatedPane {
                            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
                                rememberTopAppBarState()
                            )
                            Scaffold(
                                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                                contentWindowInsets = WindowInsets.statusBars,
                                topBar = {
                                    LargeTopAppBar(
                                        title = { Text("ZePatch") },
                                        scrollBehavior = scrollBehavior,
                                    )
                                },
                            ) { innerPadding ->
                                PatchableList(
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .padding(horizontal = 8.dp),
                                ) { name ->
                                    scope.launch {
                                        scaffoldNavigator.navigateTo(
                                            ListDetailPaneScaffoldRole.Detail,
                                            name,
                                        )
                                    }
                                }
                            }
                        }
                    },
                    detailPane = {
                        AnimatedPane {
                            scaffoldNavigator.currentDestination?.contentKey?.let { name ->
                                val patchable = patchables[name]
                                if (patchable == null) {
                                    Text("404: Patchable for $name not found")
                                } else {
                                    PatchableDetail(
                                        name = name as String,
                                        onBackClick = {
                                            scope.launch { scaffoldNavigator.navigateBack() }
                                        },
                                        patchable = patchable
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PatchableList(
    modifier: Modifier = Modifier,
    onItemClicked: (name: String) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = patchables.toList(),
            key = { it.first },
        ) { (name, patchable) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onItemClicked(name) },
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(name)
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "TODO",
                        )
                    }
                    Box(Modifier.height(4.dp))
                    PatchableBoundingBox(patchable = patchable)
                }
            }
        }
        item(key = "navigationBar") {
            Box(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun PatchableDetail(
    modifier: Modifier = Modifier,
    name: String,
    onBackClick: () -> Unit,
    patchable: @Composable (Boolean, (ImageBitmap) -> Unit) -> Unit,
) {
    val context = LocalContext.current

    val viewModel: WizardViewModel = lifecycleViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Reset wizard state when a different patchable is opened
    LaunchedEffect(name) {
        viewModel.reset()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        savePesAfterSelection(context, result, uiState.embroideryData)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "back",
                        )
                    }
                },
                title = {
                    Text(name)
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            ProgressHeader(
                            title = name,
                            imageBitmap = uiState.imageBitmap,
                            reducedImageBitmap = uiState.reducedImageBitmap,
                            currentMode = uiState.previewMode,
                            onPrev = {
                                viewModel.setPreviewMode(
                                    when (uiState.previewMode) {
                                        PatchablePreviewMode.BITMAP -> PatchablePreviewMode.BITMAP
                                        PatchablePreviewMode.REDUCED_BITMAP -> PatchablePreviewMode.BITMAP
                                        PatchablePreviewMode.STITCHES -> PatchablePreviewMode.REDUCED_BITMAP
                                    }
                                )
                            },
                            onNext = {
                                viewModel.setPreviewMode(
                                    when (uiState.previewMode) {
                                        PatchablePreviewMode.BITMAP -> PatchablePreviewMode.REDUCED_BITMAP
                                        PatchablePreviewMode.REDUCED_BITMAP -> PatchablePreviewMode.STITCHES
                                        PatchablePreviewMode.STITCHES -> uiState.previewMode
                                    }
                                )
                                if (uiState.previewMode == PatchablePreviewMode.BITMAP) viewModel.computeReducedBitmap()
                            }
                        )

            WizardContent(
                uiState.previewMode,
                uiState.imageBitmap,
                uiState.colorCount,
                uiState.reducedImageBitmap,
                uiState.reducedHistogram,
                name,
                onBitmapUpdated = viewModel::updateBitmap,
                onColorCountUpdated = viewModel::updateColorCount,
                computeReducedBitmap = viewModel::computeReducedBitmap,
                patchable = patchable
            )

//            WizardButtons(
//                uiState.previewMode,
//                uiState.imageBitmap,
//                uiState.reducedImageBitmap,
//                uiState.embroideryData,
//                name,
//                launcher
//            ) {
//                viewModel.setPreviewMode(it)
//                if (it == REDUCED_BITMAP) {
//                    viewModel.computeReducedBitmap()
//                }
//            }
        }
    }
}

@Composable
private fun WizardContent(
    currentMode: PatchablePreviewMode,
    imageBitmap: ImageBitmap?,
    colorCount: Int,
    reducedImageBitmap: ImageBitmap?,
    reducedHistogram: Histogram?,
    name: String,
    onBitmapUpdated: (ImageBitmap) -> Unit,
    onColorCountUpdated: (Int) -> Unit,
    computeReducedBitmap: () -> Unit,
    patchable: @Composable (Boolean, (ImageBitmap) -> Unit) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(size = 25.dp),
    ) {
        val scrollState = androidx.compose.foundation.rememberScrollState()
        Column(modifier = Modifier.verticalScroll(scrollState).padding(16.dp)) {
        when (currentMode) {
            BITMAP -> PatchableToBitmap(
                onBitmap = onBitmapUpdated,
                patchable = patchable
            )

            REDUCED_BITMAP -> PatchableToReducedBitmap(
                image = imageBitmap,
                colorCount = colorCount,
                onColorCountChanged = onColorCountUpdated,
                computeReducedBitmap = computeReducedBitmap,
                reducedImage = reducedImageBitmap,
            )

            STITCHES -> reducedImageBitmap?.multiLet(reducedHistogram) { img, histo ->
                val vm: WizardViewModel = lifecycleViewModel()
                val ui by vm.uiState.collectAsStateWithLifecycle()
                BitmapToStitches(
                    reducedImageBitmap = img,
                    reducedHistogram = histo,
                    name = name,
                    onCreateEmbroidery = { n, b, h -> vm.createEmbroidery(n, b, h) },
                    previewImage = ui.embroideryPreviewImage,
                    creatingEmbroidery = ui.creatingEmbroidery,
                )
            }
        }
        }
    }
}

@Composable
private fun ProgressHeader(
    title: String,
    imageBitmap: ImageBitmap?,
    reducedImageBitmap: ImageBitmap?,
    currentMode: PatchablePreviewMode,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Button(onClick = onPrev, enabled = currentMode != BITMAP) { Text("Prev") }
        Text(title)
        Button(onClick = onNext, enabled = when (currentMode) {
            BITMAP -> imageBitmap != null
            REDUCED_BITMAP -> reducedImageBitmap != null
            STITCHES -> false
        }) { Text("Next") }
    }
    //ProgressPillsInternal(imageBitmap, reducedImageBitmap, currentMode)
}

@Composable
private fun ProgressPillsInternal(
    imageBitmap: ImageBitmap?,
    reducedImageBitmap: ImageBitmap?,
    currentMode: PatchablePreviewMode
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        PatchablePreviewMode.entries.toTypedArray().forEachIndexed { index, mode ->
            SegmentedButton(
                modifier = Modifier.height(48.dp),
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = PatchablePreviewMode.entries.size,
                ),
                enabled = when (mode) {
                    BITMAP -> true
                    REDUCED_BITMAP -> imageBitmap != null
                    STITCHES -> reducedImageBitmap != null
                },
                onClick = { },
                selected = currentMode == mode,
                label = { Text(mode.toString().uppercaseWords()) }
            )

        }
    }
}

private fun savePesAfterSelection(context: Context, result: ActivityResult, bytes: ByteArray?) {
    // TODO MOVE ME INTO VM

    if (bytes == null || bytes.isEmpty()) {
        Log.e("NO", "No PES created. Why are we here?")
        return
    }
    Log.i("YOLO?", "File for saving selected. $result")

    result.data?.let { data: Intent ->
        data.data?.let { uri: Uri ->
            val resolver = context.contentResolver.apply {
                takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }

            val outputStream = resolver.openOutputStream(uri)
            try {
                outputStream?.write(bytes)
            } finally {
                outputStream?.close()
            }

            Toast.makeText(
                context,
                "Done writing file! Happy embroidering 🪡",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
