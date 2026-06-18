# Backend REST-kontrakt for Android-klient

Dette dokumentet beskriver REST-kall og DTO-er som Android-appen forventer per 2026-06-15. Det er ment som støtte ved implementasjon av backend-controllere.

Kilde i Android-prosjektet:

- `feature/login/data/remote/AuthApi.kt`
- `feature/activitywizard/data/remote/CoachActivityApi.kt`
- `feature/upload/data/remote/UploadApi.kt`
- `feature/analysis/data/remote/AnalysisApi.kt`
- tilhørende `data/dto`-pakker

## Generelle krav

Base URL i debug-build er foreløpig:

```text
http://10.0.2.2:8080/
```

Android bruker Retrofit med Kotlin Serialization:

- JSON `Content-Type`: `application/json`
- ukjente JSON-felter ignoreres av klienten
- eksplisitte `null`-verdier sendes normalt ikke fra klienten

Alle endepunkter utenom auth-kall forventes å støtte:

```http
Authorization: Bearer <accessToken>
```

`POST /api/auth/login` og `POST /api/auth/refresh` kalles uten bearer-token.

Ved `401` på autentiserte kall forsøker Android-klienten automatisk `POST /api/auth/refresh` og retryer original request én gang. Hvis refresh feiler, regnes økten som utløpt.

## Auth

### POST `/api/auth/login`

Brukes ved innlogging.

Request DTO: `LoginRequestDto`

```json
{
  "email": "coach@example.com",
  "password": "password"
}
```

