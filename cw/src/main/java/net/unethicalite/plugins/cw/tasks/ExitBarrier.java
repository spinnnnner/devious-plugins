package net.unethicalite.plugins.cw.tasks;

import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldArea;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.plugins.Task;

public class ExitBarrier implements Task
{
	@Override
	public boolean validate()
	{
		return (new WorldArea(2368,3127,9,9,1).contains(Players.getLocal().getWorldLocation()) ||
			new WorldArea(2423,3072,9,9,1).contains(Players.getLocal().getWorldLocation()));
	}

	@Override
	public int execute()
	{
		TileObject energyBarrier = TileObjects.getNearest("Energy Barrier");
		if (energyBarrier == null || !Reachable.isInteractable(energyBarrier)){
			return -1;
		}
		else {
			// pass energy barrier
			energyBarrier.interact("Pass");
			MousePackets.queueClickPacket();
			return 10*60*1000;
		}
	}
}
