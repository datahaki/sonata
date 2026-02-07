// code by jph
package ch.alpine.sonata.enc.ly;

abstract class PropertyCacher {
  private boolean elevated;

  public final void handle(boolean myBoolean) {
    if (elevated) {
      if (!myBoolean) {
        elevated = false;
        endProperty();
      }
    } else {
      if (myBoolean) {
        elevated = true;
        beginProperty();
      }
    }
  }

  public final void ground() {
    handle(false);
  }

  protected abstract void beginProperty();

  protected abstract void endProperty();
}
