package eu.ibagroup.formainframe.dataops.operations

import com.intellij.openapi.progress.ProgressIndicator
import eu.ibagroup.formainframe.api.api
import eu.ibagroup.formainframe.config.connect.ConnectionConfig
import eu.ibagroup.formainframe.config.connect.UrlConnection
import eu.ibagroup.formainframe.config.connect.token
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.r2z.DataAPI

class MemberAllocatorFactory : OperationRunnerFactory {
  override fun buildComponent(dataOpsManager: DataOpsManager): Allocator<*> {
    return MemberAllocator(dataOpsManager)
  }
}

data class MemberAllocationOperation(
  override val request: MemberAllocationParams,
  override val connectionConfig: ConnectionConfig,
  override val urlConnection: UrlConnection
) : RemoteAllocationOperation<MemberAllocationParams>

class MemberAllocator(
  dataOpsManager: DataOpsManager
) : RemoteAllocatorBase<MemberAllocationOperation>(dataOpsManager) {

  override val operationClass = MemberAllocationOperation::class.java

  override fun performAllocationRequest(query: MemberAllocationOperation, progressIndicator: ProgressIndicator?) {
    progressIndicator?.checkCanceled()
    val request = api<DataAPI>(query.connectionConfig).writeToDatasetMember(
      authorizationToken = query.connectionConfig.token,
      datasetName = query.request.datasetName,
      memberName = query.request.memberName,
      content = ""
    ).execute()
    if (!request.isSuccessful) {
      throw Throwable(request.code().toString())
    }
  }
}

data class MemberAllocationParams(val datasetName: String, var memberName: String = "")