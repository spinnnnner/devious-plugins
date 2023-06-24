package net.unethicalite.plugins.randomeventsolver;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("RandomEventSolver")
public interface RandomEventSolverConfig extends Config
{
	@ConfigItem(
		keyName = "lootMazeChests",
		name = "Loot Maze Chests",
		description = "Loot small chests in maze instead of going to shrine"
	)
	default boolean lootMazeChests()
	{
		return false;
	}

	@ConfigItem(
		keyName = "dismissClogs",
		name = "Dismiss collection log randoms",
		description = "Dismiss random events that can give collection log items"
	)
	default boolean dismissClogs()
	{
		return false;
	}
}
