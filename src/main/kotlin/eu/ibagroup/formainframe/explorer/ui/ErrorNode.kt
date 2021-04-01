package eu.ibagroup.formainframe.explorer.ui

import com.intellij.CommonBundle
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import eu.ibagroup.formainframe.explorer.Explorer

class ErrorNode(
  project: Project,
  parent: ExplorerTreeNodeBase<*>,
  explorer: Explorer,
  treeStructure: ExplorerTreeStructureBase
) : InfoNodeBase(project, parent, explorer, treeStructure) {

  override val text: String = CommonBundle.message("title.error")

  override val textAttributes: SimpleTextAttributes = SimpleTextAttributes.ERROR_ATTRIBUTES

}