package de.berlindroid.zepatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import de.berlindroid.zepatch.ui.BitmapToStitches
import de.berlindroid.zepatch.ui.PatchableBoundingBox
import de.berlindroid.zepatch.ui.PatchableToBitmap
import de.berlindroid.zepatch.ui.theme.ZePatchTheme
import kotlinx.coroutines.launch

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
    patchable: @Composable () -> Unit,
) {
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
                actions = {
                    IconButton(onClick = { TODO("Not implemented") }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.Send,
                            contentDescription = "Save",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            var currentMode by remember { mutableStateOf(PatchablePreviewMode.COMPOSABLE) }
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
            ) {
                PatchablePreviewMode.entries.toTypedArray().forEachIndexed { index, mode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = PatchablePreviewMode.entries.size,
                        ),
                        onClick = { currentMode = mode },
                        selected = currentMode == mode,
                        label = { Text(mode.toString()) }
                    )
                }
            }
            when (currentMode) {
                PatchablePreviewMode.COMPOSABLE -> PatchableBoundingBox(patchable = patchable)
                PatchablePreviewMode.BITMAP -> PatchableToBitmap(patchable = patchable)
                PatchablePreviewMode.REDUCED_BITMAP -> PatchableToBitmap(patchable = patchable)
                PatchablePreviewMode.STITCHES -> BitmapToStitches(patchable = patchable)
            }
        }
    }
}

private enum class PatchablePreviewMode {
    COMPOSABLE, BITMAP, REDUCED_BITMAP, STITCHES
}
