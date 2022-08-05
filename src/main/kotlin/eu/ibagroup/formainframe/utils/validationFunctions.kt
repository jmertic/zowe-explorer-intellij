/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.utils

import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import eu.ibagroup.formainframe.config.connect.ConnectionConfig
import eu.ibagroup.formainframe.config.ws.WorkingSetConfig
import eu.ibagroup.formainframe.explorer.FilesWorkingSet
import eu.ibagroup.formainframe.explorer.JesWorkingSet
import eu.ibagroup.formainframe.explorer.ui.NodeData
import eu.ibagroup.formainframe.explorer.ui.UssDirNode
import eu.ibagroup.formainframe.explorer.ui.UssFileNode
import eu.ibagroup.formainframe.utils.crudable.Crudable
import eu.ibagroup.formainframe.utils.crudable.find
import eu.ibagroup.r2z.DatasetOrganization
import java.util.*
import javax.swing.JComponent
import javax.swing.JTextField

private val urlRegex = Regex("^(https?|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")


fun validateForBlank(text: String, component: JComponent): ValidationInfo? {
  return if (text.isBlank()) ValidationInfo("This field must not be blank", component) else null
}

fun validateForBlank(component: JTextField): ValidationInfo? {
  return validateForBlank(component.text, component)
}

fun validateConnectionName(component: JTextField, ignoreValue: String? = null, crudable: Crudable): ValidationInfo? {
  val configAlreadyExists = crudable.find<ConnectionConfig> {
    ignoreValue != it.name && it.name == component.text.trim()
  }.count() > 0
  return if (configAlreadyExists) {
    ValidationInfo("You must provide unique connection name. Connection ${component.text} already exists.", component)
  } else {
    null
  }
}

fun <WSConfig: WorkingSetConfig> validateWorkingSetName(
  component: JTextField,
  ignoreValue: String? = null,
  crudable: Crudable,
  wsConfigClass: Class<out WSConfig>
): ValidationInfo? {
  val configAlreadyExists = crudable.find(wsConfigClass) {
    ignoreValue != it.name && it.name == component.text
  }.count() > 0
  return if (configAlreadyExists) {
    return ValidationInfo(
      "You must provide unique working set name. Working Set ${component.text} already exists.",
      component
    )
  } else {
    null
  }
}

fun validateWorkingSetMaskName(component: JTextField, ws: FilesWorkingSet): ValidationInfo? {
  val maskAlreadyExists = ws.masks.map { it.mask }.contains(component.text.uppercase(Locale.getDefault()))
      || ws.ussPaths.map { it.path.uppercase(Locale.getDefault()) }.contains(component.text.uppercase(Locale.getDefault()))

  return if (maskAlreadyExists) {
    return ValidationInfo(
      "You must provide unique mask in working set. Working Set " +
          "\"${ws.name}\" already has mask - ${component.text}", component
    )
  } else {
    null
  }

}

fun validateZosmfUrl(component: JTextField): ValidationInfo? {
  return if (!component.text.matches(urlRegex)) {
    ValidationInfo("Please provide a valid URL to z/OSMF. Example: https://myhost.com:10443", component)
  } else {
    null
  }
}

fun validateFieldWithLengthRestriction(component: JTextField, length: Int, fieldName: String): ValidationInfo? {
  return if (component.text.trim().length > length) {
    ValidationInfo("$fieldName length must be not exceed $length characters.")
  } else {
    null
  }

}

fun validateUsername(component: JTextField): ValidationInfo? {
  return validateFieldWithLengthRestriction(component, 8, "Username")
}

fun validatePassword(component: JTextField): ValidationInfo? {
  return validateFieldWithLengthRestriction(component, 8, "Password")
}

private val maskRegex = Regex("^[A-Za-z\\$\\*%@#][A-Za-z0-9\\-\\$\\*%@#]{0,7}")
private val ussPathRegex = Regex("^/|(/[^/]+)+\$")

