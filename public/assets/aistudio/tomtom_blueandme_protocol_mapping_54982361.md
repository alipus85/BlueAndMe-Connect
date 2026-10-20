# Mappatura Protocollo TomTom Blue&Me: Routine di Smistamento, IID e FID

Questo documento fornisce l'analisi esaustiva della routine di smistamento dei pacchetti Reflection e la tabella completa di decodifica di tutti gli **Interface ID (`iid`)** e **Function ID (`fid`)** estratti dal demone [`carlinkd`](file:///D:/RE_BlueAndMe/firmware/carlink/carlinkd).

---

## 1. Routine e Struttura Dati di Smistamento

### 1.1 Routine di Ricezione e Validazione (`ReflectionMessageDispatcher`)
Nel demone `carlinkd`, i pacchetti provenienti dalla connessione RFCOMM Bluetooth vengono analizzati dalla funzione a indirizzo virtuale **`0x0000d52c`** (`ReflectionMessageDispatcher`):

```c
// Decompilazione Ghidra (ARM LE 32-bit) da carlinkd:0x0000d52c
undefined4 ReflectionMessageDispatcher(uint msg_size, undefined4 payload_buffer)
{
  int handler;
  
  // 1. Controllo dimensione minima header
  if (msg_size < 3) {
    // Errore: "Incoming message's size < Reflection header. Message rejected."
    Log(DAT_0000d6c8);
    return 1;
  }

  // 2. Deserializzazione header Reflection (3 byte)
  stream = Stream_Create(payload_buffer, msg_size);
  Stream_ReadUint16(stream, &iid);  // Estrae IID (2 byte Little-Endian)
  Stream_ReadUint8(stream, &fid);   // Estrae FID (1 byte)
  Stream_Destroy(stream);

  // 3. Validazione e ricerca dell'Handler registrato
  handler = ValidateAndLookupHandler(iid, fid);
  if (handler == 0) {
    // Recupera i nomi testuali per il tracciamento
    GetInterfaceName(iid, &iface_name);
    GetMethodName(iid, fid, &method_name);
    
    // Log di successo:
    // "iid = %d (%s)"
    // "fid = %d (%s)"
    return 0;
  }

  // Errore: "Unable to validate incoming message as reflection message 
  //         (message size = %d, iid = %d, fid = %d)"
  Log(DAT_0000d6c0, msg_size, iid, fid);
  return 1;
}
```

### 1.2 Formato dell'Header del Frame Reflection
L'header del protocollo Reflection è compatto ed è composto esattamente da **3 byte**:

```
+--------------------------------+-----------------+
|          iid (uint16)          |   fid (uint8)   |
|         Byte 0  -  Byte 1      |     Byte 2      |
+--------------------------------+-----------------+
```
- **`iid` (Interface ID)**: 16 bit (2 byte in Little-Endian) che identificano il sottosistema (es. `0x0022` per sensori telemetrici, `0x004D` per comandi al volante).
- **`fid` (Function ID)**: 8 bit (1 byte) che identificano l'operazione/evento all'interno dell'interfaccia.
- **`Payload`**: a partire dal byte 3 seguono gli argomenti serializzati.

### 1.3 Struttura Dati Centrale (Tabella Descrittori)
I metadati delle interfacce risiedono nella sezione `.data` a partire dall'indirizzo **`0x0008eb18`** (file offset `0x086b18`).
La tabella è costituita da 21 record da **`0xF8` byte (248 byte)** ciascuno, con la seguente struttura:

```c
struct ReflectionInterfaceDescriptor {
    uint32_t iid;               // Interface ID (es. 0x22, 0x4D, 0x46)
    const char *name;           // Puntatore a stringa (es. "iPositioningSensor")
    struct MethodEntry {
        uint32_t fid;           // Function ID (0xFF = terminatore tabella)
        const char *name;       // Nome del metodo (es. "SesnorData")
        uint32_t direction;     // 1 = CALL/RICHIESTA (->), 0 = EVENTO/RISPOSTA (<-)
    } methods[20];
};
```

