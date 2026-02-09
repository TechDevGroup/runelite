/*
 * Copyright (c) 2024, TechDevGroup
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.uicomponents.overlay;

import java.awt.Color;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.components.LineComponent;

/**
 * Advanced dockable overlay showing player stats in an intricate layout.
 * Demonstrates grid-based layout similar to hiscores plugin.
 */
public class PlayerStatsOverlay extends DockableOverlay
{
	private final Client client;

	public PlayerStatsOverlay(Plugin plugin, Client client, DockableOverlayConfig config)
	{
		super(plugin, config);
		this.client = client;
	}

	@Override
	protected void renderMinimized(Graphics2D graphics)
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			getPanelComponent().getChildren().add(LineComponent.builder()
				.left("Not logged in")
				.leftColor(Color.GRAY)
				.build());
			return;
		}

		int combat = client.getRealSkillLevel(Skill.ATTACK)
			+ client.getRealSkillLevel(Skill.STRENGTH)
			+ client.getRealSkillLevel(Skill.DEFENCE);

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left(player.getName())
			.right("CB: " + (combat / 3))
			.rightColor(Color.GREEN)
			.build());
	}

	@Override
	protected void renderExpanded(Graphics2D graphics)
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			getPanelComponent().getChildren().add(LineComponent.builder()
				.left("Not logged in")
				.leftColor(Color.GRAY)
				.build());
			return;
		}

		// Player name header
		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Player:")
			.right(player.getName())
			.rightColor(Color.CYAN)
			.build());

		// Combat stats section
		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("=== Combat ===")
			.leftColor(Color.ORANGE)
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Attack")
			.right(String.valueOf(client.getRealSkillLevel(Skill.ATTACK)))
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Strength")
			.right(String.valueOf(client.getRealSkillLevel(Skill.STRENGTH)))
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Defence")
			.right(String.valueOf(client.getRealSkillLevel(Skill.DEFENCE)))
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Hitpoints")
			.right(String.valueOf(client.getRealSkillLevel(Skill.HITPOINTS)))
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Ranged")
			.right(String.valueOf(client.getRealSkillLevel(Skill.RANGED)))
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Magic")
			.right(String.valueOf(client.getRealSkillLevel(Skill.MAGIC)))
			.build());

		// Gathering stats section
		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("=== Gathering ===")
			.leftColor(Color.ORANGE)
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Mining")
			.right(String.valueOf(client.getRealSkillLevel(Skill.MINING)))
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Fishing")
			.right(String.valueOf(client.getRealSkillLevel(Skill.FISHING)))
			.build());

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Woodcutting")
			.right(String.valueOf(client.getRealSkillLevel(Skill.WOODCUTTING)))
			.build());

		// Total level
		int totalLevel = 0;
		for (Skill skill : Skill.values())
		{
			if (skill != Skill.OVERALL)
			{
				totalLevel += client.getRealSkillLevel(skill);
			}
		}

		getPanelComponent().getChildren().add(LineComponent.builder()
			.left("Total Level:")
			.right(String.valueOf(totalLevel))
			.rightColor(Color.YELLOW)
			.build());
	}
}
