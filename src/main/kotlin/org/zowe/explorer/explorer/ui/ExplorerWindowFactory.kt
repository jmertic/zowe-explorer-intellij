/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package org.zowe.explorer.explorer.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import org.zowe.explorer.explorer.Explorer
import org.zowe.explorer.explorer.UIComponentManager

/** Explorer window. This is the main class to represent the plugin */
class ExplorerWindowFactory : ToolWindowFactory, DumbAware {

  override fun isApplicable(project: Project): Boolean {
    return true
  }

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    // TODO: ContentFactory.SERVICE.getInstance() -> ContentFactory.getInstance() in new versions of the plugin
    val contentFactory = ContentFactory.SERVICE.getInstance()
    UIComponentManager.INSTANCE.getExplorerContentProviders<Explorer<*>>().forEach {
      val content = contentFactory
        .createContent(it.buildExplorerContent(toolWindow.disposable, project), it.displayName, it.isLockable)
      toolWindow.contentManager.addContent(content)
    }
  }

  override fun init(toolWindow: ToolWindow) {}

  override fun shouldBeAvailable(project: Project): Boolean {
    return true
  }
}