Response DTO: `LoginResponseDto`

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "expiresIn": 3600
}
```

Felt:

| Felt | Type | Påkrevd | Kommentar |
| --- | --- | --- | --- |
| `accessToken` | string | ja | Må være ikke-tom. |
| `refreshToken` | string | ja | Må være ikke-tom. |
| `expiresIn` | long | ja | Må være større enn `0`. Klienten tolker dette som token-levetid. |

Forventet feilhåndtering i klient:

- `400`, `401`, `403` -> ugyldig innlogging
- `5xx` -> serverfeil
- ugyldig/manglende DTO -> ugyldig serverrespons

### POST `/api/auth/refresh`

Brukes ved token refresh.

Request DTO: `RefreshTokenRequestDto`

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

Response DTO: `LoginResponseDto`

```json
{
  "accessToken": "new-jwt-access-token",
  "refreshToken": "new-jwt-refresh-token",
  "expiresIn": 3600
}
```

Samme valideringskrav som login-responsen: tokens må være ikke-tomme, og `expiresIn` må være større enn `0`.

## Aktiviteter

Aktiviteter brukes i treneraktivitet-wizard. Android oppretter først en aktivitet, og patcher deretter tittel og kategori.

Alle aktivitetskall krever bearer-token.

### POST `/api/activities`

Oppretter en ny aktivitet.

Request DTO: `CreateCoachActivityRequestDto`

Dette er et tomt Kotlin `data object`. Backend bør akseptere tom JSON body:

```json
{}
```

Response DTO: `CoachActivityDto`

```json
{
  "activityId": "activity-123",
  "activityCategory": null,
  "title": null,
  "recordings": []
}
```

### GET `/api/activities`

Henter aktiviteter for innlogget bruker.

Response DTO: `List<CoachActivityDto>`

```json
[
  {
    "activityId": "activity-123",
    "activityCategory": "Kamp",
    "title": "G14 mot Nordstrand",
    "recordings": [
      {
        "id": "recording-123",
        "recordingType": "Audio",
        "filename": "kamp_1omgang_20260615_120000.m4a",
        "duration": 1840000
      }
    ]
  }
]
```

### GET `/api/activities/{activityId}`

Henter én aktivitet.

Path:

| Felt | Type | Påkrevd |
| --- | --- | --- |
| `activityId` | string | ja |

Response DTO: `CoachActivityDto`

```json
{
  "activityId": "activity-123",
  "activityCategory": "Trening",
  "title": "Pasningsøkt senior",
  "recordings": []
}
```

### PATCH `/api/activities/{activityId}`

Oppdaterer tittel og/eller kategori.

Path:

| Felt | Type | Påkrevd |
| --- | --- | --- |
| `activityId` | string | ja |

Request DTO: `UpdateCoachActivityRequestDto`

```json
{
  "activityCategory": "Kamp",
  "title": "G14 mot Nordstrand"
}
```

Felt:

| Felt | Type | Påkrevd | Kommentar |
| --- | --- | --- | --- |
| `activityCategory` | string/null | nei | Klienten trimmer blanke verdier til `null`. |
| `title` | string/null | nei | Klienten trimmer blanke verdier til `null`. |

Response DTO: `CoachActivityDto`

```json
{
  "activityId": "activity-123",
  "activityCategory": "Kamp",
  "title": "G14 mot Nordstrand",
  "recordings": []
}
```

### CoachActivityDto

```kotlin
data class CoachActivityDto(
    val activityId: String,
    val activityCategory: String? = null,
    val title: String? = null,
    val recordings: List<RecordingDto> = emptyList(),
)
```

JSON-felter:

| Felt | Type | Påkrevd | Kommentar |
| --- | --- | --- | --- |
| `activityId` | string | ja | Må være ikke-tom for normal klientflyt. |
| `activityCategory` | string/null | nei | Eksempelverdier i klient: `Kamp`, `Trening`, `Møte`, `Speiding`. |
| `title` | string/null | nei | Visningstittel. |
| `recordings` | array | nei | Default i klient er tom liste. |

### RecordingDto

Dette er opptakslisten som kan følge en aktivitet fra backend.

```kotlin
data class RecordingDto(
    val id: String,
    val recordingType: String,
    val filename: String,
    val duration: Long,
)
```

JSON-felter:

| Felt | Type | Påkrevd | Kommentar |
| --- | --- | --- | --- |
| `id` | string | ja | Backend-ID eller opptaks-ID. |
| `recordingType` | string | ja | Forventede verdier per nå: `Audio`, `Video`. |
| `filename` | string | ja | Filnavn som vises i appen. |
| `duration` | long | ja | Klienten viser dette som sekunder i eksisterende aktivitet-liste, men lokale opptak bruker millisekunder. Avklar gjerne enhet før backend låses. |

Aktivitet-feilkoder klienten mapper spesielt:

- `400`, `422` -> ugyldig input
- `401`, `403` -> uautorisert/session
- `404` -> ikke funnet
- `5xx` -> serverfeil

## Opplasting

Upload-API-et er definert i Android, men full UI/worker-flow er ikke ferdig implementert. Backend kan likevel bygges etter denne kontrakten.

Alle upload-kall krever bearer-token.

### POST `/api/activities/{activityId}/recordings`

Laster opp ett opptak til en aktivitet.

Path:

| Felt | Type | Påkrevd |
| --- | --- | --- |
| `activityId` | string | ja |

Request type: `multipart/form-data`

Retrofit-signatur:

```kotlin
@Multipart
@POST("api/activities/{activityId}/recordings")
suspend fun uploadRecording(
    @Path("activityId") activityId: String,
    @Part media: MultipartBody.Part,
    @Part("metadata") metadata: RequestBody,
): UploadRecordingResponseDto
```

Multipart-parts:

| Part | Type | Påkrevd | Kommentar |
| --- | --- | --- | --- |
| `media` | file | ja | Anbefalt partnavn for både lyd og video. Ny Android-kode skal bruke dette. |
| `audio` | file | nei | Midlertidig fallback som backend støtter for eldre klientkode. |
| `metadata` | JSON string/body | ja | JSON som matcher `UploadRecordingMetadataDto`. |

`activityId` hentes fra path og er autoritativ kobling mellom aktivitet og opptak. Android skal ikke sende `activityId` i metadata-bodyen.

Metadata DTO:

```json
{
  "recordingId": "local-recording-123",
  "filename": "kamp_1omgang_20260615_120000.mp4",
  "durationMillis": 1840000,
  "category": "Kamp",
  "subCategory": "1.omgang",
  "createdAtMillis": 1781517600000,
  "mediaType": "Video",
  "mimeType": "video/mp4"
}
```

`UploadRecordingMetadataDto`:

| Felt | Type | Påkrevd | Kommentar |
| --- | --- | --- | --- |
| `recordingId` | string | ja | Lokal opptaks-ID fra Android. |
| `filename` | string | ja | Display-/filnavn. |
| `durationMillis` | long | ja | Varighet i millisekunder. |
| `category` | string | ja | Aktivitetskategori. |
| `subCategory` | string | ja | Underkategori. |
| `createdAtMillis` | long | ja | Unix epoch millis. |
| `mediaType` | string | ja | Må være eksakt `Audio` eller `Video`. |
| `mimeType` | string | ja | Må starte med `audio/` når `mediaType = Audio`, og `video/` når `mediaType = Video`. |

Eksempel for lyd:

```json
{
  "recordingId": "local-recording-456",
  "filename": "kamp_1omgang_20260615_120000.m4a",
  "durationMillis": 1840000,
  "category": "Kamp",
  "subCategory": "1.omgang",
  "createdAtMillis": 1781517600000,
  "mediaType": "Audio",
  "mimeType": "audio/mp4"
}
```

Valideringsregler fra backend:

- `recordingId`, `filename`, `category`, `subCategory`, `mediaType` og `mimeType` må være utfylt.
- `durationMillis` og `createdAtMillis` må være `0` eller høyere.
- `mediaType` må være nøyaktig `Audio` eller `Video`.
- `mimeType` må starte med `audio/` når `mediaType = Audio`.
- `mimeType` må starte med `video/` når `mediaType = Video`.
- `contentUri` skal ikke sendes til backend.

Response DTO: `UploadRecordingResponseDto`

```json
{
  "uploadId": "upload-123",
  "recordingId": "local-recording-123",
  "status": "Queued",
  "statusMessage": "Opptaket er satt i kø."
}
```

Felt:

| Felt | Type | Påkrevd | Kommentar |
| --- | --- | --- | --- |
| `uploadId` | string | ja | Backend upload-ID. |
| `recordingId` | string | ja | Samme opptaks-ID som klient sendte i metadata. |
| `status` | string | ja | Må matche enum-navn i `UploadStatus`. Ukjent verdi blir `Failed` i klient. |
| `statusMessage` | string/null | nei | Visningsmelding. |

### GET `/api/uploads/{uploadId}/status`

Henter status for en upload.

Path:

| Felt | Type | Påkrevd |
| --- | --- | --- |
| `uploadId` | string | ja |

Response DTO: `UploadStatusResponseDto`

```json
{
  "uploadId": "upload-123",
  "recordingId": "local-recording-123",
  "activityId": "activity-123",
  "status": "Transcribing",
  "statusMessage": "Transkriberer opptaket.",
  "progressPercent": 65,
  "analysisId": null
}
```

Felt:

| Felt | Type | Påkrevd | Kommentar |
| --- | --- | --- | --- |
| `uploadId` | string | ja | Backend upload-ID. |
| `recordingId` | string | ja | Lokal/klientens opptaks-ID. |
| `activityId` | string/null | nei | Aktiviteten opplastingen hører til. |
| `status` | string | ja | Se statusverdier under. |
| `statusMessage` | string/null | nei | Visningsmelding. |
| `progressPercent` | int/null | nei | Bør være `0..100` hvis satt. |
| `analysisId` | string/null | nei | Settes typisk når analyse er opprettet eller ferdig. |

Forventede `UploadStatus`-verdier:

```text
Queued
Uploading
ProcessingAudio
Transcribing
Completed
Failed
```

Statusverdiene må sendes med eksakt samme casing som enum-navnene over. Ukjente verdier mappes til `Failed` i Android.

Nåværende backend-atferd i MVP:

- Upload-respons returnerer `Queued`.
- Lyd går videre via async prosessering og ender foreløpig i `Completed` med melding om at transkribering kommer senere.
- Video ender i `Completed` når filen er lagret og validert.
- `progressPercent` settes til `0`, `50` eller `100` i nåværende backendflyt.

Backend støtter disse upload-feilene:

| HTTP | `code` | Betydning |
| --- | --- | --- |
| `400` | `MISSING_MEDIA_FILE` | Multipart mangler både `media` og fallback-part `audio`. |
| `400` | `INVALID_UPLOAD_METADATA` | Metadata mangler felt, har ugyldig JSON, feil `mediaType` eller mismatch mellom `mediaType` og `mimeType`. |
| `401` | `UNAUTHORIZED` | Manglende, ugyldig eller utløpt access token. |
| `404` | `ACTIVITY_NOT_FOUND` | Aktiviteten finnes ikke eller tilhører ikke innlogget trener. |
| `404` | `UPLOAD_NOT_FOUND` | Upload finnes ikke eller tilhører ikke innlogget trener. |

Feilresponsformat:

```json
{
  "code": "INVALID_UPLOAD_METADATA",
  "message": "mimeType må matche mediaType.",
  "timestamp": "2026-06-18T19:24:00Z"
}
```

Backend lagrer media på lokal disk under:

```text
{app.storage.media-root}/activities/{activityId}/recordings/{backendRecordingId}.{ext}
```

I databasen lagrer backend blant annet:

- `activity_id`
- `client_recording_id`
- `media_type`
- `mime_type`
- `original_file_name`
- `storage_path`
- `file_size_bytes`
- `duration_millis`

Android skal bruke API-responser og ikke anta noe om serverens lokale filsti.

## Analyse

Analyse-API-et er definert, men skjermene er fortsatt enkle/under utvikling. Kontrakten bør likevel støtte liste og detalj.

Alle analysekall krever bearer-token.

### GET `/api/analyses`

Henter analyseoversikt.

Response DTO: `List<AnalysisSummaryDto>`

```json
[
  {
    "analysisId": "analysis-123",
    "activityId": "activity-123",
    "title": "G14 mot Nordstrand",
    "activityCategory": "Kamp",
    "status": "Ready",
    "createdAtMillis": 1781517600000,
    "updatedAtMillis": 1781517900000
  }
]
```

`AnalysisSummaryDto`:

| Felt | Type | Påkrevd | Kommentar |
| --- | --- | --- | --- |
| `analysisId` | string | ja | Analyse-ID. |
| `activityId` | string/null | nei | Tilknyttet aktivitet. |
| `title` | string/null | nei | Visningstittel. |
| `activityCategory` | string/null | nei | Aktivitetskategori. |
| `status` | string | ja | Må matche `AnalysisStatus`. Ukjent verdi blir `Failed`. |
| `createdAtMillis` | long | ja | Unix epoch millis. |
| `updatedAtMillis` | long | ja | Unix epoch millis. |

### GET `/api/analyses/{analysisId}`

Henter analysedetalj.

Path:

| Felt | Type | Påkrevd |
| --- | --- | --- |
| `analysisId` | string | ja |

Response DTO: `AnalysisDetailDto`

```json
{
  "analysisId": "analysis-123",
  "activityId": "activity-123",
  "title": "G14 mot Nordstrand",
  "activityCategory": "Kamp",
  "status": "Ready",
  "createdAtMillis": 1781517600000,
  "updatedAtMillis": 1781517900000,
  "content": {
    "summary": "Kort oppsummering av aktiviteten.",
    "keyPoints": [
      "Presset fungerte godt i første omgang.",
      "Laget mistet struktur etter pause."
    ]
  }
}
```

`AnalysisDetailDto`:

| Felt | Type | Påkrevd | Kommentar |
| --- | --- | --- | --- |
| `analysisId` | string | ja | Analyse-ID. |
| `activityId` | string/null | nei | Tilknyttet aktivitet. |
| `title` | string/null | nei | Visningstittel. |
| `activityCategory` | string/null | nei | Aktivitetskategori. |
| `status` | string | ja | Må matche `AnalysisStatus`. |
| `createdAtMillis` | long | ja | Unix epoch millis. |
| `updatedAtMillis` | long | ja | Unix epoch millis. |
| `content` | JSON/null | nei | Fleksibelt JSON-objekt. Android hardkoder ikke endelig analysestruktur ennå. |

Forventede `AnalysisStatus`-verdier:

```text
Pending
Ready
Failed
```

Statusverdiene må sendes med eksakt samme casing som enum-navnene over. Ukjente verdier mappes til `Failed` i Android.

## Kategorier og underkategorier brukt i opptak

Dette er ikke en egen backend-DTO ennå, men verdiene brukes i metadata og UI.

Aktivitetskategorier:

```text
Kamp
Trening
Møte
Speiding
```

Underkategorier:

| Aktivitet | Underkategorier |
| --- | --- |
| `Kamp` | `1.omgang`, `Pause`, `2.omgang` |
| `Trening` | `Spill`, `Øvelse` |
| `Møte` | `Spillermøte`, `Trenermøte` |
| `Speiding` | `Enkeltspiller`, `Motstander` |

## Klientens feiltoleranse og anbefalte backend-svar

Android håndterer disse generelle tilfellene:

- nettverksfeil -> vises som nettverksfeil
- ugyldig JSON eller manglende påkrevde DTO-felter -> ugyldig serverrespons
- `401` på autentiserte kall -> automatisk refresh og retry én gang
- refresh-feil -> bruker sendes tilbake til login

Anbefalt backend-praksis:

- Bruk `401` for utløpt/ugyldig access token.
- Bruk `403` for gyldig token uten tilgang.
- Bruk `400` eller `422` for valideringsfeil.
- Bruk `404` når aktivitet/analyse/upload ikke finnes.
- Returner DTO-er med eksakt feltnavn og casing som vist i dette dokumentet.
- Returner statusverdier med eksakt enum-casing.

## Kjente avklaringer før backend låses

- `RecordingDto.duration` fra aktivitet-respons bør avklares: klienten viser feltet som sekunder i én tekst, mens lokal opptaksmodell bruker `durationMillis`.
- `CreateCoachActivityRequestDto` er tom i klienten. Backend kan enten opprette draft-aktivitet med tom body eller kontrakten kan senere forenkles til at tittel/kategori sendes allerede ved create.
