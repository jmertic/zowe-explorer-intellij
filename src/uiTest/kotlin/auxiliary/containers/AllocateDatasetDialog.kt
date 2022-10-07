/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package auxiliary.containers

import auxiliary.ClosableCommonContainerFixture
import auxiliary.clickButton
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.*
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

/**
 * Class representing the Allocate Dataset Dialog.
 */
@FixtureName("Allocate Dataset Dialog")
open class AllocateDatasetDialog(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : ClosableCommonContainerFixture(remoteRobot, remoteComponent) {

    /**
     * Fills in the parameters for allocating dataset.
     */
    fun allocateDataset(
        datasetName: String,
        datasetOrganization: String,
        allocationUnit: String,
        primaryAllocation: Int,
        secondaryAllocation: Int,
        directory: Int,
        recordFormat: String,
        recordLength: Int,
        blockSize: Int
    ) {
        findAll<JTextFieldFixture>(byXpath("//div[@class='JBTextField']"))[0].text = datasetName
        findAll<ComboBoxFixture>(byXpath("//div[@class='ComboBox']"))[0].selectItem(datasetOrganization)

        val datasetTextParams = findAll<JTextFieldFixture>(byXpath("//div[@class='JBTextField']"))
        val datasetComboBoxParams = findAll<ComboBoxFixture>(byXpath("//div[@class='ComboBox']"))

        datasetComboBoxParams[1].selectItem(allocationUnit)
        datasetTextParams[1].text = primaryAllocation.toString()
        datasetTextParams[2].text = secondaryAllocation.toString()
        datasetComboBoxParams[2].selectItem(recordFormat)

        if (datasetOrganization == "PS") {
            datasetTextParams[3].text = recordLength.toString()
            datasetTextParams[4].text = blockSize.toString()
        } else {
            datasetTextParams[3].text = directory.toString()
            datasetTextParams[4].text = recordLength.toString()
            datasetTextParams[5].text = blockSize.toString()
        }
    }

    //TODO add allocate dataset with advanced parameters

    /**
     * The close function, which is used to close the dialog in the tear down method.
     */
    override fun close() {
        clickButton("Cancel")
    }

    companion object {
        const val name = "Allocate Dataset Dialog"

        /**
         * Returns the xPath of the Add Working Set Dialog.
         */
        @JvmStatic
        fun xPath() = byXpath(name, "//div[@accessiblename='Allocate Dataset' and @class='MyDialog']")
    }
}

/**
 * Finds the AllocateDatasetDialog and modifies fixtureStack.
 */
fun ContainerFixture.allocateDatasetDialog(
    fixtureStack: MutableList<Locator>,
    timeout: Duration = Duration.ofSeconds(60),
    function: AllocateDatasetDialog.() -> Unit = {}
) {
    find<AllocateDatasetDialog>(AllocateDatasetDialog.xPath(), timeout).apply {
        fixtureStack.add(AllocateDatasetDialog.xPath())
        function()
        fixtureStack.removeLast()
    }
}