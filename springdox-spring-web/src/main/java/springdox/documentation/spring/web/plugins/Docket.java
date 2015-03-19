package springdox.documentation.spring.web.plugins;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import org.springframework.web.bind.annotation.RequestMethod;
import springdox.documentation.PathProvider;
import springdox.documentation.schema.AlternateTypeRule;
import springdox.documentation.schema.WildcardType;
import springdox.documentation.service.ApiDescription;
import springdox.documentation.service.ApiInfo;
import springdox.documentation.service.ApiListingReference;
import springdox.documentation.service.AuthorizationType;
import springdox.documentation.service.Operation;
import springdox.documentation.service.ResponseMessage;
import springdox.documentation.spi.DocumentationType;
import springdox.documentation.spi.service.DocumentationPlugin;
import springdox.documentation.spi.service.contexts.ApiSelector;
import springdox.documentation.spi.service.contexts.AuthorizationContext;
import springdox.documentation.spi.service.contexts.DocumentationContext;
import springdox.documentation.spi.service.contexts.DocumentationContextBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.collect.FluentIterable.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static com.google.common.collect.Sets.*;
import static org.springframework.util.StringUtils.*;
import static springdox.documentation.schema.AlternateTypeRules.*;

/**
 * A builder which is intended to be the primary interface into the swagger-springmvc framework.
 * Provides sensible defaults and convenience methods for configuration.
 */
public class Docket implements DocumentationPlugin {

  private static final String DEFAULT_GROUP_NAME = "default";
  private final DocumentationType documentationType;
  private String groupName;
  private ApiInfo apiInfo;
  private PathProvider pathProvider;
  private AuthorizationContext authorizationContext;
  private List<AuthorizationType> authorizationMethods;
  private Ordering<ApiListingReference> apiListingReferenceOrdering;
  private Ordering<ApiDescription> apiDescriptionOrdering;
  private Ordering<Operation> operationOrdering;

  private AtomicBoolean initialized = new AtomicBoolean(false);
  private boolean enabled = true;
  private boolean applyDefaultResponseMessages = true;
  private Map<RequestMethod, List<ResponseMessage>> responseMessages = newHashMap();
  private List<Function<TypeResolver, AlternateTypeRule>> ruleBuilders = newArrayList();
  private Set<Class> ignorableParameterTypes = newHashSet();
  private Set<String> protocols = newHashSet();
  private Set<String> produces = newHashSet();
  private Set<String> consumes = newHashSet();
  private ApiSelector apiSelector = ApiSelector.DEFAULT;

  public Docket(DocumentationType documentationType) {
    this.documentationType = documentationType;
  }

  /**
   * Sets the api's meta information as included in the json ResourceListing response.
   *
   * @param apiInfo Indicates the api information
   * @return this DocumentationConfigurer
   */
  public Docket apiInfo(ApiInfo apiInfo) {
    this.apiInfo = apiInfo;
    return this;
  }

  /**
   * Configures the global com.wordnik.swagger.model.AuthorizationType's applicable to all or some of the api
   * operations. The configuration of which operations have associated AuthorizationTypes is configured with
   * springdox.swagger.plugins.DocumentationConfigurer#authorizationContext
   *
   * @param authorizationTypes a list of global AuthorizationType's
   * @return this DocumentationConfigurer
   */
  public Docket authorizationTypes(List<AuthorizationType> authorizationTypes) {
    this.authorizationMethods = authorizationTypes;
    return this;
  }

  /**
   * Configures which api operations (via regex patterns) and HTTP methods to apply swagger authorization to.
   *
   * @param authorizationContext
   * @return this DocumentationConfigurer
   */
  public Docket authorizationContext(AuthorizationContext authorizationContext) {
    this.authorizationContext = authorizationContext;
    return this;
  }

  /**
   * If more than one instance of DocumentationConfigurer exists, each one must have a unique groupName as
   * supplied by this method. Defaults to "default".
   *
   * @param groupName - the unique identifier of this swagger group/configuration
   * @return this DocumentationConfigurer
   */
  public Docket groupName(String groupName) {
    this.groupName = groupName;
    return this;
  }

