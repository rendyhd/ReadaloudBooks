# ReadAloud Books

An Android application for audiobook and eBook consumption with a focus on high-quality audio playback and Material You design.

## Features

### Audiobook Playback
- **Format Support**: Supports M4B, Atmos, and EAC3.
- **Transcoding**: Integrated FFmpeg streaming proxy for real-time transcoding of unsupported codecs.
- **Chapter Detection**: Native and probed chapter extraction.
- **Offline Support**: Local file downloads and storage management.
- **Playback Controls**: Variable speed (0.5x - 2.0x), sleep timer, and background playback.

### eBook Reader
- **EPUB Loading**: Fast loading and smooth scrolling of EPUB files.
- **Customization**: Support for adjustable font sizes, font families, and multiple reader themes (White, Sepia, Dark, OLED).
- **ReadAloud Sync**: Concurrent audio playback and text highlighting.

### UI & Design
- **Material You**: Full support for Material 3 and Dynamic Color on Android 12+.
- **Jetpack Compose**: Entirely declarative UI implementation.

## Getting Started

### Prerequisites
- Android device running API 24+ (Android 7.0).
- A Storyteller instance for book metadata and content.

### Build & Release
This repository uses GitHub Actions for automated releases. Push a tag starting with `v` (e.g., `v1.0.0`) to trigger the build and release process.

## Roadmap
- [x] **Media Sync**: Fix syncing across ReadAloud / Audiobook / eBook, to try and keep them all together.
- [x] **Cloud Sync**: Synchronize progress with your Storyteller instance.
- [ ] **Metadata Editor**: Edit book metadata and cover art within the app.
- [ ] **Grid Customization**: Adjustable grid sizes in library views.

## License
Distributed under the AGPL3 License.
