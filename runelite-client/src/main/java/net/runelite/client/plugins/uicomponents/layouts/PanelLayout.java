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
package net.runelite.client.plugins.uicomponents.layouts;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Base interface for panel layouts.
 * Provides fluent API for building UI layouts.
 *
 * Config-first: All layout properties come from configuration.
 * Extensible: Can be extended for custom layouts.
 */
public interface PanelLayout
{
	/**
	 * Adds a component to the layout
	 * @param component the component to add
	 * @return this layout for chaining
	 */
	PanelLayout add(JComponent component);

	/**
	 * Adds a component with custom constraints
	 * @param component the component to add
	 * @param constraints layout-specific constraints
	 * @return this layout for chaining
	 */
	PanelLayout add(JComponent component, Object constraints);

	/**
	 * Adds a gap/spacer
	 * @param size size of the gap in pixels
	 * @return this layout for chaining
	 */
	PanelLayout addGap(int size);

	/**
	 * Adds a separator line
	 * @return this layout for chaining
	 */
	PanelLayout addSeparator();

	/**
	 * Builds and returns the panel
	 * @return the constructed JPanel
	 */
	JPanel build();

	/**
	 * Gets the panel (without finalizing)
	 * @return the panel being built
	 */
	JPanel getPanel();
}