---

## 2. Elenco Riassuntivo delle Costanti `iid` (Interface ID)

| IID (Hex) | IID (Dec) | Nome Interfaccia | Area Funzionale / Descrizione |
| :---: | :---: | :--- | :--- |
| **`0x01`** | 1 | `iInterfacePublication` | Negoziazione e pubblicazione delle interfacce attive |
| **`0x22`** | 34 | `iPositioningSensor` | **Telemetria CAN-bus** (velocità ruote, retromarcia, odometro) |
| **`0x25`** | 37 | `iVehicleStatus` | Stato generale veicolo (quadro MAR/AVV, alimentazione) |
| **`0x32`** | 50 | `iHmiRemoter` | Invocazione remota comandi HMI |
| **`0x34`** | 52 | `iBasicPlanner` | Pianificazione itinerario da remoto |
| **`0x36`** | 54 | `iNavObject` | Gestione handle oggetti di navigazione |
| **`0x37`** | 55 | `iExternalGuidanceAdvice` | **Indicazioni di svolta su display cruscotto** |
| **`0x43`** | 67 | `iAuthentication` | Handshake e autenticazione iniziale Blue&Me |
| **`0x44`** | 68 | `iFeature` | Query e impostazione stato funzionalità veicolari |
| **`0x45`** | 69 | `iCallControl` | Gestione chiamate e vivavoce telefonico |
| **`0x46`** | 70 | `iFocus` | **Gestione display cruscotto e template grafici** |
| **`0x47`** | 71 | `iLocation` | Lettura e conversione coordinate GPS (WGS84) |
| **`0x48`** | 72 | `iLocationCategory` | Categorie e raggruppamenti POI |
| **`0x49`** | 73 | `iLocationStorage` | Salvataggio e recupero posizioni preferite |
| **`0x4A`** | 74 | `iRoute` | Percorso attivo, distanze e riepilogo viaggio |
| **`0x4B`** | 75 | `iRouteInstruction` | Istruzioni dettagliate di navigazione |
| **`0x4D`** | 77 | `iHidReport` | **Comandi al volante (Tasti fisici vettura)** |
| **`0x4E`** | 78 | `iRouteConstructor` | Costruzione/distruzione dinamica percorso |
| **`0x50`** | 80 | `iSetting` | Impostazioni e preferenze di sincronizzazione |
| **`0xFE`** | 254 | `iThrouputtest` | Test throughput canale RFCOMM |
| **`0x100`** | 256 | `CarLink` | Ping/Handshake ad alto livello del demone |

---

## 3. Mappatura Completa Metodi per Singolo `iid`

### 3.1 Comandi al Volante & Input (`iHidReport` - `iid = 0x4D`)
Gestisce l'arrivo dei pacchetti relativi ai pulsanti presenti sulle razze del volante.

| FID (Hex) | FID (Dec) | Tipo Flusso | Nome Metodo | Descrizione |
| :---: | :---: | :---: | :--- | :--- |
| `0x01` | 1 | `CALL (->)` | `DeviceAvailable` | Notifica periferica di input volante disponibile |
| `0x02` | 2 | `CALL (->)` | `DeviceUnavailable` | Notifica periferica non disponibile |
| `0x03` | 3 | `CALL (->)` | `Report` | **Evento pressione tasto** (`RequestId`, `aKey`) |

> [!NOTE]
> All'interno di `Report` (`0x03`), i codici tasto identificati (`aKey`) sono:
> - `0x01`: Tasto ESC / Indietro (Back)
> - `0x02`: Freccia SU (Up)
> - `0x03`: Freccia GIÙ (Down)
> - `0x04`: Freccia SINISTRA (Left)
> - `0x05`: Tasto OK / Conferma (Enter)
> - `0x18`: Tasto Telefono / Rubrica
> - `0x1a`: Tasto Menu / Source
> - `0x1d`: Tasto Comando Vocale (PTT)

