package springfox.documentation.schema;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public class ScalarType {
  public static final ScalarType INTEGER = new ScalarType("integer", "int32");
  public static final ScalarType LONG = new ScalarType("integer", "int64");
  public static final ScalarType DATE = new ScalarType("string", "date");
  public static final ScalarType DATE_TIME = new ScalarType("string", "date-time");
  public static final ScalarType STRING = new ScalarType("string");
  public static final ScalarType BYTE = new ScalarType("string", "byte");
  public static final ScalarType BINARY = new ScalarType("string", "binary");
  public static final ScalarType PASSWORD = new ScalarType("string", "password");
  public static final ScalarType BOOLEAN = new ScalarType("boolean");
  public static final ScalarType DOUBLE = new ScalarType("number", "double");
  public static final ScalarType FLOAT = new ScalarType("number", "float");
  public static final ScalarType BIGINTEGER = new ScalarType("number", "biginteger");
  public static final ScalarType BIGDECIMAL = new ScalarType("number", "bigdecimal");
  public static final ScalarType UUID = new ScalarType("string", "uuid");
  public static final ScalarType EMAIL = new ScalarType("string", "email");
  public static final ScalarType CURRENCY = new ScalarType("number", "bigdecimal");
  public static final ScalarType URI = new ScalarType("string", "uri");
  public static final ScalarType URL = new ScalarType("string", "url");
  public static final ScalarType OBJECT = new ScalarType("object");

  private static final List<ScalarType> KNOWN_TYPES =
      Arrays.asList(
          INTEGER,
          LONG,
          DATE,
          DATE_TIME,
          STRING,
          BYTE,
          BINARY,
          PASSWORD,
          BOOLEAN,
          DOUBLE,
          FLOAT,
          BIGINTEGER,
          BIGDECIMAL,
          UUID,
          EMAIL,
          CURRENCY,
          URI,
          URL,
          OBJECT);

  private final String type;
  private final String format;

  ScalarType(
      String type,
      String format) {
    this.type = type;
    this.format = format;
  }

  ScalarType(String type) {
    this(type, "");
  }

  public static Optional<ScalarType> from(String type, String format) {
    return KNOWN_TYPES.stream()
                      .filter(k -> k.type.equals(type) && k.format.equals(format))
                      .findFirst();
  }

  public String getType() {
    return type;
  }

  public String getFormat() {
    return format;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScalarType that = (ScalarType) o;
    return Objects.equals(type, that.type) &&
        Objects.equals(format, that.format);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, format);
  }

  @Override
  public String toString() {
    return new StringJoiner(
        ", ",
        ScalarType.class.getSimpleName() + "[",
        "]")
        .add("type='" + type + "'")
        .add("format='" + format + "'")
        .toString();
  }
}
