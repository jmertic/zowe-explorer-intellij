/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package org.zowe.explorer.common.ui

import com.intellij.openapi.Disposable
import com.intellij.ui.table.TableView
import org.zowe.explorer.utils.castOrNull
import java.awt.Dimension
import javax.swing.DefaultCellEditor
import javax.swing.table.TableCellRenderer

/** Validating table view class. Provides the functionality to handle table view with validators on the model */
class ValidatingTableView<Item>(
  model: ValidatingListTableModel<Item>,
  val disposable: Disposable
) : TableView<Item>(model) {

  /**
   * Get cell renderer with the changed cell size for the cells with default cell editor
   * @param row the row number to get the cell at
   * @param column the column number to get the cell at
   * @return cell renderer with the changed cell sizes
   */
  override fun getCellRenderer(row: Int, column: Int): TableCellRenderer? {
    return super.getCellRenderer(row, column)?.apply {
      val editor = getCellEditor(row, column)
      if (editor is DefaultCellEditor) {
        preferredSize = with(preferredSize) {
          Dimension(this.width, this.height.coerceAtLeast(editor.component.preferredSize.height))
        }
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun getListTableModel(): ValidatingListTableModel<Item> {
    return super.getModel() as ValidatingListTableModel<Item>
  }

  /**
   * Get cell validator by the column number
   * @param column the column number to get the validator by
   */
  fun getCellValidator(column: Int): ValidatingColumnInfo<Item>? {
    return listTableModel.columnInfos[convertColumnIndexToModel(column)].castOrNull()
  }

}
