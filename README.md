# Assistent Treneren

## Kjør lokal backend fra fysisk Android-telefon

`devDebug` er konfigurert for lokal backend via USB med:

```text
http://127.0.0.1:8080/
```

Dette forutsetter at telefonen er koblet til utviklingsmaskinen med USB, USB-feilsøking er godkjent på telefonen, og at lokal backend kjører på port `8080`.

### Før første kjøring

1. Start den lokale backend-en på maskinen.
2. Koble telefonen med USB og godkjenn feilsøkingsdialogen.
3. Kontroller at ADB finner telefonen:

   ```powershell
   adb devices
   ```

   Telefonen skal vises med status `device`.

4. Opprett portvideresending:

   ```powershell
   adb reverse tcp:8080 tcp:8080
   adb reverse --list
   ```

   Den siste kommandoen skal vise `tcp:8080 tcp:8080`.

5. I Android Studio, velg build-varianten `devDebug` og kjør appen.

Dev-appen har pakkenavnet `com.example.assistenttreneren.dev` og kan være installert parallelt med produksjonsappen. Åpne derfor dev-appen etter installasjon.

### Kjør ADB-videresending automatisk fra Android Studio

Legg inn en ekstern kommando som *Before launch*-steg på `app`-kjøringskonfigurasjonen:

```text
Run → Edit Configurations… → app → Before launch → + → Run External tool
```

Bruk disse verdiene:

```text
Program:   <Android SDK>\platform-tools\adb.exe
Arguments: reverse tcp:8080 tcp:8080
```

På en vanlig Windows-installasjon er programmet ofte:

```text
C:\Users\<bruker>\AppData\Local\Android\Sdk\platform-tools\adb.exe
```

Da kjøres portvideresendingen hver gang du starter `devDebug` fra Android Studio. Ved flere tilkoblede enheter må du velge riktig enhet i Android Studio eller angi serienummer eksplisitt:

```powershell
adb -s <device-serial> reverse tcp:8080 tcp:8080
```

### Feilsøking

- Får appen nettverksfeil: kjør `adb reverse --list`, og kontroller at backend kjører på port 8080.
- Får appen innloggingsfeil mens Bruno fungerer lokalt: kontroller at du faktisk kjører `devDebug`, ikke `prodDebug`.
- I Logcat kan du filtrere på `OkHttp`. Login-kallet skal gå til:

  ```text
  http://127.0.0.1:8080/api/auth/login
  ```

- Portvideresendingen er knyttet til enheten. Kjør kommandoen på nytt etter at telefonen kobles fra, ADB restartes eller telefonen startes på nytt.
