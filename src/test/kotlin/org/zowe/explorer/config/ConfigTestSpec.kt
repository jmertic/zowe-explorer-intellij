/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package org.zowe.explorer.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import org.zowe.explorer.config.connect.ConnectionConfig
import org.zowe.explorer.config.connect.Credentials
import org.zowe.explorer.config.connect.CredentialsConfigDeclaration
import org.zowe.explorer.config.connect.ZOSMFConnectionConfigDeclaration
import org.zowe.explorer.config.connect.getOwner
import org.zowe.explorer.config.connect.getUsername
import org.zowe.explorer.config.connect.ui.zosmf.ConnectionDialogState
import org.zowe.explorer.config.connect.ui.zosmf.ConnectionsTableModel
import org.zowe.explorer.config.connect.ui.zosmf.initEmptyUuids
import org.zowe.explorer.config.connect.whoAmI
import org.zowe.explorer.config.ws.FilesWorkingSetConfig
import org.zowe.explorer.config.ws.JesWorkingSetConfig
import org.zowe.explorer.config.ws.ui.AbstractWsDialog
import org.zowe.explorer.config.ws.ui.FilesWorkingSetDialogState
import org.zowe.explorer.config.ws.ui.files.FilesWorkingSetDialog
import org.zowe.explorer.dataops.DataOpsManager
import org.zowe.explorer.dataops.Operation
import org.zowe.explorer.dataops.operations.TsoOperation
import org.zowe.explorer.dataops.operations.TsoOperationMode
import org.zowe.explorer.explorer.Explorer
import org.zowe.explorer.explorer.WorkingSet
import org.zowe.explorer.testutils.WithApplicationShouldSpec
import org.zowe.explorer.testutils.testServiceImpl.TestDataOpsManagerImpl
import org.zowe.explorer.ui.build.tso.TSOWindowFactory
import org.zowe.explorer.utils.crudable.Crudable
import org.zowe.explorer.utils.service
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import org.zowe.kotlinsdk.MessageType
import org.zowe.kotlinsdk.TsoData
import org.zowe.kotlinsdk.TsoResponse
import org.zowe.kotlinsdk.annotations.ZVersion
import java.util.*
import java.util.stream.Stream
import javax.swing.JComponent
import kotlin.reflect.KFunction

