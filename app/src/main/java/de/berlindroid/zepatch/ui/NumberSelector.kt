package de.berlindroid.zepatch.ui

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.text.isDigitsOnly

@Composable
fun NumberSelector(
    value: String,
    label: String,
    errorText: String,
    acceptableNumberEntered: (String) -> Boolean,
    onNumberChanged: (Int) -> Unit,
    onDone: () -> Unit
) {
    var intermediateValue by rememberSaveable { mutableStateOf(value) }

    TextField(
        value = intermediateValue,
        onValueChange = { new ->
            // Allow empty and numeric-only input
            if (new.isEmpty()) {
                intermediateValue = ""
            } else if (new.isDigitsOnly()) {
                intermediateValue = new
                new.toIntOrNull()?.let { onNumberChanged(it) }
            }
            // ignore non-digit edits
        },
        label = { Text(text = label) },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (acceptableNumberEntered(intermediateValue)) {
                    onDone()
                }
            }
        ),
        isError = !acceptableNumberEntered(intermediateValue),
        supportingText = {
            if (!acceptableNumberEntered(intermediateValue)) {
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}
