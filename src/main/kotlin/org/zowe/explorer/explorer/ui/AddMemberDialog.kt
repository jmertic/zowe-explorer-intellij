/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package org.zowe.explorer.explorer.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import org.zowe.explorer.common.ui.StatefulComponent
import org.zowe.explorer.dataops.operations.MemberAllocationParams
import org.zowe.explorer.utils.validateForBlank
import org.zowe.explorer.utils.validateMemberName
import javax.swing.JComponent

/** Dialog to add dataset member */
class AddMemberDialog(project: Project?, override var state: MemberAllocationParams) : DialogWrapper(project),
  StatefulComponent<MemberAllocationParams> {

  override fun createCenterPanel(): JComponent {
    return panel {
      row {
        label("Member name: ")
        textField()
          .bindText(state::memberName)
          .validationOnInput { validateMemberName(it) }
          .validationOnApply { validateForBlank(it) }
          .apply { focused() }
          .horizontalAlign(HorizontalAlign.FILL)
      }
    }
  }

  init {
    title = "Create Member"
    init()
  }

}
