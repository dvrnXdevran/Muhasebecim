package com.muhasebecim.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhasebecim.data.model.GeneralNote
import com.muhasebecim.ui.theme.AccentBlue
import com.muhasebecim.ui.theme.DarkBackground
import com.muhasebecim.ui.theme.DarkSurface
import com.muhasebecim.ui.theme.TextPrimary
import com.muhasebecim.ui.viewmodel.GeneralLedgerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralNotesScreen(viewModel: GeneralLedgerViewModel) {
    val generalNotes by viewModel.generalNotes.collectAsState()

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<GeneralNote?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notlar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddNoteDialog = true },
                containerColor = AccentBlue,
                contentColor = DarkSurface
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Not Ekle",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            GeneralNoteList(
                notes = generalNotes,
                onEdit = { editingNote = it },
                onDelete = viewModel::deleteGeneralNote
            )
        }
    }

    if (showAddNoteDialog) {
        AddGeneralNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            title = "Not Ekle",
            onConfirm = { content ->
                viewModel.addGeneralNote(content)
                showAddNoteDialog = false
            }
        )
    }

    editingNote?.let { note ->
        AddGeneralNoteDialog(
            onDismiss = { editingNote = null },
            title = "Not Duzenle",
            initialContent = note.content,
            confirmLabel = "Kaydet",
            onConfirm = { content ->
                viewModel.updateGeneralNote(note, content)
                editingNote = null
            }
        )
    }
}
