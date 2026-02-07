// code by jph
package ch.alpine.sonata.prj.bol;

public abstract class BooleanProjection extends TicksProjection<Boolean> {
  protected final void addViolationAt(int ticks) {
    navigableMap.put(ticks, false);
  }

  @Override
  protected final boolean passValue(Boolean myBoolean) {
    return myBoolean;
  }

  @Override
  public final int getInteger(int ticks) {
    throw new RuntimeException();
  }

  /** @return true by default */
  public boolean isMeaningful() {
    return true;
  }

  /** @return character to display in gui at violation */
  public abstract char getChar();
}
