/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.explorer.actions.sort.jobs

import com.intellij.openapi.actionSystem.AnActionEvent
import eu.ibagroup.formainframe.explorer.actions.sort.SortActionGroup
import eu.ibagroup.formainframe.explorer.ui.*

/**
 * Represents the custom Jobs sort action group in the JesExplorerView context menu
 */
class JobsSortActionGroup : SortActionGroup() {
  override fun getSourceView(e: AnActionEvent): JesExplorerView? {
    return e.getExplorerView()
  }

  override fun checkNode(node: ExplorerTreeNode<*, *>): Boolean {
    return node is JesFilterNode
  }

}