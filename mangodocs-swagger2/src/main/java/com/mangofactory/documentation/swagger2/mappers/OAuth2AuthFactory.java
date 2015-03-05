package com.mangofactory.documentation.swagger2.mappers;

import com.mangofactory.documentation.service.AuthorizationCodeGrant;
import com.mangofactory.documentation.service.AuthorizationScope;
import com.mangofactory.documentation.service.AuthorizationType;
import com.mangofactory.documentation.service.GrantType;
import com.mangofactory.documentation.service.ImplicitGrant;
import com.mangofactory.documentation.service.OAuth;
import com.wordnik.swagger.models.auth.OAuth2Definition;
import com.wordnik.swagger.models.auth.SecuritySchemeDefinition;

class OAuth2AuthFactory implements SecuritySchemeFactory {
  @Override
  public SecuritySchemeDefinition create(AuthorizationType input) {
    OAuth oAuth = (OAuth) input;
    OAuth2Definition definition = new OAuth2Definition();
    for (GrantType each : oAuth.getGrantTypes()) {
      if ("authorization_code".equals(each.getType())) {
        definition.accessCode(((AuthorizationCodeGrant) each).getTokenRequestEndpoint().getUrl(),
                ((AuthorizationCodeGrant) each).getTokenEndpoint().getUrl());
      } else if ("implicit".equals(each.getType())) {
        definition.implicit(((ImplicitGrant) each).getLoginEndpoint().getUrl());
//        } else if ("application".equals(each.getType())) {
//          TODO: swagger 1 doesnt not support this
//        } else if ("password".equals(each.getType())) {
//          TODO: swagger 1 doesnt support this
      }
    }
    for (AuthorizationScope each : oAuth.getScopes()) {
      definition.addScope(each.getScope(), each.getDescription());
    }
    return definition;
  }
}
