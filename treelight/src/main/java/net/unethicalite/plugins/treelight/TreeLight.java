package net.unethicalite.plugins.treelight;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import com.google.inject.Inject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.items.Shop;
import net.unethicalite.api.magic.Magic;
import net.unethicalite.api.magic.SpellBook;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.widgets.Production;
import net.unethicalite.api.widgets.Widgets;
import org.pf4j.Extension;

// This annotation is required in order for the client to detect it as a plugin/script.
@PluginDescriptor(name = "TreeLight", enabledByDefault = false)
@Extension
public class TreeLight extends Script
{
	@Inject
	private Client client;

	private int idleTicks;

	/**
	 * Gets executed whenever a script starts.
	 * Can be used to for example initialize script settings, or perform tasks before starting the loop logic.
	 *
	 * @param args any script arguments passed to the script, separated by spaces.
	 */
	@Override
	public void onStart(String... args)
	{
		idleTicks = 3;
	}

	@Subscribe
	public void onGameTick(GameTick e){
		if (!client.getLocalPlayer().isAnimating() && !client.getLocalPlayer().isMoving())
			idleTicks++;
		else
			idleTicks = 0;
	}

	@Override
	protected int loop()
	{
		if (client.getRealSkillLevel(Skill.WOODCUTTING) >= 12)
		{
			this.setPaused(true);
			return 1000;
		}

		if (idleTicks < 3) return 937;

		if (Inventory.contains("Logs") && Inventory.contains("Tinderbox")){
			Inventory.getFirst("Tinderbox").useOn(Inventory.getFirst("Logs"));
			return 2000;
		}

		TileObject tree = TileObjects.getNearest("Tree", "Dead tree");
		tree.interact("Chop down");
		MousePackets.queueClickPacket();
		return 600;
	}
}
