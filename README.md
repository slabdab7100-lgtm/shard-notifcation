# Blood Shard Notifier Plus

A RuneLite plugin that alerts you when a Blood shard appears on the ground.

## Features

- Detects Blood shards using RuneLite's `ItemSpawned` event.
- Enable/disable the notifier.
- Choose a custom `.wav` file by entering its full path.
- Plays the notification through RuneLite's audio system.
- Adjustable sound volume.
- Leaving the sound file blank disables sound playback.

## Custom sound

Open the plugin settings and put the full path to your WAV file in **Sound file**.

Example:

`C:\\Users\\YourName\\Music\\blood-shard.wav`

The file must be a WAV file supported by Java's audio system. Playback is handled through RuneLite's `AudioPlayer`.

## Important

This project is currently a personal/external plugin build and is not an official RuneLite plugin until it has gone through RuneLite Plugin Hub review.
