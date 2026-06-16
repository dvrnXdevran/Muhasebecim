package com.muhasebecim.ui.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhasebecim.data.model.GeneralNote
import com.muhasebecim.data.model.GeneralScheduledNote
import com.muhasebecim.data.model.GeneralTransaction
import com.muhasebecim.data.model.GeneralTransactionType
import com.muhasebecim.data.model.ScheduledNoteType
import com.muhasebecim.ui.theme.*
import com.muhasebecim.ui.util.formatCurrency
import com.muhasebecim.ui.util.formatDate
import com.muhasebecim.ui.util.formatDateShort
import com.muhasebecim.ui.viewmodel.GeneralLedgerViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralLedgerScreen(viewModel: GeneralLedgerViewModel) {
    val generalTransactions by viewModel.generalTransactions.collectAsState()
    val outstandingGelir by viewModel.outstandingGelir.collectAsState()
    val outstandingGider by viewModel.outstandingGider.collectAsState()
    val generalScheduledNotes by viewModel.generalScheduledNotes.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showAddScheduledDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<GeneralTransaction?>(null) }
    var editingScheduledNote by remember { mutableStateOf<GeneralScheduledNote?>(null) }

    val tabs = listOf("Gelir/Gider", "Planlanan", "Gecmis")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Genel Defter", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> showAddTransactionDialog = true
                        1 -> showAddScheduledDialog = true
                    }
                },
                containerColor = AccentBlue,
                contentColor = DarkSurface
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ekle", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkCard, RoundedCornerShape(20.dp))
                        .padding(22.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LedgerSummaryMetric("Kalan Gelir", outstandingGelir, AlacakColor)
                    LedgerDividerBar()
                    LedgerSummaryMetric("Kalan Gider", outstandingGider, BorcColor)
                    LedgerDividerBar()
                    val bakiye = outstandingGelir - outstandingGider
                    LedgerSummaryMetric("Bakiye", bakiye, if (bakiye >= 0) AlacakColor else BorcColor)
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = AccentBlue
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, color = if (selectedTab == index) AccentBlue else TextMuted) }
                    )
                }
            }

            when (selectedTab) {
                0 -> GeneralTransactionList(
                    transactions = generalTransactions.filter { !it.isPaid },
                    onTogglePaid = viewModel::toggleGeneralTransactionPaid,
                    onEdit = { editingTransaction = it },
                    onDelete = viewModel::deleteGeneralTransaction
                )
                1 -> GeneralScheduledNoteList(
                    scheduledNotes = generalScheduledNotes.filter { !it.isCompleted },
                    onToggle = viewModel::toggleGeneralScheduledNoteCompleted,
                    onEdit = { editingScheduledNote = it },
                    onDelete = viewModel::deleteGeneralScheduledNote
                )
                2 -> GeneralHistoryList(
                    transactions = generalTransactions.filter { it.isPaid },
                    scheduledNotes = generalScheduledNotes.filter { it.isCompleted },
                    onTogglePaid = viewModel::toggleGeneralTransactionPaid,
                    onRestoreScheduled = viewModel::toggleGeneralScheduledNoteCompleted,
                    onDeleteScheduled = viewModel::deleteGeneralScheduledNote,
                    onDelete = viewModel::deleteGeneralTransaction
                )
            }
        }
    }

    if (showAddTransactionDialog) {
        AddGeneralTransactionDialog(
            onDismiss = { showAddTransactionDialog = false },
            title = "Gelir/Gider Ekle",
            onConfirm = { amount, description, type ->
                viewModel.addGeneralTransaction(amount, description, type)
                showAddTransactionDialog = false
            }
        )
    }

    if (showAddScheduledDialog) {
        AddGeneralScheduledNoteDialog(
            onDismiss = { showAddScheduledDialog = false },
            title = "Planlanan Islem Ekle",
            onConfirm = { description, amount, dueDate, type ->
                viewModel.addGeneralScheduledNote(description, amount, dueDate, type)
                showAddScheduledDialog = false
            }
        )
    }

    editingTransaction?.let { transaction ->
        AddGeneralTransactionDialog(
            onDismiss = { editingTransaction = null },
            title = "Gelir/Gider Duzenle",
            initialAmount = transaction.amount,
            initialDescription = transaction.description,
            initialType = transaction.type,
            confirmLabel = "Kaydet",
            onConfirm = { amount, description, type ->
                viewModel.updateGeneralTransaction(transaction, amount, description, type)
                editingTransaction = null
            }
        )
    }

    editingScheduledNote?.let { scheduledNote ->
        AddGeneralScheduledNoteDialog(
            onDismiss = { editingScheduledNote = null },
            title = "Planlanan Islemi Duzenle",
            initialDescription = scheduledNote.description,
            initialAmount = scheduledNote.amount,
            initialType = scheduledNote.type,
            initialDate = scheduledNote.dueDate,
            confirmLabel = "Kaydet",
            onConfirm = { description, amount, dueDate, type ->
                viewModel.updateGeneralScheduledNote(scheduledNote, description, amount, dueDate, type)
                editingScheduledNote = null
            }
        )
    }
}

