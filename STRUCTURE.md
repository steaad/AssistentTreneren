# Football Analysis Tool - Project Structure

## Purpose

This document defines the physical project structure and package organization.

For architecture principles, coding rules, technology decisions, and AI agent behavior, see `AGENTS.md`.

`STRUCTURE.md` is the source of truth for:

- package structure
- feature organization
- file placement
- naming conventions

## Target Structure

```text
app/
  src/main/java/com/example/assistenttreneren/
    AssistentTrenerenApplication.kt
    MainActivity.kt

    core/
      network/
        ApiClient.kt
        AuthInterceptor.kt
        NetworkModule.kt
        ResultWrapper.kt

      auth/
        AuthManager.kt
        TokenStorage.kt
        SessionState.kt
        JwtDecoder.kt

      media/
        VideoProcessor.kt
        AudioAnalyzer.kt
        WaveformGenerator.kt
        MediaMetadataReader.kt
        ThumbnailGenerator.kt

      database/
      datastore/
      permissions/
      logging/
      utils/
      extensions/

    feature/
      login/
        presentation/
          LoginScreen.kt
          LoginViewModel.kt
          LoginUiState.kt
          LoginEvent.kt
          LoginAction.kt
          components/

        domain/
          model/
          repository/
          usecase/
            LoginUseCase.kt
            ValidateTokenUseCase.kt
            LogoutUseCase.kt

        data/
          remote/
          local/
          mapper/
          dto/
          repository/

        navigation/

      onboarding/

      activitywizard/
        presentation/
          WizardScreen.kt
          WizardViewModel.kt
          WizardState.kt
          components/
          steps/
            SelectActivityStep.kt
            ActivityTypeStep.kt
            MediaSelectionStep.kt
            SummaryStep.kt

        domain/
        data/

      mediaimport/

      tagging/
        presentation/
          TaggingScreen.kt
          TimelineView.kt
          AudioWaveform.kt
          TagEditor.kt
          TagList.kt
          CoachingNoteView.kt

        domain/
        data/

      analysis/
        presentation/
        domain/
          usecase/
            AnalyzeMatchUseCase.kt
            DetectEventsUseCase.kt
            GenerateInsightsUseCase.kt
            SummarizeCoachingNotesUseCase.kt

        data/

      reports/
      settings/

    navigation/
      AppNavGraph.kt
      Routes.kt
      NavigationExtensions.kt

    designsystem/
      components/
        PrimaryButton.kt
        StepProgressIndicator.kt
        AppCard.kt
        LoadingOverlay.kt

      theme/
      icons/
      typography/

    domain/
      model/
        User.kt
        Activity.kt
        MediaFile.kt
        Tag.kt
        AnalysisResult.kt

      common/

    data/
      api/
      database/
      repository/

    di/
      NetworkModule.kt
      AuthModule.kt
      DatabaseModule.kt
      RepositoryModule.kt
      MediaModule.kt
```

## Core Principles

### 1. Organize by Feature

Do not primarily organize by technical type, such as:

- activities
- fragments
- utils
- api

Use `utils` only for truly generic pure helper functions. Avoid dumping unrelated logic into utility packages.

Prefer feature-based packages:

```text
feature/login/
feature/analysis/
feature/tagging/
```

### 2. Give Each Feature the Same Internal Shape

Each feature should use this internal structure when applicable:

```text
presentation/
domain/
data/
```

This keeps the project predictable and easier to maintain as it grows.

### 3. Keep Business Logic out of Compose UI

Compose should:

- display state
- send events or actions

ViewModels and use cases should handle business logic.

### 4. Keep ViewModels Small

ViewModels should coordinate screen flow and expose UI state.

Heavy logic belongs in:

```text
usecase/
repository/
```

### 5. Avoid Massive Files

Avoid:

```text
AnalysisScreen.kt (3000 lines)
```

Prefer splitting reusable UI and behavior into focused files:

```text
analysis/presentation/components/
```

### 6. Centralize Wizard State

Use one shared state model for multi-step flows:

```kotlin
data class WizardState(
    val selectedActivity: Activity? = null,
    val activityType: ActivityType? = null,
    val selectedMedia: List<MediaFile> = emptyList(),
    val currentStep: Int = 1,
)
```

### 7. Store JWT Tokens Securely

The intended authentication flow is:

```text
LoginScreen
  -> LoginViewModel
  -> LoginUseCase
  -> AuthRepository
  -> Backend API
  -> JWT token
  -> TokenStorage
```

Authenticated requests should add:

```text
Authorization: Bearer <token>
```

through an interceptor.

Token storage must remain behind an abstraction such as `TokenStorage`.

### 8. Prefer Offline-First Design

Use:

- Room
- WorkManager
- local media metadata
- sync queues

This is important because the app is intended for football environments where network quality may be unstable.

### 9. Keep Patterns Repeatable

Keep:

- naming consistent
- feature structures consistent
- mappers consistent
- architectural patterns repeatable

This improves maintainability and makes generated code easier to review.

### 10. Start Simple

Do not split the app into multiple Gradle modules too early.

Wait with modules such as:

```text
:feature-login
:feature-analysis
```

until the project has enough size and clear boundaries to justify them.
