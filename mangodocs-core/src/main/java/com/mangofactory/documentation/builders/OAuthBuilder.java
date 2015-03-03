package com.mangofactory.documentation.builders;

import com.mangofactory.documentation.service.AuthorizationScope;
import com.mangofactory.documentation.service.GrantType;
import com.mangofactory.documentation.service.OAuth;

import java.util.ArrayList;
import java.util.List;

import static com.mangofactory.documentation.builders.BuilderDefaults.*;

public class OAuthBuilder {

  private List<AuthorizationScope> scopes = new ArrayList<AuthorizationScope>();
  private List<GrantType> grantTypes = new ArrayList<GrantType>();
  private String name;


  public OAuthBuilder scopes(List<AuthorizationScope> scopes) {
    if (scopes != null) {
      this.scopes.addAll(scopes);
    }
    return this;
  }

  public OAuthBuilder grantTypes(List<GrantType> grantTypes) {
    if (grantTypes != null) {
      this.grantTypes.addAll(grantTypes);
    }
    return this;
  }

  public OAuthBuilder name(String name) {
    this.name = defaultIfAbsent(name, this.name);
    return this;
  }

  public OAuth build() {
    return new OAuth(name, scopes, grantTypes);
  }
}
