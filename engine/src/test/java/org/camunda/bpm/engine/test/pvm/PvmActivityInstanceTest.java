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
package org.camunda.bpm.engine.test.pvm;

import java.util.ArrayList;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.pvm.ProcessDefinitionBuilder;
import org.camunda.bpm.engine.impl.pvm.PvmExecution;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.test.PvmTestCase;
import org.camunda.bpm.engine.test.pvm.activities.Automatic;
import org.camunda.bpm.engine.test.pvm.activities.EmbeddedSubProcess;
import org.camunda.bpm.engine.test.pvm.activities.End;
import org.camunda.bpm.engine.test.pvm.activities.ParallelGateway;
import org.camunda.bpm.engine.test.pvm.activities.WaitState;
import org.camunda.bpm.engine.test.pvm.activities.While;

/**
 * @author Daniel Meyer
 *
 */
public class PvmActivityInstanceTest extends PvmTestCase {
  
  /**
   * +-----+   +-----+   +-------+
   * | one |-->| two |-->| three |
   * +-----+   +-----+   +-------+
   */
  public void testSequence() {
    
    ActivityInstanceVerification verifier = new ActivityInstanceVerification();
    
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("one")
        .initial()
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("two")
      .endActivity()
      .createActivity("two")
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("three")
      .endActivity()
      .createActivity("three")
        .behavior(new End())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    verifier.assertStartInstanceCount(1, "one");
    verifier.assertStartInstanceCount(1, "two");
    verifier.assertStartInstanceCount(1, "three");
    
  }
  
  /**
   *                  +----------------------------+
   *                  v                            |
   * +-------+   +------+   +-----+   +-----+    +-------+
   * | start |-->| loop |-->| one |-->| two |--> | three |
   * +-------+   +------+   +-----+   +-----+    +-------+
   *                  |
   *                  |   +-----+
   *                  +-->| end |
   *                      +-----+
   */
  public void testWhileLoop() {
    
    ActivityInstanceVerification verifier = new ActivityInstanceVerification();
    
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("loop")
      .endActivity()
      .createActivity("loop")
        .behavior(new While("count", 0, 10))
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("one", "more")
        .transition("end", "done")
      .endActivity()
      .createActivity("one")
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("two")
      .endActivity()
      .createActivity("two")
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("three")
      .endActivity()
      .createActivity("three")
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("loop")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    assertEquals(new ArrayList<String>(), processInstance.findActiveActivityIds());
    assertTrue(processInstance.isEnded());
    
    verifier.assertStartInstanceCount(1, "start");
    verifier.assertProcessInstanceParent("start", processInstance);
    
    verifier.assertStartInstanceCount(11, "loop");
    verifier.assertProcessInstanceParent("loop", processInstance);
    
    verifier.assertStartInstanceCount(10, "one");
    verifier.assertProcessInstanceParent("one", processInstance);
    
    verifier.assertStartInstanceCount(10, "two");
    verifier.assertProcessInstanceParent("two", processInstance);
    
    verifier.assertStartInstanceCount(10, "three");
    verifier.assertProcessInstanceParent("three", processInstance);
    
    verifier.assertStartInstanceCount(1, "end");
    verifier.assertProcessInstanceParent("end", processInstance);
  }
  

  /** 
   *           +-------------------------------------------------+
   *           | embeddedsubprocess        +----------+          |
   *           |                     +---->|endInside1|          |
   *           |                     |     +----------+          |
   *           |                     |                           |
   * +-----+   |  +-----------+   +----+   +----+   +----------+ |   +---+
   * |start|-->|  |startInside|-->|fork|-->|wait|-->|endInside2| |-->|end|
   * +-----+   |  +-----------+   +----+   +----+   +----------+ |   +---+
   *           |                     |                           |
   *           |                     |     +----------+          |
   *           |                     +---->|endInside3|          |
   *           |                           +----------+          |
   *           +-------------------------------------------------+
   */
  public void testMultipleConcurrentEndsInsideEmbeddedSubProcessWithWaitState() {
    
    ActivityInstanceVerification verifier = new ActivityInstanceVerification();
    
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .createActivity("startInside")
          .behavior(new Automatic())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
          .transition("fork")
        .endActivity()
        .createActivity("fork")
          .behavior(new ParallelGateway())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
          .transition("endInside1")
          .transition("wait")
          .transition("endInside3")
        .endActivity()
        .createActivity("endInside1")
          .behavior(new End())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .endActivity()
        .createActivity("wait")
          .behavior(new WaitState())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
          .transition("endInside2")
        .endActivity()
        .createActivity("endInside2")
          .behavior(new End())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .endActivity()
        .createActivity("endInside3")
          .behavior(new End())
          .executionListener(ExecutionListener.EVENTNAME_START, verifier)
          .executionListener(ExecutionListener.EVENTNAME_END, verifier)
        .endActivity()
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
        .executionListener(ExecutionListener.EVENTNAME_START, verifier)
        .executionListener(ExecutionListener.EVENTNAME_END, verifier)
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertFalse(processInstance.isEnded());
    PvmExecution execution = processInstance.findExecution("wait");
    execution.signal(null, null);
    
    assertTrue(processInstance.isEnded());
    
    verifier.assertStartInstanceCount(1, "start");
    verifier.assertProcessInstanceParent("start", processInstance);
    
    verifier.assertStartInstanceCount(1, "embeddedsubprocess");
    verifier.assertProcessInstanceParent("embeddedsubprocess", processInstance);
    
    verifier.assertStartInstanceCount(1, "startInside");
    verifier.assertParent("startInside", "embeddedsubprocess");
    
    verifier.assertStartInstanceCount(1, "fork");
    verifier.assertParent("fork", "embeddedsubprocess");
    
    verifier.assertStartInstanceCount(1, "wait");
    verifier.assertParent("wait", "embeddedsubprocess");
    
    verifier.assertStartInstanceCount(1, "endInside1");
    verifier.assertParent("endInside1", "embeddedsubprocess");
    
    verifier.assertStartInstanceCount(1, "endInside2");
    verifier.assertParent("endInside2", "embeddedsubprocess");
    
    verifier.assertStartInstanceCount(1, "endInside3");
    verifier.assertParent("endInside3", "embeddedsubprocess");
    
    verifier.assertStartInstanceCount(1, "end");
    verifier.assertProcessInstanceParent("end", processInstance);
    
  }
    
}
