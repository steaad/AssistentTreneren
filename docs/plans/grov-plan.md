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
- [x] Wizard koblet til ekte state/backend
- [x] Opptak metadata lagres lokalt
- [x] Opplasting og statuspolling implementert
- [x] Oppsummering implementert
- [x] Transkripsjonsgjennomgang i oppsummeringssteget implementert
- [ ] Historikk og Analyse MVP implementert
- [ ] Full offline opprettelse av ny aktivitet planlagt for senere fase
- [x] Tester lagt til
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

- [x] Dokumenter minimumsendepunkter for aktiviteter:
  - [x] `GET /api/activities`
  - [x] `POST /api/activities`
  - [x] `PATCH /api/activities/{activityId}`
- [x] Dokumenter totrinns opplastingsflyt:
  - [x] `POST /api/activities/{activityId}/uploads`
  - [x] `POST /api/uploads/{uploadId}/media`
- [x] Dokumenter minimumsendepunkt for upload-status:
  - [x] `GET /api/uploads/{uploadId}/status`
- [x] Dokumenter minimumsendepunkter for analyse:
  - [x] `GET /api/analyses`
  - [x] `GET /api/analyses/{analysisId}`
- [x] Opprett DTO-er for upload request/response.
- [x] Opprett DTO-er for upload-status.
- [x] Opprett DTO-er for analyse-liste og analyse-detalj.
- [x] Opprett Retrofit API-interface for upload.
- [x] Opprett Retrofit API-interface for analyse.
- [x] Opprett Retrofit API-interface for transkripsjonsgjennomgang og observasjonshandlinger.
- [x] Hold DTO-er separert fra domain models.

## 4. Trener aktivitet-wizard

- [x] Erstatt sampledata i `CoachActivityWizardViewModel`.
- [x] Injiser relevante use cases i wizard ViewModel.
- [x] Last eksisterende aktiviteter fra repository.
- [x] Vis loading-state ved henting av eksisterende aktiviteter.
- [x] Vis feilstate og retry ved nettverksfeil.
- [x] Opprett ny aktivitet før bruker går til opptakssteget.
- [x] Sørg for at `activityId` alltid finnes ved opptaksstart.
- [x] Lagre tittel og aktivitetskategori på ny aktivitet.
- [x] Bevar state for påbegynt aktivitet.
- [x] Sørg for at valgt eksisterende aktivitet beholder tilhørende opptak.
- [x] Hold business logic ute av composables.
- [x] Hold wizard-state som immutable `StateFlow`.

## 5. Opptak

- [x] Legg til CameraX-avhengigheter for videoopptak.
- [x] Legg til kamera-permission.
- [x] Legg til opptakstype i domain:
  - [x] `RecordingMediaType.Audio`
  - [x] `RecordingMediaType.Video`
- [x] Behold eksisterende MediaStore-basert lydlagring.
- [x] Behold eksisterende foreground service for lydopptak.
- [x] Legg til MediaStore-basert videolagring via CameraX.
- [x] Sørg for at video tas opp uten lyd.
- [x] Legg til lite video-preview-vindu i wizard steg 2.
- [x] La bruker velge opptakstype først i wizard steg 2:
  - [x] `Lyd`
  - [x] `Video`
- [x] Bruk samme underkategorier for lyd og video.
- [x] Vis lydpanel ved lydvalg:
  - [x] stoppeklokke
  - [x] start/stopp
- [x] Vis videopanel ved videovalg:
  - [x] preview-vindu
  - [x] start/stopp
- [x] Hindre samtidig lyd og video fra samme enhet.
- [x] Lagre fullført `RecordingSession` persistent.
- [x] Knytt hvert opptak til riktig `activityId`.
- [x] Lagre metadata:
  - [x] `recordingId`
  - [x] `activityId`
  - [x] `displayName`
  - [x] `contentUri`
  - [x] `mediaType`
  - [x] `mimeType`
  - [x] `durationMillis`
  - [x] `category`
  - [x] `subCategory`
  - [x] `createdAtMillis`
- [x] Vis fullførte opptak i upload-steget.
- [x] Håndter opptak uten nettverk.
- [x] Håndter app bakgrunn/forgrunn under aktivt lydopptak.
- [ ] Manuelt verifiser video preview og videoopptak på fysisk enhet.

## 6. Opplasting

- [x] Vis lokale opptak for valgt aktivitet i steg 3.
- [x] La bruker velge ett opptak om gangen.
- [x] Legg til `Last opp`-handling for valgt opptak.
- [x] Opprett persistent upload-jobb ved opplasting.
- [x] Bruk WorkManager for bakgrunnsopplasting.
- [ ] Foretrekk WiFi for store lydfiler.
- [x] Støtt flere samtidige eller køede upload-jobber.
- [x] Poll backend for upload-status.
- [x] Vis statuspanel med flere jobber.
- [x] Støtt statusene:
  - [x] `Queued`
  - [x] `Uploading`
  - [x] `ProcessingAudio`
  - [x] `Transcribing`
  - [x] `Completed`
  - [x] `Failed`
