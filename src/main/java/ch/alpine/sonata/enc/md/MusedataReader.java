// code by jph
package ch.alpine.sonata.enc.md;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.StringTokenizer;

import ch.alpine.sonata.Divisions;
import ch.alpine.sonata.KeySignature;
import ch.alpine.sonata.Note;
import ch.alpine.sonata.Ornament;
import ch.alpine.sonata.enc.api.DiatonicPrecision;
import ch.alpine.sonata.enc.api.ImportScoreFormat;
import ch.alpine.sonata.enc.api.KeyModeDatabaseRequest;
import ch.alpine.sonata.enc.api.TempoDatabaseRequest;
import ch.alpine.sonata.enc.st.Stage2Ornaments;
import ch.alpine.sonata.io.MusedataNoteFormat;
import ch.alpine.sonata.scr.Score;
import ch.alpine.sonata.scr.ScoreBuffer;
import ch.alpine.sonata.utl.Stylist;
import ch.alpine.sonata.utl.TimescalePacker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalars;

public class MusedataReader implements ImportScoreFormat, //
    KeyModeDatabaseRequest, TempoDatabaseRequest, DiatonicPrecision {
  // 0/0 encodes C with thru-line, same as $ is S with thru-line
  // 24/24 has something to do with triplets
  @Override
  public Score get(Path file) throws Exception {
    int ticks = 0;
    int line_count = 0;
    boolean launched = false;
    boolean comment = false;
    boolean valid;
    Score score = new Score();
    // FIXME
    // score.keyMode = null;
    score.bpm = null;
    ScoreBuffer scoreBuffer = new ScoreBuffer();
    try (BufferedReader bufferedReader = Files.newBufferedReader(file)) {
      // charset ascii
      String string;
      boolean print_flag = true;
      while (Objects.nonNull(string = bufferedReader.readLine())) {
        if (string.startsWith("/FINE") || string.startsWith("/END"))
          break;
        if (string.startsWith("/CUTOFF")) {
          score.process.append("/cut");
          break;
        }
        comment ^= string.trim().equals("&");
        if (launched) { // has parsed header line
          if (!comment) { // between & ... &
            valid = false;
            if (string.startsWith("back")) {
              ticks -= MusedataMeta.getTicks(string);
              valid = score.division.modMeasure(ticks) == 0;
            } else //
            if (string.startsWith("measure"))
              valid = score.division.modMeasure(ticks) == 0;
            else //
            if (string.startsWith("rest") || string.startsWith("irest")) {
              valid = true;
              ticks += MusedataMeta.getTicks(string);
            } else //
            if (0 < string.length()) {
              final byte myByte = (byte) string.charAt(0);
              if ('A' <= myByte && myByte <= 'G') { // A B ... G (H is not used in musedata)
                valid = true;
                Note note = MusedataNoteFormat.INSTANCE.parseNote(string.substring(0, 8));
                int voice = string.charAt(14) - 48;
                if (scoreBuffer.isFreeAt(ticks, voice))
                  try {
                    scoreBuffer.put(note, string.charAt(8) == '-', ticks, voice);
                  } catch (Exception exception) {
                    try {
                      /** when ties connect notes from different voices they have to be dropped */
                      scoreBuffer.untie(voice);
                      scoreBuffer.put(note, string.charAt(8) == '-', ticks, voice);
                      score.appendText(ticks, "tie miss", true);
                    } catch (Exception myException2) {
                      System.out.println("problem in line=" + line_count + " " + string + " " + exception.getMessage());
                      throw new RuntimeException(exception.getMessage());
                    }
                  }
                else {
                  System.out.println("problem in line=" + line_count + " " + string + " not free!");
                  throw new RuntimeException("note overlap in voice=" + voice);
                }
                try {
                  Ornament ornament = Stage2Ornaments.getFromLine(string);
                  if (Objects.nonNull(ornament))
                    scoreBuffer.getVoice(voice).shake.put(ticks, ornament);
                } catch (Exception exception) {
                  if (print_flag) {
                    System.out.println(file);
                    System.out.println(score.title);
                    print_flag = false;
                  }
                  System.out.println(string + " [" + exception.getMessage() + "][len=" + string.length() + " / in=" + (line_count + 1) + "]");
                }
                // ---
                ticks += MusedataMeta.getTicks(string);
              } else {
                switch (myByte) {
                case '$': // Musical Attributes
                  // $ D:Presto
                  // $ C2:22
                  valid = true;
                  break;
                case 'P': // Print Suggestion
                  valid = true;
                  break;
                case 'S': // Sound
                  valid = true;
                  break;
                case 'g': // Grace Note
                  // gEf5 6 1 u1 (
                  // D5 12- 1 q u1 -)
                  valid = true;
                  break;
                default:
                  break;
                }
              }
            }
            if (!valid)
              System.out.println(file + String.format(" [%4d] ", line_count) + string);
          } else
            System.out.println(" " + string);
        } else {
          if (string.startsWith("$")) {
            launched = true;
            StringTokenizer stringTokenizer = new StringTokenizer(string);
            while (stringTokenizer.hasMoreTokens()) {
              final String token = stringTokenizer.nextToken();
              switch (token.charAt(0)) {
              case 'K':
                score.keySignature = KeySignature.fromType(Integer.parseInt(token.substring(2)));
                break;
              case 'Q':
                score.quarter = Integer.parseInt(token.substring(2));
                break;
              case 'T':
                String ratio = MusedataMeta.getSafeRatio(token.substring(2));
                int measure = Scalars.intValueExact(Scalars.fromString(ratio).multiply(RealScalar.of(score.quarter * 4)));
                score.division = Divisions.best(score.quarter, measure, ratio);
                break;
              }
            }
          }
          MusedataMeta.handleLine(score, line_count, string);
        }
        ++line_count;
      }
    }
    score.voices.addAll(scoreBuffer.getVoices()); // add all voices
    new TimescalePacker(score, true).pack();
    Stylist.defaultStyle(score);
    return score;
  }
}
