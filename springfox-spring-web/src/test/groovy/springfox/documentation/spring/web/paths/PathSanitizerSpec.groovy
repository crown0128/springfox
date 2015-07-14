package springfox.documentation.spring.web.paths
import com.google.common.base.Optional
import springfox.documentation.spi.service.contexts.PathContext
import springfox.documentation.spi.service.contexts.RequestMappingContext
import springfox.documentation.spring.web.plugins.DocumentationContextSpec

class PathSanitizerSpec extends DocumentationContextSpec {
  def "Creates path adjustment in relation to servlet mapping" () {
    given:
      def requestMappingContext = Mock(RequestMappingContext)
      PathContext ctx = new PathContext(requestMappingContext, Optional.absent())
    and:
      requestMappingContext.getDocumentationContext() >> context()
    and:
      def sut = new PathSanitizer()
    when:
      def decorator = sut.decorator(ctx)
    and:
      def decorated = decorator.apply(mappingPattern)
    then:
      decorated == expected
    where:
      mappingPattern             | expected
      ""                         | "/"
      "/"                        | "/"
      "/businesses"              | "/businesses"
      "/{businessId:\\w+}"       | "/{businessId}"
      "/businesses/{businessId}" | "/businesses/{businessId}"
      "/foo/bar:{baz}"           | "/foo/bar:{baz}"
      "/foo:{foo}/bar:{baz}"     | "/foo:{foo}/bar:{baz}"
      "/foo/bar:{baz:\\w+}"      | "/foo/bar:{baz}"
    }
}
