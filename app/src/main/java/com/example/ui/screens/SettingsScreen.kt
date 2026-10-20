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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.BluetoothConnectionState
import com.example.data.VehicleBrand
import com.example.model.UpdateStatus
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    connectionState: BluetoothConnectionState,
    isSimulationActive: Boolean,
    selectedBrand: VehicleBrand,
    updateStatus: UpdateStatus,
    repoOwner: String,
    repoName: String,
    token: String,
    onToggleSimulation: (Boolean) -> Unit,
    onSelectBrand: (VehicleBrand) -> Unit,
    onSaveRepoConfig: (String, String, String) -> Unit,
    onCheckForUpdates: () -> Unit,
    onDownloadAndInstall: (String, String) -> Unit,
    onResetUpdateStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    var ownerInput by remember(repoOwner) { mutableStateOf(repoOwner) }
    var nameInput by remember(repoName) { mutableStateOf(repoName) }
    var tokenInput by remember(token) { mutableStateOf(token) }
    var isTokenVisible by remember { mutableStateOf(false) }

    var showPairingDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("1234") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Impostazioni & Sistema",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configurazione Blue&Me, parametri veicolo e aggiornamenti da GitHub Actions",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        // Sezione 1: Connessione Bluetooth Blue&Me
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AutomotiveCard)
                .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bluetooth, contentDescription = null, tint = CyanAccent)
                        }
                        Column {
                            Text("Connessione Blue&Me SPP", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = when (connectionState) {
                                    is BluetoothConnectionState.Connected -> "Connesso a ${(connectionState as BluetoothConnectionState.Connected).deviceName}"
                                    is BluetoothConnectionState.Connecting -> "Connessione in corso..."
                                    is BluetoothConnectionState.Scanning -> "Ricerca centralina Blue&Me..."
                                    else -> "Disconnesso"
                                },
                                color = if (connectionState is BluetoothConnectionState.Connected) EcoEmerald else TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Button(
                        onClick = { showPairingDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = AutomotiveBlack),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("blueandme_pair_button")
                    ) {
                        Text("Associa PIN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Sezione 2: Marchio Vettura e Simulatore
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AutomotiveCard)
                .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Marchio Veicolo Gruppo FIAT", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VehicleBrand.entries.forEach { brand ->
                        FilterChip(
                            selected = selectedBrand == brand,
                            onClick = { onSelectBrand(brand) },
                            label = { Text(brand.brandName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Divider(color = AutomotiveCardBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Simulatore Telemetria CAN Dinamico", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Genera velocità 10Hz, giri motore, marce e audio per collaudo", color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isSimulationActive,
                        onCheckedChange = { onToggleSimulation(it) },
                        modifier = Modifier.testTag("simulation_switch")
                    )
                }
            }
        }

        // Sezione 3: Aggiornamenti GitHub Actions (stile CrealityAuto con supporto Token Privato)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AutomotiveCard)
                .border(1.dp, AutomotiveCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("github_updater_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EcoEmerald.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = EcoEmerald)
                        }
                        Column {
                            Text("Aggiornamenti GitHub Actions", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Versione installata: 1.0.0", color = TextMuted, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = onCheckForUpdates,
                        enabled = updateStatus !is UpdateStatus.Checking && updateStatus !is UpdateStatus.Downloading,
                        colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald, contentColor = AutomotiveBlack),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("check_updates_button")
                    ) {
                        if (updateStatus is UpdateStatus.Checking) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AutomotiveBlack, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text("Verifica", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Configurazione Repository GitHub
                Text("Configurazione Repository (anche privato):", color = TextSecondary, fontSize = 12.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ownerInput,
                        onValueChange = {
                            ownerInput = it
                            onSaveRepoConfig(ownerInput, nameInput, tokenInput)
                        },
                        label = { Text("Owner / Utente", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = AutomotiveCardBorder
                        )
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            onSaveRepoConfig(ownerInput, nameInput, tokenInput)
                        },
                        label = { Text("Repository", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = AutomotiveCardBorder
                        )
                    )
                }

                // Campo Token Personale GitHub per repo privati
                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = {
                        tokenInput = it
                        onSaveRepoConfig(ownerInput, nameInput, tokenInput)
                    },
                    label = { Text("GitHub Token (PAT per repository privati, opzionale)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                            Icon(
                                imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = TextMuted
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = AutomotiveCardBorder
                    )
                )

                // Stato Aggiornamento & Download
                when (updateStatus) {
                    is UpdateStatus.Checking -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyanAccent)
                            Text("Interrogazione release su GitHub...", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                    is UpdateStatus.NoUpdateAvailable -> {
                        Text(
                            text = "✓ L'applicazione è aggiornata all'ultima versione.",
                            color = EcoEmerald,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    is UpdateStatus.UpdateAvailable -> {
                        val available = updateStatus as UpdateStatus.UpdateAvailable
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(EcoEmerald.copy(alpha = 0.12f))
                                .border(1.dp, EcoEmerald.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Nuova Versione: v${available.latestVersion}", color = EcoEmerald, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(available.releaseName, color = TextPrimary, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { onDownloadAndInstall(available.downloadUrl, available.latestVersion) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EcoEmerald, contentColor = AutomotiveBlack),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Scarica & Installa")
                                }
                            }
                            Text(
                                text = "Note di rilascio:\n${available.changelog}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                maxLines = 4
                            )
                        }
                    }
                    is UpdateStatus.Downloading -> {
                        val downloading = updateStatus as UpdateStatus.Downloading
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Download in corso...", color = CyanAccent, fontSize = 12.sp)
                                Text("${downloading.progressPercent}%", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { downloading.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = CyanAccent,
                                trackColor = GaugeTrack
                            )
                        }
                    }
                    is UpdateStatus.ReadyToInstall -> {
                        Text(
                            text = "✓ Download completato! Avvio dell'installer APK...",
                            color = EcoEmerald,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    is UpdateStatus.Error -> {
                        val error = updateStatus as UpdateStatus.Error
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(error.message, color = DangerRed, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            TextButton(onClick = onResetUpdateStatus) {
                                Text("Ignora", color = TextMuted)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    // Dialogo Procedura Associazione PIN Blue&Me (Template 3 FCS0)
    if (showPairingDialog) {
        AlertDialog(
            onDismissRequest = { showPairingDialog = false },
            title = { Text("Associazione Blue&Me (PIN 4 cifre)", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Procedura ufficiale di registrazione con la vettura:",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "1. Ruota la chiave dell'auto in posizione MAR.\n" +
                               "2. Premi MAIN/MENU sul volante per aprire il menu di bordo.\n" +
                               "3. Seleziona Impostazioni > Associazione.\n" +
                               "4. Sul display del quadro appare un codice PIN di 4 cifre.\n" +
                               "5. Inserisci il codice qui sotto:",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        label = { Text("Codice PIN a 4 cifre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = AutomotiveCardBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPairingDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = AutomotiveBlack)
                ) {
                    Text("Conferma Associazione", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPairingDialog = false }) {
                    Text("Annulla", color = TextSecondary)
                }
            },
            containerColor = AutomotiveSurface
        )
    }
}
