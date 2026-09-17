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
        description = "Choose the sound to play when a Blood shard appears",
        position = 1
    )
    default NotificationSound notificationSound()
    {
        return NotificationSound.BELL;
    }

    @ConfigItem(
        keyName = "soundFile",
        name = "Custom sound file",
        description = "Filename of a WAV file stored in the Blood Shard Notifier Plus plugin directory",
        position = 2
    )
    default String soundFile()
    {
        return "";
    }

    @ConfigItem(
        keyName = "selectSoundFile",
        name = "Select custom sound",
        description = "Open a Windows file picker and copy a WAV file into the plugin directory",
        position = 3
    )
    default boolean selectSoundFile()
    {
        return false;
    }

    @Range(min = 0, max = 100)
    @ConfigItem(
        keyName = "volume",
        name = "Volume",
        description = "Volume of the notification sound",
        position = 4
    )
    default int volume()
    {
        return 100;
    }

    @ConfigItem(
        keyName = "testSound",
        name = "Test sound",
        description = "Play the selected notification sound immediately",
        position = 5
    )
    default boolean testSound()
    {
        return false;
    }

    enum NotificationSound
    {
        BELL("Bell"),
        LEVEL_UP("Level Up"),
        CHIME("Chime"),
        CUSTOM("Custom");

        private final String displayName;

        NotificationSound(String displayName)
        {
            this.displayName = displayName;
        }

        @Override
        public String toString()
        {
            return displayName;
        }
    }
}
