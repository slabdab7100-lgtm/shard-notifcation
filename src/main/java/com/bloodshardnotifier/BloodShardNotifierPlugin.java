package com.bloodshardnotifier;

import com.google.inject.Provides;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.TileItem;
import net.runelite.api.events.ItemSpawned;
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
    private static final Map<String, String> BUILT_IN_SOUNDS = Map.of(
        "Bell", "bell.wav",
        "Level Up", "level-up.wav",
        "Chime", "chime.wav"
    );

    @Inject
    private BloodShardNotifierConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private AudioPlayer audioPlayer;

    @Inject
    private ScheduledExecutorService scheduledExecutorService;

    private Path temporarySoundDirectory;

    @Override
    protected void startUp()
    {
        log.info("Blood Shard Notifier Plus started");
    }

    @Override
    protected void shutDown()
    {
        if (temporarySoundDirectory != null)
        {
            try
            {
                Files.walk(temporarySoundDirectory)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try
                        {
                            Files.deleteIfExists(path);
                        }
                        catch (IOException ex)
                        {
                            log.debug("Unable to remove temporary sound file: {}", path, ex);
                        }
                    });
            }
            catch (IOException ex)
            {
                log.debug("Unable to clean up temporary sound directory", ex);
            }
            temporarySoundDirectory = null;
        }

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
            File soundFile = resolveSoundFile();
            if (soundFile == null)
            {
                return;
            }

            audioPlayer.play(soundFile, volumeToGain(config.volume()));
        }
        catch (Exception ex)
        {
            log.warn("Unable to play Blood Shard Notifier Plus sound", ex);
        }
    }

    private File resolveSoundFile() throws IOException
    {
        String selected = config.notificationSound().trim();

        if ("Custom".equalsIgnoreCase(selected))
        {
            String path = config.soundFile().trim();
            if (path.isEmpty())
            {
                log.warn("Blood Shard Notifier Plus has no custom sound file configured");
                return null;
            }

            File file = new File(path);
            if (!file.isFile())
            {
                log.warn("Blood Shard Notifier Plus sound file does not exist: {}", path);
                return null;
            }

            return file;
        }

        String resourceName = BUILT_IN_SOUNDS.get(selected);
        if (resourceName == null)
        {
            log.warn("Unknown Blood Shard Notifier Plus sound selection: {}", selected);
            return null;
        }

        if (temporarySoundDirectory == null)
        {
            temporarySoundDirectory = Files.createTempDirectory("blood-shard-notifier-");
        }

        Path target = temporarySoundDirectory.resolve(resourceName);
        if (!Files.isRegularFile(target))
        {
            String resourcePath = SOUND_RESOURCE_ROOT + resourceName;
            try (InputStream input = BloodShardNotifierPlugin.class.getResourceAsStream(resourcePath))
            {
                if (input == null)
                {
                    log.warn("Bundled Blood Shard Notifier Plus sound is missing: {}", resourcePath);
                    return null;
                }

                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        return target.toFile();
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
