package com.bloodshardnotifier;

import com.google.inject.Provides;
import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.Preferences;
import net.runelite.api.SoundEffectID;
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
	private static final String SOUND_BELL = "Bell";
	private static final String SOUND_LEVEL_UP = "Level Up";
	private static final String SOUND_CHIME = "Chime";
	private static final String SOUND_CUSTOM = "Custom";

	// OSRS sound effect used by the level-up fireworks.
	private static final int LEVEL_UP_SOUND_ID = 1352;

	@Inject
	private BloodShardNotifierConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private AudioPlayer audioPlayer;

	@Inject
	private Client client;

	@Inject
	private ScheduledExecutorService scheduledExecutorService;

	@Override
	protected void startUp()
	{
		log.info("Blood Shard Notifier Plus started");
	}

	@Override
	protected void shutDown()
	{
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
		if (!CONFIG_GROUP.equals(event.getGroup()))
		{
			return;
		}

		if ("testSound".equals(event.getKey()) && config.testSound())
		{
			configManager.setConfiguration(CONFIG_GROUP, "testSound", false);
			scheduleSound();
		}
		else if ("selectSoundFile".equals(event.getKey()) && config.selectSoundFile())
		{
			configManager.setConfiguration(CONFIG_GROUP, "selectSoundFile", false);
			SwingUtilities.invokeLater(this::chooseSoundFile);
		}
	}

	private void chooseSoundFile()
	{
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select Blood Shard notification sound");
		fileChooser.setFileFilter(new FileNameExtensionFilter("WAV audio files (*.wav)", "wav"));
		fileChooser.setAcceptAllFileFilterUsed(false);

		String currentPath = config.soundFile().trim();
		if (!currentPath.isEmpty())
		{
			File currentFile = new File(currentPath);
			if (currentFile.exists())
			{
				fileChooser.setSelectedFile(currentFile);
			}
		}

		int result = fileChooser.showOpenDialog(null);
		if (result != JFileChooser.APPROVE_OPTION)
		{
			return;
		}

		File selectedFile = fileChooser.getSelectedFile();
		if (selectedFile == null || !selectedFile.isFile())
		{
			return;
		}

		configManager.setConfiguration(CONFIG_GROUP, "soundFile", selectedFile.getAbsolutePath());
		log.info("Blood Shard Notifier Plus sound file selected: {}", selectedFile);
	}

	private void scheduleSound()
	{
		scheduledExecutorService.execute(this::playConfiguredSound);
	}

	private void playConfiguredSound()
	{
		String sound = config.notificationSound();
		if (SOUND_CUSTOM.equals(sound))
		{
			playCustomSound();
			return;
		}

		playPresetSound(sound);
	}

	private void playPresetSound(String sound)
	{
		int soundId;
		switch (sound)
		{
			case SOUND_BELL:
				soundId = SoundEffectID.TOWN_CRIER_BELL_DING;
				break;
			case SOUND_LEVEL_UP:
				soundId = LEVEL_UP_SOUND_ID;
				break;
			case SOUND_CHIME:
				soundId = SoundEffectID.GE_ADD_OFFER_DINGALING;
				break;
			default:
				log.warn("Unknown Blood Shard Notifier Plus sound '{}'; falling back to Custom", sound);
				playCustomSound();
				return;
		}

		try
		{
			Preferences preferences = client.getPreferences();
			int previousVolume = preferences.getSoundEffectVolume();
			preferences.setSoundEffectVolume(config.volume());
			client.playSoundEffect(soundId, config.volume());
			preferences.setSoundEffectVolume(previousVolume);
		}
		catch (Exception ex)
		{
			log.warn("Unable to play Blood Shard Notifier Plus preset sound: {}", sound, ex);
		}
	}

	private void playCustomSound()
	{
		String path = config.soundFile().trim();
		if (path.isEmpty())
		{
			log.warn("Blood Shard Notifier Plus has no custom sound file configured");
			return;
		}

		File file = new File(path);
		if (!file.isFile())
		{
			log.warn("Blood Shard Notifier Plus sound file does not exist: {}", path);
			return;
		}

		try
		{
			audioPlayer.play(file, volumeToGain(config.volume()));
		}
		catch (Exception ex)
		{
			log.warn("Unable to play Blood Shard Notifier Plus sound: {}", file, ex);
		}
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
