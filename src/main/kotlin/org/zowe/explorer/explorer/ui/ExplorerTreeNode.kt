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

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.SettingsProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.showYesNoDialog
import com.intellij.ui.SimpleTextAttributes
import org.zowe.explorer.analytics.AnalyticsService
import org.zowe.explorer.analytics.events.FileAction
import org.zowe.explorer.analytics.events.FileEvent
import org.zowe.explorer.dataops.DataOpsManager
import org.zowe.explorer.dataops.content.synchronizer.DocumentedSyncProvider
import org.zowe.explorer.dataops.content.synchronizer.SaveStrategy
import org.zowe.explorer.explorer.Explorer
import org.zowe.explorer.explorer.UIComponentManager
import org.zowe.explorer.utils.isBeingEditingNow
import org.zowe.explorer.utils.service
import org.zowe.explorer.vfs.MFVirtualFile
import javax.swing.tree.TreePath

/** Base class to implement the basic interactions with an explorer node */
abstract class ExplorerTreeNode<Value : Any>(
  value: Value,
  project: Project,
  val parent: ExplorerTreeNode<*>?,
  val explorer: Explorer<*>,
  protected val treeStructure: ExplorerTreeStructureBase
) : AbstractTreeNode<Value>(project, value), SettingsProvider {

  open fun init() {
    @Suppress("LeakingThis")
    treeStructure.registerNode(this)
  }

  init {
    @Suppress("LeakingThis")
    init()
  }

  private val contentProvider = UIComponentManager.INSTANCE.getExplorerContentProvider(explorer::class.java)

  private val descriptor: OpenFileDescriptor?
    get() {
      return OpenFileDescriptor(notNullProject, virtualFile ?: return null)
    }

  public override fun getVirtualFile(): MFVirtualFile? {
    return null
  }

  val notNullProject = project

  override fun getSettings(): ViewSettings {
    return treeStructure
  }

  protected fun updateMainTitleUsingCutBuffer(text: String, presentationData: PresentationData) {
    val file = virtualFile ?: return
    val textAttributes = if (contentProvider.isFileInCutBuffer(file)) {
      SimpleTextAttributes.GRAYED_ATTRIBUTES
    } else {
      SimpleTextAttributes.REGULAR_ATTRIBUTES
    }
    presentationData.addText(text, textAttributes)
  }

  override fun navigate(requestFocus: Boolean) {
    val file = virtualFile ?: return
    descriptor?.let {
      if (!file.isDirectory) {
        val dataOpsManager = explorer.componentManager.service<DataOpsManager>()
        val contentSynchronizer = dataOpsManager.getContentSynchronizer(file) ?: return
        val doSync = file.isReadable || showYesNoDialog(
          title = "File ${file.name} is not readable",
          message = "Do you want to try open it anyway?",
          project = project,
          icon = AllIcons.General.WarningDialog
        )
        if (doSync) {
          val syncProvider = DocumentedSyncProvider(file = file, saveStrategy = SaveStrategy.default(project))
          if (!file.isBeingEditingNow()) {
            contentSynchronizer.synchronizeWithRemote(syncProvider)
          }
        }
        dataOpsManager.tryToGetAttributes(file)?.let { }
        it.navigate(requestFocus)
      }
    }
  }

  override fun canNavigate(): Boolean {
    return descriptor?.canNavigate() ?: super.canNavigate()
  }

  override fun canNavigateToSource(): Boolean {
    return descriptor?.canNavigateToSource() ?: super.canNavigateToSource()
  }

  private val pathList: List<ExplorerTreeNode<*>>
    get() = if (parent != null) {
      parent.pathList + this
    } else {
      listOf(this)
    }

  val path: TreePath
    get() = TreePath(pathList.toTypedArray())

}
