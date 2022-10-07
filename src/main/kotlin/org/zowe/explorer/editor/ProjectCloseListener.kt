/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package org.zowe.explorer.editor

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.project.VetoableProjectManagerListener

//private val log = log<FileEditorEventsListener>()

// TODO: implement as soon as the syncronizer will be rewritten
/**
 * Project close event listener.
 * Handle files which are not synchronized before the close
 */
class ProjectCloseListener : ProjectManagerListener {
  init {
    val projListener = object : VetoableProjectManagerListener {
      /**
       * Check whether all the files of the project are synchronized
       * @param project the project to check the files
       */
      override fun canClose(project: Project): Boolean {
//        val configService = service<ConfigService>()
//        if (!configService.isAutoSyncEnabled.get() && ApplicationManager.getApplication().isActive) {
//          val openFiles = project.component<FileEditorManager>().openFiles
//          if (openFiles.isNotEmpty()) {
//            openFiles.forEach { file ->
//              val document = FileDocumentManager.getInstance().getDocument(file) ?: let {
//                log.info("Document cannot be used here")
//                return@forEach
//              }
//              if (showSyncOnCloseDialog(file.name, project)) {
//                runModalTask(
//                  title = "Syncing ${file.name}",
//                  project = project,
//                  cancellable = true
//                ) {
//                  runInEdt {
//                    FileDocumentManager.getInstance().saveDocument(document)
//                    service<DataOpsManager>().getContentSynchronizer(file)?.userSync(file)
//                  }
//                }
//              }
//            }
//          }
//        }
        return true
      }
    }
    ProjectManager.getInstance().addProjectManagerListener(projListener)
  }
}
