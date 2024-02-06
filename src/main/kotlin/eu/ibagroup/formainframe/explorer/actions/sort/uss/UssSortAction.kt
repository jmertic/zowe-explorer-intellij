/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.explorer.actions.sort.uss

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.dataops.UnitRemoteQueryImpl
import eu.ibagroup.formainframe.dataops.fetch.UssQuery
import eu.ibagroup.formainframe.dataops.sort.SortQueryKeys
import eu.ibagroup.formainframe.explorer.actions.sort.SortAction
import eu.ibagroup.formainframe.explorer.ui.FileExplorerView
import eu.ibagroup.formainframe.explorer.ui.UssDirNode
import eu.ibagroup.formainframe.explorer.ui.getExplorerView
import eu.ibagroup.formainframe.utils.clearAndMergeWith
import eu.ibagroup.formainframe.utils.clearOldKeysAndAddNew
import eu.ibagroup.formainframe.utils.runWriteActionInEdt
import eu.ibagroup.formainframe.vfs.MFVirtualFile
import java.time.LocalDateTime

/**
 * Represents internal Jobs fetch provider to be able to update the query for each Job Filter node whose sorting is enabled
 */
internal val fetchUssProvider = service<DataOpsManager>()
  .getFileFetchProvider(
    UssQuery::class.java,
    UnitRemoteQueryImpl::class.java,
    MFVirtualFile::class.java
  )
class UssSortAction : SortAction() {

  /**
   * Action performed method to register the custom behavior when any USS Sort Key was clicked in UI
   */
  override fun setSelected(e: AnActionEvent, state: Boolean) {
    val view = e.getExplorerView<FileExplorerView>() ?: return
    val sortUssKey = this.templateText?.uppercase()?.replace(" ", "_")?.let { SortQueryKeys.valueOf(it) }
      ?: throw Exception("Sort key for the selected action was not found.")
    if (isSelected(e)) return
    val selectedNode = view.mySelectedNodesData[0].node
    if (selectedNode is UssDirNode) {
      val queryToUpdate = selectedNode.query as UnitRemoteQueryImpl
      selectedNode.currentSortQueryKeysList.clearOldKeysAndAddNew(sortUssKey)
      queryToUpdate.sortKeys.clearAndMergeWith(selectedNode.currentSortQueryKeysList)
      runWriteActionInEdt {
        selectedNode.cleanCache(false)
        fetchUssProvider.apply {
          reload(queryToUpdate)
          applyRefreshCacheDate(queryToUpdate, selectedNode, LocalDateTime.now())
        }
      }
    }
  }

  /**
   * Custom isSelected method determines if the USS Sort Key is currently enabled or not. Updates UI by 'tick' mark
   */
  override fun isSelected(e: AnActionEvent): Boolean {
    val view = e.getExplorerView<FileExplorerView>() ?: return false
    val sortUssKey = this.templateText?.uppercase()?.replace(" ", "_")?.let { SortQueryKeys.valueOf(it) } ?: return false
    val selectedNode = view.mySelectedNodesData[0].node
    if (selectedNode is UssDirNode) {
      return selectedNode.currentSortQueryKeysList.contains(sortUssKey)
    }
    return false
  }
}