- [x] Gi bruker retry-mulighet ved feil.
- [x] Ikke blokker bruker fra å starte flere opplastinger.

## 7. Oppsummering, transkripsjon, Historikk og Analyse

- [x] Bygg ferdig steg 4 Oppsummering.
- [x] Vis aktivitetstittel.
- [x] Vis aktivitetskategori.
- [x] Vis opptak knyttet til aktiviteten.
- [x] Vis upload-statuser.
- [x] Gi tydelig avslutning tilbake til Home.
- [x] Gjør seksjonene Opptak og Opplasting utvidbare, lukket som standard.
- [x] Legg til åpen Transkripsjon-seksjon i steg 4.
- [x] Hent ferdige transkripsjoner med `GET /api/activities/{activityId}/transcription-review`.
- [x] Vis transkripsjonstekst, automatiske observasjoner og ventende parseravvik.
- [x] Vis transkripsjonsstatus og antall ventende avvik i aktivitetskortet.
- [x] Støtt redigering og sletting av automatiske observasjoner.
- [x] Støtt løsning og avvisning av parseravvik.
- [x] Oppdater review-data etter vellykket transkripsjonshandling.
- [x] Tillat at brukeren fullfører wizarden med ventende parseravvik.
- [x] Legg til unit tests for transkripsjons-DTO-er og mapper.
- [ ] Legg til unit tests for validering av transkripsjonshandlinger.
- [ ] Legg til ViewModel-tester med fakes for review-innhenting og transkripsjonshandlinger.
- [ ] Legg til Compose/instrumented tester for utvidbare seksjoner og transkripsjonsdialoger.
- [ ] Manuelt verifiser transkripsjonsflyt, observasjonsendringer og avvikshåndtering mot backend.
- [ ] Lag enkel Historikk-skjerm.
- [ ] Vis aktiviteter fra lokal cache og backend.
- [ ] Lag enkel Analyse-liste.
- [ ] Lag enkel Analyse-detaljvisning.
- [ ] Presenter strukturert analyse uten å hardkode én analyseform for tidlig.

## 8. Testing og verifikasjon

- [x] Legg til unit tests for kategori og underkategori-regler.
- [ ] Legg til unit tests for wizard-state transitions.
- [x] Legg til unit tests for validering av tittel og kategori.
- [x] Legg til unit tests for DTO/domain-mappere for transkripsjonsgjennomgang.
- [ ] Legg til unit tests for entity-mappere.
- [x] Legg til unit tests for upload-status mapping.
- [ ] Legg til unit tests for upload polling-resultater.
- [x] Legg til ViewModel-tester med fakes for wizard.
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

## 9. Fremtidig offline synk

- [ ] Implementer full offline opprettelse av ny aktivitet.
- [ ] Opprett lokal midlertidig aktivitets-ID for aktiviteter laget uten nett.
- [ ] Legg til sync-status for aktivitet:
  - [ ] `PendingCreate`
  - [ ] `Synced`
  - [ ] `Failed`
- [ ] Legg aktivitet-opprettelse i lokal sync-kø når backend ikke er tilgjengelig.
- [ ] Synkroniser aktivitet til backend når nett er tilbake.
- [ ] Lagre mapping fra lokal aktivitets-ID til backend `activityId`.
- [ ] Oppdater lokale opptak som peker på midlertidig aktivitets-ID etter vellykket sync.
- [ ] Oppdater upload-jobber som peker på midlertidig aktivitets-ID etter vellykket sync.
- [ ] Håndter retry og feilstatus for aktiviteter som ikke kan synkroniseres.
- [ ] Legg til tester for lokal aktivitet, sync-kø og ID-mapping.

## Antakelser

- MVP prioriterer Trener aktivitet-flyten, opplasting/status, enkel Historikk og enkel Analysevisning.
- Backend er ikke ferdig utviklet ennå, så Android bygges mot minimumskontraktene i denne planen.
- Room og WorkManager innføres som godkjente nye avhengigheter.
- Opplasting gjøres ett opptak av gangen fra UI, men flere upload-jobber kan være aktive eller køet samtidig.
- Lyd lagres fortsatt lokalt på telefonen via MediaStore.
- Analyseproduksjon skjer på backend etter transkribering og LLM-behandling.
- Debug-bypass er midlertidig og skal holdes separat fra ekte auth.
- Full offline opprettelse av ny aktivitet utsettes til senere sync/queue-fase.
