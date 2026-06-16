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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.muhasebecim.data.model.Note
import com.muhasebecim.data.model.ScheduledNote
import com.muhasebecim.data.model.ScheduledNoteType
import com.muhasebecim.data.model.Transaction
import com.muhasebecim.data.model.TransactionType
import com.muhasebecim.ui.theme.*
import com.muhasebecim.ui.util.formatCurrency
import com.muhasebecim.ui.util.formatDate
import com.muhasebecim.ui.util.formatDateShort
import com.muhasebecim.ui.viewmodel.PersonDetailViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    viewModel: PersonDetailViewModel,
    onBack: () -> Unit
) {
    val person by viewModel.person.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val outstandingAlacak by viewModel.outstandingAlacak.collectAsState()
    val outstandingBorc by viewModel.outstandingBorc.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val scheduledNotes by viewModel.scheduledNotes.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddScheduledDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var editingScheduledNote by remember { mutableStateOf<ScheduledNote?>(null) }

    val tabs = listOf("Hesap", "Notlar", "Planlanan", "Gecmis")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(person?.name ?: "Yukleniyor...", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showEditNameDialog = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Isim Duzenle",
                            tint = AccentBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
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
                        1 -> showAddNoteDialog = true
                        2 -> showAddScheduledDialog = true
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
                    SummaryMetric("Kalan Alacak", outstandingAlacak, AlacakColor)
                    DividerBar()
                    SummaryMetric("Kalan Borc", outstandingBorc, BorcColor)
                    DividerBar()
                    val bakiye = outstandingAlacak - outstandingBorc
                    SummaryMetric("Bakiye", bakiye, if (bakiye >= 0) AlacakColor else BorcColor)
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
                0 -> TransactionList(
                    transactions = transactions.filter { !it.isPaid },
                    onTogglePaid = viewModel::toggleTransactionPaid,
                    onEdit = { editingTransaction = it },
                    onDelete = viewModel::deleteTransaction
                )
                1 -> NoteList(
                    notes = notes,
                    onEdit = { editingNote = it },
                    onDelete = viewModel::deleteNote
                )
                2 -> ScheduledNoteList(
                    scheduledNotes = scheduledNotes.filter { !it.isCompleted },
                    onToggle = viewModel::toggleScheduledNoteCompleted,
                    onEdit = { editingScheduledNote = it },
                    onDelete = viewModel::deleteScheduledNote
                )
                3 -> HistoryList(
                    transactions = transactions.filter { it.isPaid },
                    scheduledNotes = scheduledNotes.filter { it.isCompleted },
                    onTogglePaid = viewModel::toggleTransactionPaid,
                    onRestoreScheduled = viewModel::toggleScheduledNoteCompleted,
                    onDeleteScheduled = viewModel::deleteScheduledNote,
                    onDelete = viewModel::deleteTransaction
                )
            }
        }
    }

    if (showAddTransactionDialog) {
        AddTransactionDialog(
            onDismiss = { showAddTransactionDialog = false },
            title = "Islem Ekle",
            onConfirm = { amount, description, type ->
                viewModel.addTransaction(amount, description, type)
                showAddTransactionDialog = false
            }
        )
    }

    if (showAddNoteDialog) {
        AddNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            title = "Not Ekle",
            onConfirm = { content ->
                viewModel.addNote(content)
                showAddNoteDialog = false
            }
        )
    }

    if (showAddScheduledDialog) {
        AddScheduledNoteDialog(
            onDismiss = { showAddScheduledDialog = false },
            title = "Planlanan Islem Ekle",
            onConfirm = { description, amount, dueDate, type ->
                viewModel.addScheduledNote(description, amount, dueDate, type)
                showAddScheduledDialog = false
            }
        )
    }

    editingTransaction?.let { transaction ->
        AddTransactionDialog(
            onDismiss = { editingTransaction = null },
            title = "Islem Duzenle",
            initialAmount = transaction.amount,
            initialDescription = transaction.description,
            initialType = transaction.type,
            confirmLabel = "Kaydet",
            onConfirm = { amount, description, type ->
                viewModel.updateTransaction(transaction, amount, description, type)
                editingTransaction = null
            }
        )
    }

    editingNote?.let { note ->
        AddNoteDialog(
            onDismiss = { editingNote = null },
            title = "Not Duzenle",
            initialContent = note.content,
            confirmLabel = "Kaydet",
            onConfirm = { content ->
                viewModel.updateNote(note, content)
                editingNote = null
            }
        )
    }

    editingScheduledNote?.let { scheduledNote ->
        AddScheduledNoteDialog(
            onDismiss = { editingScheduledNote = null },
            title = "Planlanan Islemi Duzenle",
            initialDescription = scheduledNote.description,
            initialAmount = scheduledNote.amount,
            initialType = scheduledNote.type,
            initialDate = scheduledNote.dueDate,
            confirmLabel = "Kaydet",
            onConfirm = { description, amount, dueDate, type ->
                viewModel.updateScheduledNote(scheduledNote, description, amount, dueDate, type)
                editingScheduledNote = null
            }
        )
    }

    if (showEditNameDialog) {
        person?.let { currentPerson ->
            EditPersonDialog(
                person = currentPerson,
                onDismiss = { showEditNameDialog = false },
                onConfirm = { newName ->
                    viewModel.updatePersonName(newName)
                    showEditNameDialog = false
                }
            )
        }
    }
}