class ConfigTestSpec : WithApplicationShouldSpec({
  afterSpec {
    clearAllMocks()
  }
  context("config module: connect") {
    context("ui/ConnectionsTableModel") {

      lateinit var crudable: Crudable
      lateinit var connTab: ConnectionsTableModel

      fun mockConfigService() {
        val mockConfigServiceInstance = mockk<ConfigService>()
        every {
          mockConfigServiceInstance.getConfigDeclaration(ConnectionConfig::class.java)
        } returns ZOSMFConnectionConfigDeclaration(crudable)
        every {
          mockConfigServiceInstance.getConfigDeclaration(Credentials::class.java)
        } returns CredentialsConfigDeclaration(crudable)

        mockkObject(ConfigService)
        every { ConfigService.instance } returns mockConfigServiceInstance
      }

      val connectionDialogState = ConnectionDialogState(
        connectionName = "a", connectionUrl = "https://a.com", username = "a", password = "a"
      )

      beforeEach {
        val configCollections: MutableMap<String, MutableList<*>> = mutableMapOf(
          Pair(ConnectionConfig::class.java.name, mutableListOf<ConnectionConfig>()),
          Pair(FilesWorkingSetConfig::class.java.name, mutableListOf<ConnectionConfig>()),
          Pair(JesWorkingSetConfig::class.java.name, mutableListOf<ConnectionConfig>()),
        )
        val sandboxState = SandboxState(ConfigStateV2(configCollections))

        crudable =
          makeCrudableWithoutListeners(true, { sandboxState.credentials }) { sandboxState.configState }
        connectionDialogState.initEmptyUuids(crudable)
        connTab = ConnectionsTableModel(crudable)
        mockConfigService()
      }

      context("fetch") {
        should("fetch connections from crudable") {

          connTab.addRow(connectionDialogState)

          val actual = connTab.fetch(crudable)
          val expected = mutableListOf(connectionDialogState)

          assertSoftly {
            actual shouldBe expected
          }
        }
      }
      context("onAdd") {
        should("add connection to crudable") {

          val connectionDialogStateB = ConnectionDialogState(
            connectionName = "b", connectionUrl = "https://b.com", username = "b", password = "b"
          )
          connectionDialogStateB.initEmptyUuids(crudable)

          connTab.onAdd(crudable, connectionDialogState)
          connTab.onAdd(crudable, connectionDialogStateB)

          val actual = connTab.fetch(crudable)
          val expected = mutableListOf(connectionDialogState, connectionDialogStateB)

          assertSoftly {
            actual shouldBe expected
          }
        }
        should("add connection with existing name") {

          val connectionDialogStateB = ConnectionDialogState(connectionName = connectionDialogState.connectionName)
          connectionDialogStateB.initEmptyUuids(crudable)

          connTab.onAdd(crudable, connectionDialogState)
          connTab.onAdd(crudable, connectionDialogStateB)

          val actual = connTab.fetch(crudable)
          val expected = mutableListOf(connectionDialogState)

          assertSoftly {
            actual shouldBe expected
          }
        }
        should("add connection with existing url") {

          val connectionDialogStateB = ConnectionDialogState(connectionUrl = connectionDialogState.connectionUrl)
          connectionDialogStateB.initEmptyUuids(crudable)

          connTab.onAdd(crudable, connectionDialogState)
          connTab.onAdd(crudable, connectionDialogStateB)

          val actual = connTab.fetch(crudable)
          val expected = mutableListOf(connectionDialogState, connectionDialogStateB)

          assertSoftly {
            actual shouldBe expected
          }
        }
      }
      context("onDelete") {
        should("delete connection from crudable") {

          connTab.onAdd(crudable, connectionDialogState)
          connTab.onDelete(crudable, connectionDialogState)

          val actual = connTab.fetch(crudable)
          val expected = mutableListOf<ConnectionDialogState>()

          assertSoftly {
            actual shouldBe expected
          }
        }
      }
      context("set") {
        should("set connection to crudable") {

          connTab.addRow(ConnectionDialogState().initEmptyUuids(crudable))
          connTab[0] = connectionDialogState

          assertSoftly {
            connTab[0].connectionName shouldBe connectionDialogState.connectionName
            connTab[0].connectionUrl shouldBe connectionDialogState.connectionUrl
            connTab[0].username shouldBe connectionDialogState.username
            connTab[0].password shouldBe connectionDialogState.password
            connTab[0].connectionUuid shouldNotBe connectionDialogState.connectionUuid
          }
        }
      }
    }
    context("connectUtils") {
      val connectionConfig = ConnectionConfig()

      val explorerMock = mockk<Explorer<ConnectionConfig, WorkingSet<ConnectionConfig, *>>>()
      every { explorerMock.componentManager } returns ApplicationManager.getApplication()

      val dataOpsManagerService =
        ApplicationManager.getApplication().service<DataOpsManager>() as TestDataOpsManagerImpl

      beforeEach {
        dataOpsManagerService.testInstance = object : TestDataOpsManagerImpl(explorerMock.componentManager) {
          override fun <R : Any> performOperation(operation: Operation<R>, progressIndicator: ProgressIndicator): R {
            val tsoResponse = TsoResponse(
              servletKey = "servletKey",
              tsoData = listOf(TsoData())
            )
            if ((operation as TsoOperation).mode == TsoOperationMode.SEND_MESSAGE) {
              tsoResponse.tsoData = listOf(
                TsoData(tsoMessage = MessageType("", "ZOSMFAD  "))
              )
            }
            @Suppress("UNCHECKED_CAST")
            return tsoResponse as R
          }
        }

        mockkObject(TSOWindowFactory)
        every { TSOWindowFactory.getTsoMessageQueue(any()) } answers {
          TsoResponse(
            tsoData = listOf(
              TsoData(tsoPrompt = MessageType(""))
            )
          )
        }

        val getUsernameRef: (ConnectionConfig) -> String = ::getUsername
        mockkStatic(getUsernameRef as KFunction<*>)
        every { getUsername(any<ConnectionConfig>()) } returns "ZOSMF"
      }
      afterEach {
        unmockkAll()
      }

      // whoAmI
      should("get the owner by TSO request") {

        val actual = whoAmI(connectionConfig)

        assertSoftly { actual shouldBe "ZOSMFAD" }
      }
      should("do not get the owner by TSO request if servlet key is null") {
        dataOpsManagerService.testInstance = object : TestDataOpsManagerImpl(explorerMock.componentManager) {
          override fun <R : Any> performOperation(operation: Operation<R>, progressIndicator: ProgressIndicator): R {
            @Suppress("UNCHECKED_CAST")
            return TsoResponse() as R
          }
        }

        val actual = whoAmI(connectionConfig)

        assertSoftly { actual shouldBe null }
      }
      should("do not get the owner by TSO request if servlet key is empty") {
        dataOpsManagerService.testInstance = object : TestDataOpsManagerImpl(explorerMock.componentManager) {
          override fun <R : Any> performOperation(operation: Operation<R>, progressIndicator: ProgressIndicator): R {
            @Suppress("UNCHECKED_CAST")
            return TsoResponse(servletKey = "") as R
          }
        }

        val actual = whoAmI(connectionConfig)

        assertSoftly { actual shouldBe null }
      }
      should("do not get the owner by TSO request if send message request fails") {
        dataOpsManagerService.testInstance = object : TestDataOpsManagerImpl(explorerMock.componentManager) {
          override fun <R : Any> performOperation(operation: Operation<R>, progressIndicator: ProgressIndicator): R {
            val tsoResponse = TsoResponse(
              servletKey = "servletKey",
              tsoData = listOf(TsoData())
            )
            if ((operation as TsoOperation).mode == TsoOperationMode.SEND_MESSAGE) {
              throw Exception("Failed to send message")
            }
            @Suppress("UNCHECKED_CAST")
            return tsoResponse as R
          }
        }

        val actual = whoAmI(connectionConfig)

        assertSoftly { actual shouldBe null }
      }

      // getOwner
      should("get owner by connection config when owner is not empty") {
        val owner = getOwner(
          ConnectionConfig("", "", "", true, ZVersion.ZOS_2_3, null, "ZOSMFAD")
        )

        assertSoftly { owner shouldBe "ZOSMFAD" }
      }
      should("get owner by connection config when owner is empty") {
        val owner = getOwner(
          ConnectionConfig("", "", "", true, ZVersion.ZOS_2_3, null, "")
        )

        assertSoftly { owner shouldBe "ZOSMF" }
      }
    }
    context("Credentials.hashCode") {
      should("check hashcode for uniqueness") {
        val credentials = Credentials("uuid", "username", "password")
        val credentials2 = Credentials("uuid", "username", "password")
        val hashcode = credentials.hashCode()
        val hashcode2 = credentials2.hashCode()

        assertSoftly {
          hashcode shouldNotBe hashcode2
        }
      }
    }
  }
  context("config module: xmlUtils") {
    // get
    should("get XML child element by tag") {}
    // toElementList
    should("convert node list to elements list") {}
  }
  context("config module: ConfigSandboxImpl") {
    // apply
    should("apply changes of the config sandbox") {}
    // rollback
    should("rollback all the changes of the config sandbox") {}
    // isModified
    should("check if the sandbox is modified") {}
  }
  context("config module: ws") {

    lateinit var crudableMockk: Crudable

    beforeEach {
      mockkObject(AbstractWsDialog)
      every { AbstractWsDialog["initialize"](any<() -> Unit>()) } returns Unit

      crudableMockk = mockk<Crudable>()
      every { crudableMockk.getAll(ConnectionConfig::class.java) } returns Stream.of()
      every {
        crudableMockk.getByUniqueKey(ConnectionConfig::class.java, any<String>())
      } returns Optional.of(ConnectionConfig())

      mockkConstructor(DialogPanel::class)
      every { anyConstructed<DialogPanel>().registerValidators(any(), any()) } answers {
        val componentValidityChangedCallback = secondArg<(Map<JComponent, ValidationInfo>) -> Unit>()
        componentValidityChangedCallback(mapOf())
      }
    }

    afterEach {
      unmockkAll()
      clearAllMocks()
    }

    // WSNameColumn.validateEntered
    should("check that the entered working set name is not empty") {}
    should("check that the entered working set name is not blank") {}
    // jes/JesWsDialog.validateOnApply
    should("check that there are no errors for job filters") {}
    should("check that the error appears on any errors for job filters") {}
    should("check that the error appears on empty JES working set") {}
    should("check that the error appears on adding the same job filter again") {}
    // files/WorkingSetDialog.validateOnApply
    should("check that there are no errors for file masks") {}
    should("check that the error appears on any errors for file masks") {}
    should("check that the error appears on empty file working set") {}
    should("check that the error appears on adding the same file mask again") {}
    // ui/AbstractWsDialog.init
    should("check that OK action is enabled if validation map is empty") {

      val dialog = FilesWorkingSetDialog(crudableMockk, FilesWorkingSetDialogState())

      verify { anyConstructed<DialogPanel>().registerValidators(any(), any()) }
      assertSoftly { dialog.isOKActionEnabled shouldBe true }
    }
  }
})
