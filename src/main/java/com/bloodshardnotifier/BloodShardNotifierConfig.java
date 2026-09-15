package com.bloodshardnotifier;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("bloodshardnotifierplus")
public interface BloodShardNotifierConfig extends Config
{
	@ConfigItem(
		keyName = "enabled",
		name = "Enable notifier",
		description = "Play a sound when a Blood shard appears on the ground"
	)
	default boolean enabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "soundFile",
		name = "Sound file",
		description = "Path to the WAV file used for the Blood shard notification"
	)
	default String soundFile()
	{
		return "";
	}

	@ConfigItem(
		keyName = "selectSoundFile",
		name = "Select sound file",
		description = "Open a file picker to select a WAV file for the notification"
	)
	default boolean selectSoundFile()
	{
		return false;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "volume",
		name = "Volume",
		description = "Volume of the custom WAV notification"
	)
	default int volume()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "testSound",
		name = "Test sound",
		description = "Turn this on to immediately play the configured sound; it automatically turns itself back off"
	)
	default boolean testSound()
	{
		return false;
	}
}
