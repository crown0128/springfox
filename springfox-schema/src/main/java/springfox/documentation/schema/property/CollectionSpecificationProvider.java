package springfox.documentation.schema.property;

import com.fasterxml.classmate.ResolvedType;
import springfox.documentation.schema.CollectionSpecification;
import springfox.documentation.schema.CollectionType;
import springfox.documentation.schema.Collections;
import springfox.documentation.schema.ModelSpecification;
import springfox.documentation.spi.schema.contexts.ModelContext;

import java.util.Optional;

public class CollectionSpecificationProvider {

  private final ModelSpecificationFactory models;

  public CollectionSpecificationProvider(ModelSpecificationFactory models) {
    this.models = models;
  }

  Optional<CollectionSpecification> create(
      ModelContext modelContext,
      ResolvedType type) {

    if (!Collections.isContainerType(type)) {
      return Optional.empty();
    }
    ResolvedType itemType = Collections.collectionElementType(type);
    CollectionType collectionType = Collections.collectionType(type);
    ModelSpecification itemModel = models.create(modelContext, itemType);
    return Optional.of(new CollectionSpecification(itemModel, collectionType));
  }

}
