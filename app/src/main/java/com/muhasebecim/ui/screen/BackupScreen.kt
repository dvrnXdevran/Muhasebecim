package com.muhasebecim.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhasebecim.data.backup.BackupSnapshot
import com.muhasebecim.ui.theme.AccentBlue
import com.muhasebecim.ui.theme.AccentGreen
import com.muhasebecim.ui.theme.AccentOrange
import com.muhasebecim.ui.theme.AccentRed
import com.muhasebecim.ui.theme.DarkBackground
import com.muhasebecim.ui.theme.DarkCard
import com.muhasebecim.ui.theme.DarkSurface
import com.muhasebecim.ui.theme.DarkSurfaceVariant
import com.muhasebecim.ui.theme.TextMuted
import com.muhasebecim.ui.theme.TextPrimary
import com.muhasebecim.ui.theme.TextSecondary
import com.muhasebecim.ui.util.formatDateTime
import com.muhasebecim.ui.viewmodel.BackupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(viewModel: BackupViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Yedekleme",
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
        containerColor = DarkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                BackupStatusCard(
                    isConfigured = uiState.isConfigured,
                    lastSuccessfulBackupAt = uiState.lastSuccessfulBackupAt,
                    lastStatusMessage = uiState.lastStatusMessage,
                    lastErrorMessage = uiState.lastErrorMessage,
                    isBusy = uiState.isBusy
                )
            }

            item {
                BackupActionCard(
                    isBusy = uiState.isBusy,
                    onBackupNow = viewModel::backupNow,
                    onPrepareRestore = viewModel::prepareRestore
                )
            }

            if (!uiState.isConfigured) {
                item {
                    InfoCard(
                        title = "Sunucu ayari eksik",
                        message = "local.properties icindeki backup.serverBaseUrl ve backup.apiToken degerleri doldurulmadan yedekleme calismaz.",
                        isWarning = true
                    )
                }
            }

            item {
                InfoCard(
                    title = "Geri yukleme guvenligi",
                    message = "Sunucudan geri yukleme otomatik yapilmaz. Son yedek bulununca once onay istenir; onaydan sonra telefondaki lokal veriler sunucudaki yedekle degisir.",
                    isWarning = false
                )
            }
        }
    }

    uiState.pendingRestoreSnapshot?.let { snapshot ->
        RestoreConfirmDialog(
            snapshot = snapshot,
            onDismiss = viewModel::dismissRestoreDialog,
            onConfirm = viewModel::confirmRestore
        )
    }
}

@Composable
private fun BackupStatusCard(
    isConfigured: Boolean,
    lastSuccessfulBackupAt: Long?,
    lastStatusMessage: String?,
    lastErrorMessage: String?,
    isBusy: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (lastErrorMessage == null) Icons.Default.Info else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (lastErrorMessage == null) AccentBlue else AccentRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = if (isConfigured) "Sunucu yedekleme" else "Yapilandirma bekliyor",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (isBusy) {
                    Spacer(modifier = Modifier.weight(1f))
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = AccentBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Son basarili yedek",
                color = TextMuted,
                fontSize = 13.sp
            )
            Text(
                text = lastSuccessfulBackupAt?.let { formatDateTime(it) } ?: "Henuz yedek alinmadi",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )

            lastStatusMessage?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = it, color = AccentGreen, fontSize = 14.sp)
            }

            lastErrorMessage?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = it, color = AccentRed, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun BackupActionCard(
    isBusy: Boolean,
    onBackupNow: () -> Unit,
    onPrepareRestore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(18.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onBackupNow,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text("Simdi Yedekle")
            }

            OutlinedButton(
                onClick = onPrepareRestore,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text("Sunucudan Geri Yukle")
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, message: String, isWarning: Boolean) {
    val color = if (isWarning) AccentOrange else AccentBlue
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, color.copy(alpha = 0.24f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(message, color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun RestoreConfirmDialog(
    snapshot: BackupSnapshot,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yedegi Geri Yukle", color = TextPrimary) },
        text = {
            Column {
                Text(
                    text = "Sunucudaki son yedek telefondaki lokal verilerin yerine yazilacak.",
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Yedek tarihi: ${formatDateTime(snapshot.createdAt)}",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Kayit sayisi: ${countRecords(snapshot)}",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Geri Yukle", color = AccentRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Iptal", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}

private fun countRecords(snapshot: BackupSnapshot): Int {
    return snapshot.payload.persons.size +
        snapshot.payload.transactions.size +
        snapshot.payload.notes.size +
        snapshot.payload.scheduledNotes.size +
        snapshot.payload.generalNotes.size +
        snapshot.payload.generalTransactions.size +
        snapshot.payload.generalScheduledNotes.size
}
