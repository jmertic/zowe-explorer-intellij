/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package org.zowe.explorer.explorer

import com.intellij.openapi.Disposable
import org.zowe.explorer.config.ws.DSMask
import org.zowe.explorer.config.ws.JobsFilter
import org.zowe.explorer.config.ws.UssPath

interface WorkingSet<Mask> : ExplorerUnit, Disposable {

  val masks: Collection<Mask>

  fun addMask(mask: Mask)

  fun removeMask(mask: Mask)

}

interface FilesWorkingSet : WorkingSet<DSMask> {
  val ussPaths: Collection<UssPath>

  fun addUssPath(ussPath: UssPath)

  fun removeUssPath(ussPath: UssPath)

}

interface JesWorkingSet : WorkingSet<JobsFilter> {}