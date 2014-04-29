package com.mangofactory.swagger.scanners;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimaps;
import com.mangofactory.swagger.core.ResourceGroupingStrategy;
import com.mangofactory.swagger.core.SwaggerPathProvider;
import com.wordnik.swagger.model.ApiListingReference;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.google.common.base.Strings.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Ordering.*;
import static com.mangofactory.swagger.ScalaUtils.*;
import static com.mangofactory.swagger.scanners.ResourceGroups.groupUris;
import static java.lang.String.*;

public class ApiListingReferenceScanner {
    private static final String REQUEST_MAPPINGS_EMPTY =
            "No RequestMappingHandlerMapping's found have you added <mvc:annotation-driven/>";

    private static final Logger log = LoggerFactory.getLogger(ApiListingReferenceScanner.class);
    private List<RequestMappingHandlerMapping> requestMappingHandlerMapping;
    private List<ApiListingReference> apiListingReferences = newArrayList();
    private ArrayListMultimap<ResourceGroup, RequestMappingContext> resourceGroupRequestMappings = ArrayListMultimap
            .create();
    private String swaggerGroup;
    private List<Class<? extends Annotation>> excludeAnnotations;
    private ResourceGroupingStrategy resourceGroupingStrategy;
    private SwaggerPathProvider swaggerPathProvider;
    private List<String> includePatterns = newArrayList(".*?");
    private RequestMappingPatternMatcher requestMappingPatternMatcher = new RegexRequestMappingPatternMatcher();

    public ApiListingReferenceScanner() {
    }

    public List<ApiListingReference> scan() {
        Assert.notNull(requestMappingHandlerMapping, REQUEST_MAPPINGS_EMPTY);
        Assert.notEmpty(requestMappingHandlerMapping, REQUEST_MAPPINGS_EMPTY);
        Assert.notNull(resourceGroupingStrategy, "resourceGroupingStrategy is required");
        Assert.notNull(swaggerGroup, "swaggerGroup is required");
       if(StringUtils.isBlank(swaggerGroup)){
          throw new IllegalArgumentException("swaggerGroup must not be empty");
       }
        Assert.notNull(swaggerPathProvider, "swaggerPathProvider is required");

        log.info("Scanning for api listing references");
        scanSpringRequestMappings();
        return this.apiListingReferences;
    }

    public void scanSpringRequestMappings() {
        Map<String, String> resourceGroupDescriptions = new HashMap<String, String>();
        for (RequestMappingHandlerMapping requestMappingHandlerMapping : this.requestMappingHandlerMapping) {
            for (Entry<RequestMappingInfo, HandlerMethod> handlerMethodEntry :
                    requestMappingHandlerMapping.getHandlerMethods().entrySet()) {
                RequestMappingInfo requestMappingInfo = handlerMethodEntry.getKey();
                HandlerMethod handlerMethod = handlerMethodEntry.getValue();
                if (shouldIncludeRequestMapping(requestMappingInfo, handlerMethod)) {
                    Set<ResourceGroup> resourceGroups = resourceGroupingStrategy.getResourceGroups(requestMappingInfo,
                            handlerMethod);
                    String resourceDescription = resourceGroupingStrategy.getResourceDescription(requestMappingInfo,
                            handlerMethod);
                    RequestMappingContext requestMappingContext = new RequestMappingContext(requestMappingInfo,
                            handlerMethod);
                    for (ResourceGroup group : resourceGroups) {
                        resourceGroupDescriptions.put(group.getGroupName(), resourceDescription);
                        log.info("Adding resource to group {} for handler method: {}", group,
                                handlerMethod.getMethod().getName());
                        resourceGroupRequestMappings.put(group, requestMappingContext);
                    }
                }
            }
        }

        int groupPosition = 0;
        List<String> groups = natural().sortedCopy(groupUris(resourceGroupRequestMappings.keySet()));
        for (String resourceGroupUri : groups) {
            String listingDescription = resourceGroupDescriptions.get(resourceGroupUri);
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
            if (!isNullOrEmpty(swaggerGroup)) {
                uriComponentsBuilder.pathSegment(swaggerGroup);
            }
            String path = uriComponentsBuilder
                    .pathSegment(resourceGroupUri)
                    .build()
                    .toString();
            log.info(format("Create resource listing Path: %s Description: %s Position: %s",
                    path, resourceGroupUri, groupPosition));

            this.apiListingReferences.add(new ApiListingReference(path, toOption(listingDescription), groupPosition++));
        }
    }

