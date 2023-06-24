package net.unethicalite.plugins.splash;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.scene.Tiles;
import org.pf4j.Extension;

// This annotation is required in order for the client to detect it as a plugin/script.
@PluginDescriptor(name = "Splash", enabledByDefault = false)
@Extension
public class SplashPlugin extends Plugin
{
	@Inject
	private Client client;

	private int lastHit;

	@Override
	protected void startUp(){
		lastHit = -1;
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied e){
		if (e.getActor() != client.getLocalPlayer()){
			return;
		}
		lastHit = client.getTickCount();
		WorldPoint wp = new WorldPoint(2155,5285,0);

		Tile destination = Tiles.getAt(wp);
		destination.walkHere();

		MousePackets.queueClickPacket();
	}

	@Subscribe
	public void onGameTick(GameTick e){
		if (!Inventory.contains("Air rune","Mind rune")){
			Game.logout();
		}

		if (!client.getLocalPlayer().isInteracting() && lastHit + 3 < client.getTickCount()){
			NPC catablepon = NPCs.getNearest(2475);
			catablepon.interact("Attack");
			MousePackets.queueClickPacket(catablepon);
		}
	}
}
