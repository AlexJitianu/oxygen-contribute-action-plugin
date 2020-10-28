package com.oxygenxml.actions;

import java.util.StringTokenizer;

import ro.sync.basic.util.PlatformDetector;

public class KeystrokeUtil {
  /**
   * Convert a platform independent representation to a Java KeyStroke .
   * 
   * The "M" modifier keys are a platform-independent way of representing keys, 
   * and these are generally preferred. 
   * - M1 is the COMMAND key on MacOS X, and the CTRL key on most other platforms. 
   * - M2 is the SHIFT key. 
   * - M3 is the Option key on MacOS X, and the ALT key on most other platforms. 
   * - M4 is the CTRL key on MacOS X, and is undefined on other platforms. 
   * 
   * @param accelerator An platform independent representation that uses M1, M2, M3, M4 as modifiers. 
   * Space is considered the used separator. 
   * @param delimiter The delimiter used to separate tokens.
   * 
   * @return An Java Keystroke string representation.
   */
  public static String convertToPlatformDependentKeystroke(String accelerator, String delimiter) {
    String platfDep = null;
    boolean error = false;

    if (accelerator != null) {
      StringBuilder buf = new StringBuilder();
      StringTokenizer st = new StringTokenizer(accelerator, delimiter);
      while (st.hasMoreTokens()) {
        String nextToken = st.nextToken();

        if (buf.length() > 0) {
          buf.append(delimiter);
        }

        if ("M1".equals(nextToken)) {
          if (PlatformDetector.isMacOS()) {
            if(PlatformDetector.isEclipse()){
              //EXM-29609 On EC the meta key is called command.
              buf.append("command");
            } else {
              buf.append("meta");
            }
          } else {
            buf.append("ctrl");
          }
        } else if ("M2".equals(nextToken)) {
          buf.append("shift");
        } else if ("M3".equals(nextToken)) {
          buf.append("alt");
        } else if ("M4".equals(nextToken)) {
          if (PlatformDetector.isMacOS()) {
            buf.append("ctrl");
          } else {
            // On windows we can't map the M4.
            error = true;
            break;
          }
        } else {
          buf.append(nextToken);
        }
      }
      
      platfDep = buf.toString();
    }

    return error ? null : platfDep;
  }
}
