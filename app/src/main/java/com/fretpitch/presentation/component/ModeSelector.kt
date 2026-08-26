package com.fretpitch.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fretpitch.R
import com.fretpitch.domain.model.AppMode
import com.fretpitch.domain.model.GuitarString
import com.fretpitch.domain.model.Note
import com.fretpitch.presentation.util.nameResId

@Composable
fun ModeSelector(
    currentMode: AppMode,
    includeSharps: Boolean,
    onModeChange: (AppMode) -> Unit,
    onSharpsToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = currentMode is AppMode.OneNote,
                onClick = {
                    if (currentMode !is AppMode.OneNote) {
                        onModeChange(AppMode.OneNote(Note.E))
                    }
                },
                label = {
                    Text(
                        text = stringResource(R.string.mode_one_note),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                },
                modifier = Modifier.weight(0.85f)
            )
            FilterChip(
                selected = currentMode is AppMode.OneString,
                onClick = {
                    if (currentMode !is AppMode.OneString) {
                        onModeChange(AppMode.OneString(GuitarString.STRING_1))
                    }
                },
                label = {
                    Text(
                        text = stringResource(R.string.mode_one_string),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                },
                modifier = Modifier.weight(1.3f)
            )
            FilterChip(
                selected = currentMode is AppMode.All,
                onClick = {
                    if (currentMode !is AppMode.All) {
                        onModeChange(AppMode.All)
                    }
                },
                label = {
                    Text(
                        text = stringResource(R.string.mode_all),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                },
                modifier = Modifier.weight(0.85f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (currentMode) {
            is AppMode.OneNote -> {
                NotePicker(
                    selectedNote = currentMode.note,
                    includeSharps = includeSharps,
                    onNoteSelected = { onModeChange(AppMode.OneNote(it)) }
                )
            }
            is AppMode.OneString -> {
                StringPicker(
                    selectedString = currentMode.guitarString,
                    onStringSelected = { onModeChange(AppMode.OneString(it)) }
                )
            }
            is AppMode.All -> { }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.label_sharps),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Switch(
                checked = includeSharps,
                onCheckedChange = onSharpsToggle
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotePicker(
    selectedNote: Note,
    includeSharps: Boolean,
    onNoteSelected: (Note) -> Unit
) {
    val notes = if (includeSharps) Note.allNotes() else Note.naturalNotes()
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = stringResource(selectedNote.nameResId()),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_note)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            notes.forEach { note ->
                DropdownMenuItem(
                    text = { Text(stringResource(note.nameResId())) },
                    onClick = {
                        onNoteSelected(note)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StringPicker(
    selectedString: GuitarString,
    onStringSelected: (GuitarString) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = stringResource(R.string.string_format, selectedString.number),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_string)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            GuitarString.all().forEach { string ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.string_format, string.number)) },
                    onClick = {
                        onStringSelected(string)
                        expanded = false
                    }
                )
            }
        }
    }
}
