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
		keyName = "notificationSound",
		name = "Notification sound",
		description = "Choose the sound to play when a Blood shard appears"
	)
	default String notificationSound()
	{
		return "Custom";
	}

	@ConfigItem(
		keyName = "soundFile",
		name = "Custom sound file",
		description = "Path to the WAV file used when Notification sound is set to Custom"
	)
	default String soundFile()
	{
		return "";
	}

	@ConfigItem(
		keyName = "selectSoundFile",
		name = "Select sound file",
		description = "Open a file picker to select a WAV file for the custom notification"
	)
	default boolean selectSoundFile()
	{
		return false;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "volume",
		name = "Volume",
		description = "Volume of the notification sound"
	)
	default int volume()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "testSound",
		name = "Test sound",
		description = "Turn this on to immediately play the selected sound; it automatically turns itself back off"
	)
	default boolean testSound()
	{
		return false;
	}
}
