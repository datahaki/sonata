// code by jph
package ch.alpine.sonata;

import java.util.Iterator;

public enum FiguredBase {
  ;
  public static FiguredBass complete(FiguredBass figuredBass) {
    switch (figuredBass.figures().size()) {
    case 0: {
      FiguredBass myReturn = new FiguredBass();
      myReturn.insert(Figure.TERZ);
      myReturn.insert(Figure.QUINTE);
      return myReturn;
    }
    case 1: {
      FiguredBass myReturn = new FiguredBass();
      Iterator<Figure> iterator = figuredBass.figures().iterator();
      Figure figure = iterator.next();
      myReturn.insert(figure);
      switch (figure.number) {
      case 2: // "sekunde, quarte und sexte sind zu ergaenzen"
        myReturn.insert(Figure.QUARTE);
        myReturn.insert(Figure.SEXTE);
        break;
      case 3:
      case 4: // "quarte und quinte sind zu ergaenzen"
        myReturn.insert(Figure.QUINTE);
        break;
      case 5:
      case 6: // "terz und sexte sind zu ergaenzen"
        myReturn.insert(Figure.TERZ);
        break;
      case 7: // "terz, quinte und septime sind zu ergaenzen"
        myReturn.insert(Figure.TERZ);
        myReturn.insert(Figure.QUINTE);
        break;
      }
      return myReturn;
    }
    case 2: {
      FiguredBass myReturn = new FiguredBass();
      Iterator<Figure> iterator = figuredBass.figures().iterator();
      Figure myLo = iterator.next();
      Figure myHi = iterator.next();
      myReturn.insert(myLo);
      myReturn.insert(myHi);
      if (myLo.number == 5 && myHi.number == 6)
        myReturn.insert(Figure.TERZ); // "terz, quinte, und sexte sind zu ergaenzen"
      else //
      if (myLo.number == 3 && myHi.number == 4)
        myReturn.insert(Figure.SEXTE); // "terz, quinte, und sexte sind zu ergaenzen"
      return myReturn;
    }
    }
    return figuredBass; // TODO NOTATION provide new instance for consistency
  }
}
