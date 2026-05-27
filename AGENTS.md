# Football Analysis Tool - General AI Agent Instructions

## Project Overview

This project is a modern Android application for football coaches and analysts.

The application is used for:

* recording voice notes during football matches and training sessions
* tagging football events in real-time
* synchronizing audio recordings with video/media
* AI-assisted analysis and categorization
* activity/session management
* reviewing timelines and tagged events

The application must prioritize:

* stability
* maintainability
* scalability
* offline-first behavior
* clean architecture
* production-quality code

This is NOT a prototype project.
Generated code should be suitable for long-term maintenance.

---

# Primary Goals

The project should:

* be easy to extend
* support future AI integrations
* support media-heavy workflows
* work well on mobile devices during live football activities
* minimize technical debt

The project should avoid:

* unnecessary complexity
* premature optimization
* overengineering
* inconsistent architecture

---

# Tech Stack

## Language

* Kotlin only

## UI

* Jetpack Compose
* Material 3
* Navigation Compose

## Architecture

* MVVM
* Clean Architecture principles
* Unidirectional Data Flow (UDF)

## Async / State

* Kotlin Coroutines
* StateFlow

## Dependency Injection

* Hilt

## Networking

* Retrofit
* OkHttp
* Kotlin Serialization

## Local Storage

* Room database
* DataStore for preferences

## Media

* Media3 / ExoPlayer
* CameraX
* MediaRecorder

## Logging

* Timber

---

# Minimum Android Requirements

* Minimum SDK: 26
* Target latest stable Android SDK

Do not generate code for legacy Android compatibility unless explicitly requested.

---

# Project Structure

STRUCTURE.md is the source of truth for physical project structure.
Use feature-first organization where appropriate.

## Core Terms

* Activity = football activity session
* Recording = audio recording
* Tag = tagged football event
* Timeline = synchronized event timeline
* MediaAsset = uploaded media file
* Analysis = AI-generated or manual analysis
* Event = football-related occurrence
* CoachNote = spoken or written coaching note

Avoid inventing new terminology unless requested.

---

# Architecture Rules

## General Principles

* Prefer simple and readable code
* Prefer explicit code over magic
* Avoid hidden side effects
* Keep business logic outside UI
* Prefer composition over inheritance
* Prefer immutable state

## Clean Architecture

* UI layer must not access APIs directly
* ViewModels should coordinate use cases
* Repositories abstract data sources
* Domain layer should contain business logic

---

# Compose Rules

## General

* Use Compose only
* Do NOT introduce XML layouts
* Do NOT introduce legacy Android Views
* Avoid interoperability unless explicitly needed

## Composable Design

* Composables should be stateless when possible
* Business logic must NOT live inside composables
* Use remember only for local transient UI state
* Screen state belongs in ViewModels
* Prefer reusable UI components

## Recomposition

* Avoid unnecessary recomposition
* Use stable models where appropriate
* Avoid expensive calculations inside composables

## Lists

* Use LazyColumn/LazyRow for dynamic lists
* Avoid rendering unnecessary items

---

# ViewModel Rules

## State

Each screen should expose:

* UiState data class
* immutable StateFlow
* event handlers

Example:

```kotlin
data class ScreenUiState(
    val isLoading: Boolean = false
)

class ScreenViewModel : ViewModel()

@Composable
fun screen()
```

## Rules

* Use StateFlow instead of LiveData
* Expose immutable state only
* Avoid mutable shared state
* Avoid large ViewModels

---

# Navigation Rules

* Use Navigation Compose only
* Avoid fragment-based navigation
* Prefer typed route definitions where possible

Navigation should:

* be centralized
* remain predictable
* avoid deep coupling between screens

---

# Dependency Injection Rules

* Use Hilt
* Prefer constructor injection
* Avoid service locators
* Avoid unnecessary singletons

---

# Coroutine Rules

* Use structured concurrency
* Never use GlobalScope
* Avoid blocking the main thread
* Prefer suspend functions

---

# Networking Rules

## API Layer

* Use Retrofit
* Use Kotlin Serialization
* Keep DTOs separated from domain models
* Use mappers between layers

## Error Handling

* Never swallow exceptions silently
* Return meaningful error states
* Prefer Result wrappers or sealed classes

---

# Database Rules

* Use Room
* Keep entities separate from domain models
* Use repository abstractions

---

# Media Rules

The application is media-heavy.

Generated solutions should:

* prioritize stability
* avoid memory leaks
* avoid unnecessary buffering
* support long-running recordings

Use:

* Media3 / ExoPlayer for playback
* CameraX for camera integrations
* MediaRecorder for audio recording unless another approach is explicitly requested

---

# Logging Rules

* Use Timber
* Avoid excessive logs
* Never log sensitive information
* Keep logs meaningful

---

# UI/UX Principles

The application is used in real football environments.

UI should:

* work quickly under pressure
* minimize taps
* prioritize readability
* support large touch targets
* work outdoors
* avoid clutter

---

# Design Preferences

Preferred design characteristics:

* clean
* modern
* minimal
* functional
* fast

Avoid:

* overly decorative UI
* excessive animations
* complex navigation flows

---

# Performance Rules

* Avoid unnecessary allocations
* Avoid unnecessary recompositions
* Avoid large object creation in UI
* Keep scrolling smooth
* Keep startup lightweight

---

# Testing Rules

## Preferred Testing Strategy

* Unit test business logic
* Keep ViewModels testable
* UI tests only for critical flows

## Avoid

* unnecessary UI snapshot testing
* tightly coupled tests

---

# Important Restrictions

Do NOT:

* introduce XML layouts
* use legacy Android Views
* introduce RxJava
* introduce LiveData
* use deprecated Android APIs
* use GlobalScope
* create massive god classes
* mix multiple architectural styles
* bypass repository abstractions
* place business logic in composables

---

# AI Agent Behavior Rules

When generating code:

* generate compilable production-quality code
* preserve existing architecture
* prefer incremental changes
* explain major architectural decisions briefly
* avoid placeholders unless requested
* avoid mock implementations unless requested

When creating files:

* give a short explanation per file (1-2 lines)
* explain non-obvious decisions
* skip explaining trivial code

Before:

* introducing a new dependency
* performing a large refactor
* changing architecture significantly

ask for confirmation first.

---

# Code Style

## Naming

Use:

* clear naming
* domain-driven naming
* descriptive method names

Avoid:

* abbreviations
* generic names like Manager, Helper, Utils unless truly appropriate

## Functions

* Keep functions focused
* Prefer small functions
* Avoid deeply nested logic

## Files

* Keep files reasonably small
* Split responsibilities clearly

---

# Offline-First Philosophy

The application should continue functioning even with unstable internet.

Prefer:

* local persistence
* queued uploads
* resilient synchronization patterns

Avoid:

* network-dependent UI flows
* blocking the user on network calls

---

# Future Scalability

The architecture should support future additions such as:

* AI transcription
* event classification
* cloud synchronization
* multi-device synchronization
* wearable integrations
* video synchronization
* timeline analysis
* collaborative coaching features

Do not hardcode assumptions that prevent future scalability.

---

# Final Principle

Prefer:

* maintainability
* readability
* predictability
* stability

over:

* cleverness
* unnecessary abstraction
* premature optimization
* framework-heavy solutions
