# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0-beta27] - 2025-02-23

### Key Features
- **Configurable Locking Mechanism:** The locking strategy for `suspend` and `arrow` interfaces is now configurable via the new `LockManager` interface.
- **New Page Attributes API:** Introduced `PageAttributes` class and corresponding API to efficiently retrieve comprehensive page metadata in a single call.

### Improvements & Refactoring
- **Memory Leaks Fixed:** Addressed several memory leaks to ensure better stability.
- **Performance Enhancements:** Significant optimization of internal operations.
- **Major Refactoring:**
    - Project structure reorganized. Core data classes moved to `io.legere.pdfiumandroid.api`.
    - **Migration Aids:** Provided `typealias` and `@Deprecated` replacements to automate migration via IDE.

### Breaking Changes
- **Deprecated APIs Removed:** Cleaned up old APIs.
- **Property Access:** Restricted direct access to internal properties like `.page.pageIndex`. Use wrapper properties (e.g., `.pageIndex`) instead.

## [2.0.0-beta28] - 2025-02-23

### Improvements
- Just a better deploy process

## [2.0.0-beta29] - 2025-02-23

### Improvements
- An even better deploy process

## [2.0.0-beta30] - 2025-02-23

### Improvements
- An even better yet, deploy process
