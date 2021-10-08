/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.common.ui

import com.intellij.ide.util.treeView.AbstractTreeStructure
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.util.ui.tree.TreeUtil
import org.jetbrains.concurrency.Promise
import javax.swing.JTree
import javax.swing.tree.TreePath

fun <S : AbstractTreeStructure> StructureTreeModel<S>.promisePath(
  node: Any,
  tree: JTree,
): Promise<TreePath> {
  return promiseVisitor(node).thenAsync {
    TreeUtil.promiseVisit(tree, it)
  }
}