package org.camunda.bpm.cockpit.plugin.base.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;

import org.camunda.bpm.cockpit.plugin.base.persistence.entity.ProcessInstanceDto;
import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginResource;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

public class ProcessInstanceResource extends AbstractPluginResource {

  public static final String PATH = "/process-instance";

  public ProcessInstanceResource(String engineName) {
    super(engineName);
  }

  @GET
  public List<ProcessInstanceDto> getProcessInstances(@QueryParam("processDefinitionId") String processDefinitionId,
      @QueryParam("firstResult") int firstResult, @QueryParam("maxResults") int maxResults) {
    ProcessInstanceQueryParameter param = new ProcessInstanceQueryParameter(firstResult, maxResults);
    param.setProcessDefinitionId(processDefinitionId);

    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) getProcessEngine()).getProcessEngineConfiguration();
    
    if (processEngineConfiguration.getHistoryLevel() == 0) {
      param.setHistoryEnabled(false);
    }
    return getQueryService().executeQuery("selectRunningProcessInstances", param);
  }

}
