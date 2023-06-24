package net.unethicalite.plugins.salmonfishing;

import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Production;
import org.pf4j.Extension;

// This annotation is required in order for the client to detect it as a plugin/script.
@PluginDescriptor(name = "Salmon Fishing", enabledByDefault = false)
@Extension
public class SalmonFishing extends Script
{
	@Inject
	private Client client;

	private Random random;
	private State state;
	private int lastActive;

	@Override
	public void onStart(String... args)
	{
		lastActive = -1;
		random = new Random();
		state = State.IDLE;
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged e){
		if (this.isPaused()) return;

		if (e.getSource() != client.getLocalPlayer()){
			return;
		}

		if (e.getTarget() == null){
			state = State.IDLE;
		}
		else if (Objects.equals(e.getTarget().getName(), "Rod Fishing spot")){
			state = State.FISHING;
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged e){
		if (this.isPaused()) return;

		if (e.getActor() != client.getLocalPlayer()){
			return;
		}

		if (e.getActor().getAnimation() != -1){
			lastActive = client.getTickCount();
		}
	}

	@Override
	protected int loop()
	{
//		if (client.getRealSkillLevel(Skill.COOKING) >= 62){
//			this.setPaused(true);
//			try
//			{
//				this.shutDown();
//			}
//			catch (Exception e)
//			{
//				e.printStackTrace();
//			}
//		return 100000;
//		}

		if (Dialog.canContinue())
		{
			Keyboard.sendSpace();
			return 731;
		}

		if (!client.getLocalPlayer().isIdle()){
			return 1553;
		}

		if (lastActive > client.getTickCount() - 6){
				return 1237;
		}

		if (Production.isOpen())
		{
			state = State.COOKING;

			Production.choosePreviousOption();  // this just sends space
			return 6529;
		}

		if (state == State.DROPPING && !Inventory.contains("Logs")){
			chopTree();
			return 4179;
		}

		if (Inventory.isFull())
		{
			if (Inventory.contains("Logs") && Inventory.contains("Tinderbox")){
				state = State.FIREMAKING;

				Inventory.getFirst("Tinderbox").useOn(Inventory.getFirst("Logs"));
				return 1813;
			}
			else if (Inventory.contains("Tinderbox")){
				Inventory.getFirst("Raw trout","Raw salmon").drop();
				MousePackets.queueClickPacket();
				Time.sleep(349);

				chopTree();
				return 1779;
			}
			else
			{
				logger.warn("Tinderbox not found in inventory. Dropping raw fish");
				state = State.DROPPING;

				Inventory.getAll("Raw salmon","Raw trout").forEach(fish -> {
					fish.drop();
					MousePackets.queueClickPacket(fish);
					Time.sleep(random.nextInt(83) + 137);
				});
			}
		}
		else if (Inventory.getFreeSlots() == 1)
		{
			if (Inventory.contains("Raw trout", "Raw salmon"))
			{
				// just made fire?
				TileObject fire = TileObjects.getFirstSurrounding(Players.getLocal().getWorldLocation(), 2, "Fire");
				if (fire != null)
				{
					state = State.COOKING;

					Inventory.getFirst("Raw salmon", "Raw trout").useOn(fire);
				}
				else
				{
					chopTree();
				}
				return 827;
			}
			else
			{
				state = State.DROPPING;

				Inventory.getAll("Salmon","Trout","Burnt fish").forEach(fish -> {
					fish.drop();
					MousePackets.queueClickPacket(fish);
					Time.sleep(random.nextInt(143) + 117);
				});
			}
		}
		else
		{
			state = State.FISHING;

			NPC fishingSpot = NPCs.getNearest("Rod Fishing spot");
			if (fishingSpot != null)
			{
				fishingSpot.interact("Lure");
				MousePackets.queueClickPacket(fishingSpot);
			}
			else
			{
				logger.warn("Can't find fishing spot");
			}
			return 1617;
		}
		return 833;
	}

	private void chopTree(){
		state = State.CHOPPING;

		TileObject tree = TileObjects.getNearest("Tree", "Dead tree");
		tree.interact("Chop down");
		MousePackets.queueClickPacket(tree);
	}

	enum State{
		FISHING,
		CHOPPING,
		FIREMAKING,
		COOKING,
		DROPPING,
		IDLE
	}
}
