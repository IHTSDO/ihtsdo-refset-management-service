package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.Random;

/**
 * Generates a random string.
 */
public class RandomString {

  /** The Constant symbols. */
  private static final char[] symbols;

  static {
    StringBuilder tmp = new StringBuilder();
    for (char ch = '0'; ch <= '9'; ++ch)
      tmp.append(ch);
    for (char ch = 'a'; ch <= 'z'; ++ch)
      tmp.append(ch);
    symbols = tmp.toString().toCharArray();
  }

  /** The random. */
  private final Random random = new Random();

  /** The buf. */
  private final char[] buf;

  /**
   * Instantiates a {@link RandomString} from the specified parameters.
   *
   * @param length the length
   */
  public RandomString(int length) {
    if (length < 1)
      throw new IllegalArgumentException("length < 1: " + length);
    buf = new char[length];
  }

  /**
   * Next string.
   *
   * @return the string
   */
  public String nextString() {
    for (int idx = 0; idx < buf.length; ++idx)
      buf[idx] = symbols[random.nextInt(symbols.length)];
    return new String(buf);
  }
}