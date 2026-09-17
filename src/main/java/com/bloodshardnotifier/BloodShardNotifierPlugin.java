package com.bloodshardnotifier;

import com.google.inject.Provides;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.TileItem;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.RuneLite;
import net.runelite.client.audio.AudioPlayer;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
    name = "Blood Shard Notifier Plus",
    description = "Plays a configurable sound when a Blood shard appears on the ground",
    tags = {"bloodshard", "notification", "sound", "vampyres", "vyrewatch"}
)
public class BloodShardNotifierPlugin extends Plugin
{
    private static final String CONFIG_GROUP = "bloodshardnotifierplus";
    private static final String SOUND_RESOURCE_ROOT = "/com/bloodshardnotifier/sounds/";

    @Inject
    private BloodShardNotifierConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private AudioPlayer audioPlayer;

    @Inject
    private ScheduledExecutorService scheduledExecutorService;

    private Path pluginDirectory;

    @Override
    protected void startUp() throws Exception
    {
        pluginDirectory = RuneLite.RUNELITE_DIR.toPath().resolve(CONFIG_GROUP).normalize();
        Files.createDirectories(pluginDirectory);
        log.info("Blood Shard Notifier Plus started");
    }

    @Override
    protected void shutDown()
    {
        pluginDirectory = null;
        log.info("Blood Shard Notifier Plus stopped");
    }

    @Subscribe
    public void onItemSpawned(ItemSpawned event)
    {
        if (!config.enabled())
        {
            return;
        }

        TileItem item = event.getItem();
        if (item.getId() == ItemID.BLOOD_SHARD)
        {
            scheduleSound();
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!CONFIG_GROUP.equals(event.getGroup()) || !"testSound".equals(event.getKey()))
        {
            return;
        }

        if (config.testSound())
        {
            configManager.setConfiguration(CONFIG_GROUP, "testSound", false);
            scheduleSound();
        }
    }

    private void scheduleSound()
    {
        scheduledExecutorService.execute(this::playConfiguredSound);
    }

    private void playConfiguredSound()
    {
        try
        {
            BloodShardNotifierConfig.NotificationSound selected = config.notificationSound();
            if (selected == BloodShardNotifierConfig.NotificationSound.CUSTOM)
            {
                File file = resolveCustomSoundFile();
                if (file == null)
                {
                    return;
                }

                audioPlayer.play(file, volumeToGain(config.volume()));
                return;
            }

            String resourceName;
            switch (selected)
            {
                case BELL:
                    resourceName = "bell.wav";
                    break;
                case LEVEL_UP:
                    resourceName = "level-up.wav";
                    break;
                case CHIME:
                    resourceName = "chime.wav";
                    break;
                default:
                    return;
            }

            audioPlayer.play(
                BloodShardNotifierPlugin.class,
                SOUND_RESOURCE_ROOT + resourceName,
                volumeToGain(config.volume())
            );
        }
        catch (Exception ex)
        {
            log.warn("Unable to play Blood Shard Notifier Plus sound", ex);
        }
    }

    private File resolveCustomSoundFile() throws IOException
    {
        if (pluginDirectory == null)
        {
            log.warn("Blood Shard Notifier Plus plugin directory is not available");
            return null;
        }

        String fileName = config.soundFile().trim();
        if (fileName.isEmpty())
        {
            log.warn("Blood Shard Notifier Plus has no custom sound file configured");
            return null;
        }

        Path file = pluginDirectory.resolve(fileName).normalize();
        if (!file.startsWith(pluginDirectory))
        {
            log.warn("Blood Shard Notifier Plus custom sound must be inside {}", pluginDirectory);
            return null;
        }

        if (!Files.isRegularFile(file))
        {
            log.warn("Blood Shard Notifier Plus custom sound file does not exist: {}", file);
            return null;
        }

        return file.toFile();
    }

    private float volumeToGain(int volume)
    {
        if (volume <= 0)
        {
            return -80.0f;
        }

        return (float) (20.0 * Math.log10(volume / 100.0));
    }

    @Provides
    BloodShardNotifierConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BloodShardNotifierConfig.class);
    }
}
