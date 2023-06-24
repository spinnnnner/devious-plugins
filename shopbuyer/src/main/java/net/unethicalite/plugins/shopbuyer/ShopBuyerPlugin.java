package net.unethicalite.plugins.shopbuyer;

import com.google.inject.Provides;
import com.google.inject.Inject;
import java.util.List;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.items.Shop;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.client.Static;
import org.pf4j.Extension;

@PluginDescriptor(name = "Shop Buyer", enabledByDefault = false)
@Extension
public class ShopBuyerPlugin extends Script
{
	@Inject
	private ShopBuyerConfig config;

	@Inject
	private ConfigManager configManager;

	@Provides
	ShopBuyerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ShopBuyerConfig.class);
	}

//	@Subscribe
//	public void onGameTick(GameTick e){
//	}

	@Override
	protected int loop()
	{
		// 5095: betty
		// gerrant: 2891
		if (Shop.isOpen()){
			if (Inventory.isFull()){
				Tiles.getAt(Players.getLocal().getWorldLocation()).walkHere();
				return -1;
			}
			else
			{
				List<Integer> shopItems = Shop.getItems();

				if (shopItems.contains(314) && Shop.getStock(314) >= 999){
					Shop.buyFifty(314);
					MousePackets.queueClickPacket();
					return 1000;
				}

				if (shopItems.contains(11881))
				{
					if (Shop.getStock(11881) > 99)
					{
						if (Inventory.getFreeSlots() <= 5){
							Shop.buyFive(11881);
							MousePackets.queueClickPacket();
							Time.sleep(123);
							Tiles.getAt(Players.getLocal().getWorldLocation()).walkHere();
							MousePackets.queueClickPacket();
							return -1;
						}
						else
						{
							Shop.buyOne(11881);
							MousePackets.queueClickPacket();
							return -2;
						}
					}
					else return 323;
				}
			}
		}

		// shop is not open
		else if (Inventory.contains(12728,12730,12732,12734,12736,11881,11883)){
			Inventory.getAll(12728,12730,12732,12734,12736,11881,11883).forEach(item -> {
				item.interact("Open");
				MousePackets.queueClickPacket(item);
				Time.sleep(193);
			});
			Time.sleepTick();
		}

		NPC shopkeeper = NPCs.getNearest(2891);
		if (shopkeeper.getInteracting() != Players.getLocal()){
			shopkeeper.interact("Talk-to");
			Time.sleepTicksUntil(Dialog::canContinue, 10);
			Dialog.continueSpace();
			Time.sleepTick();
		}

		shopkeeper.interact("Trade");
		MousePackets.queueClickPacket(shopkeeper);
		Time.sleepTicksUntil(Shop::isOpen, 10);
		return 234;
	}

	@Override
	public void onStart(String... args)
	{
		NPC shopkeeper = NPCs.getNearest(2891);
		shopkeeper.interact("Talk-to");
		MousePackets.queueClickPacket(shopkeeper);
		Time.sleepTicksUntil(Dialog::isOpen, 10);
	}

	private boolean buyItem(int itemid, int qty){
		if (Inventory.getFirst(995).getQuantity() < 100){
			return false;
		}

		if (Shop.isOpen() && Shop.getItems().contains(itemid)){
			Shop.buyOne(itemid);
			return true;
		}
		return false;
	}
}
