# Implementeringsoppsummering

Dette dokumentet samler korte oppsummeringer etter implementerte steg i utviklingsplanen. Bruk dette sammen med `docs/plans/grov-plan.md`, `AGENTS.md` og `STRUCTURE.md` ved nye sessions.

## Login og token refresh

Implementert automatic JWT refresh for Android.

- La til `POST api/auth/refresh` i `AuthApi`.
- La til `RefreshTokenRequestDto`.
- Utvidet `AuthRepository` og `AuthRepositoryImpl` med `refreshTokens()`.
- La til `AuthTokenRefresher` og implementasjon med `Mutex`, slik at parallelle refresh-forsøk serialiseres.
- La til `AuthAuthenticator` for 401-håndtering:
  - refresher token
  - retryer original request én gang
  - utløper session hvis refresh feiler
- Delte nettverkslaget i:
  - no-auth OkHttp/Retrofit for login/refresh
  - authenticated OkHttp/Retrofit for øvrige API-er
- Oppdaterte `SessionManager.initializeSession()` slik at utløpt access token forsøker refresh før bruker sendes til login.
- Oppdaterte `docs/plans/grov-plan.md` med auth-status.

Verifisering:

- `.\gradlew.bat testDebugUnitTest` kjørte grønt.

## Steg 2 - Grunnmur

Implementert lokal database- og bakgrunnsjobb-grunnmur.

- La til Room, WorkManager og Hilt Work dependencies.
- Opprettet lokal databasegrunnmur:
  - `AssistentTrenerenDatabase`
  - entities for aktiviteter, lokale opptak, upload-jobber og analyse-metadata
  - DAO-er for aktivitet, opptak, upload og analyse
- La til Hilt `DatabaseModule`.
- La til Hilt `LocalRepositoryModule`.
- La til lokale repository-abstraksjoner og implementasjoner for:
  - aktivitet
  - opptak
  - upload
  - analyse
- La til domain-modeller for:
  - `UploadStatus`
  - `UploadJob`
  - `AnalysisStatus`
  - `AnalysisMetadata`
- La til mappere mellom Room entities og domain models.
- Konfigurerte WorkManager/Hilt i `AssistentTrenerenApplication`.
- Oppdaterte `docs/plans/grov-plan.md`:
  - `Avhengigheter lagt til`
  - `Lokal lagring etablert`
  - alle sub-steg under `## 2. Grunnmur`

Verifisering:

- `.\gradlew.bat testDebugUnitTest` kjørte grønt.

## Steg 3 - Backend-kontrakt

Implementert tynt backend-kontraktlag for upload og analyse.

- La til upload-kontrakt:
  - `UploadRecordingMetadataDto`
  - `UploadRecordingResponseDto`
  - `UploadStatusResponseDto`
  - `UploadApi`
  - `UploadStatusDtoMapper`
- La til analyse-kontrakt:
  - `AnalysisSummaryDto`
  - `AnalysisDetailDto`
  - `AnalysisApi`
  - `AnalysisDtoMapper`
- Oppdaterte `NetworkModule` slik at `UploadApi` og `AnalysisApi` tilbys via authenticated Retrofit.
- Android-kontrakten dekker nå:
  - `POST api/activities/{activityId}/recordings`
  - `GET api/uploads/{uploadId}/status`
  - `GET api/analyses`
  - `GET api/analyses/{analysisId}`
- `AnalysisDetailDto.content` bruker `JsonElement?`, slik at appen ikke låses til ett bestemt LLM-analyseformat før backend finnes.
- Oppdaterte `docs/plans/grov-plan.md`, og alle sub-steg under `## 3. Backend-kontrakt` er markert fullført.

Verifisering:

- `.\gradlew.bat testDebugUnitTest` kjørte grønt.

## Steg 4 - Trener aktivitet-wizard

Implementert ekte state/backend-kobling i wizard.

- Fjernet sampledata fra `CoachActivityWizardViewModel`.
- ViewModelen injiserer nå:
  - `GetCoachActivitiesUseCase`
  - `CreateCoachActivityUseCase`
  - `UpdateCoachActivityUseCase`
  - `LocalCoachActivityRepository`
- Eksisterende aktiviteter lastes fra lokal cache først, deretter backend.
- Backend-resultat lagres lokalt.
- Ny aktivitet opprettes før navigasjon til opptakssteget:
  - `createActivity()`
  - `updateActivity(activityId, title, category)`
  - lagres lokalt
  - `selectedActivityId` settes
- `ActivityTypeStepScreen` viser nå loading, feil og retry.
- `Neste` går via ViewModelen, slik at ny aktivitet opprettes før navigasjon.
- `AudioRecordingStepScreen` bruker `uiState.selectedActivityId`, så opptak får riktig `activityId` for både ny og eksisterende aktivitet.
- Oppdaterte `docs/plans/grov-plan.md`:
  - `Wizard koblet til ekte state/backend`
  - alle sub-steg under `## 4. Trener aktivitet-wizard`

Verifisering:

- `.\gradlew.bat testDebugUnitTest` kjørte grønt.

Merknad:

- Full offline opprettelse av ny aktivitet er bevisst utsatt til senere sync/queue-fase.

## Steg 5 - Opptak med lyd og video

Implementert videoopptak som alternativ til lydopptak i wizard steg 2.

- La til CameraX-avhengigheter:
  - `camera-camera2`
  - `camera-lifecycle`
  - `camera-video`
  - `camera-view`
- La til `CAMERA` permission.
- La til `RecordingMediaType` med:
  - `Audio`
  - `Video`
- Utvidet `RecordingSession` og `LocalRecordingEntity` med:
  - `mediaType`
  - `mimeType`
- La til Room-migrasjon `1 -> 2` for nye recording-kolonner.
- La til `CameraXVideoRecorder` som:
  - binder preview med `PreviewView`
  - bruker CameraX `Recorder`
  - lagrer video til MediaStore under `Movies/AssistentTreneren`
  - tar opp video uten lyd
- Oppdaterte lydopptak slik at metadata lagres som `Audio` / `audio/mp4`.
- Oppdaterte `RecordingViewModel` med:
  - valgt opptakstype
  - video preview binding
  - start/stopp for video
  - fortsatt start/stopp for lyd
  - lagring av fullførte lyd/video-opptak via `LocalRecordingRepository`
- Oppdaterte wizard steg 2:
  - først valg mellom `Lyd` og `Video`
  - samme underkategori-valg for begge
  - lyd viser stoppeklokke og start/stopp
  - video viser preview-vindu og start/stopp
  - hindrer bytte mellom lyd/video mens opptak pågår
- Oppdaterte `docs/plans/grov-plan.md` med videooppgaver og checkmarks.

Verifisering:

- `.\gradlew.bat testDebugUnitTest` kjørte grønt.

Gjenstår:

- Manuell test på fysisk enhet eller emulator med kamera for å bekrefte preview og faktisk videoopptak.
