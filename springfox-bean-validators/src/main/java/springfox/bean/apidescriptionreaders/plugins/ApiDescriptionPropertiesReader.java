/*
 *
 *  Copyright 2015-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package springfox.bean.apidescriptionreaders.plugins;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;

@Component
public class ApiDescriptionPropertiesReader {

  private String propertyFilePath = "/api_description.properties";
  private Properties props = new Properties();

  @PostConstruct
  public void init() throws IOException {
    props.load(this.getClass().getResourceAsStream(propertyFilePath));
  }

  public String getProperty(String key) {
    return props.getProperty(key);
  }

  public String getPropertyFilePath() {
    return propertyFilePath;
  }

  public void setPropertyFilePath(String propertyFilePath) {
    this.propertyFilePath = propertyFilePath;
  }
}