---

### 3.2 Telemetria CAN-bus (`iPositioningSensor` - `iid = 0x22`)
Consente all'applicazione di ricevere i dati in tempo reale dai sensori dell'auto.

| FID (Hex) | FID (Dec) | Tipo Flusso | Nome Metodo | Descrizione |
| :---: | :---: | :---: | :--- | :--- |
| `0x01` | 1 | `CALL (->)` | `Subscribe` | Sottoscrizione al flusso dati sensori |
| `0x02` | 2 | `EVENT/RESP (<-)` | `Configuration` | Dati di configurazione sensori veicolo |
| `0x03` | 3 | `EVENT/RESP (<-)` | `SesnorData` | **Dati CAN**: velocità ruote, stato retromarcia, odometro |

---

### 3.3 Display Cruscotto e Finestre (`iFocus` - `iid = 0x46`)
Controlla la visualizzazione sul display a matrice del quadro strumenti (cluster display) e gestisce i template grafici `focus/*.fba`.

| FID (Hex) | FID (Dec) | Tipo Flusso | Nome Metodo | Descrizione |
| :---: | :---: | :---: | :--- | :--- |
| `0x01` | 1 | `EVENT/RESP (<-)` | `Init` | Inizializzazione del sottosistema display |
| `0x05` | 5 | `EVENT/RESP (<-)` | `RequestFullMode` | Richiesta visualizzazione a schermo intero |
| `0x06` | 6 | `EVENT/RESP (<-)` | `ReleaseFullMode` | Rilascio modalità schermo intero |
| `0x07` | 7 | `CALL (->)` | `LostFullMode` | Notifica perdita focus schermo |
| `0x08` | 8 | `CALL (->)` | `AssignFullMode` | Assegnazione focus schermo |
| `0x0B` | 11 | `EVENT/RESP (<-)` | `SetTemplate` | Attivazione template cruscotto (es. 100 = Car Menu) |
| `0x0C` | 12 | `CALL (->)` | `SetHostTemplate` | Conferma template assegnato dalla vettura |
| `0x0D` | 13 | `EVENT/RESP (<-)` | `SetProperty` | Impostazione valore testo/icona nel template |
| `0x0E` | 14 | `EVENT/RESP (<-)` | `SetIndexedProperty` | Impostazione proprietà indicizzata (voci lista) |
| `0x0F` | 15 | `CALL (->)` | `RaiseEvent` | Invio evento interfaccia (es. click elemento) |
| `0x10` | 16 | `CALL (->)` | `RaiseIndexedEvent` | Invio evento indice selezionato |
| `0x11` | 17 | `EVENT/RESP (<-)` | `UpdateScreen` | Commit e refresh grafico su display quadro |
| `0x12` | 18 | `EVENT/RESP (<-)` | `ResetProperties` | Ripristino campi template |
| `0x13` | 19 | `EVENT/RESP (<-)` | `ResetIndexedProperties` | Ripristino liste |
| `0x14` | 20 | `EVENT/RESP (<-)` | `GetStatus` | Richiesta stato visualizzazione |
| `0x15` | 21 | `CALL (->)` | `Status` | Invio stato display |
| `0x7F` | 127 | `CALL (->)` | `Reply` | Ack/Risposta generica |

---

### 3.4 Frecce e Guida Cruscotto (`iExternalGuidanceAdvice` - `iid = 0x37`)

| FID (Hex) | FID (Dec) | Tipo Flusso | Nome Metodo | Descrizione |
| :---: | :---: | :---: | :--- | :--- |
| `0x01` | 1 | `CALL (->)` | `StartAdvice` | Inizio invio frecce di manovra al cruscotto |
| `0x02` | 2 | `CALL (->)` | `StopAdvice` | Termine indicazioni di manovra |
| `0x03` | 3 | `EVENT/RESP (<-)` | `Advice` | Frame dati manovra (icona svolta, distanza, nome via) |

