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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.smile.u2bkaraoke.model.Language
import com.smile.u2bkaraoke.model.Singer
import com.smile.u2bkaraoke.model.Song
import com.smile.u2bkaraoke.retrofit.U2bKkRestApiSync
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
    val songNoIndex = 0
    val songNaIndex = 1
    val langIndex = 2
    val numOfWordIndex = 3
    val numFwIndex = 4
    val numPwIndex = 5
    val sing1Index = 6
    val sing2Index = 7
    val chorIndex = 8
    val vodYnIndex = 9
    val musicTrackIndex = 10
    val vocalTrackIndex = 11
    val inDateIndex = 12
    val labels = buildList {
        add(songNoIndex, "Song No.")
        add(songNaIndex, "Song Name")
        add(langIndex, "Language")
        add(numOfWordIndex, "Num_Of_word")
        add(numFwIndex, "Num_fw")
        add(numPwIndex, "Num_pw")
        add(sing1Index, "Singer 1")
        add(sing2Index, "Singer 2")
        add(chorIndex, "Chorus")
        add(vodYnIndex, "Vod_yn")
        add(musicTrackIndex, "Music_track")
        add(vocalTrackIndex, "Vocal_Track")
        add(inDateIndex, "In_Date")
    }
    var values by remember(initialSong) {
        mutableStateOf(
            buildList {
                add(songNoIndex, initialSong.songNo)
                add(songNaIndex, initialSong.songNa)
                add(langIndex, initialSong.languageNa)
                add(numOfWordIndex, initialSong.sNumWord.toString())
                add(numFwIndex, initialSong.numFw.toString())
                add(numPwIndex, initialSong.numPw.uppercase(Locale.ROOT).filter { it in 'A'..'Z' }.take(1))
                add(sing1Index, initialSong.singer1Na)
                add(sing2Index, initialSong.singer2Na)
                add(chorIndex, initialSong.chor.uppercase(Locale.ROOT).filter { it == 'Y' || it == 'N' }.take(1))
                add(vodYnIndex, initialSong.vodYn.uppercase(Locale.ROOT).filter { it == 'Y' || it == 'N' }.take(1))
                add(musicTrackIndex, initialSong.nMpeg)
                add(vocalTrackIndex, initialSong.mMpeg)
                add(inDateIndex, dateFormat.format(initialSong.inDate))
            }
        )
    }
    var languages by remember { mutableStateOf<List<Language>>(emptyList()) }
    var singers by remember { mutableStateOf<List<Singer>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            runCatching {
                val langList = U2bKkRestApiSync.getApiSync().getAllLanguages()?.languages
                if (!langList.isNullOrEmpty()) {
                    languages = langList.filter { it.langNa.isNotBlank() }
                }
                val singerList = U2bKkRestApiSync.getApiSync().getSingers(10000, 1, "SingNa", null)?.singers
                if (!singerList.isNullOrEmpty()) {
                    singers = singerList.filter { it.singNa.isNotBlank() }
                }
            }
        }
    }

    var saving by remember { mutableStateOf(false) }
    var saveStatusMessage by remember { mutableStateOf("") }
    val integerFields = setOf(numOfWordIndex, numFwIndex)
    val padding = Modifier.padding(horizontal = 16.dp)

    val sNumWord = values[numOfWordIndex].toIntOrNull()
    val numFw = values[numFwIndex].toIntOrNull()
    val inDate = remember(values) {
        runCatching {
            dateFormat.isLenient = false
            dateFormat.parse(values[inDateIndex].trim())
        }.getOrNull()
    }
    val fieldErrors = values.mapIndexed { index, value ->
        val trimmedValue = value.trim()
        when {
            trimmedValue.isBlank() -> "${labels[index]} cannot be empty"
            index == songNoIndex && !trimmedValue.matches(Regex("[0-9]{6}")) ->
                "Song No. must contain 6 digits"
            index == langIndex && languages.isNotEmpty() && languages.none { it.langNa.equals(trimmedValue, ignoreCase = true) } ->
                "Language does not exist"
            index == numOfWordIndex && (sNumWord == null || sNumWord !in 1..25) ->
                "Num_Of_word must be 1 to 25"
            index == numFwIndex && (numFw == null || numFw !in 0..50) ->
                "Num_fw must be between 0 and 50"
            index == numPwIndex && (trimmedValue.length != 1 || trimmedValue[0] !in 'A'..'Z') ->
                "Num_pw must be one letter from A to Z"
            index == sing1Index && singers.isNotEmpty() && singers.none { it.singNa.equals(trimmedValue, ignoreCase = true) } ->
                "Singer 1 does not exist"
            index == sing2Index && singers.isNotEmpty() && singers.none { it.singNa.equals(trimmedValue, ignoreCase = true) } ->
                "Singer 2 does not exist"
            index == chorIndex && trimmedValue !in setOf("Y", "N") -> "Chorus must be Y or N"
            index == vodYnIndex && trimmedValue !in setOf("Y", "N") -> "Vod_yn must be Y or N"
            index == musicTrackIndex && !trimmedValue.matches(Regex("[0-9]{2}")) ->
                "Music_track must contain 2 digits"
            index == vocalTrackIndex && !trimmedValue.matches(Regex("[0-9]{2}")) ->
                "Vocal_Track must contain 2 digits"
            index == inDateIndex && inDate == null -> "Invalid date format (yyyy-MM-dd)"
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
                if (index in setOf(langIndex, sing1Index, sing2Index)) {
                    val options = if (index == langIndex) {
                        languages.map { it.langNa }.filter { it.isNotBlank() }.distinct()
                    } else {
                        singers.map { it.singNa }.filter { it.isNotBlank() }.distinct()
                    }
                    SearchableDropdownField(
                        value = values[index],
                        onValueChange = { selectedValue ->
                            values = values.toMutableList().also { it[index] = selectedValue }
                            saveStatusMessage = ""
                        },
                        label = label,
                        options = options,
                        isError = fieldErrors[index] != null,
                        modifier = Modifier.fillMaxWidth().then(padding)
                    )
                } else {
                    OutlinedTextField(
                        value = values[index],
                        onValueChange = { newValue ->
                            val inputValue = when (index) {
                                songNoIndex -> newValue.filter { it in '0'..'9' }.take(6)
                                numPwIndex -> newValue.uppercase(Locale.ROOT).filter { it in 'A'..'Z' }.take(1)
                                chorIndex, vodYnIndex -> newValue.uppercase(Locale.ROOT)
                                    .filter { it == 'Y' || it == 'N' }
                                    .take(1)
                                musicTrackIndex, vocalTrackIndex -> newValue.filter { it in '0'..'9' }.take(2)
                                else -> newValue
                            }
                            values = values.toMutableList().also { it[index] = inputValue }
                            saveStatusMessage = ""
                        },
                        label = { Text(label) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (index == songNoIndex || index in integerFields || index in setOf(musicTrackIndex, vocalTrackIndex)) KeyboardType.Number
                            else KeyboardType.Text,
                            capitalization = if (index in setOf(numPwIndex, chorIndex, vodYnIndex)) KeyboardCapitalization.Characters
                            else KeyboardCapitalization.None
                        ),
                        isError = fieldErrors[index] != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().then(padding)
                    )
                }
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
                        val selectedLang = languages.firstOrNull {
                            it.langNa.equals(values[langIndex].trim(), ignoreCase = true)
                        }
                        val selectedSinger1 = singers.firstOrNull {
                            it.singNa.equals(values[sing1Index].trim(), ignoreCase = true)
                        }
                        val selectedSinger2 = singers.firstOrNull {
                            it.singNa.equals(values[sing2Index].trim(), ignoreCase = true)
                        }

                        val editedSong = initialSong.copy(
                            songNo = values[songNoIndex],
                            songNa = values[songNaIndex],
                            languageId = selectedLang?.id ?: initialSong.languageId,
                            languageNo = selectedLang?.langNo ?: initialSong.languageNo,
                            languageNa = values[langIndex].trim(),
                            sNumWord = checkNotNull(sNumWord),
                            numFw = checkNotNull(numFw),
                            numPw = values[numPwIndex],
                            singer1Id = selectedSinger1?.id ?: initialSong.singer1Id,
                            singer1No = selectedSinger1?.singNo ?: initialSong.singer1No,
                            singer1Na = values[sing1Index].trim(),
                            singer2Id = selectedSinger2?.id ?: initialSong.singer2Id,
                            singer2No = selectedSinger2?.singNo ?: initialSong.singer2No,
                            singer2Na = values[sing2Index].trim(),
                            chor = values[chorIndex],
                            vodYn = values[vodYnIndex],
                            nMpeg = values[musicTrackIndex],
                            mMpeg = values[vocalTrackIndex],
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchableDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val filteredOptions = remember(value, options) {
        if (value.isBlank()) {
            options
        } else {
            options.filter { it.contains(value, ignoreCase = true) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filteredOptions.isNotEmpty(),
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                expanded = true
            },
            label = { Text(label) },
            singleLine = true,
            isError = isError,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded && filteredOptions.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 250.dp)
        ) {
            filteredOptions.take(50).forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
