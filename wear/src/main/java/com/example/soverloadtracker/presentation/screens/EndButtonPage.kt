package com.example.soverloadtracker.presentation.screens

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextButton
import com.example.soverloadtracker.presentation.PhoneListenerService


/**
 * Page to mark the end of a log.
 * @param nextButtonOnClick Callback to move to the next page (back to start)
 */
@Composable
fun EndButton(nextButtonOnClick: () -> Unit) {
    val context = LocalContext.current

    TextButton(
        onClick = {
            PhoneListenerService.sendMarkEnd(context)
            nextButtonOnClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Text("Mark End", textAlign = TextAlign.Center)
    }
}
