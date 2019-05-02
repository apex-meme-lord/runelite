package net.runelite.client.plugins.pickpocket;

import java.awt.Color;
import lombok.Getter;


enum PickPocketState {
	NONE(Color.GREEN),
	SELECTING(Color.GREEN),
	PENDING(Color.ORANGE),
	FAILED(Color.RED);

	@Getter
	private final Color color;

	PickPocketState(Color color)
	{
		this.color = color;
	}
}

