package io.holunda.polyflow.view.query.process

import org.axonframework.queryhandling.QueryResponseMessage

/**
 * Process instance API.
 */
interface ProcessInstanceApi {

  /**
   * Query for process instances.
   * @param query query object.
   * @return list of process instances.
   */
  fun query(query: ProcessInstancesByStateQuery): QueryResponseMessage<ProcessInstanceQueryResult>
}
