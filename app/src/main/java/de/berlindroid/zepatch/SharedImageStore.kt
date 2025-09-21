package de.berlindroid.zepatch

import android.net.Uri

/**
 * Simple in-memory holder to pass a one-shot shared image Uri from Activity/navigation
 * into the PatchableDetail composable where the WizardViewModel lives.
 */
object SharedImageStore {
    @Volatile
    private var pendingUri: Uri? = null

    fun put(uri: Uri?) {
        pendingUri = uri
    }

    fun take(): Uri? {
        val uri = pendingUri
        pendingUri = null
        return uri
    }
}
