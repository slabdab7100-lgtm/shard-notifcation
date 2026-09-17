# Blood Shard Notifier Plus

**Never miss a Blood shard drop again.**

Blood Shard Notifier Plus is a RuneLite plugin that detects when a **Blood shard appears on the ground** and plays a configurable alert sound. It is designed for players who want an immediate audio cue while grinding Vyrewatch Sentinels or other content where Blood shards can appear.

## Features

- 🔔 Detects Blood shards when they spawn on the ground.
- 🔊 Includes three bundled notification sounds: **Bell**, **Level Up**, and **Chime**.
- 📁 Supports a **Custom** WAV file stored in the plugin's RuneLite directory.
- 🎚️ Adjustable notification volume.
- 🧪 Test the selected notification sound from the plugin settings.
- ⚙️ Simple enable/disable control.
- 🚫 Does not rely on chat messages, so other players' chat or clan/channel loot messages do not trigger the alert.

## Quick setup

1. Install **Blood Shard Notifier Plus** from the RuneLite Plugin Hub.
2. Enable the plugin in RuneLite's plugin configuration.
3. Choose a **Notification sound**: **Bell**, **Level Up**, **Chime**, or **Custom**.
4. Adjust the **Volume** to your preference.
5. For **Custom**, place your WAV file in the plugin directory and enter its filename in **Sound file**.
6. Use **Test sound** to confirm the selected alert works.
7. Start your grind and let RuneLite alert you when a Blood shard hits the ground.

## Notification sounds

The **Bell**, **Level Up**, and **Chime** sounds are bundled with the plugin, so no additional files are required for those options.

**Custom** allows you to use your own WAV file. The file must be stored inside the plugin-specific directory:

`RUNELITE_DIR/bloodshardnotifierplus/`

For example:

`RUNELITE_DIR/bloodshardnotifierplus/blood-shard.wav`

Enter only the filename in **Sound file**, such as `blood-shard.wav`. The plugin resolves the filename against its plugin directory and rejects paths that would escape that directory.

The file must be a WAV file supported by Java's audio system. Playback is handled through RuneLite's audio system.

## Who is this for?

This plugin is especially useful for:

- Vyrewatch Sentinel grinders
- Blood shard hunters
- AFK PvM players
- Players who use audio cues while watching videos or multitasking
- Anyone who wants a dedicated alert for Blood shard ground drops

## Feedback

If you find a bug or have an idea for an improvement, please open an issue on the GitHub repository with your RuneLite version and plugin configuration details when relevant.
