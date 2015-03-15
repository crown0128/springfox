package springdox.documentation.swagger.integration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification
import springdox.documentation.spring.web.mixins.JsonSupport
import springdox.documentation.swagger.configuration.CustomJavaPluginConfig

@WebAppConfiguration
@ContextConfiguration(classes = CustomJavaPluginConfig.class)
@Mixin(JsonSupport)
class CustomJavaPluginStartupSpec extends Specification {

  @Autowired
  WebApplicationContext context;

  def "Should start app with custom java config"() {
    when:
      MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
      MvcResult petApi = mockMvc.perform(MockMvcRequestBuilders.get('/v1/api-docs?group=customPlugin')).andReturn()
      MvcResult demoApi = mockMvc.perform(MockMvcRequestBuilders.get('/v1/api-docs?group=secondCustomPlugin'))
              .andReturn()
    then:
      jsonBodyResponse(petApi).apis.size() == 4
      jsonBodyResponse(demoApi).apis.size() == 1
  }
}
