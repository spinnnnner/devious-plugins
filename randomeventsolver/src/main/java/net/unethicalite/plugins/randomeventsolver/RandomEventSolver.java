package net.unethicalite.plugins.randomeventsolver;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import com.google.inject.Inject;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.coords.Direction;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.input.naturalmouse.util.Pair;
import net.unethicalite.api.items.DepositBox;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Tabs;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;
import org.pf4j.Extension;

@SuppressWarnings("MagicConstant")
@PluginDescriptor(name = "Random Event Solver", enabledByDefault = false)
@Extension
public class RandomEventSolver extends Script
{
	// from RuneLite
	private static final Set<Integer> EVENT_NPCS = ImmutableSet.of(
		NpcID.BEE_KEEPER_6747,
		NpcID.CAPT_ARNAV,
		NpcID.DR_JEKYLL, NpcID.DR_JEKYLL_314,
		NpcID.DRUNKEN_DWARF,
		NpcID.DUNCE_6749,
		NpcID.EVIL_BOB, NpcID.EVIL_BOB_6754,
		NpcID.FLIPPA_6744,
		NpcID.FREAKY_FORESTER_6748,
		NpcID.FROG_5429,
		NpcID.GENIE, NpcID.GENIE_327,
		NpcID.GILES, NpcID.GILES_5441,
		NpcID.LEO_6746,
		NpcID.MILES, NpcID.MILES_5440,
		NpcID.MYSTERIOUS_OLD_MAN_6750, NpcID.MYSTERIOUS_OLD_MAN_6751,
		NpcID.MYSTERIOUS_OLD_MAN_6752, NpcID.MYSTERIOUS_OLD_MAN_6753,
		NpcID.NILES, NpcID.NILES_5439,
		NpcID.PILLORY_GUARD,
		NpcID.POSTIE_PETE_6738,
		NpcID.QUIZ_MASTER_6755,
		NpcID.RICK_TURPENTINE, NpcID.RICK_TURPENTINE_376,
		NpcID.SANDWICH_LADY,
		NpcID.SERGEANT_DAMIEN_6743
	);
	private static final Set<Integer> EVENT_INTERNAL_NPCS = ImmutableSet.of(
		NpcID.BEE_KEEPER, NpcID.SERGEANT_DAMIEN, NpcID.SERVANT_393, NpcID.EVIL_BOB_391,
		NpcID.FREAKY_FORESTER, NpcID.LEO, NpcID.MIME, NpcID.TILT, NpcID.QUIZ_MASTER, NpcID.MR_MORDAUT
	);

	private final List<Integer> mimeAnimations = Arrays.asList(857,860,861,866,1128,1129,1130,1131);

	// these are offset to the centre of the maze to ensure the tile is walkable (reachable is being annoying)
	// yellow, orange, green, blue
	private List<Pair<Integer,Integer>> mazeStartPositions;
	private List<Pair<Integer, Integer>> mazeCommon;
	private List<Pair<Integer, Integer>> mazeYellow;
	private List<Pair<Integer, Integer>> mazeOrange;
	private List<Pair<Integer, Integer>> mazeGreen;
	private List<Pair<Integer, Integer>> mazeBlue;

	private boolean inRandomEvent;
	private NPC currentRandomEvent;
	private int lastMimeEmote;

	private final HashMap<Integer, Integer> mollyMap = new HashMap<>();
	private final HashMap<String, Integer> arnavChoices = new HashMap<>();

	private final int[] drillDemonMats = {20810, 16508, 9313, 20801};

	@Inject
	private Client client;

	@Inject
	private RandomEventSolverConfig config;

	@Inject
	private Notifier notifier;

