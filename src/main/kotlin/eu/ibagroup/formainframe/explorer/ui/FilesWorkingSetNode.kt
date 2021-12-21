package eu.ibagroup.formainframe.explorer.ui

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import eu.ibagroup.formainframe.config.ws.DSMask
import eu.ibagroup.formainframe.explorer.FilesWorkingSet

class FilesWorkingSetNode(
  workingSet: FilesWorkingSet,
  project: Project,
  parent: ExplorerTreeNode<*>,
  treeStructure: ExplorerTreeStructureBase
) : WorkingSetNode<DSMask>(
  workingSet, project, parent, treeStructure
), MFNode, RefreshableNode {

  private val valueForFilesWS = value as FilesWorkingSet

  override fun update(presentation: PresentationData) {
    presentation.addText(valueForFilesWS.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
    when {
      valueForFilesWS.connectionConfig == null -> connectionIsNotSet(presentation)
      valueForFilesWS.masks.isEmpty() && valueForFilesWS.ussPaths.isEmpty() -> destinationsAreEmpty(presentation)
      else -> regular(presentation)
    }
    if (treeStructure.showWorkingSetInfo) {
      addInfo(presentation)
    }
  }

  override fun getChildren(): MutableCollection<out AbstractTreeNode<*>> {
    return valueForFilesWS.masks.map { DSMaskNode(it, notNullProject, this, valueForFilesWS, treeStructure) }.plus(
      valueForFilesWS.ussPaths.map { UssDirNode(it, notNullProject, this, valueForFilesWS, treeStructure, isRootNode = true) }
    ).toMutableList().also { cachedChildrenInternal = it }
  }
}