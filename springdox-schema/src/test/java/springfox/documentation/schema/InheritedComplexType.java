package springfox.documentation.schema;

public class InheritedComplexType extends ComplexType {
  private String inheritedProperty;

  public String getInheritedProperty() {
    return inheritedProperty;
  }

  public void setInheritedProperty(String inheritedProperty) {
    this.inheritedProperty = inheritedProperty;
  }
}
