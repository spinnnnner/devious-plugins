package net.unethicalite.plugins.treechop;

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
@PluginDescriptor(name = "TreeChop", enabledByDefault = false)
@Extension
public class TreeChop extends Script
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
		if (idleTicks < 3) return 937;

		if (Bank.isOpen()){
			if (Inventory.contains("Oak logs"))
			{
				Bank.depositAll("Oak logs");
				// Inventory.getFirst("Logs").interact("Deposit-all")? what's the difference. (THERE IS NONE)
				MousePackets.queueClickPacket();
				Time.sleep(231);
			}

			// doubles as closing bank if, very cool
			if (!Movement.isRunEnabled()) Movement.toggleRun();
			MousePackets.queueClickPacket();

			Time.sleep(414);

			Movement.walk(new WorldPoint(3167,3423, client.getPlane()));
			MousePackets.queueClickPacket();
			return 12000;
		}

		if (Inventory.isFull()){
			if (Movement.isRunEnabled() && Movement.getRunEnergy() <= 40){
				Movement.toggleRun();
				MousePackets.queueClickPacket();
			}

			// try to find a bank booth. if this doesnt work, go to predetermined bank tile.
			TileObject bankBooth = TileObjects.getNearest("Bank booth");

			if (bankBooth == null){
				Movement.walk(new WorldPoint(3171,3423, client.getPlane()));
				Time.sleepTicksUntil(()-> TileObjects.getNearest("Bank booth") != null, 50);
				bankBooth = TileObjects.getNearest("Bank booth");
			}

			bankBooth.interact("Bank");
			MousePackets.queueClickPacket();
			return 1000;
		}

		TileObject tree = TileObjects.getNearest("Oak");
		tree.interact("Chop down");
		MousePackets.queueClickPacket();

		return 600;
	}
}