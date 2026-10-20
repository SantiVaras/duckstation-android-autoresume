# DuckStation Auto Resume — experimental Android arm64 build

Unofficial personal fork by SantiVaras. This is **the last publicly available official
Android source**, not a rebuild of the 2025 Android application and not the current
desktop emulator ported to Android. Upstream development and support are separate.

## Provenance and license

Base: [`81da9be2d1040665ebfaf2db6d7fdb710a48a383`](https://github.com/stenzek/duckstation/commit/81da9be2d1040665ebfaf2db6d7fdb710a48a383),
7 June 2021, GNU GPLv3 (see the LICENSE file at that exact revision).
The next upstream commit,
[`6e49adb508c009c5f7998a38d47e1d0d174990d0`](https://github.com/stenzek/duckstation/commit/6e49adb508c009c5f7998a38d47e1d0d174990d0),
removed the Android app on 8 June 2021. The recent official repository has a
different license (CC BY-NC-ND 4.0); no recent code has been incorporated here.
Only the historical GPL branch is modified. Original authors and notices remain.

## What changed

* `resumeState=true` loads this game's resume state after opening the disc and
  identifying its game code, including launches through a `content://` URI.
* Reading a previous state is independent of the Save State On Exit setting.
* Boolean, integer 0/1 and string true/false/0/1 extras are accepted. Missing or
  invalid extras default to resume. Explicit false requests a clean boot.
* `bootPath` takes priority; Intent data is a fallback, and file URIs are decoded.
* Missing state starts normally. An on-screen message distinguishes missing,
  loaded and failed states. RetroAchievements hardcore mode still prevents loading.
* Package: `com.github.santivaras.duckstation.autoresume`. Java/JNI class names
  remain `com.github.stenzek.duckstation.*` for native compatibility.
* Data is isolated in this package's external files directory, normally
  `Android/data/com.github.santivaras.duckstation.autoresume/files/`, with an
  internal-directory fallback. It never selects the official app's data directory
  or legacy `/sdcard/duckstation` directory.
* APK contains arm64-v8a only. Android minimum 6.0, target API 29. The old target
  and native toolchain may be unsuitable for newer Android devices, especially
  devices requiring 16 KB native memory pages. Physical-device testing is required.

## Existing saves — read before installing

The historical core writes state version **52** and accepts versions 42–52.
The desktop source corresponding to Android 0.1-8969's revision `611bb8fb4` writes
version **82**. Therefore **2025 save states cannot be loaded by this build**.
Changing the version field does not convert the format. This build supports
future auto resume using states created by this build; it does not recover the
current 2025 snapshot. Keep the original app to continue that snapshot.

To transfer in-game progress, export/back up the original memory cards using the
original app and work only on copies. Test a copy of a memory card in the new app;
compatibility and per-game naming must be checked. Never point both apps to the
same writable memory card, and never overwrite the originals. Import your own
BIOS into the new app. No BIOS or games are included.

This APK is signed with a dedicated experimental key, not the official key.
It installs alongside the official app; it cannot update that app. Keep the same
experimental key for updates of this package. Keys/passwords are GitHub Actions
secrets and are never included in source or downloadable build artifacts.
Uninstalling either application can delete its app-specific files; export first.
No installation, uninstallation or phone data migration is automated here.

## ES-DE configuration

Back up existing custom configuration before merging the files in `extras/es-de/`
into ES-DE's `custom_systems/` folder. Do not replace an existing customized file
without merging. Select **DuckStation Auto Resume (2021 GPL)** as PSX alternative
emulator. The original DuckStation entry continues targeting the original app.

The relevant command is:

```xml
<command label="DuckStation Auto Resume (2021 GPL)">%EMULATOR_DUCKSTATION-AUTORESUME% %ACTIVITY_CLEAR_TASK% %ACTIVITY_CLEAR_TOP% %EXTRABOOL_resumeState%=true %EXTRA_bootPath%=%ROMSAF%</command>
```

Its custom discovery rule must use the complete component name:

```text
com.github.santivaras.duckstation.autoresume/com.github.stenzek.duckstation.EmulationActivity
```

Open the new app first, import BIOS, grant storage permissions and add the ROM
directory with Android's folder picker. A URI grant to the official app is not
automatically granted to this new package. For CUE/M3U games, access to the whole
game directory is needed for referenced files.

For Cartridge, use the same package/activity and send `bootPath` (string game
path or authorized content URI) and `resumeState` (boolean true). No Cartridge
configuration format has been assumed or tested.

## Manual verification on the device

1. Install alongside the original. Verify the distinct name and package.
2. Open the new app once and complete BIOS/ROM access setup.
3. Enable Save State On Exit; leave RetroAchievements hardcore disabled.
4. Launch a game from ES-DE. With no state, expect the missing-state message.
5. Play to a recognizable screen and exit using the emulator menu so it saves.
6. Relaunch the same game from ES-DE: expect the loaded-state message and the
   exact previous screen. Repeat after force-stopping the test app, then after a reboot.
7. Set resumeState to false in a separate test command: expect a clean boot.
8. With an existing test state, disable saving on exit and request resume=true:
   the previous state should still load. Re-enable saving afterwards.
9. Test a filename with spaces, a content URI, CUE/M3U and a second game. Each
   game must resume its own state. Test Cartridge with the same Intent extras.
10. Check both the original app and its saves remain intact. Do not import a
    version-82 state expecting it to work.

Optional ADB launch (replace the example ROM path; no deletion is performed):

```sh
adb shell am start --activity-clear-task --activity-clear-top \
  -n com.github.santivaras.duckstation.autoresume/com.github.stenzek.duckstation.EmulationActivity \
  --es bootPath /storage/emulated/0/ROMs/psx/game.chd --ez resumeState true
```

## Build

Workflow `.github/workflows/android-arm64.yml` runs on pushes to
`android-autoresume` and manually through Actions. It uses JDK 11, Gradle 7.0.2,
Android Gradle Plugin 4.2.1, platform 29, build-tools 30.0.2, NDK 21.4.7075529,
CMake 3.22.1. It runs launch-option tests, builds a release APK, verifies its
signature/package/arm64 libraries and uploads the APK, checksum, verification
reports and corresponding source. It does not publish a public release.

Required repository secrets: `AUTORESUME_KEYSTORE_BASE64` (dedicated PKCS12,
alias `autoresume`) and `AUTORESUME_KEYSTORE_PASSWORD`. Do not use an official
or unrelated application's signing key. Do not commit signing material.

For a local unsigned build, run from `android/` with the toolchain installed:

```sh
./gradlew --no-daemon --max-workers=2 testReleaseUnitTest assembleRelease
```

For a signed local build, set `AUTORESUME_KEYSTORE` to the absolute PKCS12 path
and `AUTORESUME_KEYSTORE_PASSWORD` before the same command. Compilation and unit
tests do not verify gameplay or frontend integration; use the manual checklist.

## Investigation references

* [Official Android removal](https://github.com/stenzek/duckstation/commit/6e49adb508c009c5f7998a38d47e1d0d174990d0)
* [License at the historical base](https://github.com/stenzek/duckstation/blob/81da9be2d1040665ebfaf2db6d7fdb710a48a383/LICENSE)
* [Historical state version](https://github.com/stenzek/duckstation/blob/81da9be2d1040665ebfaf2db6d7fdb710a48a383/src/core/save_state_version.h)
* [2025 state version](https://github.com/stenzek/duckstation/blob/611bb8fb4/src/core/save_state_version.h)
* [Similar launcher report](https://github.com/TapiocaFox/Daijishou/issues/549) — user report, not a proven diagnosis of the 2025 APK.
* [Official ES-DE Android commands](https://gitlab.com/es-de/emulationstation-de/-/blob/master/resources/systems/android/es_systems.xml)
* [Android app signing](https://developer.android.com/studio/publish/app-signing)
