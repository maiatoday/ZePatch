@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package de.berlindroid.zepatch

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import de.berlindroid.zepatch.ui.PatchableDetail
import de.berlindroid.zepatch.ui.PatchableList
import de.berlindroid.zepatch.ui.theme.ZePatchTheme
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
@ExperimentalMaterial3AdaptiveApi
class MainActivity : ComponentActivity() {

    private fun extractSharedImage(intent: Intent?): Uri? {
        if (intent == null) return null
        if (intent.action == Intent.ACTION_SEND) {
            val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            return uri
        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialShared = extractSharedImage(intent)
        if (initialShared != null) {
            SharedImageStore.put(initialShared)
        }
        enableEdgeToEdge()
        setContent {
            ZePatchTheme {
                val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator()
                val scope = rememberCoroutineScope()

                // If launched via share, navigate directly into the wizard
                androidx.compose.runtime.LaunchedEffect(initialShared) {
                    if (initialShared != null) {
                        patchables.keys.firstOrNull()?.let { name ->
                            scope.launch {
                                scaffoldNavigator.navigateTo(
                                    ListDetailPaneScaffoldRole.Detail,
                                    name,
                                )
                            }
                        }
                    }
                }

                // Gallery picker
                val pickImageLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    if (uri != null) {
                        // Persist permission for long-term access
                        try {
                            contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (_: SecurityException) {
                            // Not from SAF or no persistable flag
                        }
                        SharedImageStore.put(uri)
                        // Navigate to first available patchable
                        patchables.keys.firstOrNull()?.let { name ->
                            scope.launch {
                                scaffoldNavigator.navigateTo(
                                    ListDetailPaneScaffoldRole.Detail,
                                    name,
                                )
                            }
                        }
                    }
                }

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
                                        actions = {
                                            TextButton(onClick = { pickImageLauncher.launch("image/*") }) {
                                                Text("Import image")
                                            }
                                        }
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
                                    Text("404: Patchable for $name not found.")
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

