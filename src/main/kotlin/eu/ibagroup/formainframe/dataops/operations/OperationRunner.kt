/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.dataops.operations

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.progress.DumbProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import eu.ibagroup.formainframe.dataops.Operation

interface OperationRunner<O : Operation<R>, R : Any> {

  companion object {
    @JvmField
    val EP = ExtensionPointName.create<OperationRunnerFactory>("eu.ibagroup.formainframe.operationRunner")
  }

  val operationClass: Class<out O>

  val resultClass: Class<out R>

  fun canRun(operation: O): Boolean

  fun run(operation: O, progressIndicator: ProgressIndicator = DumbProgressIndicator.INSTANCE): R

}