	@Provides
	RandomEventSolverConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RandomEventSolverConfig.class);
	}

	@Override
	public void startUp()  // regular plugin stuff
	{
		logger.debug("test");
		lastMimeEmote = -1;
		inRandomEvent = false;
		currentRandomEvent = null;

		mollyMap.put(342,5468);
		mollyMap.put(352,357);
		mollyMap.put(356,351);
		mollyMap.put(361,5484);
		mollyMap.put(362,5465);
		mollyMap.put(363,343);
		mollyMap.put(364,345);
		mollyMap.put(365,354);
		mollyMap.put(366,355);
		mollyMap.put(367,359);
		mollyMap.put(5464,5469);
		mollyMap.put(5467,347);
		mollyMap.put(5471,350);
		mollyMap.put(5474,5470);
		mollyMap.put(5476,358);
		mollyMap.put(5478,349);
		mollyMap.put(5480,346);
		mollyMap.put(5485,5482);
		mollyMap.put(5486,5483);
		mollyMap.put(5487,344);

		arnavChoices.put("bowl", 20104);
		arnavChoices.put("ring", 7753);
		arnavChoices.put("coins", 7752);
		arnavChoices.put("bar", 7751);

		mazeStartPositions = Arrays.asList(new Pair<>(11,-21),new Pair<>(-21,15), new Pair<>(15,21), new Pair<>(-8,-21));
		mazeCommon = Arrays.asList(new Pair<>(4, 10), new Pair<>(0, 8), new Pair<>(6, 0), new Pair<>(0, -4), new Pair<>(-2, 0));
		mazeYellow = Arrays.asList(new Pair<>(12, -22), (new Pair<>(4, -24)),(new Pair<>(24, -16)),
			(new Pair<>(22, -11)), (new Pair<>(20, -15)), (new Pair<>(18, -3)),(new Pair<>(18, 5)),
			(new Pair<>(18, 10)), (new Pair<>(16, 12)), (new Pair<>(14, 2)), (new Pair<>(12, 7)));
		mazeOrange = Arrays.asList(new Pair<>(-22, 16), (new Pair<>(-24, 11)),(new Pair<>(-14, 24)),
			(new Pair<>(-17, 22)), (new Pair<>(-13, 20)), (new Pair<>(-18, 11)),(new Pair<>(-16, 7)),
			(new Pair<>(-14, -6)), (new Pair<>(-12, -9)), (new Pair<>(-1, -10)),(new Pair<>(-6, 10)));
		mazeGreen = Arrays.asList(new Pair<>(16, 22), (new Pair<>(9, 24)),(new Pair<>(2, 24)),
			(new Pair<>(-2, 22)), (new Pair<>(3, 20)), (new Pair<>(-9, 18)),(new Pair<>(-4, 16)),
			(new Pair<>(-14, 9)), (new Pair<>(-9, 12)), (new Pair<>(-10, -1)),(new Pair<>(-6, 10)));
		mazeBlue = Arrays.asList(new Pair<>(-9, -22), (new Pair<>(-22, -13)),
			(new Pair<>(-20, -2)), (new Pair<>(-18, 4)), (new Pair<>(-16, -14)), (new Pair<>(-7, -16)),
			(new Pair<>(3, -16)), (new Pair<>(-3, -14)), (new Pair<>(12, -10)), (new Pair<>(10, -1)),
			(new Pair<>(4, -8)), (new Pair<>(-8, -3)), (new Pair<>(-6, 10)));
	}

	@Override
	public void onStart(String... args)  // looped plugin stuff
	{
		if (NPCs.getNearest("Molly") != null){
			molly(true);
		}
		else if (TileObjects.getNearest(14979) != null){
			maze(true);
		}
		resetRandomState();

		EVENT_INTERNAL_NPCS.forEach(id -> {
			if (NPCs.getNearest(id) != null){
				currentRandomEvent = NPCs.getNearest(id);
			}
		});
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned e){
		// runs every time if u run in/out of render distance. does this matter?
		if (inRandomEvent) return;  // don't change to internal npc if the plugin triggered the start of the event

		if (EVENT_INTERNAL_NPCS.contains(e.getNpc().getId())){
			inRandomEvent = true;
			currentRandomEvent = e.getNpc();
		}

		if (e.getNpc().getId() != NpcID.STRANGE_PLANT){
			return;
		}

		WorldPoint pos = e.getNpc().getWorldLocation();

		if (new WorldArea(pos.dx(-2).dy(-2),5,5).contains(client.getLocalPlayer().getWorldLocation())){
			e.getNpc().interact("Pick");
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged e){
		if (client.getGameState() != GameState.LOGGED_IN) return;
		if (!inRandomEvent || !(e.getActor() instanceof NPC) || e.getActor().getId() != 321){
			return;
		}

		int animationId = e.getActor().getAnimation();

		if (mimeAnimations.contains(animationId)){
			logger.debug("mime did animation: "+animationId);
			lastMimeEmote = animationId;
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged e)
	{
		if (inRandomEvent) return;

		Actor source = e.getSource();
		Actor target = e.getTarget();
		Player player = client.getLocalPlayer();

		// Check that the npc is interacting with the player and the player isn't interacting with the npc, so
		// that the notification doesn't fire from talking to other user's randoms
		if (player == null
			|| target != player
			|| player.getInteracting() == source
			|| !(source instanceof NPC)
			|| !EVENT_NPCS.contains(source.getId()))
		{
			return;
		}

		currentRandomEvent = (NPC) source;
		logger.info("Got a random event: "+ Text.removeTags(currentRandomEvent.getName()));
		notifier.notify("Got a random event: "+ Text.removeTags(currentRandomEvent.getName()));
	}

	@Override
	protected int loop(){  // have to do this because clientThread can't sleep
		if (currentRandomEvent == null){
			return 5000;
		}

		// if in combat when the random event appears, wait until it is dead then accept random event (change to config option?)
		// TODO: change this to use hitsplat instead
		if (client.getLocalPlayer().isInteracting()){
			Time.sleepTicksUntil(() -> client.getLocalPlayer().getInteracting().isDead(), 20);
		}

		int id = currentRandomEvent.getId();

		if (config.dismissClogs()){
			switch(id){
				case NpcID.BEE_KEEPER_6747:
				case NpcID.SERGEANT_DAMIEN_6743:
				case NpcID.FREAKY_FORESTER_6748:
				case NpcID.FROG_5429:
				case NpcID.LEO_6746:
				case NpcID.MYSTERIOUS_OLD_MAN_6753:
					dismiss();
					return 1000;
			}
		}

		switch (id){
			case NpcID.BEE_KEEPER_6747:
			case NpcID.BEE_KEEPER:
				beekeeper(id == NpcID.BEE_KEEPER);
				break;
			case NpcID.CAPT_ARNAV:
				arnav();
				break;
			case NpcID.NILES:
			case NpcID.NILES_5439:
			case NpcID.MILES:
			case NpcID.MILES_5440:
			case NpcID.GILES:
			case NpcID.GILES_5441:
				niles();
				break;
			case NpcID.SERGEANT_DAMIEN_6743:
			case NpcID.SERGEANT_DAMIEN:
				drilldemon(id == NpcID.SERGEANT_DAMIEN);
				break;
			case NpcID.GENIE:
			case NpcID.GENIE_327:
			case NpcID.DRUNKEN_DWARF:
			case NpcID.RICK_TURPENTINE:
			case NpcID.RICK_TURPENTINE_376:
			case NpcID.MYSTERIOUS_OLD_MAN_6750:
			case NpcID.MYSTERIOUS_OLD_MAN_6751:
				talkTo();
				break;
			case NpcID.DR_JEKYLL:
				jekyll();
				break;
			case NpcID.EVIL_BOB:
			case NpcID.EVIL_BOB_391:
				evilbobfishing(id == NpcID.EVIL_BOB_391);
				break;
			case NpcID.POSTIE_PETE_6738:
				molly(false);
				break;
			case NpcID.FREAKY_FORESTER_6748:
			case NpcID.FREAKY_FORESTER:
				freakyforester(id == NpcID.FREAKY_FORESTER);
				break;
			case NpcID.LEO_6746:
			case NpcID.LEO:
				leo(id == NpcID.LEO);
				break;
			case NpcID.FROG_5429:
				frog();
				break;
			case NpcID.MYSTERIOUS_OLD_MAN_6752:  // this is correct
				maze(false);
				break;
			case NpcID.MYSTERIOUS_OLD_MAN_6753:  // this is correct
			case NpcID.MIME:
				mime(id == NpcID.MIME);
				break;
			case NpcID.PILLORY_GUARD:
				pillory();
				break;
			case NpcID.FLIPPA_6744:
			case NpcID.TILT:
				pinball(id == NpcID.TILT);
				break;
			case NpcID.EVIL_BOB_6754:
			case NpcID.PRISON_PETE:
				prisonpete(id == NpcID.PRISON_PETE);
				break;
			case NpcID.QUIZ_MASTER_6755:
			case NpcID.QUIZ_MASTER:
				quiz(id == NpcID.QUIZ_MASTER);
				break;
			case NpcID.SANDWICH_LADY:
				sandwichlady();
				break;
			case NpcID.DUNCE_6749:
			case NpcID.MR_MORDAUT:
				exam(id == NpcID.MR_MORDAUT);
				break;
			default:
				return 1000;
		}
		resetRandomState();
		return 1000;
	}

	// verified
	private void beekeeper(boolean alreadyInside){
		if (!alreadyInside) acceptRandom();
		inRandomEvent = true;

		while (true)
		{
			Widget hiveWidget = Widgets.get(420,0);

			if (hiveWidget != null && hiveWidget.isVisible()) break;

			if (!Dialog.isOpen())
			{
				NPC beekeeper = NPCs.getNearest(NpcID.BEE_KEEPER);

				if (beekeeper == null)
				{
					logger.error("inside beekeeper event but no beekeeper?");
					return;
				}

				beekeeper.interact("Talk-to");
				MousePackets.queueClickPacket(beekeeper);
				Time.sleepTicks(3);
				continue;
			}

			if (Dialog.canContinue()){
				Dialog.continueSpace();
				Time.sleepTick();
				continue;
			}

			if (Dialog.isViewingOptions()){
				Dialog.chooseOption(2);
				Time.sleepTick();
			}
		}

		while (Widgets.get(420,0).isVisible())
		{
			for (int i = 10; i < 14; i++)
			{
				Widget piece = Widgets.get(420, i);
				Widget release;

				switch (piece.getModelId())
				{
					case 28806: //lid
						release = Widgets.get(420, 15);
						break;
					case 28428: // body
						release = Widgets.get(420, 17);
						break;
					case 28803: // entrance
						release = Widgets.get(420, 19);
						break;
					default: // legs : 28808
						release = Widgets.get(420, 21);
				}

				// TODO: improve this with packets. this is a very bad solution lol

				Point pressPoint = piece.getClickPoint();
				Point releasePoint = release.getClickPoint();

				MouseEvent mousePressed = new MouseEvent(client.getCanvas(), MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0,
					pressPoint.getX(), pressPoint.getY(), 1, false, 1);
				MouseEvent move = new MouseEvent(client.getCanvas(), MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0,
					releasePoint.getX(), releasePoint.getY(), 0, false);
				MouseEvent mouseReleased = new MouseEvent(client.getCanvas(), MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0,
					releasePoint.getX(), releasePoint.getY(), 1, false, 1);
				MouseEvent mouseClicked = new MouseEvent(client.getCanvas(), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
					releasePoint.getX(), releasePoint.getY(), 1, false, 1);

				client.getCanvas().dispatchEvent(mousePressed);
				Time.sleep(362);

				client.getCanvas().dispatchEvent(move);
				client.getCanvas().dispatchEvent(mouseReleased);
				client.getCanvas().dispatchEvent(mouseClicked);

				Time.sleepTicks(2);
			}

			Widgets.get(420, 22).interact(0);
			Time.sleepTicks(3);
		}

		while (Dialog.canContinue()){
			Dialog.continueSpace();
			Time.sleepTick();
		}
	}

	private void arnav(){
		acceptRandom();

		Time.sleepTicksUntil(() -> Widgets.get(26,5).isVisible(), 5);
		Widget[] columns = {Widgets.get(26,22),Widgets.get(26,23),Widgets.get(26,24)};
		for (int i = 0; i < 3; i++){
			String goal = columns[i].getText().toLowerCase();

			// check if model matches string
			while (arnavChoices.get(goal) != Widgets.get(26,16+i).getModelId()){
				Widgets.get(26,5+i*3).interact(0);
				MousePackets.queueClickPacket(Widgets.get(26,5));
				Time.sleepTick();
			}
		}

		// unlock chest
		Widgets.get(26,25).interact(0);
		MousePackets.queueClickPacket(Widgets.get(26,25));
	}

	private void niles(){
		acceptRandom();

		Time.sleepTicksUntil(Widgets.get(184,7)::isVisible, 5);

		int model = Widgets.get(184,7).getModelId();
		String modelString = "";

		// how many of these are actually possible?
		switch(model){
			case 8828:
				modelString = "axe";
				break;
			case 8829:
			case 8830:
				modelString = "fish";
				break;
			case 8831:
				modelString = "necklace";
				break;
			case 8832:
				modelString = "shield";
				break;
			case 8833:
				modelString = "helm";
				break;
			case 8834:
				modelString = "ring";
				break;
			case 8835:
				modelString = "shears";
				break;
			case 8836:
				modelString = "sword";
				break;
			case 8837:
				modelString = "spade";
				break;
			default:
				logger.error("niles: couldn't match model to a string");
		}

		for (int i = 0; i < 3; i++){
			if (Widgets.get(184,i+1).getText().toLowerCase().contains(modelString)){
				Widgets.get(184,i+8).interact(0);
				MousePackets.queueClickPacket(Widgets.get(184,i+8));
				return;
			}
		}
	}

	// verified
	private void drilldemon(boolean alreadyInside){
		if (!alreadyInside) acceptRandom();
		inRandomEvent = true;

		NPC drillDemon = NPCs.getNearest(NpcID.SERGEANT_DAMIEN);

		while (!Dialog.isOpen()){
			drillDemon.interact("Talk-to");
			MousePackets.queueClickPacket(drillDemon);
			Time.sleepTicksUntil(Dialog::isOpen, 5);
		}

		while (true){
			Time.sleepOneTickThenUntil(Dialog::canContinueNPC, 15);
			if (!Dialog.canContinue()){
				drillDemon.interact("Talk-to");
				MousePackets.queueClickPacket(drillDemon);
				continue;
			}

			int action;

			String text = Dialog.getText();

			if (text.contains("you actually did it")) break;
			else if (text.contains("Follow my orders") || text.contains("given you the order yet")) {
				Dialog.continueSpace();
				continue;
			}

			else if (text.contains("jog")) action = 1;
			else if (text.contains("sit up")) action = 2;
			else if (text.contains("push up")) action = 3;
			else if (text.contains("star jump")) action = 4;
			else {
				logger.error("couldn't find the action: "+Dialog.getText());
				return;
			}

			for (int i = 0; i < 4; i++){
				if (client.getVarbitValue(i+1335) == action){
					TileObject mat = TileObjects.getNearest(drillDemonMats[i]);
					mat.interact(0);
					MousePackets.queueClickPacket(mat);
					break;
				}
			}
		}

		if (Dialog.canContinue())
		{
			Dialog.continueSpace();
			Time.sleepTick();
		}
	}

	private void talkTo(){
		// genie, drunken dwarf, rick turpentine, f2p mom (which one?)
		currentRandomEvent.interact("Talk-to");
		MousePackets.queueClickPacket(currentRandomEvent);
	}

	// verified
	private void evilbobfishing(boolean alreadyInside){
		if (!alreadyInside) acceptRandom();
		inRandomEvent = true;

		NPC servant = NPCs.getNearest(393);
		NPC bob = NPCs.getNearest(391);

		int cameraYaw = client.getCameraYaw();

		Direction cutsceneDirection = null;

		while (!Inventory.contains(6202)){
			// talk to servant
			servant.interact("Talk-to");
			MousePackets.queueClickPacket(servant);
			Time.sleepTicksUntil(Dialog::canContinue, 8);

			// watch camera yaw
			while (Dialog.isOpen()){
				if (Dialog.canContinue()){
					Dialog.continueSpace();
				}
				else // TODO: change to detect the "Watch the statue!" widget
				{
					int cutsceneYaw = client.getCameraYaw();
					if (cutsceneDirection == null && cutsceneYaw != cameraYaw){
						if (cutsceneYaw < 256 || cutsceneYaw > 1792){
							cutsceneDirection = Direction.NORTH;
						}
						else if (cutsceneYaw < 784){ // cutsceneYaw >= 256 &&
							cutsceneDirection = Direction.EAST;
						}
						else if (cutsceneYaw < 1296){ // cutsceneYaw >= 784 &&
							cutsceneDirection = Direction.SOUTH;
						}
						else cutsceneDirection = Direction.WEST;
					}
				}
			}
			Time.sleepTicksUntil(() -> !Dialog.isOpen(), 10);

			// grab fishing net 6209 first if i don't have one
			if (!Inventory.contains(6209)){
				TileItem net = TileItems.getNearest(6209);
				net.pickup();
				MousePackets.queueClickPacket(net);
				Time.sleepTicksUntil(() -> Inventory.contains(6209), 12);
			}

			// go in cutscene direction
			WorldPoint goalTile = new WorldPoint(2526,4778,0);
			if (cutsceneDirection == null) continue;
			switch (cutsceneDirection){
				case NORTH:
					goalTile = goalTile.dy(12);
					break;
				case EAST:
					goalTile = goalTile.dx(15);
					break;
				case SOUTH:
					goalTile = goalTile.dy(-12);
					break;
				case WEST:
					goalTile = goalTile.dx(-15);
			}
			Tiles.getAt(goalTile).walkHere();
			MousePackets.queueClickPacket();
			Time.sleepOneTickThenUntil(() -> !client.getLocalPlayer().isMoving(), 12);

			TileObject fishingSpot = TileObjects.getNearest(23114);
			fishingSpot.interact(0);
			MousePackets.queueClickPacket(fishingSpot);
			Time.sleepTicksUntil(() -> Inventory.contains(6202) || Inventory.contains(6206), 10);

			// fish the fish, make sure it's 6202. if not, drop the 6206 and restart from talking to servant
			if (Inventory.contains(6206)){
				Item wrongFish = Inventory.getFirst(6206);
				wrongFish.drop();
				MousePackets.queueClickPacket(wrongFish);
			}
			if (Inventory.contains(6202)) break;
		}

		// TODO: handle if it gets multiple fish?

		// if we have 6202 in invent, use on tileobject 23113
		Item rightFish = Inventory.getFirst(6202);
		TileObject uncookingPot = TileObjects.getNearest(23113);
		MousePackets.queueClickPacket(rightFish);
		Time.sleep(161);
		rightFish.useOn(uncookingPot);
		MousePackets.queueClickPacket(uncookingPot);
		Time.sleepTicksUntil(() -> Inventory.contains(6200), 20);

		// should get 6200, not 6204 (drop 6204 if we get any)

		// use 6200 on 391, sleep until invent doesn't contain 6200
		Item rawFish = Inventory.getFirst(6200);
		MousePackets.queueClickPacket(rawFish);
		Time.sleep(112);
		rawFish.useOn(bob);
		MousePackets.queueClickPacket(bob);
		Time.sleepTicksUntil(() -> !Inventory.contains(6200), 8);

		// tileobject 23115 "Enter"
		TileObject exitPortal = TileObjects.getNearest(23115);
		exitPortal.interact(0);
		MousePackets.queueClickPacket(exitPortal);
		Time.sleepTicks(7);
	}

	private void molly(boolean alreadyInside){
		if (!alreadyInside) acceptRandom();
		inRandomEvent = true;

		int mollyId = NPCs.getNearest("Molly").getId();
		int suspectId = mollyMap.get(mollyId);

		// go through door
		TileObject door = TileObjects.getNearest(20817);
		door.interact(0);
		MousePackets.queueClickPacket(door);
		Time.sleepTicksUntil(Dialog::canContinue, 5);
		acceptRandom();  // go through door dialogue

		TileObject console = TileObjects.getNearest(20813);
		console.interact(0);
		MousePackets.queueClickPacket(console);
		Time.sleepTicksUntil(Widgets.get(277,3)::isVisible, 8);


		while (Widgets.get(277,3).isVisible()){ // TODO: change this to related varbit?
			WorldPoint suspectPos = NPCs.getNearest(suspectId).getWorldLocation();
			WorldPoint grabberPos = TileObjects.getNearest(20811).getWorldLocation();

			int dx = grabberPos.getX() - suspectPos.getX();
			int dy = grabberPos.getY() - suspectPos.getY();

			if (dx == 0 && dy == 0){
				Widgets.get(277,3).interact(0);
				MousePackets.queueClickPacket(Widgets.get(277,3));
				Time.sleepTick();
				continue;
			}

			Widget click;
			if (dx != 0)
			{
				if (dx > 0) // positive: need to move west = right
				{
					click = Widgets.get(277, 7);
				}
				else
				{
					click = Widgets.get(277, 9);
				}
				for (int i = 0; i < Math.max(Math.abs(dx), 3); i++)
				{
					click.interact(0);
					MousePackets.queueClickPacket(click);
					Time.sleep(87);
				}
			}
			else
			{
				if (dy > 0) // positive: need to move south = up
				{
					click = Widgets.get(277, 8);
				}
				else
				{
					click = Widgets.get(277, 10);
				}
				for (int i = 0; i < Math.max(Math.abs(dy), 3); i++)
				{
					click.interact(0);
					MousePackets.queueClickPacket(click);
					Time.sleep(87);
				}
			}

			Time.sleepTick();
		}

		// go through door again
		door.interact(0);
		MousePackets.queueClickPacket(door);
		Time.sleepOneTickThenUntil(() -> client.getLocalPlayer().isIdle(), 10);

		// talk to molly again, spacebar through dialogue
	}

	// verified
	private void freakyforester(boolean alreadyInside){
		if (!alreadyInside) acceptRandom();
		inRandomEvent = true;

		NPC forester = NPCs.getNearest(372);

		while (!Dialog.canContinue()){
			forester.interact("Talk-to");
			MousePackets.queueClickPacket(forester);
			Time.sleepTicksUntil(Dialog::canContinue, 5);
		}

//		Time.sleepTicksUntil(Dialog::canContinueNPC, 5);
//		String npcText = Widgets.get(WidgetID.DIALOG_NPC_GROUP_ID, 4).getText();
//		Pattern pattern = Pattern.compile(" ([\\w\\d]+)[ -]+tail"); // apparently dialogue is "X- tailed": space after hyphen
//
//		Matcher matcher = pattern.matcher(npcText);
//		String numberString = matcher.group();
//
//		int npcId;
//
//		switch(numberString){
//			case "one":
//			case "1":
//				npcId = 373;
//				break;
//			case "two":
//			case "2":
//				npcId = 5500;
//				break;
//			case "three":
//			case "3":
//				npcId = 374;
//				break;
//			default:
//				npcId = 5502;
//		}

		NPC pheasant = NPCs.getNearest("Pheasant");
		pheasant.interact("Kill");
		MousePackets.queueClickPacket(pheasant);
		Time.sleepTicksUntil(() -> TileItems.getNearest("Raw pheasant") != null, 10);

		TileItems.getNearest("Raw pheasant").pickup();
		MousePackets.queueClickPacket();
		Time.sleepTicksUntil(() -> Inventory.contains("Raw pheasant"), 5);

		forester.interact("Talk-to");
		MousePackets.queueClickPacket(forester);
		Time.sleepTicksUntil(() -> !Inventory.contains("Raw pheasant"), 10);

		TileObject exitPortal = TileObjects.getNearest(new WorldPoint(2611,4776,0), 20843);
		exitPortal.interact("Use");
		MousePackets.queueClickPacket(exitPortal);
		Time.sleepOneTickThenUntil(() -> client.getLocalPlayer().isIdle(), 10);
	}

	private void leo(boolean alreadyInside){
		if (!alreadyInside) acceptRandom();
		inRandomEvent = true;

		int[] coffinIds = {7587, 7588, 7589, 7590, 7591};
		int[] coffinOrder = new int[5];

		if (Inventory.getFreeSlots() < 5){
			TileObject depositBox = TileObjects.getNearest(10055);
			depositBox.interact("Deposit");
			MousePackets.queueClickPacket(depositBox);
			Time.sleepOneTickThenUntil(Dialog::isOpen, 5);

			// press space to continue through the warning
			Dialog.continueSpace();
			Time.sleepTicksUntil(DepositBox::isOpen, 3);

			// deposit all noted items
			for (int i = 0; i < 28; i++){
				if (Inventory.getItem(i) != null && Objects.requireNonNull(Inventory.getItem(i)).isNoted()){
					Objects.requireNonNull(Widgets.get(192, 2, i)).interact("Deposit-All");
					MousePackets.queueClickPacket(Objects.requireNonNull(Widgets.get(192, 2, i)));
					Time.sleepTick();
				}
			}

		}

		// talk to leo
		NPC leo = NPCs.getNearest(6745);
		leo.interact("Talk-to");
		MousePackets.queueClickPacket(leo);
		Time.sleepTicksUntil(Dialog::canContinueNPC, 5);
		while (Dialog.canContinue()){
			Dialog.continueSpace();
			Time.sleepTick();
		}

		// check gravestones for order
		for (int i = 0; i < 5; i++){
			TileObject gravestone = TileObjects.getNearest(9359 + i);
			gravestone.interact(0);
			MousePackets.queueClickPacket(gravestone);

			Time.sleepTicksUntil(() -> Widgets.get(175,1).isVisible(), 10);

			switch (Widgets.get(175,1).getModelId()){
				case 13403:
					coffinOrder[i] = 7587;
					break;
				case 16305:
					coffinOrder[i] = 7588;
					break;
				case 13404:
					coffinOrder[i] = 7589;
					break;
				case 13402:
					coffinOrder[i] = 7590;
					break;
				case 13399:
					coffinOrder[i] = 7591;
					break;
				default:
					logger.error(
						"leo: can't figure out what profession the gravestone widget corresponds to. "+Widgets.get(175,1).getModelId());
			}
		}

		// take coffins and replace
		int[] graveIds = {9364, 9365, 9366, 9367, 10049};

		for (int i = 0; i < graveIds.length; i++){
			// take coffin from grave
			TileObject grave = TileObjects.getNearest(graveIds[i]);
			grave.interact(0);
			MousePackets.queueClickPacket();
			Time.sleepOneTickThenUntil(() -> client.getLocalPlayer().isIdle(), 10);
			Time.sleepTick();

			if (Inventory.contains(coffinOrder[i])){
				Inventory.getFirst(coffinOrder[i]).useOn(grave);
				MousePackets.queueClickPacket();
				Time.sleepTicks(2);
			}
		}

		for (int i = 0; i < coffinIds.length; i++){
			if (Inventory.contains(coffinIds[i])){
				Inventory.getFirst(coffinIds[i]).useOn(TileObjects.getNearest(graveIds[i]));
				MousePackets.queueClickPacket();
				Time.sleepOneTickThenUntil(() -> client.getLocalPlayer().isIdle(), 10);
			}
		}

		leo.interact("Talk-to");
		MousePackets.queueClickPacket();

		Time.sleepTicksUntil(Dialog::isOpen, 5);
//		logger.debug("There, finished");
		Dialog.chooseOption(1);
		Time.sleepTick();
		Dialog.continueSpace();
//		logger.debug("Let's see");
		Time.sleepTicksUntil(Dialog::isOpen, 5);
		while (Dialog.canContinueNPC()){
			Dialog.continueSpace();
			Time.sleepTick();
		}
	}

	private void frog(){
		NPCs.getNearest(5431).interact(0);
		Time.sleepTicksUntil(Dialog::canContinue, 5);
		while (Dialog.canContinue()){
			Dialog.continueSpace();
			Time.sleepTick();
		}
		if (Dialog.isViewingOptions()){
			List<Widget> options = Dialog.getOptions();
			for (int i = 0; i < options.size(); i++){
				Widget w = options.get(i);
				String text = w.getText().strip().toLowerCase();
				if (text.contains("sure") || text.contains("okay") || text.contains("yes") || text.contains("right")){
					Dialog.chooseOption(1+i);
					break;
				}
			}
		}

	}

	private void jekyll(){
		acceptRandom();
	}

	// verified regular run, chest run not yet
	private void maze(boolean alreadyInside){
		if (!alreadyInside) acceptRandom();
		inRandomEvent = true;

		Time.sleepTicksUntil(Dialog::canContinue, 5);

		Direction mazeDirection;
		int whichMazeStart = -1;
		List<Pair<Integer, Integer>> firstMazeSection;

		TileObject shrine = TileObjects.getNearest(14985);
		WorldPoint shrineLocation = shrine.getWorldLocation();
		logger.info("Shrine location: "+shrineLocation.getWorldLocation());

		WorldPoint gateLocation = TileObjects.getNearest(shrineLocation, 14979).getWorldLocation();
		logger.debug("Gate location: "+gateLocation.getWorldLocation());

		if (shrineLocation.getX() == gateLocation.getX()){
			if (shrineLocation.getY() == gateLocation.getY() - 2)
				mazeDirection = Direction.NORTH;
			else if (shrineLocation.getY() == gateLocation.getY() + 2)
				mazeDirection = Direction.SOUTH;
			else {
				logger.error("maze random: direction?? x = x but y is not 2 difference");
				return;
			}
		}
		else if (shrineLocation.getY() == gateLocation.getY()){
			if (shrineLocation.getX() == gateLocation.getX() - 2)
				mazeDirection = Direction.EAST;
			else if (shrineLocation.getX() == gateLocation.getX() + 2)
				mazeDirection = Direction.WEST;
			else {
				logger.error("maze random: direction?? y = y but x is not 2 difference");
				return;
			}
		}
		else {
			logger.error("maze random: x!=x and y!=y");
			return;
		}

		logger.info("direction: " + mazeDirection);

		mazeCommon.forEach(p -> rotate(p, mazeDirection));
		mazeStartPositions.forEach(p -> rotate(p, mazeDirection));

		for (int i = 0; i < mazeStartPositions.size(); i++)
		{
			WorldPoint startGateLocation =
				new WorldPoint(mazeStartPositions.get(i).x + shrineLocation.getX(), mazeStartPositions.get(i).y + shrineLocation.getY(), client.getPlane());
			TileObject gate = TileObjects.getFirstSurrounding(startGateLocation, 2,14979);

			if (gate == null){
				logger.info("gate is null???");
				return;
			}

			logger.info(i+" start gate location: "+gate.getWorldLocation().getX()+", "+gate.getWorldLocation().getY());

			if (Reachable.isWalkable(startGateLocation)){
				logger.info("This gate is reachable");
				whichMazeStart = i;
				break;
			}
		}

		if (whichMazeStart == -1)
		{
			logger.error("maze random: can't find the starting point");
			return;
		}

		switch(whichMazeStart){
			case 0:
				firstMazeSection = mazeYellow;
				break;
			case 1:
				firstMazeSection = mazeOrange;
				break;
			case 2:
				firstMazeSection = mazeGreen;
				break;
			case 3:
				firstMazeSection = mazeBlue;
				break;
			default:
				logger.error("got maze start but couldn't match first maze section to it? "+whichMazeStart);
				return;
		}

		logger.debug("First maze section doors");
		firstMazeSection.forEach(i -> {
			rotate(i, mazeDirection);
			i.x = i.x + shrineLocation.getX();
			i.y = i.y + shrineLocation.getY();

			logger.debug("("+i.x+", "+i.y+")");
		});

		logger.debug("Maze common doors");
		mazeCommon.forEach(i -> {
			i.x = i.x + shrineLocation.getX();
			i.y = i.y + shrineLocation.getY();

			logger.debug("("+i.x+", "+i.y+")");
		});

		firstMazeSection.forEach(Pair -> {
			if (client.getVarbitValue(995) == 0) return;

			if (!Tabs.isOpen(Tab.ACCOUNT)){
				Static.getClientThread().invoke(() -> client.runScript(915, 8));  // change to accounts tab
				Time.sleepTick();
				Widgets.get(109,11).interact(0);
				MousePackets.queueClickPacket(Widgets.get(109,11));
			}
			goThroughGate(Pair);
		});

		mazeCommon.forEach(Pair -> {
			if (client.getVarbitValue(995) == 0) return;

			if (!Tabs.isOpen(Tab.ACCOUNT)){
				Static.getClientThread().invoke(() -> client.runScript(915, 8));  // change to accounts tab
				Time.sleepTick();
				Widgets.get(109,11).interact(0);
				MousePackets.queueClickPacket(Widgets.get(109,11));
			}
			goThroughGate(Pair);
		});

		shrine.interact("Touch");
		MousePackets.queueClickPacket(shrine);
	}

	private void mime(boolean alreadyInside){
		if (!alreadyInside) acceptRandom();
		inRandomEvent = true;

		while (NPCs.getNearest(321) != null){
			Time.sleepTicksUntil(() -> Widgets.get(188,2).isVisible(), 10);

			if (lastMimeEmote == -1){
				// log error
				logger.error("mime emote widget is visible but mime didn't emote?");
				Time.sleepTicksUntil(() -> lastMimeEmote != -1, 5);
			}
			else
			{
				Widget emote;

				switch(lastMimeEmote){
					case 857:
						emote = Widgets.get(188,2);
						break;
					case 861:
						emote = Widgets.get(188,3);
						break;
					case 1130:
						emote = Widgets.get(188,4);
						break;
					case 1131:
						emote = Widgets.get(188,5);
						break;
					case 860:
						emote = Widgets.get(188,6);
						break;
					case 866:
						emote = Widgets.get(188,7);
						break;
					case 1129:
						emote = Widgets.get(188,8);
						break;
					default:
						emote = Widgets.get(188,9);
				}

				lastMimeEmote = -1;
				emote.interact(0);
				MousePackets.queueClickPacket(emote);
				Time.sleepTick();
			}
		}
	}

	private void pillory(){
		acceptRandom();
		inRandomEvent = true;

		HashMap<Integer, Integer> locks = new HashMap<>();
		locks.put(13393, 11032);
		locks.put(13382, 13396);
		locks.put(13390, 4141);
		locks.put(13394, 13395);

		int locksLeft = 3;

		while (locksLeft > 0){
			Widget lock = Widgets.get(27,3);
			if (!lock.isVisible()) return; // does this work?

			for (int i = 7; i < 10; i++){
				Widget key = Widgets.get(27,i);
				if (locks.get(lock.getModelId()) == key.getModelId()){
					key.interact(0);
					MousePackets.queueClickPacket();
					break;
				}
			}
			Time.sleepTick();

			if (Dialog.isOpen()){
				if (locksLeft < 6) {
					locksLeft++;
				}
				Dialog.continueSpace();
			}
			else
			{
				locksLeft--;
			}
		}
	}

	// verified
	private void pinball(boolean alreadyInside){
		if (!alreadyInside) acceptRandom();
		inRandomEvent = true;

		int score = 0;

		while (score < 10){

			TileObject pillar;

			switch(client.getVarbitValue(2119)){
				case 0:
					pillar = TileObjects.getNearest(8982);
					break;
				case 1:
					pillar = TileObjects.getNearest(8984);
					break;
				case 2:
					pillar = TileObjects.getNearest(9079);
					break;
				case 3:
					pillar = TileObjects.getNearest(9081);
					break;
				default:
					pillar = TileObjects.getNearest(9258);
			}

			pillar.interact("Tag");
			MousePackets.queueClickPacket(pillar);

			int tempScore = score;
			Time.sleepTicksUntil(() -> client.getVarbitValue(2121) != tempScore, 12);
			score = client.getVarbitValue(2121);
		}
		TileObject exit = TileObjects.getNearest(9293);
		exit.interact(0);
		MousePackets.queueClickPacket(exit);
	}

	private void prisonpete(boolean alreadyInside){
		if (!alreadyInside) acceptRandom();
		inRandomEvent = true;
		NPC balloon;

		// TODO: if inventory is full, drop an item and pick it up before leaving

		TileObject lever = TileObjects.getFirstAt(2094,4464,0,24296);

		while (client.getVarbitValue(1547) < 3){

			// pull lever (press spacebar if necessary)
			lever.interact("Pull");
			MousePackets.queueClickPacket(lever);

			while (!Widgets.get(273, 4).isVisible())
			{
				if (Dialog.canContinue())
					Dialog.continueSpace();
				Time.sleepTick();
			}

			// get the model id
			int modelId = Widgets.get(273,4).getModelId();

			// find the closest npc matching that model id and pop it
			switch(modelId){
				case 10750:
					balloon = NPCs.getNearest(371);
					break;
				case 10751:
					balloon = NPCs.getNearest(369);
					break;
				case 11028:
					balloon = NPCs.getNearest(370);
					break;
				default:
					balloon = NPCs.getNearest(5491);
			}
			balloon.interact(0);
			MousePackets.queueClickPacket(balloon);
			Time.sleepTicksUntil(() -> Inventory.contains("Prison key"), 15);

			Dialog.continueSpace(); // "Now bring it to me"

			Time.sleepOneTickThenUntil(Dialog::canContinueNPC, 10);
			Dialog.continueSpace();
			Time.sleepTicks(5);
		}

		int x = ThreadLocalRandom.current().nextInt(2100,2107);
		int y = ThreadLocalRandom.current().nextInt(4466,4470);

		Tiles.getAt(new WorldPoint(x,y,client.getPlane())).walkHere();
		MousePackets.queueClickPacket();
		Time.sleepOneTickThenUntil(Dialog::canContinueNPC, 15);
	}

	private void quiz(boolean alreadyInside){
		if (!alreadyInside) acceptRandom();
		inRandomEvent = true;

		Widget optionsWidget = Widgets.get(191,1);

		while (!Dialog.isViewingOptions()){
			Time.sleepTicksUntil(optionsWidget::isVisible, 5);

			Widget choice0 = Widgets.get(191,1,0);
			Widget choice1 = Widgets.get(191,1,1);
			Widget choice2 = Widgets.get(191,1,2);

			assert choice0 != null;
			assert choice1 != null;
			assert choice2 != null;

			switch(choice0.getModelId()){
				case 8828:
					if (choice1.getModelId() == 8836){
						choice2.interact(0);
					}
					else choice1.interact(0);
					break;
				case 8836:
					if (choice1.getModelId() == 8828){
						choice2.interact(0);
					}
					else choice1.interact(0);
					break;
				case 8829:
					if (choice1.getModelId() == 8830){
						choice2.interact(0);
					}
					else choice1.interact(0);
					break;
				case 8830:
					if (choice1.getModelId() == 8829){
						choice2.interact(0);
					}
					else choice1.interact(0);
					break;
				case 8832:
					if (choice1.getModelId() == 8833){
						choice2.interact(0);
					}
					else choice1.interact(0);
					break;
				case 8833:
					if (choice1.getModelId() == 8832){
						choice2.interact(0);
					}
					else choice1.interact(0);
					break;
				case 8831:
					if (choice1.getModelId() == 8834){
						choice2.interact(0);
					}
					else choice1.interact(0);
					break;
				case 8834:
					if (choice1.getModelId() == 8831){
						choice2.interact(0);
					}
					else choice1.interact(0);
					break;
				case 8835:
					if (choice1.getModelId() == 8837){
						choice2.interact(0);
					}
					else choice1.interact(0);
					break;
				case 8837:
					if (choice1.getModelId() == 8835){
						choice2.interact(0);
					}
					else choice1.interact(0);
					break;
				default:
					logger.error("quiz master: wrong model id? "+choice0.getModelId());
					return;
			}
		}

		Dialog.chooseOption(2);
	}

	private void sandwichlady(){
		acceptRandom();

		Widget choiceTextWidget = Widgets.get(297,2);
		Time.sleepTicksUntil(choiceTextWidget::isVisible, 5);
		String choiceText = choiceTextWidget.getText().toLowerCase();
		int modelId;

		if (choiceText.contains("baguette")){
			modelId = 10726;
		}
		else if (choiceText.contains("roll")){
			modelId = 10727;
		}
		else if (choiceText.contains("chocolate bar")){
			modelId = 10728;
		}
		else if (choiceText.contains("kebab")){
			modelId = 10729;
		}
		else if (choiceText.contains("meat pie")){
			modelId = 10730;
		}
		else if (choiceText.contains("square sandwich")){
			modelId = 10731;
		}
		else if (choiceText.contains("triangle sandwich")){
			modelId = 10732;
		}
		else {
			// TODO: add error messaging here
			logger.error("sandwich lady: can't find the item name? " + choiceText);
			currentRandomEvent.interact("Dismiss");
			currentRandomEvent = null;
			return;
		}

		Widget[] options = {Widgets.get(297,6),Widgets.get(297,7),Widgets.get(297,8),
			Widgets.get(297,9),Widgets.get(297,10),Widgets.get(297,11),Widgets.get(297,12)};

		for (Widget w : options){
			if (w.getModelId() == modelId){
				w.interact(0);  // TODO: double check this works?
				MousePackets.queueClickPacket(w);
				return;
			}
		}

		logger.error("couldn't find the widget with the appropriate model id: " + modelId + choiceTextWidget);
	}

	//incomplete, need lots of data to get all the matching stuff?
	private void exam(boolean alreadyInside){
		if (!alreadyInside) acceptRandom();
		inRandomEvent = true;

	}

	private void acceptRandom(){
		currentRandomEvent.interact("Talk-to");
		MousePackets.queueClickPacket(currentRandomEvent);

		Time.sleepTicksUntil(Dialog::isOpen, 5);
		while (Dialog.isOpen())
		{
			if (Dialog.canContinue())
			{
				Dialog.continueSpace();
				Time.sleepTick();
			}
			else if (Dialog.isViewingOptions())
			{
				Dialog.chooseOption(1);
				Time.sleepTick();
			}
			else break;
		}

		// wait for teleport to be over
		Time.sleepOneTickThenUntil(() -> client.getLocalPlayer().isIdle(), 6);
	}

	private void dismiss(){
		currentRandomEvent.interact("Dismiss");
		MousePackets.queueClickPacket(currentRandomEvent);
		resetRandomState();
	}

	private void resetRandomState(){
		inRandomEvent = false;
		currentRandomEvent = null;
	}

	// maze solver
	@SuppressWarnings("SuspiciousNameCombination")
	public void rotate(Pair<Integer,Integer> p, Direction i){
		if (i == Direction.WEST) return;
		if (i == Direction.NORTH)
		{
			int tempX = p.x;
			p.x = p.y;
			p.y = -tempX;
		}
		if (i == Direction.EAST){
			p.x = -p.x;
			p.y = -p.y;
			return;
		}
		if (i == Direction.SOUTH){
			int tempX = p.x;
			p.x = -p.y;
			p.y = tempX;
		}
	}
	public void goThroughGate(Pair<Integer, Integer> tile){
		if (config.lootMazeChests()){
			TileObjects.getAll("Chest").forEach(c -> {
				if (Reachable.isInteractable(c)){
					c.interact("Open");
					Widgets.get(109,61).interact(0);  // open poll history widget to stall the timer

					Time.sleepOneTickThenUntil(() -> !client.getLocalPlayer().isMoving(), 20);
					while (client.getVarbitValue(995) > 0){
						c.interact("Open");
						Time.sleepTicks(101,130);

						if (TileObjects.getNearest(14979) == null) break;
					}
				}
			});
		}

		if (client.getVarbitValue(995) == 0) return;

		WorldPoint destination = new WorldPoint(tile.x,tile.y,client.getPlane());
		TileObject gate = TileObjects.getFirstAt(destination,"Gate");

		Widget pollHistoryWidget = Widgets.get(109,61);

		// if not in a 3x3 of the door tile, move to destination tile again
		while (!(new WorldArea((destination.dx(-1).dy(-1)),3,3).contains(client.getLocalPlayer().getWorldLocation())))
		{
			Tiles.getAt(destination).walkHere();
			MousePackets.queueClickPacket();
			Time.sleepTick();
			pollHistoryWidget.interact(0);  // open poll history widget to stall the timer
			MousePackets.queueClickPacket(pollHistoryWidget);
			Time.sleepOneTickThenUntil(() -> (!client.getLocalPlayer().isMoving()), 25);
		}

		gate.interact("Open");
		MousePackets.queueClickPacket(gate);
		Time.sleepTick();
	}

	/*
	identifier 0?
	opcode MenuAction.WIDGET_TARGET_ON_WIDGET.getId()
	param0 widget.getIndex()
	param1 widget.getId()
	itemid -1

	*MenuAutomated.MenuAutomatedBuilder builder = MenuAutomated.builder()
				.identifier(identifier)
				.opcode(MenuAction.of(opcode))
				.param0(param0)
				.param1(param1)
				.itemId(itemId);
	*Point clickPoint = getClickPoint();
			builder.clickX(clickPoint.getX())
					.clickY(clickPoint.getY());
		return builder.build();

	*/
}