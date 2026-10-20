package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun PhoneScreen(
    modifier: Modifier = Modifier
) {
    var dialNumber by remember { mutableStateOf("") }
    var isInCall by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Tastierino, 1: Recenti, 2: Rubrica

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Intestazione con selettore schede
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Telefono Vivavoce Blue&Me",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gestione chiamate tramite microfono e altoparlanti dell'auto",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(AutomotiveCard)
                    .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                val tabs = listOf("Tastierino", "Recenti", "Rubrica")
                tabs.forEachIndexed { index, title ->
                    Button(
                        onClick = { selectedTab = index },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == index) CyanAccent else AutomotiveCard,
                            contentColor = if (selectedTab == index) AutomotiveBlack else TextSecondary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Se in chiamata attiva, mostra il banner di chiamata
        if (isInCall) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EcoEmerald.copy(alpha = 0.15f))
                    .border(1.dp, EcoEmerald, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(EcoEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = AutomotiveBlack)
                        }
                        Column {
                            Text("Chiamata in corso...", color = EcoEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(dialNumber.ifEmpty { "+39 02 892345" }, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { isInCall = false },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = TextPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Termina")
                    }
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // Scheda Tastierino
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Display numero digitato
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AutomotiveCard)
                                .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(14.dp))
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = dialNumber.ifEmpty { "Digita un numero..." },
                                color = if (dialNumber.isEmpty()) TextMuted else TextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (dialNumber.isNotBlank()) isInCall = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald, contentColor = AutomotiveBlack),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Chiama", fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = { if (dialNumber.isNotEmpty()) dialNumber = dialNumber.dropLast(1) },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AutomotiveCard)
                                    .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(14.dp))
                            ) {
                                Icon(Icons.Default.Backspace, contentDescription = "Cancella", tint = TextSecondary)
                            }
                        }
                    }

                    // Griglia tastierino telefonico 3x4
                    Column(
                        modifier = Modifier.weight(1.2f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val keypad = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("*", "0", "#")
                        )
                        keypad.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { digit ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(AutomotiveCard)
                                            .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(12.dp))
                                            .clickable { dialNumber += digit },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(digit, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Scheda Ultime Chiamate
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val recents = listOf(
                        Triple("Casa", "+39 02 892345", "Oggi, 10:15"),
                        Triple("Officina Fiat Service", "+39 06 432198", "Ieri, 16:40"),
                        Triple("Marco Rossi", "+39 347 1234567", "18 Ott, 11:05"),
                        Triple("Ufficio", "+39 02 554433", "17 Ott, 09:20")
                    )
                    recents.forEach { (name, number, date) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AutomotiveCard)
                                .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(14.dp))
                                .clickable {
                                    dialNumber = number
                                    isInCall = true
                                }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text("$number • $date", color = TextMuted, fontSize = 12.sp)
                            }
                            Icon(Icons.Default.PhoneForwarded, contentDescription = "Chiama", tint = CyanAccent)
                        }
                    }
                }
            }
            2 -> {
                // Scheda Rubrica Blue&Me
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val contacts = listOf(
                        "Alessandro Bianchi" to "+39 338 9876543",
                        "Assistenza Stradale ACI" to "803.116",
                        "Centri Assistenza Gruppo FIAT" to "800.3428.00",
                        "Giulia Romano" to "+39 340 7654321",
                        "Mamma" to "+39 335 1122334"
                    )
                    contacts.forEach { (name, number) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AutomotiveCard)
                                .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(14.dp))
                                .clickable {
                                    dialNumber = number
                                    isInCall = true
                                }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(CyanAccentDim.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(name.first().toString(), color = CyanAccent, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text(name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                    Text(number, color = TextMuted, fontSize = 12.sp)
                                }
                            }
                            Icon(Icons.Default.Call, contentDescription = "Chiama", tint = EcoEmerald)
                        }
                    }
                }
            }
        }
    }
}
