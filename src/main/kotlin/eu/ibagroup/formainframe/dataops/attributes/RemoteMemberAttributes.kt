package eu.ibagroup.formainframe.dataops.attributes

import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.utils.clone
import eu.ibagroup.formainframe.vfs.MFVirtualFile
import eu.ibagroup.r2z.Member

data class RemoteMemberAttributes(
  val memberInfo: Member,
  val libraryFile: MFVirtualFile
) : VFileInfoAttributes {

  val name
    get() = memberInfo.name

  override fun clone(): VFileInfoAttributes {
    return RemoteMemberAttributes(memberInfo.clone(), libraryFile)
  }

}

fun RemoteMemberAttributes.getLibraryAttributes(dataOpsManager: DataOpsManager): RemoteDatasetAttributes? {
  return dataOpsManager.getAttributesService(RemoteDatasetAttributes::class.java, libraryFile::class.java)
    .getAttributes(libraryFile)
}