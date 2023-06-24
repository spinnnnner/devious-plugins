package net.unethicalite.plugins.cw.tasks;

import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldArea;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.plugins.Task;

public class EnterWaitingPortal implements Task
{
	@Override
	public boolean validate()
	{// TODO: change this to actual worldArea
		return (new WorldArea(2368,3127,9,9,1).contains(Players.getLocal().getWorldLocation()));
	}

	@Override
	public int execute()
	{
		int needToBank = 0;

		if (Equipment.fromSlot(EquipmentInventorySlot.HEAD) != null ||
			Equipment.fromSlot(EquipmentInventorySlot.CAPE) != null )
		{
			needToBank += 1;
		}
		else if (!Inventory.isEmpty() && !(Inventory.getFreeSlots() == 27 && Inventory.contains(4067)))
		{
			needToBank += 2;
		}

		if (needToBank > 0){
			TileObject bankChest = TileObjects.getNearest("Bank chest");
			if (bankChest == null || !Reachable.isInteractable(bankChest)){
				return -1;
			}
			else {
				bankChest.interact("Use");
				Time.sleepTicksUntil(Bank::isOpen, 8);
				if (Bank.isOpen()){
					if ((needToBank & 1) == 1){
						Bank.depositEquipment();
						MousePackets.queueClickPacket();
					}
					if ((needToBank & 2) == 2){
						Bank.depositInventory();
						MousePackets.queueClickPacket();
					}
				}
			}
		}

		TileObject guthixPortal = TileObjects.getNearest(4408);
		if (guthixPortal == null)
		{
			return -1;
		}
		else
		{
			guthixPortal.interact("Enter");
			MousePackets.queueClickPacket();
			return 10000;
		}
	}
}
