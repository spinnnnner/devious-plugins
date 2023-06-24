package net.unethicalite.plugins.ratplugin;

import java.util.Random;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.TileItem;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcSpawned;
import com.google.inject.Inject;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.plugins.Script;
import org.pf4j.Extension;

// This annotation is required in order for the client to detect it as a plugin/script.
@PluginDescriptor(name = "Rats", enabledByDefault = false)
@Extension
public class RatPlugin extends Script
{
	@Inject
	private Client client;

	private boolean triedToPickup = false;
	Random random;
	Integer counter = 0;

	@Override
	public void onStart(String... args)
	{
		random = new Random();
	}

//	@Subscribe
	public void onGameStateChanged(GameStateChanged e){
		logger.info("keyboard: " + client.getKeyboardIdleTicks());
		logger.info("mouse: " + client.getMouseIdleTicks());
		logger.info("setting: " + client.getIdleTimeout());
	}

	@Override
	protected int loop()
	{
		if (Players.getLocal().isMoving()){
			return 989;
		}

		Item bones = Inventory.getFirst("Bones");
		if (bones != null){
			bones.interact("Bury");
			MousePackets.queueClickPacket();
			triedToPickup = false;
			counter = 0;
			if (random.nextInt(20) > 0)
			return random.nextInt(425) + 781;
		}

		if (!Players.getLocal().isInteracting()){
			// try to pick up bones
			if (!triedToPickup)
			{
				TileItem bone = TileItems.getNearest("Bones");
				// if it's not reachable, ignore
				if (bone != null && Reachable.isInteractable(bone))
				{
					bone.pickup();
					MousePackets.queueClickPacket();
					triedToPickup = true;
					counter = 0;
					return 1387;
				}
			}

			// if can't be picked up, attack rat
			NPC rat = NPCs.getNearest("Rat");
			if (rat != null && Reachable.isInteractable(rat)){
				if (Movement.isWalking()){
					Movement.toggleRun();
					MousePackets.queueClickPacket();
				}
				rat.interact("Attack");
				MousePackets.queueClickPacket();
				triedToPickup = false;
				counter = 0;
				return 734;
//				return rollAFK(200);
			}
		}
		counter++;
		if (counter > 20){
			logger.warn("Counter got to 20");
			NPCs.getNearest("Rat").interact("Attack");
			MousePackets.queueClickPacket();
			counter = 0;
		}
		return 833;
	}

//	@Subscribe
	public void onNpcSpawned(NpcSpawned npc){
		// if its a random event and its interacting with me, paint canvas to update what's on there? does this work
		client.getCanvas().repaint();
	}

	private int rollAFK(int odds){
//		logger.info("Last afk on "+ lastAFK);
		int sleepTime;
		if (random.nextInt(odds) == 0){
			sleepTime = random.nextInt(20) * 4217 + 30000;  // between 30 and 30 + 80 seconds
			logger.info("AFK for " + sleepTime/1000 );
		}
		else {
			sleepTime = random.nextInt(425) + 881;  // between 0.881 and 1.8 seconds
		}
//		lastAFK = client.getTickCount();
		return sleepTime;
	}
}