  /**
   * Determines the generated, swagger specific, urls.
   * <p/>
   * By default, relative urls are generated. If absolute urls are required, supply an implementation of
   * AbsoluteSwaggerPathProvider
   *
   * @param pathProvider
   * @return this DocumentationConfigurer
   * @see springdox.documentation.spring.web.AbstractPathProvider
   */
  public Docket pathProvider(PathProvider pathProvider) {
    this.pathProvider = pathProvider;
    return this;
  }

  /**
   * Overrides the default http response messages at the http request method level.
   * <p/>
   * To set specific response messages for specific api operations use the swagger core annotations on
   * the appropriate controller methods.
   *
   * @param requestMethod    - http request method for which to apply the message
   * @param responseMessages - the message
   * @return this DocumentationConfigurer
   * @see com.wordnik.swagger.annotations.ApiResponse
   * and
   * @see com.wordnik.swagger.annotations.ApiResponses
   * @see springdox.documentation.spi.service.contexts.Defaults#defaultResponseMessages()
   */
  public Docket globalResponseMessage(RequestMethod requestMethod,
                                      List<ResponseMessage> responseMessages) {

    this.responseMessages.put(requestMethod, responseMessages);
    return this;
  }

  /**
   * Adds ignored controller method parameter types so that the framework does not generate swagger model or parameter
   * information for these specific types.
   * e.g. HttpServletRequest/HttpServletResponse which are already included in the pre-configured ignored types.
   *
   * @param classes the classes to ignore
   * @return this DocumentationConfigurer
   * @see springdox.documentation.spi.service.contexts.Defaults#defaultIgnorableParameterTypes()
   */
  public Docket ignoredParameterTypes(Class... classes) {
    this.ignorableParameterTypes.addAll(Arrays.asList(classes));
    return this;
  }

  public Docket produces(Set<String> produces) {
    this.produces.addAll(produces);
    return this;
  }

  public Docket consumes(Set<String> consumes) {
    this.consumes.addAll(consumes);
    return this;
  }

  public Docket protocols(Set<String> protocols) {
    this.protocols.addAll(protocols);
    return this;
  }

  /**
   * Adds model substitution rules (alternateTypeRules)
   *
   * @param alternateTypeRules
   * @return this DocumentationConfigurer
   * @see springdox.documentation.schema.AlternateTypeRules#newRule(java.lang.reflect.Type,
   * java.lang.reflect.Type)
   */
  public Docket alternateTypeRules(AlternateTypeRule... alternateTypeRules) {
    this.ruleBuilders.addAll(from(newArrayList(alternateTypeRules)).transform(identityRuleBuilder()).toList());
    return this;
  }

  public Docket operationOrdering(Ordering<Operation> operationOrdering) {
    this.operationOrdering = operationOrdering;
    return this;
  }

  private Function<AlternateTypeRule, Function<TypeResolver, AlternateTypeRule>> identityRuleBuilder() {
    return new Function<AlternateTypeRule, Function<TypeResolver, AlternateTypeRule>>() {
      @Override
      public Function<TypeResolver, AlternateTypeRule> apply(AlternateTypeRule rule) {
        return identityFunction(rule);
      }
    };
  }

  private Function<TypeResolver, AlternateTypeRule> identityFunction(final AlternateTypeRule rule) {
    return new Function<TypeResolver, AlternateTypeRule>() {
      @Override
      public AlternateTypeRule apply(TypeResolver typeResolver) {
        return rule;
      }
    };
  }

  /**
   * Directly substitutes a model class with the supplied substitute
   * e.g
   * <code>directModelSubstitute(LocalDate.class, Date.class)</code>
   * would substitute LocalDate with Date
   *
   * @param clazz class to substitute
   * @param with  the class which substitutes 'clazz'
   * @return this DocumentationConfigurer
   */
  public Docket directModelSubstitute(final Class clazz, final Class with) {
    this.ruleBuilders.add(newSubstitutionFunction(clazz, with));
    return this;
  }

  /**
   * Substitutes each generic class with it's direct parameterized type.
   * e.g.
   * <code>.genericModelSubstitutes(ResponseEntity.class)</code>
   * would substitute ResponseEntity &lt;MyModel&gt; with MyModel
   *
   * @param genericClasses - generic classes on which to apply generic model substitution.
   * @return this DocumentationConfigurer
   */
  public Docket genericModelSubstitutes(Class... genericClasses) {
    for (Class clz : genericClasses) {
      this.ruleBuilders.add(newGenericSubstitutionFunction(clz));
    }
    return this;
  }

