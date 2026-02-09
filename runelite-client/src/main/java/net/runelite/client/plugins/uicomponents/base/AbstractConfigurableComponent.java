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
package net.runelite.client.plugins.uicomponents.base;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for configurable components.
 * Implements common functionality and enforces config-first principle.
 *
 * Template Method Pattern: defines the skeleton of component lifecycle.
 * Single Responsibility Principle: focuses on component lifecycle management.
 */
@Slf4j
public abstract class AbstractConfigurableComponent<T extends ComponentConfig> implements ConfigurableComponent<T>
{
	@Getter
	protected T config;

	protected JComponent component;

	protected AbstractConfigurableComponent(T config)
	{
		if (config == null || !config.isValid())
		{
			throw new IllegalArgumentException("Invalid configuration provided");
		}
		this.config = config;
	}

	@Override
	public JComponent getComponent()
	{
		if (component == null)
		{
			component = buildComponent();
		}
		return component;
	}

	@Override
	public void updateConfig(T config)
	{
		if (config == null || !config.isValid())
		{
			log.warn("Attempted to update with invalid config for {}", getClass().getSimpleName());
			return;
		}

		this.config = config;
		refresh();
	}

	@Override
	public void refresh()
	{
		if (component != null)
		{
			SwingUtilities.invokeLater(this::refreshInternal);
		}
	}

	@Override
	public void dispose()
	{
		if (component != null)
		{
			disposeInternal();
			component = null;
		}
	}

	/**
	 * Builds the actual Swing component.
	 * Called once when getComponent() is first invoked.
	 *
	 * @return the built component
	 */
	protected abstract JComponent buildComponent();

	/**
	 * Refreshes the component's display based on current config.
	 * Called on EDT via SwingUtilities.invokeLater.
	 */
	protected abstract void refreshInternal();

	/**
	 * Cleans up component-specific resources.
	 * Override to add custom disposal logic.
	 */
	protected void disposeInternal()
	{
		// Override if needed
	}
}
