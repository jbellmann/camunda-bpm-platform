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
package org.camunda.bpm.engine.rest.dto.runtime;

/**
 * @author: drobisch
 */
public class VariableValueDto {
  String name;
  Object value;
  String type;

  public VariableValueDto() {
  }
  
  public VariableValueDto(String name, Object value, String type) {
    this.name = name;
    this.value = value;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }

  public String getType() {
    return type;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public void setType(String type) {
    this.type = type;
  }
}
