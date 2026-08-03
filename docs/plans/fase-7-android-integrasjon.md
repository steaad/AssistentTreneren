# Fase 7 – Android-integrasjon for transkripsjonsoppsummering

Dette er en midlertidig integrasjonsguide for steg 4 – **Oppsummering** – i treneraktivitets-wizarden.

Backend transkriberer kun lydopptak. Når transkripsjonen er ferdig, får Android både automatisk opprettede observasjoner og parseravvik som treneren kan korrigere eller avvise.

## 1. Fra opplasting til oppsummering

Android bruker den eksisterende totrinns opplastingsflyten:

1. `POST /api/activities/{activityId}/uploads` oppretter uploaden.
2. `POST /api/uploads/{uploadId}/media` laster opp lydfilen som multipart-feltet `media`.
3. `GET /api/uploads/{uploadId}/status` poller behandlingsstatus.

For lyd går status normalt slik:

```text
Uploading → Queued → Transcribing → Completed
```

Når status er `Completed`, kan Android hente oppsummeringsgrunnlaget. Ved `Failed` kan Android tilby nytt forsøk med `POST /api/uploads/{uploadId}/retry`.

Alle kall under krever headeren:

```http
Authorization: Bearer <accessToken>
```

## 2. Hent data for steg 4 – Oppsummering

```http
GET /api/activities/{activityId}/transcription-review
```

Responsen inneholder bare lydopptak med ferdig transkripsjon. Ett aktivitetsopptak kan ha full transkripsjon, automatiske observasjoner og null eller flere ventende parseravvik.

```json
{
  "activityId": "activity-123",
  "recordings": [
    {
      "recordingId": "recording-123",
      "transcriptionId": "transcription-123",
      "transcriptText": "Full transkribert tekst ...",
      "events": [
        {
          "eventId": "event-123",
          "text": "Spille oss fremover i banen Midtstopper tar ikke ut nok dybde",
          "startMillis": 12340,
          "endMillis": 18450,
          "manuallyEdited": false
        }
      ],
      "issues": [
        {
          "issueId": "issue-123",
          "issueType": "MISSING_END_MARKER",
          "candidateText": "Spille oss fremover i banen Midtstopper tar ikke ut nok dybde",
          "contextBefore": "...",
          "contextAfter": "...",
          "startMillis": 25340,
          "endMillis": 32450,
          "status": "Pending"
        }
      ]
    }
  ]
}
```

`issues` inneholder bare avvik med status `Pending`. Avviste og løste avvik returneres ikke i dette oppslaget.

## 3. Visning i Android

Anbefalt oppsett i oppsummeringssteget:

- Vis automatiske `events` som ferdige observasjoner treneren kan lese, redigere eller slette.
- Vis `issues` i en egen seksjon, for eksempel **Trenger gjennomgang**.
- Vis `candidateText`, `contextBefore`, `contextAfter` og tidsrom der det finnes. Dette er viktig fordi rå lydfilen kan være slettet etter vellykket transkripsjon.
- La treneren velge **Lagre korrigert observasjon** eller **Avvis** for hvert avvik.

Parseravvikene betyr:

| `issueType` | Betydning i UI |
|---|---|
| `MISSING_END_MARKER` | `Tag` ble funnet, men `Slutt` manglet. Kandidatteksten kan ofte lagres etter kontroll. |
| `NESTED_START_MARKER` | En ny `Tag` kom før forrige observasjon ble avsluttet. Kandidaten gjelder den avbrutte observasjonen. |
| `END_WITHOUT_START_MARKER` | `Slutt` ble funnet uten en aktiv `Tag`. Treneren må normalt opprette observasjonen manuelt eller avvise avviket. |
| `EMPTY_EVENT` | `Tag` og `Slutt` kom uten tekst mellom. Avvises normalt. |

## 4. Løs et parseravvik

Når treneren har korrigert en kandidat, oppretter Android en gyldig observasjon og løser avviket i ett kall:

```http
POST /api/transcription-event-issues/{issueId}/resolve
Content-Type: application/json
```

```json
{
  "text": "Midtstopper tar ikke ut nok dybde for å skaffe tid og rom.",
  "startMillis": 25340,
  "endMillis": 32450
}
```

Krav:

- `text` må være utfylt.
- `startMillis` og `endMillis` må være oppgitt.
- Starttid må være null eller positiv.
- Sluttid må være lik eller senere enn starttid.

Ved vellykket kall oppretter backend en observasjon, setter avviket til `Resolved` og svarer `200 OK` uten response body. Hent review-data på nytt etterpå, eller oppdater lokal UI-state optimistisk.

Feil:

| Status | Kode | Betydning |
|---|---|---|
| `400` | `INVALID_TRANSCRIPTION_EVENT` | Tekst eller tidsrom er ugyldig. |
| `404` | `TRANSCRIPTION_EVENT_ISSUE_NOT_FOUND` | Avviket finnes ikke, er allerede håndtert eller tilhører en annen bruker. |

## 5. Avvis et parseravvik

```http
POST /api/transcription-event-issues/{issueId}/dismiss
```

Kallet har ingen body. Backend setter avviket til `Dismissed` og svarer `200 OK`. Avviket vises ikke ved neste `transcription-review`-oppslag.

## 6. Rediger eller slett en automatisk observasjon

Redigering:

```http
PATCH /api/transcription-events/{eventId}
Content-Type: application/json
```

```json
{
  "text": "Korrigert observasjon.",
  "startMillis": 12340,
  "endMillis": 18450
}
```

Ved redigering blir `manuallyEdited` satt til `true`.

Sletting:

```http
DELETE /api/transcription-events/{eventId}
```

Begge kallene gir `404 TRANSCRIPTION_EVENT_NOT_FOUND` dersom observasjonen ikke finnes eller brukeren ikke eier aktiviteten.

## 7. Anbefalt klientflyt

```text
Poll upload-status
  ├─ Completed → hent transcription-review → vis observasjoner og avvik
  ├─ Failed    → vis feil og eventuelt «Prøv igjen»
  └─ ellers    → vis pågående behandling

I Oppsummering
  ├─ rediger/slett automatisk observasjon ved behov
  ├─ løs eller avvis hvert parseravvik
  └─ hent review-data på nytt når alle brukerhandlinger er ferdige
```

Det finnes foreløpig ingen krav om at alle avvik må være løst eller avvist før brukeren kan gå videre i wizarden. Android bør vise antall ventende avvik tydelig, men la treneren bestemme om de skal behandles nå.
