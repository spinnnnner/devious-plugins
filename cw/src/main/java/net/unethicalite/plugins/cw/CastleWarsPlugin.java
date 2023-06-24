package net.unethicalite.plugins.cw;

import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldArea;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.plugins.Task;
import net.unethicalite.api.widgets.Minigames;
import net.unethicalite.plugins.cw.tasks.EnterWaitingPortal;
import net.unethicalite.plugins.cw.tasks.ExitBarrier;
import net.unethicalite.plugins.cw.tasks.Travel;
import org.pf4j.Extension;

// This annotation is required in order for the client to detect it as a plugin/script.
@PluginDescriptor(name = "Sus Castle Wars", enabledByDefault = false)
@Extension
public class CastleWarsPlugin extends Script
{
	private static final Task[] TASKS = new Task[]{
		new Travel(),
		new EnterWaitingPortal(),
		new ExitBarrier()
	};

	@Override
	public void onStart(String... args)
	{
	}

	@Override
	protected int loop(){

		for (Task task : TASKS)
		{
			if (task.validate())
			{
				int sleep = task.execute();

				if (sleep == -1)  // jank; using this as an error code
				{
					this.logger.warn("Task failed: "+task.getClass().getName()+". Script paused.");
					this.setPaused(true);
				}

				if (task.isBlocking())
				{
					return sleep;
				}
			}
		}

		return 1000;
	}

	protected int loopOld()
	{
		if (Movement.isWalking()){
			return 787; //TODO: randomness!
		}

		// in game
		if (Players.getLocal().getWorldLocation().getRegionID() == 9520){
			// TODO: check time left in the game. Need castle wars widget id

			if (new WorldArea(2368,3127,9,9,1).contains(Players.getLocal().getWorldLocation()) ||
				new WorldArea(2423,3072,9,9,1).contains(Players.getLocal().getWorldLocation())){
				TileObject energyBarrier = TileObjects.getNearest("Energy Barrier");
				if (energyBarrier == null || !Reachable.isInteractable(energyBarrier)){
					this.logger.error("Castle Wars: in respawn room but can't find/reach energy barrier");
					this.setPaused(true);
				}
				else {
					// pass energy barrier
					energyBarrier.interact("Pass");
					return 1200;
				}
			}
			this.logger.debug("in game, sleeping 1 minute");
			return 60000;  // sleep for 1 minute
		}

		// in waiting area
		if (Players.getLocal().getWorldLocation().getRegionID() == 9620){
			// TODO: if there are any players not on friend list, hop to the next world down?
			this.logger.debug("in waiting area, sleeping 30 seconds");
			return 30000;
		}

		// check if guthix portal is reachable
		TileObject guthixPortal = TileObjects.getNearest(4408);
		if (guthixPortal == null || !Reachable.isInteractable(guthixPortal)){
			if (Minigames.canTeleport()){
				Minigames.teleport(Minigames.Destination.CASTLE_WARS);
				this.logger.debug("using minigame teleport to castle wars");
				// wait for teleport to finish
				return 15000;  // TODO: detect if teleport was interrupted? also randomness!
			}
			else
			{
				this.logger.error("not in cw lobby and can't minigame tele - stopping");
				this.stop();
			}
		}

		// portal is reachable: check if inventory and gear are empty
		if (Bank.isOpen()){
			if (!Inventory.isEmpty())
			{
				this.logger.debug("depositing inventory");
				Bank.depositInventory();
			}
			if (Equipment.fromSlot(EquipmentInventorySlot.HEAD) != null ||
				Equipment.fromSlot(EquipmentInventorySlot.CAPE) != null)
			{
				this.logger.debug("had cape or hat, depositing equipment");
				Bank.depositEquipment();
			}

			// close interface
			Bank.close();
			return 677;
		}
		boolean needToBank = false;
		if (Equipment.fromSlot(EquipmentInventorySlot.HEAD) != null ||
			Equipment.fromSlot(EquipmentInventorySlot.CAPE) != null )
		{
			this.logger.debug("have hat or cape equipped; need to bank them");
			needToBank = true;
		}
		else if (!Inventory.isEmpty() && !(Inventory.getFreeSlots() == 27 && Inventory.getCount(4067) > 0))
		{
			this.logger.debug("have stuff in inventory apart from castle wars tickets");
			needToBank = true;
		}

		if (needToBank){
			TileObject bankChest = TileObjects.getNearest("Bank chest");
			if (bankChest == null || !Reachable.isInteractable(bankChest)){
				this.logger.error("Castle Wars: in lobby but can't find/reach bank chest");
				// TODO: how do I add messages to game chat? not necessary in the long run but nice for debugging now
				this.setPaused(true);
			}
			else {
				this.logger.debug("had stuff equipped, going to bank chest");
				// interact with bank chest
				bankChest.interact("Use");
				return 800;
			}
		}

		if (guthixPortal != null)
		{
			this.logger.debug("entering guthix portal");
			guthixPortal.interact("Enter");
			return 5000;
		}

//		this.logger.warn("should have covered all cases at this point");
		return 10000;
	}
}
