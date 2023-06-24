package net.unethicalite.plugins.guildonly;

import java.util.ArrayList;
import java.util.Objects;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import com.google.inject.Inject;
import net.runelite.api.InventoryID;
import net.runelite.api.NPC;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.packets.MousePackets;
import org.pf4j.Extension;

// This annotation is required in order for the client to detect it as a plugin/script.
@PluginDescriptor(name = "Guild Only", enabledByDefault = false)
@Extension
public class GuildOnly extends Plugin
{
	private Actor lastInteracting;

	private final int BONES = 526;

	private final ArrayList<TileItem> toPickup = new ArrayList<>();

	private Action currentAction;

	private int lastAttacked;

	private TileItem targetedItem;

	@Inject
	private Client client;

	@Override
	public void startUp()
	{
		lastAttacked = -1;
		targetedItem = null;

		if (Inventory.isFull()){
			if (Inventory.contains("Raw chicken")){
				currentAction = Action.COOK;
			}
			else {
				currentAction = Action.SELL;
			}
		}
		else {
			currentAction = Action.CHICKEN;
		}
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned e){
		// if in the list of events
		// if spawn location is within 5x5 of myself
		// wait until my current thing is dead, then interact
	}

	@Subscribe
	private void onInteractingChanged(InteractingChanged e){
		if (e.getSource() != client.getLocalPlayer() || e.getTarget() == null){
			return;
		}
		lastInteracting = e.getTarget();
	}

	@Subscribe
	private void onActorDeath(ActorDeath e){
		if (e.getActor() != lastInteracting){
			return;
		}
		lastInteracting = null;
	}

	@Subscribe
	private void onItemContainerChanged(ItemContainerChanged e){
		if (e.getContainerId() != InventoryID.INVENTORY.getId()){
			return;
		}

		if (e.getItemContainer().contains(BONES)){
			Inventory.getFirst(BONES).interact("Bury");
			MousePackets.queueClickPacket();
		}
		else if (Inventory.isFull()){
			TileObject range = TileObjects.getNearest("Range");
			// open the 2 doors
		}
	}

	@Subscribe
	private void onItemSpawned(ItemSpawned e){
		if (e.getTile().getWorldLocation() == lastInteracting.getWorldLocation()){
			// add this to a list of items that need to be picked up, then pick them up 1 by 1 on onGameTick?
			toPickup.add(e.getItem());
		}
	}

	@Subscribe
	private void onItemDespawned(ItemDespawned e){
		if (toPickup.remove(e.getItem())){
			targetedItem = null;
		};
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged e){
		if (e.getActor() != client.getLocalPlayer()){
			return;
		}
		if (e.getActor().getAnimation() == 1){ // TODO: punch or kick animation id
			lastAttacked = client.getTickCount();
		}
	}

	@Subscribe
	private void onGameTick(GameTick e){
		if (currentAction == Action.CHICKEN)
		{
			chickens();
		}

		else if (currentAction == Action.COOK){
			cook();
		}
	}

	enum Action{
		CHICKEN,
		COOK,
		SELL,
		RANDOM_EVENT
	}

	private void chickens(){
		// items to pickup and not on attack cooldown
		if (!toPickup.isEmpty() && client.getTickCount() == lastAttacked + 1 && targetedItem == null)
		{
			for (TileItem i : toPickup)
			{
				if (i.getId() == BONES)
				{
					targetedItem = i;
					i.pickup();
					MousePackets.queueClickPacket();
					return;
				}
			}
			toPickup.get(0).pickup();
			MousePackets.queueClickPacket();
			return;
		}

		// not picking up an item and not attacking something
		else if (!client.getLocalPlayer().isInteracting() && targetedItem == null)
		{
			// click chicken
			if (lastInteracting != null && !lastInteracting.isDead())
			{
				lastInteracting.interact("Attack");
			}
			else {
				NPC chicken = NPCs.getNearest(e -> (Objects.equals(e.getName(), "Chicken")) && !e.isDead());
				chicken.interact("Attack");
			}
			MousePackets.queueClickPacket();
			return;
		}

		int a = 5;
	}

	private void cook(){
		// if player is playing cooking animation, return
		// get to the range: open doors if necessary
	}

	/*
	* click chickens
	* when they despawn, take items: feathers, bones, raw chicken
	* when bones enter the inventory, bury them
	* when inventory is full, make sure the closer door and the further door are open
	* when second door (closer to range) is open,
	* */
}