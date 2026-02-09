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

import java.util.function.Supplier;

/**
 * Provides dynamically generated HTML content.
 * Useful for content that changes based on game state or user actions.
 */
public class DynamicHTMLContentProvider implements HTMLContentProvider
{
	private final Supplier<String> htmlSupplier;
	private final Supplier<String> cssSupplier;

	/**
	 * Create provider with dynamic HTML generation
	 * @param htmlSupplier Function that generates HTML content
	 */
	public DynamicHTMLContentProvider(Supplier<String> htmlSupplier)
	{
		this(htmlSupplier, () -> "");
	}

	/**
	 * Create provider with dynamic HTML and CSS generation
	 * @param htmlSupplier Function that generates HTML content
	 * @param cssSupplier Function that generates CSS content
	 */
	public DynamicHTMLContentProvider(Supplier<String> htmlSupplier, Supplier<String> cssSupplier)
	{
		this.htmlSupplier = htmlSupplier;
		this.cssSupplier = cssSupplier;
	}

	@Override
	public String getHTML()
	{
		return htmlSupplier.get();
	}

	@Override
	public String getCSS()
	{
		return cssSupplier.get();
	}

	@Override
	public boolean hasChanged()
	{
		return true; // Always dynamic
	}
}
