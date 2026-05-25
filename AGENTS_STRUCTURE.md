# Football Analysis Tool - AI Agent Instructions for file and folder structure

# Purpose

This document defines the physical project structure and package organization.

For architecture principles, coding rules, technology decisions, and AI behavior:
See AGENTS_BASE.md

AGENTS_STRUCTURE.md is the source of truth for:
- package structure
- feature organization
- file placement
- naming conventions

```text
app/
 └── src/main/java/no/dittfirma/fotballanalyse/

      MainActivity.kt

      core/
      │
      ├── network/
      │    ├── ApiClient.kt
      │    ├── AuthInterceptor.kt
      │    ├── NetworkModule.kt
      │    └── ResultWrapper.kt
      │
      ├── auth/
      │    ├── AuthManager.kt
      │    ├── TokenStorage.kt
      │    ├── SessionState.kt
      │    └── JwtDecoder.kt
      │
      ├── media/
      │    ├── VideoProcessor.kt
      │    ├── AudioAnalyzer.kt
      │    ├── WaveformGenerator.kt
      │    ├── MediaMetadataReader.kt
      │    └── ThumbnailGenerator.kt
      │
      ├── database/
      ├── datastore/
      ├── permissions/
      ├── logging/
      ├── utils/
      └── extensions/

      feature/
      │
      ├── login/
      │    ├── presentation/
      │    │    ├── LoginScreen.kt
      │    │    ├── LoginViewModel.kt
      │    │    ├── LoginUiState.kt
      │    │    ├── LoginEvent.kt
      │    │    ├── LoginAction.kt
      │    │    └── components/
      │    │
      │    ├── domain/
      │    │    ├── model/
      │    │    ├── repository/
      │    │    └── usecase/
      │    │         ├── LoginUseCase.kt
      │    │         ├── ValidateTokenUseCase.kt
      │    │         └── LogoutUseCase.kt
      │    │
      │    ├── data/
      │    │    ├── remote/
      │    │    ├── local/
      │    │    ├── mapper/
      │    │    ├── dto/
      │    │    └── repository/
      │    │
      │    └── navigation/
      │
      ├── onboarding/
      │
      ├── activitywizard/
      │    ├── presentation/
      │    │    ├── WizardScreen.kt
      │    │    ├── WizardViewModel.kt
      │    │    ├── WizardState.kt
      │    │    ├── components/
      │    │    └── steps/
      │    │         ├── SelectActivityStep.kt
      │    │         ├── ActivityTypeStep.kt
      │    │         ├── MediaSelectionStep.kt
      │    │         └── SummaryStep.kt
      │    │
      │    ├── domain/
      │    └── data/
      │
      ├── mediaimport/
      │
      ├── tagging/
      │    ├── presentation/
      │    │    ├── TaggingScreen.kt
      │    │    ├── TimelineView.kt
      │    │    ├── AudioWaveform.kt
      │    │    ├── TagEditor.kt
      │    │    ├── TagList.kt
      │    │    └── CoachingNoteView.kt
      │    │
      │    ├── domain/
      │    └── data/
      │
      ├── analysis/
      │    ├── presentation/
      │    ├── domain/
      │    │    └── usecase/
      │    │         ├── AnalyzeMatchUseCase.kt
      │    │         ├── DetectEventsUseCase.kt
      │    │         ├── GenerateInsightsUseCase.kt
      │    │         └── SummarizeCoachingNotesUseCase.kt
      │    │
      │    └── data/
      │
      ├── reports/
      └── settings/

      navigation/
      ├── AppNavGraph.kt
      ├── Routes.kt
      └── NavigationExtensions.kt

      designsystem/
      ├── components/
      │    ├── PrimaryButton.kt
      │    ├── StepProgressIndicator.kt
      │    ├── AppCard.kt
      │    └── LoadingOverlay.kt
      │
      ├── theme/
      ├── icons/
      └── typography/

      domain/
      ├── model/
      │    ├── User.kt
      │    ├── Activity.kt
      │    ├── MediaFile.kt
      │    ├── Tag.kt
      │    └── AnalysisResult.kt
      │
      └── common/

      data/
      ├── api/
      ├── database/
      └── repository/

      di/
      ├── NetworkModule.kt
      ├── DatabaseModule.kt
      ├── RepositoryModule.kt
      └── MediaModule.kt
```

---

# Viktigste prinsipper

## 1. Organiser etter FEATURE

Ikke organiser etter:

* activities
* fragments
* utils
* api

Use utils only for truly generic pure helper functions.
Avoid dumping unrelated logic into utils.

Bruk:

```text
feature/login/
feature/analysis/
feature/tagging/
```

---

## 2. Hver feature skal ha samme struktur

```text
presentation/
domain/
data/
```

Dette gjør prosjektet mye enklere å vedlikeholde.

---

## 3. Ingen businesslogikk i Compose UI

Compose skal:

* vise state
* sende events

ViewModel + UseCases håndterer logikken.

---

## 4. Hold ViewModels små

ViewModels skal koordinere flyt.

Tung logikk skal ligge i:

```text
usecase/
repository/
```

---

## 5. Ikke lag gigantiske filer

Dårlig:

```text
AnalysisScreen.kt (3000 linjer)
```

Bra:

```text
analysis/presentation/components/
```

---

## 6. Wizard-state skal være sentralisert

Bruk én samlet state:

```kotlin
data class WizardState(
    val selectedActivity: Activity? = null,
    val activityType: ActivityType? = null,
    val selectedMedia: List<MediaFile> = emptyList(),
    val currentStep: Int = 1
)
```

---

## 7. JWT-token lagres i DataStore

Flyt:

```text
LoginScreen
  ↓
LoginViewModel
  ↓
LoginUseCase
  ↓
AuthRepository
  ↓
Backend API
  ↓
JWT token
  ↓
DataStore
```

Interceptor legger automatisk på:

```text
Authorization: Bearer <token>
```

---

## 8. Offline-first anbefales

Bruk:

* Room
* WorkManager
* lokal media metadata
* sync queue

Dette er viktig for mediaanalyse-app.

---

## 9. AI-agent optimalisering

Hold:

* naming konsekvent
* features identiske
* mapper like
* patterns repeterbare

Dette gjør AI-generert kode mye bedre.

---

## 10. Start enkelt

Ikke moduliser for tidlig.

Vent med:

```text
:feature-login
:feature-analysis
```

Til prosjektet faktisk trenger det.
