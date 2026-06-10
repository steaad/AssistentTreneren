# Grov utviklingsplan for Assistent Treneren

Dette dokumentet beskriver neste MVP-fase for Android-appen. Planen skal brukes som arbeidsliste under implementering og oppdateres fortløpende.

## Før videre arbeid

- Les `AGENTS.md` for prosjektregler, arkitekturkrav, teknologistack og AI-agent-instruksjoner.
- Les `STRUCTURE.md` for gjeldende pakke- og filstruktur.
- Bruk `Status`-seksjonen i dette dokumentet for å se hva som er implementert og hva som gjenstår.
- Oppdater checkboxer i dette dokumentet når en fase eller deloppgave fullføres.

## Status

- [x] Plan dokumentert
- [x] Login og token refresh planlagt
- [x] Auth refresh API-kontrakt definert
- [x] Automatic JWT refresh implementert
- [x] 401 retry/session-expired håndtering implementert
- [x] Auth refresh tester lagt til
- [x] Avhengigheter lagt til
- [x] Lokal lagring etablert
- [ ] Wizard koblet til ekte state/backend
- [ ] Opptak metadata lagres lokalt
- [ ] Opplasting og statuspolling implementert
- [ ] Oppsummering implementert
- [ ] Historikk og Analyse MVP implementert
- [ ] Tester lagt til
- [ ] Manuell verifikasjon fullført

## 1. Login og token refresh

- [x] Bekreft minimum backend-kontrakt for auth:
  - [x] `POST /api/auth/login`
  - [x] `POST /api/auth/refresh`
  - [x] valgfri `POST /api/auth/logout`
- [x] Utvid `AuthApi` med refresh-endepunkt.
- [x] Opprett DTO-er for refresh request og refresh response.
- [x] Utvid `AuthRepository` med `refreshTokens()`.
- [x] Implementer refresh i `AuthRepositoryImpl`.
- [x] Lagre nye access/refresh tokens atomisk i `TokenStorage`.
- [x] Endre session initialization:
  - [x] gyldig access token gir authenticated session
  - [x] utløpt access token forsøker refresh når refresh token finnes
  - [x] refresh-feil rydder tokens og sender bruker til login
- [x] Legg til OkHttp `Authenticator` eller tilsvarende refresh-komponent.
- [x] Retry original request én gang etter vellykket refresh.
- [x] Unngå refresh-loop ved gjentatt 401.
- [x] Synkroniser parallelle 401-kall slik at bare ett refresh-kall kjøres om gangen.
- [x] Behold debug-bypass `Fortsett uten backend`.
- [x] Sørg for at debug-bypass ikke lagrer falske tokens.
- [x] Sørg for at debug-bypass ikke trigger refresh-flow.
- [x] Ikke logg tokens eller sensitive auth-data.

## 2. Grunnmur

- [x] Legg til Room som lokal databaseavhengighet.
- [x] Legg til WorkManager for robust bakgrunnsopplasting.
- [x] Legg til nødvendig Hilt-integrasjon for Room og WorkManager.
- [x] Opprett databaseklasse.
- [x] Opprett entities for aktiviteter.
- [x] Opprett entities for lokale opptak.
- [x] Opprett entities for upload-jobber og upload-status.
- [x] Opprett entities for analyse-metadata.
- [x] Opprett DAO-er for aktivitet, opptak, upload og analyse.
- [x] Opprett repository-abstraksjoner over lokal lagring.
- [x] Definer domain-modeller for upload-status.
- [x] Definer mapper mellom DTO, Room entity og domain model.

## 3. Backend-kontrakt

- [ ] Dokumenter minimumsendepunkter for aktiviteter:
  - [ ] `GET /api/activities`
  - [ ] `POST /api/activities`
  - [ ] `PATCH /api/activities/{activityId}`
- [ ] Dokumenter minimumsendepunkt for opplasting:
  - [ ] `POST /api/activities/{activityId}/recordings`
- [ ] Dokumenter minimumsendepunkt for upload-status:
  - [ ] `GET /api/uploads/{uploadId}/status`
- [ ] Dokumenter minimumsendepunkter for analyse:
  - [ ] `GET /api/analyses`
  - [ ] `GET /api/analyses/{analysisId}`
- [ ] Opprett DTO-er for upload request/response.
- [ ] Opprett DTO-er for upload-status.
- [ ] Opprett DTO-er for analyse-liste og analyse-detalj.
- [ ] Opprett Retrofit API-interface for upload.
- [ ] Opprett Retrofit API-interface for analyse.
- [ ] Hold DTO-er separert fra domain models.

## 4. Trener aktivitet-wizard

