// code by jph
package ch.alpine.sonata.enc;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import ch.alpine.tensor.ext.PathName;

public enum Encodings {
  INSTANCE;

  private final Map<String, Encoding> map = new HashMap<>();

  Encodings() {
    for (Encoding encoding : Encoding.values())
      encoding.extensions().stream().forEach(extension -> map.put(extension, encoding));
  }

  private static String getMarker(Path file) {
    return PathName.of(file).extension().toLowerCase();
  }

  public Optional<Encoding> getEncoding(Path file) {
    return Optional.ofNullable(map.get(getMarker(file)));
  }
}