---

### 3.5 Handshake e Autenticazione (`iAuthentication` - `iid = 0x43`)

| FID (Hex) | FID (Dec) | Tipo Flusso | Nome Metodo | Descrizione |
| :---: | :---: | :---: | :--- | :--- |
| `0x01` | 1 | `CALL (->)` | `Request` | Avvio richiesta autenticazione |
| `0x02` | 2 | `CALL (->)` | `Challenge` | Invio del token/challenge cifrato |
| `0x03` | 3 | `CALL (->)` | `Proof` | Prova di autenticazione |
| `0x04` | 4 | `CALL (->)` | `Result` | Esito autenticazione (Success/Fail) |
| `0x05` | 5 | `CALL (->)` | `Abort` | Interruzione sessione di auth |

---

### 3.6 Pubblicazione Interfacce (`iInterfacePublication` - `iid = 0x01`)

| FID (Hex) | FID (Dec) | Tipo Flusso | Nome Metodo | Descrizione |
| :---: | :---: | :---: | :--- | :--- |
| `0x01` | 1 | `CALL (->)` | `PublishInterfaces` | Negoziazione degli IID supportati dai due endpoint |

---

### 3.7 Gestione Chiamate Vivavoce (`iCallControl` - `iid = 0x45`)

| FID (Hex) | FID (Dec) | Tipo Flusso | Nome Metodo | Descrizione |
| :---: | :---: | :---: | :--- | :--- |
| `0x00` | 0 | `CALL (->)` | `Create` | Avvia composizione chiamata |
| `0x01` | 1 | `CALL (->)` | `Accept` | Rispondi a chiamata in arrivo |
| `0x02` | 2 | `CALL (->)` | `Delete` | Rifiuta/Chiudi chiamata |
| `0x03` | 3 | `CALL (->)` | `Deleted` | Notifica terminazione chiamata |
| `0x04` | 4 | `CALL (->)` | `Hold` | Metti chiamata in attesa |
| `0x05` | 5 | `CALL (->)` | `Signal` | Segnalazione stato linea |

---

### 3.8 Percorso e Istruzioni (`iRoute` - `0x4A`, `iRouteInstruction` - `0x4B`)

#### `iRoute` (`iid = 0x4A`):
- `0x01`: `ActiveRoute` (Informa l'auto che esiste una rotta attiva)
- `0x02`: `GetRouteList` / `0x03`: `RouteList`
- `0x04`: `ProgressOnRoute` (Avanzamento chilometrico e tempo stimato ETA)
- `0x05`: `LocationHandle`
- `0x06`: `GetLocationDistance` / `0x07`: `LocationDistance`
- `0x08`: `Release`
- `0x09`: `GetLocationHandle`
- `0x0A`: `RequestActiveRoute`
- `0x0B`: `GetRouteSummary` / `0x0C`: `RouteSummary`

#### `iRouteInstruction` (`iid = 0x4B`):
- `0x01`: `GetInstructions` (Richiesta dell'auto delle istruzioni di navigazione)
- `0x02`: `Instructions` (Invio del testo e delle icone della manovra)

---

### 3.9 Impostazioni e Sincronizzazione (`iSetting` - `iid = 0x50`)

| FID (Hex) | FID (Dec) | Tipo Flusso | Nome Metodo | Descrizione |
| :---: | :---: | :---: | :--- | :--- |
| `0x04` | 4 | `EVENT/RESP (<-)` | `ReportResetToFactoryDefault` | Reset impostazioni di fabbrica |
| `0x05` | 5 | `CALL (->)` | `Set` | Scrittura di un parametro veicolo/dispositivo |
| `0x06` | 6 | `CALL (->)` | `Get` | Lettura di un parametro |
| `0x07` | 7 | `EVENT/RESP (<-)` | `Report` | Notifica valore parametro |
