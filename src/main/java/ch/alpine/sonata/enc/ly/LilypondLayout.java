// code by jph
package ch.alpine.sonata.enc.ly;

import java.util.LinkedList;
import java.util.List;

import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;

@ReflectionMarker
public class LilypondLayout {
  public static LilypondLayout png_default() {
    LilypondLayout lilypondLayout = new LilypondLayout();
    lilypondLayout.global_staff_size = "28";
    // lilypondLayout.writeln("#(set-default-paper-size '(cons (* 1374 pt) (* 773 pt)))"); // 1920 x 1080
    // lilypondLayout.default_paper_size = "'(cons (* 1288 pt) (* 737 pt))"; // 1800 x 1030
    // lilypondLayout.default_paper_size = "'(cons (* 1288 pt) (* 773 pt))"; // 1800 x 1080
    lilypondLayout.default_paper_size = "'(cons (* 1374 pt) (* 750 pt))"; // 1800 x 1048 == 1080-32
    lilypondLayout.top_margin = "0\\mm";
    lilypondLayout.bottom_margin = "0\\mm";
    lilypondLayout.left_margin = "8\\mm";
    lilypondLayout.right_margin = "1\\mm";
    lilypondLayout.indent = "0";
    return lilypondLayout;
  }

  public static LilypondLayout pdf_default() {
    LilypondLayout lilypondLayout = new LilypondLayout();
    return lilypondLayout;
  }

  // ---
  public Boolean copyright = false;
  public Boolean tagline = false;
  public Boolean toc = false;
  // FOR PAPER
  /** If ragged_bottom set to true, systems will not spread vertically down the page.
   * This does not affect the last page.
   * This should be set to true for pieces that have
   * only two or three systems per page, for example orchestral scores. */
  public Boolean ragged_bottom = false;
  public Boolean print_page_number = false;
  /** additional info for debugging */
  public Boolean annotate_spacing = false;
  // ---
  /** on a screen with 15 inch diagonal, staff size == 28 is comfortable for sight reading
   * smaller is also possible */
  public String global_staff_size = "";
  /** "a4" "a4landscape" (quotation marks included!) */
  public String default_paper_size = "";
  public String top_margin = "";
  public String bottom_margin = "";
  public String left_margin = "";
  public String right_margin = "";
  // ---
  // FOR LAYOUT TAG
  /** indentation of first system on first page, example: 6\cm */
  public String indent = "";
  /** only for png export */
  @FieldClip(min = "1", max = "8")
  public Integer anti_alias_factor = 3;

  public List<String> paper() {
    List<String> list = new LinkedList<>();
    list.add("print-page-number = " + LilypondConstants.of(print_page_number));
    list.add("ragged-bottom = " + LilypondConstants.of(ragged_bottom));
    list.add("annotate-spacing = " + LilypondConstants.of(annotate_spacing));
    if (!top_margin.isBlank())
      list.add("top-margin = " + top_margin);
    if (!bottom_margin.isBlank())
      list.add("bottom-margin = " + bottom_margin);
    if (!left_margin.isBlank())
      list.add("left-margin = " + left_margin);
    if (!right_margin.isBlank())
      list.add("right-margin = " + right_margin);
    return list;
  }

  public List<String> layout() {
    List<String> list = new LinkedList<>();
    if (!indent.isBlank()) {
      list.add("indent = " + indent);
    }
    return list;
  }
}
