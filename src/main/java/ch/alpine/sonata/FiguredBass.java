// code by jph
package ch.alpine.sonata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import sys.mat.TokenStream;

public class FiguredBass implements Serializable {
  private static final Comparator<Figure> COMPARATOR = //
      (Comparator<Figure> & Serializable) //
      (figure1, figure2) -> Integer.compare(figure1.number, figure2.number);
  // ---
  private final NavigableSet<Figure> navigableSet = new TreeSet<>(COMPARATOR);

  public FiguredBass(Figure... figures) {
    List.of(figures).forEach(this::insert);
  }

  public void insert(Figure figure) {
    navigableSet.remove(figure);
    navigableSet.add(figure);
  }

  public boolean containsNumber(int number) {
    return contains(new Figure(number));
  }

  public boolean contains(Figure figure) {
    return navigableSet.contains(figure);
  }

  public boolean isDefault() {
    return navigableSet.isEmpty();
  }

  /** @return read-only access on set of figures */
  public NavigableSet<Figure> figures() {
    return Collections.unmodifiableNavigableSet(navigableSet);
  }

  public List<Figure> figuresReversed() {
    return new ArrayList<>(navigableSet.reversed());
  }

  public static FiguredBass fromString(String string) {
    FiguredBass figuredBass = new FiguredBass();
    TokenStream.of(string).map(Figure::fromString).forEach(figuredBass::insert);
    return figuredBass;
  }

  public String toStringReversed() {
    return figuresReversed().stream().map(Figure::toString).collect(Collectors.joining(" "));
  }

  @Override
  public String toString() {
    return navigableSet.stream().map(Figure::toString).collect(Collectors.joining(" "));
  }

  @Override
  public int hashCode() {
    return navigableSet.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    FiguredBass figuredBass = (FiguredBass) object;
    return navigableSet.equals(figuredBass.navigableSet);
  }
}
