/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package org.zowe.explorer.dataops.attributes

import org.zowe.explorer.config.connect.ConnectionConfig
import org.zowe.explorer.config.ws.DSMask
import org.zowe.explorer.utils.nullIfBlank

data class MaskedRequester(
  override val connectionConfig: ConnectionConfig,
  val queryMask: DSMask,
) : Requester {
  val queryVolser: String?
    get() = queryMask.volser.nullIfBlank()
}