- [ ] Erstatt sampledata i `CoachActivityWizardViewModel`.
- [ ] Injiser relevante use cases i wizard ViewModel.
- [ ] Last eksisterende aktiviteter fra repository.
- [ ] Vis loading-state ved henting av eksisterende aktiviteter.
- [ ] Vis feilstate og retry ved nettverksfeil.
- [ ] Opprett ny aktivitet før bruker går til opptakssteget.
- [ ] Sørg for at `activityId` alltid finnes ved opptaksstart.
- [ ] Lagre tittel og aktivitetskategori på ny aktivitet.
- [ ] Bevar state for påbegynt aktivitet.
- [ ] Sørg for at valgt eksisterende aktivitet beholder tilhørende opptak.
- [ ] Hold business logic ute av composables.
- [ ] Hold wizard-state som immutable `StateFlow`.

## 5. Opptak

- [ ] Behold eksisterende MediaStore-basert lydlagring.
- [ ] Behold eksisterende foreground service for opptak.
- [ ] Behold stoppeklokke og start/stopp-kontroller i steg 2.
- [ ] Lagre fullført `RecordingSession` persistent.
- [ ] Knytt hvert opptak til riktig `activityId`.
- [ ] Lagre metadata:
  - [ ] `recordingId`
  - [ ] `activityId`
  - [ ] `displayName`
  - [ ] `contentUri`
  - [ ] `durationMillis`
  - [ ] `category`
  - [ ] `subCategory`
  - [ ] `createdAtMillis`
- [ ] Vis fullførte opptak i upload-steget.
- [ ] Håndter opptak uten nettverk.
- [ ] Håndter app bakgrunn/forgrunn under aktivt opptak.

## 6. Opplasting

- [ ] Vis lokale opptak for valgt aktivitet i steg 3.
- [ ] La bruker velge ett opptak om gangen.
- [ ] Legg til `Last opp`-handling for valgt opptak.
- [ ] Opprett persistent upload-jobb ved opplasting.
- [ ] Bruk WorkManager for bakgrunnsopplasting.
- [ ] Foretrekk WiFi for store lydfiler.
- [ ] Støtt flere samtidige eller køede upload-jobber.
- [ ] Poll backend for upload-status.
- [ ] Vis statuspanel med flere jobber.
- [ ] Støtt statusene:
  - [ ] `Queued`
  - [ ] `Uploading`
  - [ ] `ProcessingAudio`
  - [ ] `Transcribing`
  - [ ] `Completed`
  - [ ] `Failed`
- [ ] Gi bruker retry-mulighet ved feil.
- [ ] Ikke blokker bruker fra å starte flere opplastinger.

## 7. Oppsummering, Historikk og Analyse

- [ ] Bygg ferdig steg 4 Oppsummering.
- [ ] Vis aktivitetstittel.
- [ ] Vis aktivitetskategori.
- [ ] Vis opptak knyttet til aktiviteten.
- [ ] Vis upload-statuser.
- [ ] Gi tydelig avslutning tilbake til Home.
- [ ] Lag enkel Historikk-skjerm.
- [ ] Vis aktiviteter fra lokal cache og backend.
- [ ] Lag enkel Analyse-liste.
- [ ] Lag enkel Analyse-detaljvisning.
- [ ] Presenter strukturert analyse uten å hardkode én analyseform for tidlig.

## 8. Testing og verifikasjon

- [ ] Legg til unit tests for kategori og underkategori-regler.
- [ ] Legg til unit tests for wizard-state transitions.
- [ ] Legg til unit tests for validering av tittel og kategori.
- [ ] Legg til unit tests for DTO/entity/domain-mappere.
- [ ] Legg til unit tests for upload-status mapping.
- [ ] Legg til unit tests for upload polling-resultater.
- [ ] Legg til ViewModel-tester med fakes for wizard.
- [ ] Legg til ViewModel-tester med fakes for upload-status.
- [ ] Legg til auth tester for token refresh.
- [ ] Legg til Compose/instrumented test for login til Home.
- [ ] Legg til Compose/instrumented test for start av Trener aktivitet-wizard.
- [ ] Legg til Compose/instrumented test for upload-skjerm med opptak og status.
- [ ] Manuelt verifiser mikrofontillatelse.
- [ ] Manuelt verifiser foreground service notification.
- [ ] Manuelt verifiser langt opptak.
- [ ] Manuelt verifiser app bakgrunn/forgrunn under opptak.
- [ ] Manuelt verifiser opplasting med og uten nettverk.
- [ ] Manuelt verifiser WiFi-preferanse for store lydfiler.

## Antakelser

- MVP prioriterer Trener aktivitet-flyten, opplasting/status, enkel Historikk og enkel Analysevisning.
- Backend er ikke ferdig utviklet ennå, så Android bygges mot minimumskontraktene i denne planen.
- Room og WorkManager innføres som godkjente nye avhengigheter.
- Opplasting gjøres ett opptak av gangen fra UI, men flere upload-jobber kan være aktive eller køet samtidig.
- Lyd lagres fortsatt lokalt på telefonen via MediaStore.
- Analyseproduksjon skjer på backend etter transkribering og LLM-behandling.
- Debug-bypass er midlertidig og skal holdes separat fra ekte auth.
