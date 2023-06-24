package net.unethicalite.plugins.noclogprep.tasks;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Worlds;
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
@PluginDescriptor(name = "Silver", enabledByDefault = false)
@Extension
public class Silver extends Script
{
	@Inject
	private Client client;

	private int idleTicks;
//	Random random;

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
//		random = new Random();
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
		if (client.getRealSkillLevel(Skill.CRAFTING) >= 41) {
			this.setPaused(true);
		}

		if (idleTicks < 3) return 937;

		// bar menu
		if (Production.isOpen()){
			Production.chooseOption(3);
			MousePackets.queueClickPacket();
			return 3233;
		}
		// silver menu
		else if (Widgets.get(6,23) != null)  // unstrung holy symbol widget
		{
			Widgets.get(6, 23).interact("Craft");
			MousePackets.queueClickPacket();
			return 3233;
		}
		// MAKE CANOE WIDGET
//		Widget shapeCanoeWidget = Widgets.get(416,3);
//		if (shapeCanoeWidget != null){
//			logger.info("shape canoe widget is not null");
//			Widgets.get(416,20,0).interact("Make Log canoe");
//			return 800;
//		}
//		else {
//			logger.info("shape canoe widget is null :/");
//		}

//		// group 647
//		// id: 14 edgeville, 15 lumbridge, 16 champ guild, 17 barb village, 18 wildy, 19 ferox
		else if (client.getWidget(647,15) != null)
		{
			Widgets.get(647, 15, 0).interact("Travel to Lumbridge");
			MousePackets.queueClickPacket();
			return 6325;
		}

		if (Inventory.isFull()){
			if (Inventory.contains("Silver ore") || Inventory.contains("Silver bar"))
			{
				if (!(client.getLocalPlayer().getWorldLocation().getRegionID() == 12850)) {
					if (Movement.getRunEnergy() < 50 && Movement.isRunEnabled())
					{
						Movement.toggleRun();
						MousePackets.queueClickPacket();
					}

					if (!Magic.isHomeTeleportOnCooldown())  // HOME TELE
					{
						Magic.cast(SpellBook.Standard.HOME_TELEPORT);
						MousePackets.queueClickPacket();
						waitUntilDone();

						Movement.walk(new WorldPoint(3220,3239,client.getPlane()));
						MousePackets.queueClickPacket();
						waitUntilDone();

						if (Inventory.contains("Mind rune") && Inventory.contains("Air rune")){
							Inventory.getFirst("Mind rune").drop();
							MousePackets.queueClickPacket();
							Time.sleep(283);
							Inventory.getFirst("Air rune").drop();
							MousePackets.queueClickPacket();
							Time.sleep(411);
							NPCs.getNearest("Magic combat tutor").interact("Claim");
							MousePackets.queueClickPacket();
							waitUntilDone();
							TileItems.getNearest("Mind rune").pickup();
							MousePackets.queueClickPacket();
							waitUntilDone(2);
							TileItems.getNearest("Air rune").pickup();
							MousePackets.queueClickPacket();
						}
					}
					else  // CANOE
					{
						TileObject canoeTree = TileObjects.getNearest("Canoe Station");
						if (canoeTree == null)
						{
							// south of champ guild
							Movement.walk(new WorldPoint(3187, 3349, client.getPlane()));
							MousePackets.queueClickPacket();
							Time.sleep(6373);

							canoeTree = TileObjects.getNearest("Canoe Station");
						}

						if (canoeTree.hasAction("Chop-down"))
						{
							canoeTree.interact("Chop-down");
							MousePackets.queueClickPacket();
							return 1800;
						}

						if (canoeTree.hasAction("Shape-Canoe"))
						{
							canoeTree.interact("Shape-Canoe");
							MousePackets.queueClickPacket();
							return 1337;
						}

						if (canoeTree.hasAction("Float Log"))
						{
							canoeTree.interact("Float Log");
							MousePackets.queueClickPacket();
							return 1657;
						}
						else if (canoeTree.hasAction("Float Canoe"))
						{
							canoeTree.interact("Float Canoe");
							MousePackets.queueClickPacket();
							return 1657;
						}

						if (canoeTree.hasAction("Paddle Canoe"))
						{
							canoeTree.interact("Paddle Canoe");
							MousePackets.queueClickPacket();
							return 1201;
						}

					}
					return 800;
				}

				TileObject furnace = TileObjects.getNearest(24009);
				if (furnace == null){
					Movement.walk(new WorldPoint(3227,3254,client.getPlane()));
					MousePackets.queueClickPacket();
					return 2000;
				}

				if (Inventory.contains("Silver ore"))
				{
					furnace.interact("Smelt");
					MousePackets.queueClickPacket();
				}
				else if (Inventory.contains("Silver bar"))
				{
					Inventory.getFirst("Silver bar").useOn(furnace);
				}
				return 1424;
			}
		}

		if (Inventory.contains(1714)) {
			if (!Movement.isRunEnabled()){
				Movement.toggleRun();
				MousePackets.queueClickPacket();
			}

			// sell to general store
			NPC shop = NPCs.getNearest("Shop keeper", "Shop assistant");
			while (shop == null){
				Movement.walk(new WorldPoint(3215,3245,client.getPlane()));
				MousePackets.queueClickPacket();
				Time.sleep(3211);
				shop = NPCs.getNearest("Shop keeper", "Shop assistant");
			}
			shop.interact("Trade");
			MousePackets.queueClickPacket();

			Time.sleepUntil(Shop::isOpen, 400,12000);

			Shop.sellFive(1714);  // unstrung holy symbol
			MousePackets.queueClickPacket();

			if (Inventory.contains(1714))
			{
				Movement.walk(client.getLocalPlayer().getWorldLocation());
				MousePackets.queueClickPacket();

				Time.sleep(367);

				hopRandom();
			}

			return 332;
		}

		// not in silver mine
		if (!Inventory.isFull() && client.getLocalPlayer().getWorldLocation().getRegionID() != 12596)
		{
			if (!Movement.isRunEnabled()) Movement.toggleRun();
			MousePackets.queueClickPacket();

			Equipment.getFirst("Chronicle").interact("Teleport");
			MousePackets.queueClickPacket();
			Time.sleep(3017);

			Movement.walk(new WorldPoint(3177,3368,0));
			MousePackets.queueClickPacket();
			Time.sleep(5462);
		}

		TileObject silverRock = TileObjects.getNearest("Silver rocks");
		if (silverRock == null)
			hopRandom();
		else
		{
			silverRock.interact("Mine");
			MousePackets.queueClickPacket();
		}

		return 600;
	}

	private void waitUntilDone(int i){
		idleTicks = 0;
		Time.sleepTicksUntil(() -> idleTicks > i, 100);
	}

	private void waitUntilDone(){
		waitUntilDone(3);
	}

	private void hopRandom(){

		int randomWorld = Worlds.getRandom(w -> !w.isMembers() && w.isNormal()).getId();
		while (randomWorld == Worlds.getCurrentId()) {
			randomWorld = Worlds.getRandom(w -> !w.isMembers() && w.isNormal()).getId();
		}
		Worlds.hopTo(Worlds.getFirst(randomWorld));

		Time.sleepTicks(3);
		Time.sleepTicksUntil(() -> client.getGameState() != GameState.HOPPING, 10);
	}
}