fun validateDatasetMask(text: String, component: JComponent): ValidationInfo? {
  val noMoreThan3AsteriskRule = "\\*{3,}"
  val noMoreThan2AsteriskBeforeTextInTheQualifierRule = "\\*{2,}[^\\*\\.]+"
  val noMoreThan2AsteriskAfterTextInTheQualifierRule = "[^\\*\\.]+\\*{2,}"
  val noSecondAsteriskInTheMiddleOfTheQualifierRule = "\\*+[^\\*\\.]+\\*+[^\\*\\.]+"
  val noFirstAsteriskInTheMiddleOfTheQualifierRule = "[^\\*\\.]+\\*+[^\\*\\.]+\\*+"
  val asteriskRegex = arrayOf(
    noMoreThan3AsteriskRule,
    noMoreThan2AsteriskAfterTextInTheQualifierRule,
    noMoreThan2AsteriskBeforeTextInTheQualifierRule,
    noSecondAsteriskInTheMiddleOfTheQualifierRule,
    noFirstAsteriskInTheMiddleOfTheQualifierRule
  ).joinToString(separator = "|")

  val qualifier = text.split('.')

  return if (text.length > 44) {
    ValidationInfo("Dataset mask length must not exceed 44 characters", component)
  } else if (qualifier.find { it.length > 8 } != null) {
    ValidationInfo("Qualifier must be in 1 to 8 characters", component)
  } else if (text.isNotBlank() && qualifier.find { !it.matches(maskRegex) } != null ) {
    ValidationInfo("Enter valid dataset mask", component)
  } else if (text.contains(Regex(asteriskRegex))) {
    ValidationInfo("Invalid asterisks in the qualifier", component)
  } else {
    null
  }
}

fun validateUssMask(text: String, component: JComponent): ValidationInfo? {
  return if (text.isNotBlank() && !text.matches(ussPathRegex)) {
    ValidationInfo("Provide a valid USS path", component)
  } else {
    null
  }
}

private const val forbiddenSymbol = "/"
private const val warningSymbols = "^[^>|:& ]*$"

fun validateUssFileName(component: JTextField): ValidationInfo? {
  return if (component.text.length > 255) {
    ValidationInfo("Filename must not exceed 255 characters.", component)
  } else if (component.text.isNotBlank() && component.text.contains(forbiddenSymbol)) {
    ValidationInfo("Filename must not contain reserved '/' symbol.", component)
  } else {
    null
  }
}

fun validateJobFilter (prefix: String, owner: String, jobId: String, ws: JesWorkingSet, component: JBTextField, isJobId: Boolean): ValidationInfo? {
  val baseValidation = validateJobFilter(prefix, owner, jobId, component, isJobId)
  if (baseValidation != null) {
    return baseValidation
  }
  val newOwner = owner.ifBlank { "" }
  val newPrefix = prefix.ifBlank { "" }
  val newJobId = jobId.ifBlank { "" }
  return if (ws.masks.any { it.owner == newOwner && it.prefix == newPrefix && it.jobId == newJobId }) {
    ValidationInfo("Job Filter with provided data already exists.", component)
  } else null
}

fun validateJobFilter (prefix: String, owner: String, jobId: String, component: JTextField, isJobId: Boolean): ValidationInfo? {
  if (jobId.isNotBlank()) {
    if (owner.isNotBlank() || prefix.isNotBlank()) {
      return ValidationInfo("You must provide either an owner and a prefix or a job id.", component)
    }
  }
  if (jobId.isBlank()) {
    if (owner.isBlank() || prefix.isBlank()) {
      return ValidationInfo("You must provide either an owner and a prefix or a job id.", component)
    }
  }
  return if (isJobId) {
    validateJobId(component)
  } else {
    validatePrefixAndOwner(component)
  }
}

private val prefixAndOwnerRegex = Regex("[A-Za-z0-9*%]+")
fun validatePrefixAndOwner(component: JTextField) : ValidationInfo? {
  return if (component.text.isNotBlank()) {
    if (component.text.length > 8) {
      ValidationInfo("Text field must not exceed 8 characters.", component)
    } else if (!component.text.matches(prefixAndOwnerRegex)) {
      ValidationInfo("Text field should contain only A-Z, a-z, 0-9, *, %.", component)
    } else {
      null
    }
  } else {
    null
  }
}

private val jobIdRegex = Regex("[A-Za-z0-9]+")
fun validateJobId(component: JTextField) : ValidationInfo? {
  return if (component.text.isNotBlank()) {
    if (component.text.length != 8) {
      ValidationInfo("Job ID length must be 8 characters.", component)
    } else if (!component.text.matches(jobIdRegex)) {
      ValidationInfo("Text field should contain only A-Z, a-z, 0-9", component)
    } else {
      null
    }
  } else {
    null
  }
}

