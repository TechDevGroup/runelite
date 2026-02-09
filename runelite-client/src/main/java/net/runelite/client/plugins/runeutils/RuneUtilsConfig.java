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
package net.runelite.client.plugins.runeutils;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("runeutils")
public interface RuneUtilsConfig extends Config
{
	@ConfigSection(
		name = "Menu Manipulation",
		description = "Settings for right-click menu priority",
		position = 0
	)
	String menuSection = "menu";

	@ConfigSection(
		name = "Inventory Highlighting",
		description = "Settings for inventory item highlighting",
		position = 1
	)
	String inventorySection = "inventory";

	// Menu Manipulation Settings
	@ConfigItem(
		keyName = "enableMenuManipulation",
		name = "Enable Menu Manipulation",
		description = "Enable right-click menu priority modification",
		section = menuSection,
		position = 0
	)
	default boolean enableMenuManipulation()
	{
		return true;
	}

	@ConfigItem(
		keyName = "shiftDepositPriority",
		name = "Shift Deposit Priority",
		description = "Menu option to prioritize when shift-clicking in bank (e.g., 'Deposit-all', 'Deposit-1')",
		section = menuSection,
		position = 1
	)
	default String shiftDepositPriority()
	{
		return "Deposit-all";
	}

	// Inventory Highlighting Settings
	@ConfigItem(
		keyName = "enableInventoryHighlighting",
		name = "Enable Inventory Highlighting",
		description = "Enable inventory item highlighting",
		section = inventorySection,
		position = 0
	)
	default boolean enableInventoryHighlighting()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightColor",
		name = "Highlight Color",
		description = "Color for highlighting matching items",
		section = inventorySection,
		position = 1
	)
	default Color highlightColor()
	{
		return new Color(255, 255, 0, 100);
	}

	@ConfigItem(
		keyName = "highlightBorderColor",
		name = "Highlight Border Color",
		description = "Border color for highlighting matching items",
		section = inventorySection,
		position = 2
	)
	default Color highlightBorderColor()
	{
		return Color.YELLOW;
	}
}
