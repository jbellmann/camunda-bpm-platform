/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.history.producer;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;

/**
 * <p>An {@link ExecutionListener} implementation that delegates to a
 * {@link HistoryEventProducer} which is passed in.
 * 
 * <p>This allows plugging the history as an execution listener into process 
 * execution and make sure history events are generated as we move through the 
 * process.
 * 
 * @author Daniel Meyer
 *
 */
public class ExecutionListenerHistoryAdapter implements ExecutionListener {
  
  protected final HistoryEventProducer historyEventProducer;

  public ExecutionListenerHistoryAdapter(HistoryEventProducer historyEventProducer) {
    this.historyEventProducer = historyEventProducer;
  }

  public void notify(DelegateExecution execution) throws Exception {
    
    // get the event handler
    final HistoryEventHandler historyEventHandler = Context.getProcessEngineConfiguration()
      .getHistoryEventHandler();
    
    // delegate creation of the history event to the producer
    HistoryEvent historyEvent = historyEventProducer.createHistoryEvent(execution);
    
    // pass the event to the handler
    historyEventHandler.handleEvent(historyEvent);
    
  }

}