fun validateUssFileNameAlreadyExists(component: JTextField, selectedNode: NodeData): ValidationInfo? {
  val text : String = component.text
  val childrenNodesFromParent = selectedNode.node.parent?.children
  when (selectedNode.node) {
    is UssFileNode -> {
      childrenNodesFromParent?.forEach {
        if (it is UssFileNode && it.value.filenameInternal == text) {
          return ValidationInfo("Filename already exists. Please specify another filename.", component).asWarning()
        }
      }
    }
    is UssDirNode -> {
      childrenNodesFromParent?.forEach {
        if (it is UssDirNode && text == it.value.path.split("/").last()) {
          return ValidationInfo("Directory name already exists. Please specify another directory name.", component).asWarning()
        }
      }
    }
  }
  return null
}

private val firstSymbol = "A-Za-z\$@#"
private val remainingSymbol = firstSymbol + "0-9\\-"
private val partPattern = "([$firstSymbol][$remainingSymbol]{0,7})"
private val notEmptyErrorText = "Dataset name must not be empty"
private val segmentLengthErrorText = "Each name segment (qualifier) is 1 to 8 characters"
private val charactersLengthExceededErrorText = "Dataset name cannot exceed 44 characters"
private val segmentCharsErrorText =
  "$segmentLengthErrorText," +
      "\nthe first of which must be alphabetic (A to Z) or national (# @ \$)." +
      "\nThe remaining seven characters are either alphabetic," +
      "\nnumeric (0 - 9), national, a hyphen (-)." +
      "\nName segments are separated by a period (.)"

fun validateDataset(
  datasetName: JTextField,
  datasetOrganization: DatasetOrganization,
  primaryAllocation: JTextField,
  secondaryAllocation: JTextField,
  directoryBlocks: JTextField,
  recordLength: JTextField,
  blockSize: JTextField,
  averageBlockLength: JTextField,
  advancedParameters: JTextField
) : ValidationInfo? {

  return validateDatasetNameOnInput(datasetName) ?: validateForGreaterValue(primaryAllocation, 1)
  ?: validateForPositiveInteger(secondaryAllocation) ?: validateForGreaterValue(directoryBlocks, 1).takeIf {
    datasetOrganization == DatasetOrganization.PO
  } ?: validateForGreaterValue(recordLength, 1) ?: validateForPositiveInteger(blockSize)
  ?: validateForPositiveInteger(averageBlockLength) ?: validateVolser(advancedParameters)
}

fun validateDatasetNameOnInput(component: JTextField): ValidationInfo? {
  val text = component.text.trim()
  val length = text.length
  val parts = text.split('.')
  return if (text.isNotEmpty()) {
    if (length > 44) {
      ValidationInfo(charactersLengthExceededErrorText, component)
    } else if (parts.find { !it.matches(Regex(partPattern)) || it.length > 8 } != null) {
      ValidationInfo(segmentCharsErrorText, component)
    } else {
      return null
    }
  } else {
    ValidationInfo(notEmptyErrorText, component)
  }
}


private val volserRegex = Regex("[A-Za-z0-9]{1,6}")

fun validateVolser(component: JTextField): ValidationInfo? {
  return if (component.text.isNotBlank() && !component.text.matches(volserRegex)) {
    ValidationInfo("Enter a valid volume serial", component)
  } else {
    null
  }
}

fun validateForPositiveInteger(component: JTextField): ValidationInfo? {
  return validateForGreaterValue(component, 0)
}

fun validateForGreaterValue(component: JTextField, value: Int): ValidationInfo? {
  return if (component.text.toIntOrNull() ?: -1 < value) {
    ValidationInfo(if (value == 0) "Enter a positive number" else "Enter a number grater than $value", component)
  } else {
    null
  }
}

private val firstLetterRegex = Regex("[A-Z@\$#a-z]")
private val memberRegex = Regex("[A-Z@$#a-z][A-Z@#\$a-z0-9]{0,7}")

fun validateMemberName(component: JTextField): ValidationInfo? {
  return if (component.text.length > 8) {
    ValidationInfo("Member name must not exceed 8 characters.", component)
  } else if (component.text.isNotEmpty() && !component.text[0].toString().matches(firstLetterRegex)) {
    ValidationInfo("Member name should start with A-Z a-z or national characters", component)
  } else if (component.text.isNotBlank() && !component.text.matches(memberRegex)) {
    ValidationInfo("Member name should contain only A-Z a-z 0-9 or national characters", component)
  } else {
    null
  }
}




