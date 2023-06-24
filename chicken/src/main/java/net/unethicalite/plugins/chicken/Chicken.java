package net.unethicalite.plugins.chicken;

import com.google.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;
import org.pf4j.Extension;

// This annotation is required in order for the client to detect it as a plugin/script.
@PluginDescriptor(name = "AAAA test", enabledByDefault = false)
@Extension
public class Chicken extends LoopedPlugin
{
	@Inject
	private Client client;

	@Override
	protected void startUp(){
	}

	@Override
	public int loop(){
//		if (NPCs.getNearest(391) != null)
		pinball();
		return 1000;
	}

	private void pinball(){
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
		Time.sleepTicksUntil(() -> TileItems.getNearest("Raw pheasant") != null, 15);

		TileItems.getNearest("Raw pheasant").pickup();
		MousePackets.queueClickPacket();
		Time.sleepTicksUntil(() -> Inventory.contains("Raw pheasant"), 5);

		forester.interact("Talk-to");
		MousePackets.queueClickPacket(forester);
		Time.sleepTicks(10);

		TileObject exitPortal = TileObjects.getNearest(new WorldPoint(2611,4776,0), 20843);
		exitPortal.interact("Use");
		MousePackets.queueClickPacket(exitPortal);
		Time.sleepTicks(10);
	}
}
