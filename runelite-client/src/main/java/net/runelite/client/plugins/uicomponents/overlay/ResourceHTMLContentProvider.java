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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Loads HTML content from plugin resources (files bundled in JAR).
 * Uses class loader to load resources from src/main/resources or equivalent.
 */
@Slf4j
public class ResourceHTMLContentProvider implements HTMLContentProvider
{
	private final Class<?> pluginClass;
	private final String htmlResourcePath;
	private final String cssResourcePath;
	private String cachedHTML;
	private String cachedCSS;

	/**
	 * Create provider that loads HTML from plugin resources
	 * @param pluginClass The plugin class (for class loader context)
	 * @param htmlResourcePath Path to HTML resource (e.g., "/html/graph.html")
	 */
	public ResourceHTMLContentProvider(Class<?> pluginClass, String htmlResourcePath)
	{
		this(pluginClass, htmlResourcePath, null);
	}

	/**
	 * Create provider that loads HTML and CSS from plugin resources
	 * @param pluginClass The plugin class (for class loader context)
	 * @param htmlResourcePath Path to HTML resource (e.g., "/html/graph.html")
	 * @param cssResourcePath Path to CSS resource (e.g., "/css/graph.css")
	 */
	public ResourceHTMLContentProvider(Class<?> pluginClass, String htmlResourcePath, String cssResourcePath)
	{
		this.pluginClass = pluginClass;
		this.htmlResourcePath = htmlResourcePath;
		this.cssResourcePath = cssResourcePath;
		reload();
	}

	@Override
	public String getHTML()
	{
		return cachedHTML != null ? cachedHTML : "<html><body>Failed to load HTML</body></html>";
	}

	@Override
	public String getCSS()
	{
		return cachedCSS != null ? cachedCSS : "";
	}

	@Override
	public void reload()
	{
		cachedHTML = loadResource(htmlResourcePath);
		if (cssResourcePath != null)
		{
			cachedCSS = loadResource(cssResourcePath);
		}
	}

	private String loadResource(String resourcePath)
	{
		try (InputStream is = pluginClass.getResourceAsStream(resourcePath))
		{
			if (is == null)
			{
				log.warn("Resource not found: {}", resourcePath);
				return null;
			}

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)))
			{
				return reader.lines().collect(Collectors.joining("\n"));
			}
		}
		catch (IOException e)
		{
			log.error("Failed to load resource: " + resourcePath, e);
			return null;
		}
	}
}
