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
package org.camunda.bpm.engine.rest.dto.converter;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

/**
 * Reads a list of {@link VariableQueryParameterDto}s from a single parameter. Expects a given format (see method comments).
 * @author Thorben Lindhauer
 *
 */
public class VariableListConverter implements
    StringToTypeConverter<List<VariableQueryParameterDto>> {

  /**
   * Expects a query parameter of format KEY_OPERATOR_VALUE, e.g. aVariable_eq_aValue
   */
  @Override
  public List<VariableQueryParameterDto> convertQueryParameterToType(String value) {
    VariableQueryParameterDto queryVariable = new VariableQueryParameterDto();
    
    String[] valueTriple = value.split("_");
    if (valueTriple.length != 3) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "variable query parameter has to have format KEY_OPERATOR_VALUE.");
    }
    queryVariable.setName(valueTriple[0]);
    queryVariable.setOperator(valueTriple[1]);
    queryVariable.setValue(valueTriple[2]);
    
    List<VariableQueryParameterDto> queryVariables = new ArrayList<VariableQueryParameterDto>();
    queryVariables.add(queryVariable);
    return queryVariables;
  }
}
