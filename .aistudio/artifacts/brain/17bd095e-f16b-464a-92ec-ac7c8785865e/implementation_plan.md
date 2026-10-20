# BlueAndMe Connect - Implementation Plan (Specifiche RE & Template FCS0 Ufficiali)

Dashboard automotive stile Android Auto per il sistema **Blue&Me** del Gruppo FIAT (Fiat, Alfa Romeo, Lancia, Abarth), basata sull'analisi del firmware TomTom (`ttsystem`, `carlinkd`) e sui **file template originali FCS0** (`focus/*.fba`) per l'interazione bidirezionale con la centralina di bordo CAN-bus via Bluetooth RFCOMM.

---

### Anteprima Grafica dell'Interfaccia

![Anteprima Grafica BlueAndMe Connect](/app/src/main/res/drawable/dashboard_preview_1792478654790.jpg)

---

### Analisi dei Template Ufficiali del Quadro Strumenti (`focus/*.fba`)

L'ispezione dei binari con magic **`FCS0`** estratti dall'archivio `focus.zip` ha rivelato l'esatta mappatura delle schermate gestite da `iFocus` (`iid = 0x46`):

| Template ID | Nome / Scopo | Campi & Valori Identificati | Utilizzo nell'App |
| :---: | :--- | :--- | :--- |
| **`Template100.fba`** | **Car Menu Principale** | `Field 1..5`, `Value 1..5` (5 slot per telemetria/menu) | Rendering e proiezione dei parametri di bordo sul quadro strumenti |
| **`Template3.fba`** | **PIN Code Pairing** | `PIN code ?`, `Back`, `Clear`, `Done` | Dialogo ufficiale di associazione Bluetooth con PIN a 4 cifre |
| **`Template248.fba`** | **Unit Alignment** | `Would you like to synchronise the unit settings...?` | Sincronizzazione automatica unità km/h, consumi e orologio |
| **`Template250.fba`** | **Low Fuel Warning** | `Low on fuel!`, `n kilometers Left`, `Back` | Allarme riserva carburante dinamico con stima km residui |
| **`Template247.fba`** | **SMS Reader** | `SMS`, `From`, `Mr Test`, testo messaggio, `Next` | Notifica e lettura messaggi SMS di bordo |
| **`Template255.fba`** | **Head Unit Selection** | `Select Head Unit device`, `Cancel` | Ricerca e selezione della centralina Blue&Me attiva |
| **`Template113.fba`** | **Car Menu Paginato** | `Car Menu 1 of 1` | Navigazione pagine del menu auto |
| **`Template96/98`** | **Gadget Menù** | `CQuickMenuGadget`, `CMainMenuGadget` | Integrazione con i menu rapidi del volante |

---

### Architettura del Subsystem `FocusClusterHandler` (`iid = 0x46`)

Con queste informazioni, il nostro gestore `FocusClusterHandler` è in grado di inviare e ricevere frame Reflection autentici:
1. **`SetTemplate(templateId = 100)`**: Attiva la vista Car Menu sul display a matrice dell'auto.
2. **`SetProperty(propId, textValue)`**: Scrive nei campi `Value 1..5` la velocità, i consumi, il brano musicale in riproduzione o lo stato eco:Drive.
3. **`UpdateScreen()` (`fid = 0x11`)**: Forza il refresh visivo sul display del cruscotto della vettura.
4. **`SetTemplate(templateId = 250)`**: Proietta l'avviso di riserva carburante con i km residui calcolati.

---

### Architettura Completa Applicazione Android (`com.bamconnect.app`)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            BlueAndMe Connect                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                           PRESENTATION (Compose)                            │
│  ┌───────────────┐ ┌────────────────┐ ┌───────────────┐ ┌─────────────────┐ │
│  │ DashboardHUD  │ │ TripComputer   │ │ EcoDriveView  │ │ MediaController │ │
│  └───────┬───────┘ └───────┬────────┘ └───────┬───────┘ └────────┬────────┘ │
│          │                 │                  │                  │          │
│          └─────────────────┴────────┬─────────┴──────────────────┘          │
│                                     ▼                                       │
│                       DashboardViewModel & State                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                          SERVICE & DOMAIN HANDLERS                          │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                    BlueAndMeForegroundService                         │  │
│  │  - HidReportHandler (0x4D): comandi al volante (Up/Down/Enter/Back/..)│  │
│  │  - PositioningSensorHandler (0x22): velocità 10Hz, odometro, retro    │  │
│  │  - VehicleStatusHandler (0x25): chiave quadro MAR/AVV/STOP            │  │
│  │  - FocusClusterHandler (0x46): Template 100, 3, 248, 250 cruscotto    │  │
│  │  - CallControlHandler (0x45): gestione vivavoce e chiamate            │  │
│  └──────────────────────────────────┬────────────────────────────────────┘  │
├─────────────────────────────────────┼───────────────────────────────────────┤
│                          TRANSPORT & HARDWARE                               │
│  ┌──────────────────────────────────┴────────────────────────────────────┐  │
│  │ BluetoothSppManager: Socket RFCOMM SPP, loop asincrono Coroutines     │  │
│  │ SlipFrameCodec: Decodifica e codifica frame SLIP (0xC0)              │  │
│  │ SimulatedBlueAndMeDataSource: Simulazione realistica per test banco   │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────────────────────┤
│                   AUTO-UPDATER (GitHub Actions / CrealityAuto)              │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │ GitHubUpdateRepository: API Releases (Supporto token repo privati)    │  │
│  │ ApkDownloadManager & FileProvider Package Installer                   │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### Fasi di Sviluppo (al click di Procedi / Proceed)
1. **Configurazione Gradle, Package & Risorse**:
   - `applicationId = "com.bamconnect.app"`, `namespace = "com.bamconnect.app"`.
   - `metadata.json`: Nome `"BlueAndMe Connect"`.
   - Permessi Bluetooth, Foreground Service, Installazione pacchetti e `FileProvider`.
2. **Modulo Updater GitHub Actions (CrealityAuto)**:
   - Client per download release e supporto a Personal Access Token per repo privati.
3. **Core Protocollo Reflection, SLIP & Template FCS0**:
   - Codificatore SLIP `0xC0`, parser Reflection 3 byte (`iid`, `fid`), `HidReportHandler` volante, `PositioningSensorHandler` telemetria e `FocusClusterHandler` per template 100/3/248/250.
   - Simulatore dinamico attivo per collaudo immediato su emulatore e banco.
4. **UI Automotive Android Auto Minimal**:
   - HUD tachimetro/contagiri con GSI, Trip Computer A/B, eco:Drive con rating, Media Player USB con D-pad e Schermata Impostazioni con verifica aggiornamenti OTA.
5. **Compilazione & Verifica con `compile_applet`**.
