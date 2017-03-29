package com.tanmay.blip.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnicodeUtils {

    private static Pattern utf8CodeUnitPattern = Pattern.compile("(\\\\u00([a-z0-9]{2}))+");

    public static String unescapeUTF8(String input) {
        StringBuffer output = null;
        Matcher m = utf8CodeUnitPattern.matcher(input);
        while (m.find()) {
            String[] utf8CodeUnits = m.group().split("\\\\u00");
            StringBuilder unicodeCharacters = new StringBuilder();

            for (int counter = 0, n = 0, i = 1; i < utf8CodeUnits.length; i++ ) {
                int b = Integer.parseInt(utf8CodeUnits[i], 16);
                switch (counter) {
                    case 0:
                        if (0 <= b && b <= 0x7F) {              // 0xxxxxxx
                            unicodeCharacters.append(Character.toChars(b));
                        } else if (0xC0 <= b && b <= 0xDF) {    // 110xxxxx
                            counter = 1;
                            n = b & 0x1F;
                        } else if (0xE0 <= b && b <= 0xEF) {    // 1110xxxx
                            counter = 2;
                            n = b & 0xF;
                        } else if (0xF0 <= b && b <= 0xF7) {    // 11110xxx
                            counter = 3;
                            n = b & 0x7;
                        } else {
                            return input; //error
                        }
                        break;
                    case 1:
                        if (b < 0x80 || b > 0xBF) {
                            return input; //error
                        }
                        counter--;
                        unicodeCharacters.append(Character.toChars((n << 6) | (b-0x80)));
                        n = 0;
                        break;
                    case 2:
                    case 3:
                        if (b < 0x80 || b > 0xBF) {
                            return input; //error
                        }
                        n = (n << 6) | (b-0x80);
                        counter--;
                        break;
                }
            }

            if (output == null) output = new StringBuffer();
            m.appendReplacement(output, Matcher.quoteReplacement(unicodeCharacters.toString()));
        }

        if (output == null) return input; //no changes

        m.appendTail(output);
        return output.toString();
    }
}
