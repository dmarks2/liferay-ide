/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.ide.ui.liferay.page.editor;

import com.liferay.ide.ui.swtbot.page.CTabItem;
import com.liferay.ide.ui.swtbot.page.Editor;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

/**
 * @author Terry Jia
 */
public class PomXmlEditor extends Editor {

	public PomXmlEditor(SWTWorkbenchBot bot) {
		super(bot);

		_pomXml = new CTabItem(bot, POM_XML);
	}

	public PomXmlEditor(SWTWorkbenchBot bot, String editorName) {
		super(bot, editorName);

		_pomXml = new CTabItem(bot, POM_XML);
	}

	public CTabItem getPomXml() {
		return _pomXml;
	}

	private CTabItem _pomXml;

}