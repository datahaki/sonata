// code by jph
package ch.alpine.sonata.enc;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ImportFileFilter extends FileFilter implements java.io.FileFilter {
  @Override
  public boolean accept(File file) {
    return Encodings.INSTANCE.getEncoding(file.toPath()).filter(Encoding::isImportable).isPresent();
  }

  @Override
  public String getDescription() {
    return "Importable score files";
  }
}
