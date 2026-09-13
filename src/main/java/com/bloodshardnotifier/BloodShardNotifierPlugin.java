package com.bloodshardnotifier;

import com.google.inject.Provides;
import java.io.File;
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

	@Inject
	private BloodShardNotifierConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private AudioPlayer audioPlayer;

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
		String path = config.soundFile().trim();
		if (path.isEmpty())
		{
			log.warn("Blood Shard Notifier Plus has no sound file configured; no sound will be played");
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
