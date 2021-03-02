package eu.ibagroup.formainframe.explorer.ui

import com.intellij.ide.util.treeView.AbstractTreeStructureBase
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import eu.ibagroup.formainframe.explorer.Explorer
import eu.ibagroup.formainframe.explorer.ExplorerViewSettings

abstract class ExplorerTreeStructureBase(
  protected val explorer: Explorer,
  protected val project: Project
) : AbstractTreeStructureBase(project), ExplorerViewSettings {

  abstract fun registerNode(node: ExplorerTreeNodeBase<*>)

  abstract fun <V : Any> findByValue(value: V): Collection<ExplorerTreeNodeBase<V>>

  abstract fun findByPredicate(predicate: (ExplorerTreeNodeBase<*>) -> Boolean): Collection<ExplorerTreeNodeBase<*>>

  abstract fun findByVirtualFile(file: VirtualFile): Collection<ExplorerTreeNodeBase<*>>

}