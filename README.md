<p align="center"><br><img src="https://user-images.githubusercontent.com/236501/85893648-1c92e880-b7a8-11ea-926d-95355b8175c7.png" width="128" height="128" /></p>
<h3 align="center">Native Audio</h3>
<p align="center"><strong><code>@capacitor-community/native-audio</code></strong></p>
<p align="center">
  Capacitor community plugin for playing sounds.
</p>

<p align="center">
  <img src="https://img.shields.io/maintenance/yes/2021?style=flat-square" />
  <a href="https://github.com/capacitor-community/native-audio/actions?query=workflow%3A%22Test+and+Build+Plugin%22"><img src="https://img.shields.io/github/workflow/status/capacitor-community/native-audio/Test%20and%20Build%20Plugin?style=flat-square" /></a>
  <a href="https://www.npmjs.com/package/@capacitor-community/native-audio"><img src="https://img.shields.io/npm/l/@capacitor-community/native-audio?style=flat-square" /></a>
<br>
  <a href="https://www.npmjs.com/package/@capacitor-community/native-audio"><img src="https://img.shields.io/npm/dw/@capacitor-community/native-audio?style=flat-square" /></a>
  <a href="https://www.npmjs.com/package/@capacitor-community/native-audio"><img src="https://img.shields.io/npm/v/@capacitor-community/native-audio?style=flat-square" /></a>
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
<a href="#contributors-"><img src="https://img.shields.io/badge/all%20contributors-6-orange?style=flat-square" /></a>
<!-- ALL-CONTRIBUTORS-BADGE:END -->
</p>

# Capacitor Native Audio Plugin

Capacitor plugin for native audio engine.
Capacitor v3 - ✅ Support!

Click on video to see example 💥

[![YouTube Example](https://img.youtube.com/vi/XpUGlWWtwHs/0.jpg)](https://www.youtube.com/watch?v=XpUGlWWtwHs)


## Maintainers

| Maintainer    | GitHub                                      | Social                              |
| ------------- | ------------------------------------------- | ----------------------------------- |
| Maxim Bazuev  | [bazuka5801](https://github.com/bazuka5801) | [Telegram](https://t.me/bazuka5801) |

Mainteinance Status: Actively Maintained

## Preparation
All audio place in specific platform folder

Andoid: `android/app/src/assets`

iOS: `ios/App/App/sounds`

Web: `assets/sounds`

## Installation

To use npm

```bash
npm install @capacitor-community/native-audio
```

To use yarn

```bash
yarn add @capacitor-community/native-audio
```

Sync native files

```bash
npx cap sync
```

On iOS, Android and Web, no further steps are needed.

## Configuration

No configuration required for this plugin.
<docgen-config>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->



</docgen-config>

## Supported methods

| Name           | Android | iOS | Web |
| :------------- | :------ | :-- | :-- |
| configure      | ✅      | ✅  | ❌  |
| preload        | ✅      | ✅  | ✅  |
| play           | ✅      | ✅  | ✅  |
| pause          | ✅      | ✅  | ✅  |
| resume         | ✅      | ✅  | ✅  |
| loop           | ✅      | ✅  | ✅  |
| stop           | ✅      | ✅  | ✅  |
| unload         | ✅      | ✅  | ✅  |
| setVolume      | ✅      | ✅  | ✅  |
| getDuration    | ✅      | ✅  | ✅  |
| getCurrentTime | ✅      | ✅  | ✅  |
| isPlaying      | ✅      | ✅  | ✅  |

## Usage

[Example repository](https://github.com/bazuka5801/native-audio-example)

```typescript
import {NativeAudio} from '@capacitor-community/native-audio'


/**
 * This method will load more optimized audio files for background into memory.
 * @param assetPath - relative path of the file or absolute url (file://)
 *        assetId - unique identifier of the file
 *        audioChannelNum - number of audio channels
 *        isUrl - pass true if assetPath is a `file://` url
 * @returns void
 */
NativeAudio.preload({
    assetId: "fire",
    assetPath: "fire.mp3",
    audioChannelNum: 1,
    isUrl: false
});

/**
 * This method will play the loaded audio file if present in the memory.
 * @param assetId - identifier of the asset
 * @param time - (optional) play with seek. example: 6.0 - start playing track from 6 sec
 * @returns void
 */
NativeAudio.play({
    assetId: 'fire',
    // time: 6.0 - seek time
});

/**
 * This method will loop the audio file for playback.
 * @param assetId - identifier of the asset
 * @returns void
 */
NativeAudio.loop({
  assetId: 'fire',
});


/**
 * This method will stop the audio file if it's currently playing.
 * @param assetId - identifier of the asset
 * @returns void
 */
NativeAudio.stop({
  assetId: 'fire',
});

/**
 * This method will unload the audio file from the memory.
 * @param assetId - identifier of the asset
 * @returns void
 */
NativeAudio.unload({
  assetId: 'fire',
});

/**
 * This method will set the new volume for a audio file.
 * @param assetId - identifier of the asset
 *        volume - numerical value of the volume between 0.1 - 1.0
 * @returns void
 */
NativeAudio.setVolume({
  assetId: 'fire',
  volume: 0.4,
});

/**
 * this method will get the duration of an audio file.
 * only works if channels == 1
 */
NativeAudio.getDuration({
  assetId: 'fire'
})
.then(result => {
  console.log(result.duration);
})

/**
 * this method will get the current time of a playing audio file.
 * only works if channels == 1
 */
NativeAudio.getCurrentTime({
  assetId: 'fire'
});
.then(result => {
  console.log(result.currentTime);
})

/**
 * This method will return false if audio is paused or not loaded.
 * @param assetId - identifier of the asset
 * @returns {isPlaying: boolean}
 */
NativeAudio.isPlaying({
  assetId: 'fire'
})
.then(result => {
  console.log(result.isPlaying);
})
```

## API

<docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### playRaw(...)

```typescript
playRaw(options: PlayRawOptions, callback: PlayRawCallback) => Promise<void>
```

Takes in a base64-encoded string, decodes, and plays the raw mp3 data via routing
to device speakers

| Param          | Type                                                        | Description                                                                       |
| -------------- | ----------------------------------------------------------- | --------------------------------------------------------------------------------- |
| **`options`**  | <code><a href="#playrawoptions">PlayRawOptions</a></code>   | Object: { rawAudio: string, bluetoothBuffer: number, bluetoothKeepAlive: number } |
| **`callback`** | <code><a href="#playrawcallback">PlayRawCallback</a></code> | Callback: (success, error) =&gt; {}                                               |

--------------------


### stop()

```typescript
stop() => Promise<Response>
```

Immediately stops all audio being played.

**Returns:** <code>Promise&lt;<a href="#response">Response</a>&gt;</code>

--------------------


### bingbong()

```typescript
bingbong() => Promise<void>
```

If you see these dogs in your front yard, just know upstairs I'm going hard. BING BONG.

--------------------


### Interfaces


#### PlayRawOptions

| Prop                     | Type                |
| ------------------------ | ------------------- |
| **`rawAudio`**           | <code>string</code> |
| **`bluetoothBuffer`**    | <code>number</code> |
| **`bluetoothKeepAlive`** | <code>number</code> |


#### Response

| Prop       | Type                 |
| ---------- | -------------------- |
| **`ok`**   | <code>boolean</code> |
| **`done`** | <code>boolean</code> |
| **`msg`**  | <code>string</code>  |


### Type Aliases


#### PlayRawCallback

<code>(response: <a href="#response">Response</a>, error?: any): void</code>

</docgen-api>
