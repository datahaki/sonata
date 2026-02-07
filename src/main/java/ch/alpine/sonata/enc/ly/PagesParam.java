package ch.alpine.sonata.enc.ly;

import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.num.Boole;
import ch.alpine.tensor.tmp.ResamplingMethods;
import ch.alpine.tensor.tmp.TimeSeries;

@ReflectionMarker
public class PagesParam {
  public Tensor measures = Tensors.vector(0);
  public Boolean partial = false;
  public Integer piy = 146;
  public Integer dy = 91;
  @FieldSelectionArray({ "1/2", "1", "3/2", "2" })
  public Scalar pageTurn = RealScalar.of(1);

  public int measureAt(int page) {
    Scalar incr = Boole.of(0 < page && partial);
    Scalar scalar = measures.Get(page).add(incr);
    return Scalars.intValueExact(scalar);
  }

  public TimeSeries timeSeries(Scalar pageTurn) {
    TimeSeries timeSeries = TimeSeries.empty(ResamplingMethods.LINEAR_INTERPOLATION.get());
    timeSeries.insert(RealScalar.ZERO, RealScalar.ZERO);
    Scalar incr = Boole.of(partial);
    for (int page = 1; page < measures.length(); ++page) {
      Scalar x = measures.Get(page).add(incr);
      timeSeries.insert(x.subtract(pageTurn), RealScalar.of(page - 1));
      timeSeries.insert(x, RealScalar.of(page));
    }
    return timeSeries;
  }

  /** @param measure
   * @return integral and fractional part */
  public Scalar pageOf(Scalar measure) {
    TimeSeries timeSeries = timeSeries(pageTurn);
    Scalar x = timeSeries.domain().apply(measure);
    return (Scalar) timeSeries.evaluate(x);
  }
}
