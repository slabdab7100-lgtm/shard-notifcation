package com.bloodshardnotifier;

import com.google.inject.Provides;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.inject.Inject;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.TileItem;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Blood Shard Notifier",
	description = "Plays a configurable sound when a Blood shard appears on the ground",
	tags = {"blood shard", "bloodshard", "notification", "sound", "vampyres"}
)
public class BloodShardNotifierPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private BloodShardNotifierConfig config;

	private Clip currentClip;

	@Override
	protected void startUp()
	{
		log.info("Blood Shard Notifier started");
	}

	@Override
	protected void shutDown()
	{
		stopCurrentClip();
		log.info("Blood Shard Notifier stopped");
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
			playConfiguredSound();
		}
	}

	private void playConfiguredSound()
	{
		String path = config.soundFile().trim();
		if (path.isEmpty())
		{
			java.awt.Toolkit.getDefaultToolkit().beep();
			return;
		}

		File file = new File(path);
		if (!file.isFile())
		{
			log.warn("Blood Shard Notifier sound file does not exist: {}", path);
			java.awt.Toolkit.getDefaultToolkit().beep();
			return;
		}

		try
		{
			stopCurrentClip();
			try (AudioInputStream audio = AudioSystem.getAudioInputStream(file))
			{
				Clip clip = AudioSystem.getClip();
				clip.open(audio);
				applyVolume(clip, config.volume());
				currentClip = clip;
				clip.start();
			}
		}
		catch (UnsupportedAudioFileException | IOException | LineUnavailableException e)
		{
			log.warn("Unable to play Blood Shard Notifier sound: {}", file, e);
			java.awt.Toolkit.getDefaultToolkit().beep();
		}
	}

	private void applyVolume(Clip clip, int volume)
	{
		if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
		{
			return;
		}

		FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		if (volume <= 0)
		{
			control.setValue(control.getMinimum());
			return;
		}

		float min = control.getMinimum();
		float max = control.getMaximum();
		float normalized = volume / 100.0f;
		float gain = (float) (20.0 * Math.log10(normalized));
		control.setValue(Math.max(min, Math.min(max, gain)));
	}

	private void stopCurrentClip()
	{
		if (currentClip != null)
		{
			currentClip.stop();
			currentClip.close();
			currentClip = null;
		}
	}

	@Provides
	BloodShardNotifierConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BloodShardNotifierConfig.class);
	}
}
