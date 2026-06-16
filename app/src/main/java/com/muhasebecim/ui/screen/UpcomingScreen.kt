package com.muhasebecim.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhasebecim.data.model.ScheduledNoteType
import com.muhasebecim.ui.theme.AccentBlue
import com.muhasebecim.ui.theme.AccentGreen
import com.muhasebecim.ui.theme.AccentRed
import com.muhasebecim.ui.theme.AlacakColor
import com.muhasebecim.ui.theme.BorcColor
import com.muhasebecim.ui.theme.DarkBackground
import com.muhasebecim.ui.theme.DarkCard
import com.muhasebecim.ui.theme.DarkSurface
import com.muhasebecim.ui.theme.DarkSurfaceVariant
import com.muhasebecim.ui.theme.TextMuted
import com.muhasebecim.ui.theme.TextPrimary
import com.muhasebecim.ui.theme.TextSecondary
import com.muhasebecim.ui.util.formatCurrency
import com.muhasebecim.ui.util.formatDate
import com.muhasebecim.ui.viewmodel.UpcomingListItem
import com.muhasebecim.ui.viewmodel.UpcomingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingScreen(
    viewModel: UpcomingViewModel,
    personNames: Map<Long, String>
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val allUpcoming by viewModel.allUpcoming.collectAsState()
    val upcomingOdemeler by viewModel.upcomingOdemeler.collectAsState()
    val upcomingAlacaklar by viewModel.upcomingAlacaklar.collectAsState()

    val tabs = listOf("Tumu", "Odemeler", "Alacaklar")

    val currentList = when (selectedTab) {
        0 -> allUpcoming
        1 -> upcomingOdemeler
        2 -> upcomingAlacaklar
        else -> allUpcoming
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Yaklasan Islemler",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Odenecek",
                    amount = upcomingOdemeler.sumOf { it.amount },
                    count = upcomingOdemeler.size,
                    color = BorcColor,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Alinacak",
                    amount = upcomingAlacaklar.sumOf { it.amount },
                    count = upcomingAlacaklar.size,
                    color = AlacakColor,
                    modifier = Modifier.weight(1f)
                )
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = AccentBlue
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) AccentBlue else TextMuted,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            if (currentList.isEmpty()) {
                EmptyState(
                    title = "Yaklasan islem yok",
                    subtitle = "Kisi detayindan veya genel defterden planlanan islem ekleyebilirsiniz"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList, key = { "${if (it.isGeneral) "general" else "person"}-${it.id}" }) { scheduledNote ->
                        UpcomingCard(
                            scheduledNote = scheduledNote,
                            ownerLabel = if (scheduledNote.isGeneral) "Genel Defter" else personNames[scheduledNote.personId] ?: "Bilinmiyor",
                            onToggle = { viewModel.toggleCompleted(scheduledNote) },
                            onDelete = { viewModel.deleteScheduledNote(scheduledNote) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(20.dp))
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = title,
                    color = color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = formatCurrency(amount),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$count islem",
                color = TextMuted,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun UpcomingCard(
    scheduledNote: UpcomingListItem,
    ownerLabel: String,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isOverdue = scheduledNote.dueDate < System.currentTimeMillis()
    val amountColor = if (scheduledNote.type == ScheduledNoteType.ODEME) BorcColor else AlacakColor

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) AccentRed.copy(alpha = 0.1f) else DarkCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isOverdue) AccentRed.copy(alpha = 0.22f) else DarkSurfaceVariant,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = scheduledNote.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = AccentGreen,
                    uncheckedColor = TextMuted,
                    checkmarkColor = Color.White
                )
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scheduledNote.description,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = TextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ownerLabel,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isOverdue) AccentRed else TextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(scheduledNote.dueDate),
                        color = if (isOverdue) AccentRed else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (isOverdue) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = formatCurrency(scheduledNote.amount),
                    color = amountColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Box(
                    modifier = Modifier
                        .background(amountColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (scheduledNote.type == ScheduledNoteType.ODEME) "Odeme" else "Alacak",
                        color = amountColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = AccentRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Yaklasan Islemi Sil", color = TextPrimary) },
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
