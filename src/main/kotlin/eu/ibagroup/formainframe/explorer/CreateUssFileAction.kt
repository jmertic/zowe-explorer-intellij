package eu.ibagroup.formainframe.explorer

import eu.ibagroup.formainframe.explorer.ui.*

class CreateUssFileAction : CreateUssEntityAction() {

  override val fileType: CreateFileState
    get() = emptyFileState

  override val ussFileType: String
    get() = "USS file"
}