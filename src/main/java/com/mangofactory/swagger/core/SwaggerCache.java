package com.mangofactory.swagger.core;

import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.ResourceListing;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.google.common.collect.Maps.newLinkedHashMap;

@Component
public class SwaggerCache {
   @Getter
   private Map<String, ResourceListing> swaggerApiResourceListingMap = newLinkedHashMap();

   @Getter
   private Map<String, Map<String, ApiListing>> swaggerApiListingMap = newLinkedHashMap();

   public void addSwaggerResourceListing(String swaggerGroup, ResourceListing resourceListing) {
      swaggerApiResourceListingMap.put(swaggerGroup, resourceListing);
   }

   public void addApiListings(String swaggerGroup, Map<String, ApiListing> apiListings) {
     swaggerApiListingMap.put(swaggerGroup, apiListings);
   }

   public ResourceListing getResourceListing(String key){
      return swaggerApiResourceListingMap.get(key);
   }
}
