
package net.runelite.client.plugins.pickpocket;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("pickpocket")
public interface PickPocketConfig extends Config
{

    @ConfigItem(
            keyName = "npcToHighlight",
            name = "NPCs to Highlight",
            description = "List of NPC names to highlight"
    )
    default String getNpcToHighlight()
    {
        return "";
    }

    @ConfigItem(
            keyName = "npcColor",
            name = "Highlight Color",
            description = "Color of the NPC highlight"
    )
    default Color getHighlightColor()
    {
        return Color.GREEN;
    }
}
