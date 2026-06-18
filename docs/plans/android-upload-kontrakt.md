# Android upload-kontrakt

Dette dokumentet oppsummerer upload-funksjonaliteten som er implementert i backend etter fase 4 og 5. Det kan brukes som grunnlag for Retrofit-kall og DTO-er i Android-appen.

## Endepunkter

Alle upload-kall krever JWT:

```http
Authorization: Bearer <accessToken>
```

### Last opp opptak

```http
POST /api/activities/{activityId}/recordings
Content-Type: multipart/form-data
```

`activityId` hentes fra path og er den autoritative koblingen mellom aktivitet og opptak. Android skal ikke sende `activityId` i metadata-bodyen.

Multipart-parts:

| Part | Type | Påkrevd | Kommentar |
| --- | --- | --- | --- |
| `media` | file | ja | Anbefalt partnavn for både lyd og video. |
| `audio` | file | nei | Midlertidig fallback for eldre klientkode. Bruk `media` i ny Android-kode. |
| `metadata` | JSON string/body | ja | JSON som matcher `UploadRecordingMetadataDto`. |

### Hent upload-status

```http
GET /api/uploads/{uploadId}/status
```

Returnerer status for én upload, dersom uploaden tilhører innlogget trener.

## Request DTO

### UploadRecordingMetadataDto

```kotlin
@Serializable
data class UploadRecordingMetadataDto(
    val recordingId: String,
    val filename: String,
    val durationMillis: Long,
    val category: String,
    val subCategory: String,
    val createdAtMillis: Long,
    val mediaType: String,
    val mimeType: String,
)
```

Eksempel for video:

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

Valideringsregler:

- `recordingId`, `filename`, `category`, `subCategory`, `mediaType` og `mimeType` må være utfylt.
- `durationMillis` og `createdAtMillis` må være `0` eller høyere.
- `mediaType` må være nøyaktig `Audio` eller `Video`.
- `mimeType` må starte med `audio/` når `mediaType = Audio`.
- `mimeType` må starte med `video/` når `mediaType = Video`.
- `contentUri` skal ikke sendes til backend.

## Response DTO-er

### UploadRecordingResponseDto

Returneres fra `POST /api/activities/{activityId}/recordings`.

```kotlin
@Serializable
data class UploadRecordingResponseDto(
    val uploadId: String,
    val recordingId: String,
    val status: String,
    val statusMessage: String? = null,
)
```

Eksempel:

```json
{
  "uploadId": "2f13c33d-3eb5-4f17-9c43-7da8c7a10391",
  "recordingId": "local-recording-123",
  "status": "Queued",
  "statusMessage": "Opptaket er satt i kø."
}
```

`recordingId` i responsen er samme lokale ID som Android sendte i metadata.

### UploadStatusResponseDto

Returneres fra `GET /api/uploads/{uploadId}/status`.

```kotlin
@Serializable
data class UploadStatusResponseDto(
    val uploadId: String,
    val recordingId: String,
    val activityId: String? = null,
    val status: String,
    val statusMessage: String? = null,
    val progressPercent: Int? = null,
    val analysisId: String? = null,
)
```

Eksempel:

```json
{
  "uploadId": "2f13c33d-3eb5-4f17-9c43-7da8c7a10391",
  "recordingId": "local-recording-123",
  "activityId": "activity-123",
  "status": "Completed",
  "statusMessage": "Videoopptaket er lagret.",
  "progressPercent": 100,
  "analysisId": null
}
```

## Statusverdier

Backend støtter disse statusverdiene:

```text
Queued
Uploading
ProcessingAudio
Transcribing
Completed
Failed
```

Statusene sendes med eksakt casing.

Nåværende MVP-atferd:

- Upload-respons returnerer `Queued`.
- Lyd går videre via async prosessering og ender foreløpig i `Completed` med melding om at transkribering kommer senere.
- Video ender i `Completed` når filen er lagret og validert.
- `progressPercent` settes til `0`, `50` eller `100` i nåværende backendflyt.

## Retrofit-skisse

```kotlin
interface UploadApi {
    @Multipart
    @POST("api/activities/{activityId}/recordings")
    suspend fun uploadRecording(
        @Path("activityId") activityId: String,
        @Part media: MultipartBody.Part,
        @Part("metadata") metadata: RequestBody,
    ): UploadRecordingResponseDto

    @GET("api/uploads/{uploadId}/status")
    suspend fun getUploadStatus(
        @Path("uploadId") uploadId: String,
    ): UploadStatusResponseDto
}
```

Bygg `metadata` som JSON:

```kotlin
val metadataJson = json.encodeToString(metadataDto)
val metadataBody = metadataJson.toRequestBody("application/json".toMediaType())
```

Bygg filpart:

```kotlin
val mediaBody = file.asRequestBody(mimeType.toMediaType())
val mediaPart = MultipartBody.Part.createFormData(
    name = "media",
    filename = filename,
    body = mediaBody,
)
```

## Feilkoder fra backend

Vanlige feilsituasjoner:

| HTTP | `code` | Betydning |
| --- | --- | --- |
| `400` | `MISSING_MEDIA_FILE` | Multipart mangler `media` og `audio`. |
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

## Serverlagring

Backend lagrer filen på lokal disk under:

```text
{app.storage.media-root}/activities/{activityId}/recordings/{backendRecordingId}.{ext}
```

I databasen lagres metadata i `recording`, inkludert:

- `activity_id`
- `client_recording_id`
- `media_type`
- `mime_type`
- `original_file_name`
- `storage_path`
- `file_size_bytes`
- `duration_millis`

Android skal bruke API-responser og ikke anta noe om serverens lokale filsti.
