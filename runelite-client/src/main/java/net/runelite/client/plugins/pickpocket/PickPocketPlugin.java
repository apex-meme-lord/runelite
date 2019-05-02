package net.runelite.client.plugins.pickpocket;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Deque;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.ConfigChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.InteractingChanged;
import net.runelite.client.plugins.npchighlight.NpcSceneOverlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;
import lombok.extern.slf4j.Slf4j;


@PluginDescriptor(
        name = "Pick Pocket",
        description = "Highlight NPCs on-screen and/or on the minimap",
        tags = {"highlight", "npcs", "overlay", "thieving", "pickpocket", "tags"}
)
@Slf4j
public class PickPocketPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private PickPocketConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PickPocketOverlay pickPocketOverlay;

	private String renderStyle = "Hull";

	private boolean stunned = false;

	@Getter(AccessLevel.PACKAGE)
	private Deque<NPC> selectedPick = new LinkedList<>();

	@Getter(AccessLevel.PACKAGE)
	private Deque<NPC> pendingPick = new LinkedList<>();

	@Getter(AccessLevel.PACKAGE)
	private Map<NPC, PickPocketState> npcMarks = new HashMap<>();

	@Provides
	PickPocketConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PickPocketConfig.class);
	}

	protected void startUp() throws Exception
	{
		log.debug("foo");
		overlayManager.add(pickPocketOverlay);
		scanForNpcs();
	}

	protected void shutDown() throws Exception
	{
		overlayManager.remove(pickPocketOverlay);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
		if (!configChanged.getGroup().equals("pickpocket")) {
			return;
		}

		scanForNpcs();
	}

	private List<String> getHighlights() {
		return Collections.emptyList();
	}


	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked option) {

		if (stunned || !(option.getMenuOption().toLowerCase().matches("pickpocket"))) {
			return;
		}
		NPC target = client.getCachedNPCs()[option.getId()];
		if (target == null)
		{
			log.warn("Could not find target npc in npc cache");
			return;
		}
		if (!npcMarks.containsKey(target) || npcMarks.get(target) == PickPocketState.NONE) {
			npcMarks.put(target, PickPocketState.SELECTING);
			log.info(String.format("Selected pickpocket on %s", target.getIndex()));
		}


	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged transition)
	{
		if (transition.getActor() == client.getLocalPlayer())
		{
			log.debug(String.format("Player animation %s", transition.getActor().getGraphic()));
			if (client.getLocalPlayer().getGraphic() == -1)
			{
				resetNpcMarkStates(PickPocketState.NONE);
				stunned = false;
			}
			else
			{
				resetNpcMarkStates(PickPocketState.FAILED);
				stunned = true;
			}
		}
	}


	@Subscribe
	public void onInteractingChanged(InteractingChanged interaction) {
		if (stunned || npcMarks.isEmpty())
		{
			return;
		}
		if (interaction.getSource() == client.getLocalPlayer()) {
			if (interaction.getTarget() == null) {
				log.info("Clearing all targets");
				resetNpcMarkStates(PickPocketState.NONE);
				return;
			}
			NPC target = (NPC) interaction.getTarget();
			if (npcMarks.containsKey(target) && npcMarks.get(target) == PickPocketState.SELECTING) {
				log.debug(String.format("Moving %s to pending", target.getId()));
				resetNpcMarkStates(PickPocketState.NONE);
				npcMarks.put(target, PickPocketState.PENDING);
				return;
			}
		}
		if (interaction.getTarget() == client.getLocalPlayer())
		{
			log.debug(String.format("STUNNED!!!"));
		}
	}

	private void resetNpcMarkStates(PickPocketState state)
	{
		for (NPC npc : npcMarks.keySet()) {
			npcMarks.put(npc, state);
		}
	}

	private Set<NPC> getPendingNpcMarks()
	{
		return npcMarks
			.entrySet()
			.stream()
			.filter(entry -> entry.getValue() == PickPocketState.PENDING)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
			.keySet();
	}


	@Subscribe
	public void onGameStateChanged(GameStateChanged state)
	{
		log.debug(String.format("NEW GAME STATE %s", state.getGameState().name()));
		if (state.getGameState() == GameState.LOGGED_IN)
		{
			scanForNpcs();
		}
	}


	private void scanForNpcs()
	{
		npcMarks.clear();
		for (NPC npc : client.getNpcs())
		{
			final String npcName = npc.getName();
			log.debug(String.format("I spy %s", npcName));

			if (npcName == null)
			{
				continue;
			}
			List<String> npcOptions = Arrays.asList(npc.getComposition().getActions());
			log.debug(String.format("NPC options: %s", npcOptions));

			if (npcOptions.contains("Pickpocket"))
			{
				npcMarks.put(npc, PickPocketState.SELECTING);
			}
		}
	}

}