@Composable
private fun LedgerSummaryMetric(title: String, amount: Double, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = color, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(formatCurrency(amount), color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun LedgerDividerBar() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(DarkSurfaceVariant)
    )
}

@Composable
fun GeneralTransactionList(
    transactions: List<GeneralTransaction>,
    onTogglePaid: (GeneralTransaction) -> Unit,
    onEdit: (GeneralTransaction) -> Unit,
    onDelete: (GeneralTransaction) -> Unit
) {
    if (transactions.isEmpty()) {
        EmptyState("Henuz islem yok", "Gelir veya gider eklemek icin + butonuna tiklayin")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions, key = { it.id }) { transaction ->
                GeneralTransactionCard(
                    transaction = transaction,
                    onTogglePaid = { onTogglePaid(transaction) },
                    onEdit = { onEdit(transaction) },
                    onDelete = { onDelete(transaction) }
                )
            }
        }
    }
}

@Composable
fun GeneralTransactionCard(
    transaction: GeneralTransaction,
    onTogglePaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.4.dp, DarkSurfaceVariant, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (transaction.type == GeneralTransactionType.GELIR) AlacakColor else BorcColor,
                        RoundedCornerShape(4.dp)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (transaction.description.isNotBlank()) {
                    Text(transaction.description, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(formatDateShort(transaction.createdAt), color = TextMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = onTogglePaid,
                        label = {
                            Text(
                                when (transaction.type) {
                                    GeneralTransactionType.GIDER -> "Odedim"
                                    GeneralTransactionType.GELIR -> "Alindi"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = DarkSurface,
                            labelColor = TextSecondary,
                            leadingIconContentColor = TextMuted
                        )
                    )
                    Text(
                        if (transaction.isPaid) "Kapatildi" else "Bekliyor",
                        color = if (transaction.isPaid) AccentGreen else TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (transaction.type == GeneralTransactionType.GELIR) "+" else "-") + formatCurrency(transaction.amount),
                    color = when {
                        transaction.isPaid -> TextMuted
                        transaction.type == GeneralTransactionType.GELIR -> AlacakColor
                        else -> BorcColor
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Duzenle", tint = TextMuted, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = TextMuted, modifier = Modifier.size(22.dp))
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Islemi Sil", color = TextPrimary) },
            text = { Text("Bu islem silinecek. Emin misiniz?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Sil", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Iptal", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun GeneralHistoryList(
    transactions: List<GeneralTransaction>,
    scheduledNotes: List<GeneralScheduledNote>,
    onTogglePaid: (GeneralTransaction) -> Unit,
    onRestoreScheduled: (GeneralScheduledNote) -> Unit,
    onDeleteScheduled: (GeneralScheduledNote) -> Unit,
    onDelete: (GeneralTransaction) -> Unit
) {
    if (transactions.isEmpty() && scheduledNotes.isEmpty()) {
        EmptyState("Henuz kapanan kayit yok", "Odendi veya alindi olarak isaretlenen kayitlar burada gorunecek")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (transactions.isNotEmpty()) {
                item {
                    Text("Gelir Gider Gecmisi", color = TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                items(transactions, key = { "transaction-${it.id}" }) { transaction ->
                    GeneralTransactionHistoryCard(
                        transaction = transaction,
                        onRestore = { onTogglePaid(transaction) },
                        onDelete = { onDelete(transaction) }
                    )
                }
            }

            if (scheduledNotes.isNotEmpty()) {
                item {
                    Text("Planlanan Gecmisi", color = TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                items(scheduledNotes, key = { "scheduled-${it.id}" }) { scheduledNote ->
                    GeneralScheduledNoteHistoryCard(
                        scheduledNote = scheduledNote,
                        onRestore = { onRestoreScheduled(scheduledNote) },
                        onDelete = { onDeleteScheduled(scheduledNote) }
                    )
                }
            }
        }
    }
}

@Composable
fun GeneralTransactionHistoryCard(
    transaction: GeneralTransaction,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.4.dp, DarkSurfaceVariant, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (transaction.type == GeneralTransactionType.GELIR) AlacakColor.copy(alpha = 0.5f) else BorcColor.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (transaction.description.isNotBlank()) {
                    Text(transaction.description, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(formatDateShort(transaction.createdAt), color = TextMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = onRestore,
                    label = { Text("Geri Al") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Undo,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = DarkSurface,
                        labelColor = TextSecondary,
                        leadingIconContentColor = TextMuted
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (transaction.type == GeneralTransactionType.GELIR) "+" else "-") + formatCurrency(transaction.amount),
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (transaction.type == GeneralTransactionType.GELIR) "Alindi" else "Odendi",
                        color = AccentGreen,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = TextMuted, modifier = Modifier.size(22.dp))
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Islemi Sil", color = TextPrimary) },
            text = { Text("Bu islem silinecek. Emin misiniz?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Sil", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Iptal", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun GeneralNoteList(notes: List<GeneralNote>, onEdit: (GeneralNote) -> Unit, onDelete: (GeneralNote) -> Unit) {
    if (notes.isEmpty()) {
        EmptyState("Henuz not yok", "Not eklemek icin + butonuna tiklayin")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notes, key = { it.id }) { note ->
                GeneralNoteCard(note = note, onEdit = { onEdit(note) }, onDelete = { onDelete(note) })
            }
        }
    }
}

@Composable
fun GeneralNoteCard(note: GeneralNote, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.6.dp, DarkSurfaceVariant.copy(alpha = 0.95f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(note.content, color = TextPrimary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(formatDateShort(note.createdAt), color = TextMuted, fontSize = 12.sp)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Duzenle", tint = TextMuted, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = TextMuted, modifier = Modifier.size(22.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Notu Sil", color = TextPrimary) },
            text = { Text("Bu not silinecek. Emin misiniz?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Sil", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Iptal", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun GeneralScheduledNoteList(
    scheduledNotes: List<GeneralScheduledNote>,
    onToggle: (GeneralScheduledNote) -> Unit,
    onEdit: (GeneralScheduledNote) -> Unit,
    onDelete: (GeneralScheduledNote) -> Unit
) {
    if (scheduledNotes.isEmpty()) {
        EmptyState("Henuz planlanan islem yok", "Planlanan odeme veya alacak eklemek icin + butonuna tiklayin")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(scheduledNotes, key = { it.id }) { scheduledNote ->
                GeneralScheduledNoteCard(
                    scheduledNote = scheduledNote,
                    onToggle = { onToggle(scheduledNote) },
                    onEdit = { onEdit(scheduledNote) },
                    onDelete = { onDelete(scheduledNote) }
                )
            }
        }
    }
}

@Composable
fun GeneralScheduledNoteCard(
    scheduledNote: GeneralScheduledNote,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isOverdue = scheduledNote.dueDate < System.currentTimeMillis()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.4.dp,
                if (isOverdue) AccentRed.copy(alpha = 0.28f) else DarkSurfaceVariant,
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) AccentRed.copy(alpha = 0.1f) else DarkCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    scheduledNote.description,
                    color = TextPrimary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        formatDate(scheduledNote.dueDate),
                        color = if (isOverdue) AccentRed else TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (scheduledNote.type == ScheduledNoteType.ODEME) "Odeme" else "Alacak",
                        color = if (scheduledNote.type == ScheduledNoteType.ODEME) BorcColor else AlacakColor,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = onToggle,
                    label = { Text(if (scheduledNote.type == ScheduledNoteType.ODEME) "Odedim" else "Alindi") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = DarkSurface,
                        labelColor = TextSecondary,
                        leadingIconContentColor = TextMuted
                    )
                )
            }

            Text(
                formatCurrency(scheduledNote.amount),
                color = if (scheduledNote.type == ScheduledNoteType.ODEME) BorcColor else AlacakColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Duzenle", tint = TextMuted, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = TextMuted, modifier = Modifier.size(22.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Planlanan Islemi Sil", color = TextPrimary) },
            text = { Text("Bu planlanan kayit silinecek. Emin misiniz?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Sil", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Iptal", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun GeneralScheduledNoteHistoryCard(
    scheduledNote: GeneralScheduledNote,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.4.dp, DarkSurfaceVariant, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(scheduledNote.description, color = TextPrimary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(formatDate(scheduledNote.dueDate), color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (scheduledNote.type == ScheduledNoteType.ODEME) "Odendi" else "Alindi",
                            color = AccentGreen,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = onRestore,
                    label = { Text("Geri Al") },
                    leadingIcon = {
                        Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(22.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = DarkSurface,
                        labelColor = TextSecondary,
                        leadingIconContentColor = TextMuted
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatCurrency(scheduledNote.amount),
                    color = if (scheduledNote.type == ScheduledNoteType.ODEME) BorcColor else AlacakColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = TextMuted, modifier = Modifier.size(22.dp))
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Planlanan Islemi Sil", color = TextPrimary) },
            text = { Text("Bu planlanan kayit silinecek. Emin misiniz?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Sil", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Iptal", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun AddGeneralTransactionDialog(
    onDismiss: () -> Unit,
    title: String,
    initialAmount: Double? = null,
    initialDescription: String = "",
    initialType: GeneralTransactionType = GeneralTransactionType.GELIR,
    confirmLabel: String = "Ekle",
    onConfirm: (Double, String, GeneralTransactionType) -> Unit
) {
    var amount by remember(initialAmount) { mutableStateOf(initialAmount?.toString()?.replace(".", ",") ?: "") }
    var description by remember(initialDescription) { mutableStateOf(initialDescription) }
    var selectedType by remember(initialType) { mutableStateOf(initialType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = TextPrimary) },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == GeneralTransactionType.GELIR,
                        onClick = { selectedType = GeneralTransactionType.GELIR },
                        label = { Text("Gelir") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AlacakColor.copy(alpha = 0.2f),
                            selectedLabelColor = AlacakColor
                        )
                    )
                    FilterChip(
                        selected = selectedType == GeneralTransactionType.GIDER,
                        onClick = { selectedType = GeneralTransactionType.GIDER },
                        label = { Text("Gider") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BorcColor.copy(alpha = 0.2f),
                            selectedLabelColor = BorcColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' || c == ',' }) amount = it },
                    label = { Text("Tutar (TL)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        cursorColor = AccentBlue,
                        focusedLabelColor = AccentBlue,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Aciklama (opsiyonel)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        cursorColor = AccentBlue,
                        focusedLabelColor = AccentBlue,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            val parsedAmount = amount.replace(",", ".").toDoubleOrNull()
            TextButton(
                onClick = { parsedAmount?.let { onConfirm(it, description, selectedType) } },
                enabled = parsedAmount != null && parsedAmount > 0
            ) {
                Text(confirmLabel, color = if (parsedAmount != null && parsedAmount > 0) AccentBlue else TextMuted)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Iptal", color = TextSecondary) } },
        containerColor = DarkSurface
    )
}

@Composable
fun AddGeneralNoteDialog(
    onDismiss: () -> Unit,
    title: String,
    initialContent: String = "",
    confirmLabel: String = "Ekle",
    onConfirm: (String) -> Unit
) {
    var content by remember(initialContent) { mutableStateOf(initialContent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = TextPrimary) },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Not") },
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = DarkSurfaceVariant,
                    cursorColor = AccentBlue,
                    focusedLabelColor = AccentBlue,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { if (content.isNotBlank()) onConfirm(content) }, enabled = content.isNotBlank()) {
                Text(confirmLabel, color = if (content.isNotBlank()) AccentBlue else TextMuted)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Iptal", color = TextSecondary) } },
        containerColor = DarkSurface
    )
}

@Composable
fun AddGeneralScheduledNoteDialog(
    onDismiss: () -> Unit,
    title: String,
    initialDescription: String = "",
    initialAmount: Double? = null,
    initialType: ScheduledNoteType = ScheduledNoteType.ODEME,
    initialDate: Long? = null,
    confirmLabel: String = "Ekle",
    onConfirm: (String, Double, Long, ScheduledNoteType) -> Unit
) {
    var description by remember(initialDescription) { mutableStateOf(initialDescription) }
    var amount by remember(initialAmount) { mutableStateOf(initialAmount?.toString()?.replace(".", ",") ?: "") }
    var selectedType by remember(initialType) { mutableStateOf(initialType) }
    var selectedDate by remember(initialDate) { mutableStateOf(initialDate) }
    val context = LocalContext.current
    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = TextPrimary) },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == ScheduledNoteType.ODEME,
                        onClick = { selectedType = ScheduledNoteType.ODEME },
                        label = { Text("Odeme") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BorcColor.copy(alpha = 0.2f),
                            selectedLabelColor = BorcColor
                        )
                    )
                    FilterChip(
                        selected = selectedType == ScheduledNoteType.ALACAK,
                        onClick = { selectedType = ScheduledNoteType.ALACAK },
                        label = { Text("Alacak") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AlacakColor.copy(alpha = 0.2f),
                            selectedLabelColor = AlacakColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Aciklama") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        cursorColor = AccentBlue,
                        focusedLabelColor = AccentBlue,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' || c == ',' }) amount = it },
                    label = { Text("Tutar (TL)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        cursorColor = AccentBlue,
                        focusedLabelColor = AccentBlue,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = selectedDate?.takeIf { it >= todayStart } ?: todayStart
                        }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val cal = Calendar.getInstance()
                                cal.set(year, month, dayOfMonth, 0, 0, 0)
                                cal.set(Calendar.MILLISECOND, 0)
                                selectedDate = cal.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).apply {
                            datePicker.minDate = todayStart
                        }.show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = AccentBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        selectedDate?.let { formatDate(it) } ?: "Tarih Secin",
                        color = if (selectedDate != null) TextPrimary else TextMuted
                    )
                }
            }
        },
        confirmButton = {
            val parsedAmount = amount.replace(",", ".").toDoubleOrNull()
            val isValid = description.isNotBlank() &&
                parsedAmount != null &&
                parsedAmount > 0 &&
                selectedDate != null &&
                selectedDate!! >= todayStart
            TextButton(
                onClick = { if (isValid) onConfirm(description, parsedAmount!!, selectedDate!!, selectedType) },
                enabled = isValid
            ) {
                Text(confirmLabel, color = if (isValid) AccentBlue else TextMuted)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Iptal", color = TextSecondary) } },
        containerColor = DarkSurface
    )
}

