package com.smile.u2bkktool

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.smile.karaoke.ui.theme.KaraokePlayerTheme
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bkaraoke.model.Song
import com.smile.u2bkktool.retrofit.U2bKkToolRestApiSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

private const val DATE_PATTERN = "yyyy-MM-dd"

class EditSongActivity : AppCompatActivity() {

    companion object {
        const val SONG_EXTRA = "com.smile.u2bkktool.SONG"
        private const val TAG = "EditSongActivity"
    }

    private lateinit var song: Song

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        song = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(SONG_EXTRA, Song::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(SONG_EXTRA)
        } ?: run {
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {
            KaraokePlayerTheme {
                Scaffold { innerPadding ->
                    EditSongScreen(
                        modifier = Modifier.padding(innerPadding),
                        initialSong = song,
                        onCancel = {
                            setResult(RESULT_CANCELED)
                            finish()
                        },
                        onSave = ::saveSong
                    )
                }
            }
        }
    }

    private fun saveSong(editedSong: Song, onComplete: (Boolean) -> Unit) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                U2bKkToolRestApiSync.getApiSync().updateOneSong(editedSong)
            }
            val succeeded = result == 1
            if (succeeded) {
                setResult(RESULT_OK)
                finish()
            } else {
                LogUtil.e(TAG, "saveSong.failed for song id ${editedSong.id}")
                onComplete(false)
            }
        }
    }
}

@Composable
private fun EditSongScreen(
    modifier: Modifier = Modifier,
    initialSong: Song,
    onCancel: () -> Unit,
    onSave: (Song, (Boolean) -> Unit) -> Unit
) {
    val succeededMsg = "Succeeded"
    val failedMsg = "Failed"
    val dateFormat = remember { SimpleDateFormat(DATE_PATTERN, Locale.US) }
    val labels = listOf(
        "Song No.",
        "Song Name",
        "Language",
        "Num_Of_word",
        "Num_fw",
        "Num_pw",
        "Singer 1",
        "Singer 2",
        "Chorus",
        "Vod_yn",
        "Music_track",
        "Vocal_Track",
        "In_Date"
    )
    var values by remember(initialSong) {
        mutableStateOf(
            listOf(
                initialSong.songNo,
                initialSong.songNa,
                initialSong.languageNa,
                initialSong.sNumWord.toString(),
                initialSong.numFw.toString(),
                initialSong.numPw.uppercase(Locale.ROOT).filter { it in 'A'..'Z' }.take(1),
                initialSong.singer1Na,
                initialSong.singer2Na,
                initialSong.chor.uppercase(Locale.ROOT).filter { it == 'Y' || it == 'N' }.take(1),
                initialSong.vodYn.uppercase(Locale.ROOT).filter { it == 'Y' || it == 'N' }.take(1),
                initialSong.nMpeg,
                initialSong.mMpeg,
                dateFormat.format(initialSong.inDate)
            )
        )
    }
    var saving by remember { mutableStateOf(false) }
    var saveStatusMessage by remember { mutableStateOf("") }
    val integerFields = setOf(3, 4)
    val padding = Modifier.padding(horizontal = 16.dp)

    val sNumWord = values[3].toIntOrNull()
    val numFw = values[4].toIntOrNull()
    val inDate = remember(values) {
        runCatching {
            dateFormat.isLenient = false
            dateFormat.parse(values[12].trim())
        }.getOrNull()
    }
    val fieldErrors = values.mapIndexed { index, value ->
        when {
            value.isBlank() -> "${labels[index]} cannot be empty"
            index == 0 && !value.matches(Regex("[0-9]{6}")) ->
                "Song No. must contain exactly 6 digits"
            index == 3 && (sNumWord == null || sNumWord !in 1..25) ->
                "Num_Of_word must be 1 to 25"
            index == 4 && (numFw == null || numFw !in 0..50) ->
                "Num_fw must be between 0 and 50"
            index == 5 && (value.length != 1 || value[0] !in 'A'..'Z') ->
                "Num_pw must be one letter from A to Z"
            index == 8 && value !in setOf("Y", "N") -> "Chorus must be Y or N"
            index == 9 && value !in setOf("Y", "N") -> "Vod_yn must be Y or N"
            index == 12 && inDate == null -> "Invalid date format (yyyy-MM-dd)"
            else -> null
        }
    }
    val isInputValid = fieldErrors.all { it == null }
    val inputErrorMessage = fieldErrors.firstOrNull { it != null }.orEmpty()

    Column(
        modifier = modifier.fillMaxSize().padding(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Edit Song",
            style = MaterialTheme.typography.headlineSmall,
            modifier = padding
        )
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            labels.forEachIndexed { index, label ->
                OutlinedTextField(
                    value = values[index],
                    onValueChange = { newValue ->
                        val inputValue = when (index) {
                            0 -> newValue.filter { it in '0'..'9' }
                            5 -> newValue.uppercase(Locale.ROOT).filter { it in 'A'..'Z' }.take(1)
                            8, 9 -> newValue.uppercase(Locale.ROOT)
                                .filter { it == 'Y' || it == 'N' }
                                .take(1)
                            else -> newValue
                        }
                        values = values.toMutableList().also { it[index] = inputValue }
                        saveStatusMessage = ""
                    },
                    label = { Text(label) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (index == 0 || index in integerFields) KeyboardType.Number
                        else KeyboardType.Text,
                        capitalization = if (index in setOf(5, 8, 9)) KeyboardCapitalization.Characters
                        else KeyboardCapitalization.None
                    ),
                    isError = fieldErrors[index] != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().then(padding)
                )
            }
            Text(
                text = if (isInputValid) saveStatusMessage else inputErrorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = padding
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().then(padding),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (isInputValid) {
                        saving = true
                        val editedSong = initialSong.copy(
                            songNo = values[0],
                            songNa = values[1],
                            languageNa = values[2],
                            sNumWord = checkNotNull(sNumWord),
                            numFw = checkNotNull(numFw),
                            numPw = values[5],
                            singer1Na = values[6],
                            singer2Na = values[7],
                            chor = values[8],
                            vodYn = values[9],
                            nMpeg = values[10],
                            mMpeg = values[11],
                            inDate = checkNotNull(inDate)
                        )
                        onSave(editedSong) { succeeded ->
                            saving = false
                            saveStatusMessage = if (succeeded) succeededMsg else failedMsg
                        }
                    }
                },
                enabled = !saving && isInputValid,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Save")
            }
            Button(
                onClick = onCancel,
                enabled = !saving,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Cancel")
            }
        }
    }
}