@Composable
private fun SummaryMetric(title: String, amount: Double, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = color, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(formatCurrency(amount), color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun DividerBar() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(DarkSurfaceVariant)
    )
}

@Composable
fun TransactionList(
    transactions: List<Transaction>,
    onTogglePaid: (Transaction) -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit
) {
    if (transactions.isEmpty()) {
        EmptyState("Henuz islem yok", "Yeni borc veya alacak eklemek icin + butonuna tiklayin")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions, key = { it.id }) { transaction ->
                TransactionCard(
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
fun TransactionCard(
    transaction: Transaction,
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
                        if (transaction.type == TransactionType.ALACAK) AlacakColor else BorcColor,
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
                                    TransactionType.BORC -> "Odedim"
                                    TransactionType.ALACAK -> "Alindi"
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
                    (if (transaction.type == TransactionType.ALACAK) "+" else "-") + formatCurrency(transaction.amount),
                    color = when {
                        transaction.isPaid -> TextMuted
                        transaction.type == TransactionType.ALACAK -> AlacakColor
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
fun HistoryList(
    transactions: List<Transaction>,
    scheduledNotes: List<ScheduledNote>,
    onTogglePaid: (Transaction) -> Unit,
    onRestoreScheduled: (ScheduledNote) -> Unit,
    onDeleteScheduled: (ScheduledNote) -> Unit,
    onDelete: (Transaction) -> Unit
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
                    Text("Hesap Gecmisi", color = TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                items(transactions, key = { "transaction-${it.id}" }) { transaction ->
                    TransactionHistoryCard(
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
                    ScheduledNoteHistoryCard(
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
fun TransactionHistoryCard(
    transaction: Transaction,
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
                        if (transaction.type == TransactionType.ALACAK) AlacakColor.copy(alpha = 0.5f) else BorcColor.copy(alpha = 0.5f),
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
                    (if (transaction.type == TransactionType.ALACAK) "+" else "-") + formatCurrency(transaction.amount),
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
                        if (transaction.type == TransactionType.ALACAK) "Alindi" else "Odendi",
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
fun NoteList(notes: List<Note>, onEdit: (Note) -> Unit, onDelete: (Note) -> Unit) {
    if (notes.isEmpty()) {
        EmptyState("Henuz not yok", "Not eklemek icin + butonuna tiklayin")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notes, key = { it.id }) { note ->
                NoteCard(note = note, onEdit = { onEdit(note) }, onDelete = { onDelete(note) })
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, onEdit: () -> Unit, onDelete: () -> Unit) {
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
fun ScheduledNoteList(
    scheduledNotes: List<ScheduledNote>,
    onToggle: (ScheduledNote) -> Unit,
    onEdit: (ScheduledNote) -> Unit,
    onDelete: (ScheduledNote) -> Unit
) {
    if (scheduledNotes.isEmpty()) {
        EmptyState("Henuz planlanan islem yok", "Planlanan odeme veya alacak eklemek icin + butonuna tiklayin")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(scheduledNotes, key = { it.id }) { scheduledNote ->
                ScheduledNoteCard(
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
fun ScheduledNoteCard(
    scheduledNote: ScheduledNote,
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
fun ScheduledNoteHistoryCard(
    scheduledNote: ScheduledNote,
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
fun EmptyState(title: String, subtitle: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(48.dp), tint = TextMuted)
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = TextMuted, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = TextMuted, fontSize = 13.sp)
        }
    }
}

@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    title: String,
    initialAmount: Double? = null,
    initialDescription: String = "",
    initialType: TransactionType = TransactionType.ALACAK,
    confirmLabel: String = "Ekle",
    onConfirm: (Double, String, TransactionType) -> Unit
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
                        selected = selectedType == TransactionType.ALACAK,
                        onClick = { selectedType = TransactionType.ALACAK },
                        label = { Text("Alacak") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AlacakColor.copy(alpha = 0.2f),
                            selectedLabelColor = AlacakColor
                        )
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.BORC,
                        onClick = { selectedType = TransactionType.BORC },
                        label = { Text("Borc") },
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
fun AddNoteDialog(
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
fun AddScheduledNoteDialog(
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

