/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package org.zowe.explorer.dataops.synchronizer

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.progress.DumbProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.zowe.explorer.config.connect.ConnectionConfig
import org.zowe.kotlinsdk.CodePage
import org.zowe.kotlinsdk.XIBMDataType
import org.zowe.kotlinsdk.annotations.ZVersion

enum class AcceptancePolicy {
  IF_EMPTY_ONLY,
  FORCE_REWRITE
}

interface ContentSynchronizer {

  companion object {
    @JvmField
    val EP = ExtensionPointName.create<ContentSynchronizerFactory>("org.zowe.explorer.contentSynchronizer")
  }

  fun accepts(file: VirtualFile): Boolean

  fun isAlreadySynced(file: VirtualFile): Boolean

  fun startSync(
    file: VirtualFile,
    project: Project,
    acceptancePolicy: AcceptancePolicy,
    saveStrategy: SaveStrategy,
    removeSyncOnThrowable: (file: VirtualFile, t: Throwable) -> Boolean,
    progressIndicator: ProgressIndicator = DumbProgressIndicator.INSTANCE
  )

  fun triggerSync(file: VirtualFile)

  fun startSyncIfNeeded(
    file: VirtualFile,
    project: Project,
    acceptancePolicy: AcceptancePolicy,
    saveStrategy: SaveStrategy,
    removeSyncOnThrowable: (file: VirtualFile, t: Throwable) -> Boolean,
    progressIndicator: ProgressIndicator = DumbProgressIndicator.INSTANCE
  ) {
    if (!isAlreadySynced(file)) {
      startSync(file, project, acceptancePolicy, saveStrategy, removeSyncOnThrowable, progressIndicator)
    }
  }

  fun removeSync(file: VirtualFile)

}

fun updateDataTypeWithEncoding(connectionConfig: ConnectionConfig, oldDataType: XIBMDataType) : XIBMDataType {
  return if (connectionConfig.zVersion == ZVersion.ZOS_2_4 && oldDataType.encoding != null && oldDataType.encoding != CodePage.IBM_1047 && oldDataType.type == XIBMDataType.Type.TEXT) {
    XIBMDataType(oldDataType.type, connectionConfig.codePage)
  } else {
    oldDataType
  }
}