  /**
   * Allows ignoring predefined response message defaults
   *
   * @param apply flag to determine if the default response messages are used
   *              true   - the default response messages are added to the global response messages
   *              false  - the default response messages are added to the global response messages
   * @return this DocumentationConfigurer
   */
  public Docket useDefaultResponseMessages(boolean apply) {
    this.applyDefaultResponseMessages = apply;
    return this;
  }

  /**
   * Controls how ApiListingReference's are sorted.
   * i.e the ordering of the api's within the swagger Resource Listing.
   * The default sort is Lexicographically by the ApiListingReference's path
   *
   * @param apiListingReferenceOrdering
   * @return this DocumentationConfigurer
   */
  public Docket apiListingReferenceOrdering(Ordering<ApiListingReference>
                                                apiListingReferenceOrdering) {
    this.apiListingReferenceOrdering = apiListingReferenceOrdering;
    return this;
  }

  /**
   * Controls how <code>com.wordnik.swagger.model.ApiDescription</code>'s are ordered.
   * The default sort is Lexicographically by the ApiDescription's path.
   *
   * @param apiDescriptionOrdering
   * @return this DocumentationConfigurer
   * @see springdox.documentation.spring.web.scanners.ApiListingScanner
   */
  public Docket apiDescriptionOrdering(Ordering<ApiDescription> apiDescriptionOrdering) {
    this.apiDescriptionOrdering = apiDescriptionOrdering;
    return this;
  }

  /**
   * Hook to externally control auto initialization of this swagger plugin instance.
   * Typically used if defer initialization.
   *
   * @param externallyConfiguredFlag - true to turn it on, false to turn it off
   * @return this DocumentationConfigurer
   */
  public Docket enable(boolean externallyConfiguredFlag) {
    this.enabled = externallyConfiguredFlag;
    return this;
  }

  public ApiSelectorBuilder select() {
    return new ApiSelectorBuilder(this);
  }

  /**
   * Builds the DocumentationConfigurer by merging/overlaying user specified values.
   * It is not necessary to call this method when defined as a spring bean.
   * NOTE: Calling this method more than once has no effect.
   *
   * @see DocumentationPluginsBootstrapper
   */
  public DocumentationContext configure(DocumentationContextBuilder builder) {
    if (initialized.compareAndSet(false, true)) {
      configureDefaults();
    }
    return builder
        .apiInfo(apiInfo)
        .selector(apiSelector)
        .applyDefaultResponseMessages(applyDefaultResponseMessages)
        .additionalResponseMessages(responseMessages)
        .additionalIgnorableTypes(ignorableParameterTypes)
        .ruleBuilders(ruleBuilders)
        .groupName(groupName)
        .pathProvider(pathProvider)
        .authorizationContext(authorizationContext)
        .authorizationTypes(authorizationMethods)
        .apiListingReferenceOrdering(apiListingReferenceOrdering)
        .apiDescriptionOrdering(apiDescriptionOrdering)
        .operationOrdering(operationOrdering)
        .produces(produces)
        .consumes(consumes)
        .protocols(protocols)
        .build();
  }

  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public DocumentationType getDocumentationType() {
    return documentationType;
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return documentationType.equals(delimiter);
  }

  private Function<TypeResolver, AlternateTypeRule> newSubstitutionFunction(final Class clazz, final Class with) {
    return new Function<TypeResolver, AlternateTypeRule>() {

      @Override
      public AlternateTypeRule apply(TypeResolver typeResolver) {
        return newRule(typeResolver.resolve(clazz), typeResolver.resolve(with));
      }
    };
  }

  private Function<TypeResolver, AlternateTypeRule> newGenericSubstitutionFunction(final Class clz) {
    return new Function<TypeResolver, AlternateTypeRule>() {
      @Override
      public AlternateTypeRule apply(TypeResolver typeResolver) {
        return newRule(typeResolver.resolve(clz, WildcardType.class), typeResolver.resolve(WildcardType.class));
      }
    };
  }

  private void configureDefaults() {
    if (!hasText(this.groupName)) {
      this.groupName = DEFAULT_GROUP_NAME;
    }

    if (null == this.apiInfo) {
      this.apiInfo = ApiInfo.DEFAULT;
    }
  }

  Docket selector(ApiSelector apiSelector) {
    this.apiSelector = apiSelector;
    return this;
  }

}
