package net.runelite.client.plugins.pickpocket;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.Text;

public class PickPocketOverlay extends Overlay {

	private final Client client;
	private final PickPocketConfig config;
	private final PickPocketPlugin plugin;

	@Inject
	PickPocketOverlay(Client client, PickPocketConfig config, PickPocketPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		for (Map.Entry<NPC, PickPocketState> entry: plugin.getNpcMarks().entrySet())
		{
			renderNpc(graphics, entry.getKey(), entry.getValue());
		}

		return null;
	}

	private void renderNpc(Graphics2D graphics, NPC npc, PickPocketState state)
	{
		Polygon box = npc.getConvexHull();
		if (box != null)
		{
			Color color = state.getColor();
			graphics.setColor(color);
			graphics.setStroke(new BasicStroke(2));
			graphics.draw(box);
			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
			graphics.fill(box);
		}
	}
}
