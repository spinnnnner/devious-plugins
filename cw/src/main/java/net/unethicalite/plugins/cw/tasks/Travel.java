package net.unethicalite.plugins.cw.tasks;

import com.google.common.collect.ImmutableSet;
import net.runelite.api.AnimationID;
import net.runelite.api.TileObject;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.plugins.Task;
import net.unethicalite.api.widgets.Minigames;

public class Travel implements Task
{
	// TODO: twisted? or trailblazer? and speedy
	private static final ImmutableSet<Integer> HOME_TELE_ANIMATIONS = ImmutableSet.of(
		AnimationID.BOOK_HOME_TELEPORT_1, AnimationID.BOOK_HOME_TELEPORT_2, AnimationID.BOOK_HOME_TELEPORT_3,
		AnimationID.BOOK_HOME_TELEPORT_4, AnimationID.BOOK_HOME_TELEPORT_5,
		AnimationID.COW_HOME_TELEPORT_1, AnimationID.COW_HOME_TELEPORT_2, AnimationID.COW_HOME_TELEPORT_3,
		AnimationID.COW_HOME_TELEPORT_4, AnimationID.COW_HOME_TELEPORT_5, AnimationID.COW_HOME_TELEPORT_6,
		AnimationID.LEAGUE_HOME_TELEPORT_1, AnimationID.LEAGUE_HOME_TELEPORT_2, AnimationID.LEAGUE_HOME_TELEPORT_3,
		AnimationID.LEAGUE_HOME_TELEPORT_4, AnimationID.LEAGUE_HOME_TELEPORT_5, AnimationID.LEAGUE_HOME_TELEPORT_6,
		AnimationID.SHATTERED_LEAGUE_HOME_TELEPORT_1, AnimationID.SHATTERED_LEAGUE_HOME_TELEPORT_2, AnimationID.SHATTERED_LEAGUE_HOME_TELEPORT_3,
		AnimationID.SHATTERED_LEAGUE_HOME_TELEPORT_4, AnimationID.SHATTERED_LEAGUE_HOME_TELEPORT_5, AnimationID.SHATTERED_LEAGUE_HOME_TELEPORT_6
		);

	private static final ImmutableSet<Integer> cwSquares = ImmutableSet.of(9520,9620,9776);

	@Override
	public boolean validate()
	{
		return !cwSquares.contains(Players.getLocal().getWorldLocation().getRegionID());
	}

	@Override
	public int execute()
	{
		TileObject guthixPortal = TileObjects.getNearest(4408);
		if (guthixPortal != null && Reachable.isInteractable(guthixPortal)){
			Movement.walk(guthixPortal.getWorldLocation().dx(6));
			MousePackets.queueClickPacket();
			Time.sleepTicksUntil(() -> Players.getLocal().isMoving(), 3);
			Time.sleepTicksUntil(() -> !Players.getLocal().isMoving(), 20);
		}
		else if (Minigames.canTeleport()){
			Minigames.teleport(Minigames.Destination.CASTLE_WARS);
			MousePackets.queueClickPacket();
			Time.sleepTicksUntil(()-> Players.getLocal().isAnimating(), 10);
			Time.sleepTicksUntil(() -> !HOME_TELE_ANIMATIONS.contains(Players.getLocal().getAnimation()), 40);
		}
		else {
			return -1;
		}
		return 300;
	}
}
