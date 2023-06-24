package net.unethicalite.plugins.tutorialisland;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import org.pf4j.Extension;

@Slf4j
@Extension
public class TutorialIslandPlugin extends Plugin
{
	@Inject
	private Client client;

	private String text = null;
	private Widget[] dialogueOptions;
	private int sleep = 0;

	private int varbit = -1;

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (sleep-- > 0){
			return;
		}
		// do stuff based on varp
		if (varbit == 2){

		}
	}

	private int analyseDialogueLength(){
		Widget npcDialogueTextWidget = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);

		if (npcDialogueTextWidget != null && !npcDialogueTextWidget.getText().equals(text)) {
			text = npcDialogueTextWidget.getText();
		}

		Widget playerDialogueTextWidget = client.getWidget(WidgetInfo.DIALOG_PLAYER_TEXT);
		if (playerDialogueTextWidget != null && !playerDialogueTextWidget.getText().equals(text))
		{
			text = playerDialogueTextWidget.getText();
		}

		Widget playerDialogueOptionsWidget = client.getWidget(WidgetID.DIALOG_OPTION_GROUP_ID, 1);
		if (playerDialogueOptionsWidget != null && playerDialogueOptionsWidget.getChildren() != dialogueOptions)
		{
			dialogueOptions = playerDialogueOptionsWidget.getChildren();
			assert dialogueOptions != null;
			int numOptions = dialogueOptions.length;
		}

		Widget spriteTextWidget = client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT);
		if (spriteTextWidget != null && !spriteTextWidget.getText().equals(text))
		{
			text = spriteTextWidget.getText();
		}

		Widget msgTextWidget = client.getWidget(229, 1);
		if (msgTextWidget != null && !msgTextWidget.getText().equals(text))
		{
			text = msgTextWidget.getText();
		}

		Widget doubleSpriteTextWidget = client.getWidget(11, 2);
		if (doubleSpriteTextWidget != null && !doubleSpriteTextWidget.getText().equals(text))
		{
			text = doubleSpriteTextWidget.getText();
		}
		return 5;
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged e){
		if (e.getVarpId() != 281) return;

		varbit = e.getVarbitId();
	}
}

/*
	client.getVarbitValue(4675) returns an int from 0 to 13(?)
	0 is no key set, 1-12 are f1-12, 13 is esc
	CAN I USE F KEYS FOR TUTORIAL ISLAND?

	COMBAT_TAB_BINDING = 4675;
	SKILLS_TAB_BINDING = 4676;
	QUESTS_TAB_BINDING = 4677;
	INVENTORY_TAB_BINDING = 4678;
	EQUIPMENT_TAB_BINDING = 4679;
	PRAYER_TAB_BINDING = 4680;
	MAGIC_TAB_BINDING = 4682;
	FRIENDS_TAB_BINDING = 4684;
	ACCOUNT_MANAGEMENT_TAB_BINDING = 6517;
	LOGOUT_BINDING = 4689;
	SETTINGS_TAB_BINDING = 4686;
	EMOTE_TAB_BINDING = 4687;
	CHAT_CHANNEL_TAB_BINDING = 4683;
	MUSIC_PLAYER_TAB_BINDING = 4688;
 */