    private boolean requestMappingMatchesAnIncludePattern(RequestMappingInfo requestMappingInfo,
                                                          HandlerMethod handlerMethod) {
        PatternsRequestCondition patternsCondition = requestMappingInfo.getPatternsCondition();
        boolean isMatch = requestMappingPatternMatcher.patternConditionsMatchOneOfIncluded(patternsCondition,
                includePatterns);
        if (isMatch) {
            return true;
        }
        log.info(format("RequestMappingInfo did not match any include patterns: | %s", requestMappingInfo));
        return false;
    }

    private boolean shouldIncludeRequestMapping(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
        return requestMappingMatchesAnIncludePattern(requestMappingInfo, handlerMethod)
                && !hasIgnoredAnnotatedRequestMapping(handlerMethod);
    }

    public boolean hasIgnoredAnnotatedRequestMapping(HandlerMethod handlerMethod) {
        if (null != excludeAnnotations) {
            for (Class<? extends Annotation> annotation : excludeAnnotations) {
                if (null != AnnotationUtils.findAnnotation(handlerMethod.getMethod(), annotation)) {
                    log.info(format("Excluding method as it contains the excluded annotation: %s", annotation));
                    return true;
                }
            }
        }
        return false;
    }

    public Map<ResourceGroup, List<RequestMappingContext>> getResourceGroupRequestMappings() {
        return Multimaps.asMap(resourceGroupRequestMappings);
    }

    public List<RequestMappingHandlerMapping> getRequestMappingHandlerMapping() {
        return requestMappingHandlerMapping;
    }

    public void setRequestMappingHandlerMapping(List<RequestMappingHandlerMapping> requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    public List<ApiListingReference> getApiListingReferences() {
        return apiListingReferences;
    }

    public void setApiListingReferences(List<ApiListingReference> apiListingReferences) {
        this.apiListingReferences = apiListingReferences;
    }

    public void setResourceGroupRequestMappings(ArrayListMultimap<ResourceGroup,
            RequestMappingContext> resourceGroupRequestMappings) {
        this.resourceGroupRequestMappings = resourceGroupRequestMappings;
    }

    public String getSwaggerGroup() {
        return swaggerGroup;
    }

    public void setSwaggerGroup(String swaggerGroup) {
        this.swaggerGroup = swaggerGroup;
    }

    public List<Class<? extends Annotation>> getExcludeAnnotations() {
        return excludeAnnotations;
    }

    public void setExcludeAnnotations(List<Class<? extends Annotation>> excludeAnnotations) {
        this.excludeAnnotations = excludeAnnotations;
    }

    public ResourceGroupingStrategy getResourceGroupingStrategy() {
        return resourceGroupingStrategy;
    }

    public void setResourceGroupingStrategy(ResourceGroupingStrategy resourceGroupingStrategy) {
        this.resourceGroupingStrategy = resourceGroupingStrategy;
    }

    public SwaggerPathProvider getSwaggerPathProvider() {
        return swaggerPathProvider;
    }

    public void setSwaggerPathProvider(SwaggerPathProvider swaggerPathProvider) {
        this.swaggerPathProvider = swaggerPathProvider;
    }

    public List<String> getIncludePatterns() {
        return includePatterns;
    }

    public void setIncludePatterns(List<String> includePatterns) {
        this.includePatterns = includePatterns;
    }

    public RequestMappingPatternMatcher getRequestMappingPatternMatcher() {
        return requestMappingPatternMatcher;
    }

    public void setRequestMappingPatternMatcher(RequestMappingPatternMatcher requestMappingPatternMatcher) {
        this.requestMappingPatternMatcher = requestMappingPatternMatcher;
    }
}
