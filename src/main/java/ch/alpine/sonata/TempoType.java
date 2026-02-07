// code by jph
package ch.alpine.sonata;

/** description from Wikipedia:
 * alla breve � in short style, i.e., "duple time", with the half note (minim) rather than the quarter note (crotchet) */
public enum TempoType {
  Larghissimo("very, very slow", 1, 19),
  Grave("slow and solemn", 20, 40),
  Lento("slowly", 40, 45),
  Largo("broadly", 45, 50),
  Larghetto("rather broadly", 50, 55),
  Adagio(" slow and stately, at ease", 55, 65),
  Adagietto("rather slow", 65, 69),
  Andante_moderato("a bit slower than andante", 69, 72),
  Andante("at a walking pace", 73, 77),
  Andantino("slightly faster than andante", 78, 83),
  Marcia_moderato("moderately, in the manner of a march", 83, 85),
  Moderato("moderately", 86, 97),
  Allegretto("moderately fast", 98, 109),
  Allegro("fast, quickly and bright", 109, 132),
  Vivace("lively and fast", 132, 140),
  Vivacissimo("very fast and lively", 140, 150),
  Allegrissimo("very fast", 150, 167),
  Presto("very fast", 168, 177),
  Prestissimo("extremely fast", 178, 220);

  final String string;
  final int min;
  final int max;

  private TempoType(String string, int min, int max) {
    this.string = string;
    this.min = min;
    this.max = max;
  }
}
