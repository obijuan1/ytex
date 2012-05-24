/*
 * Copyright: (c) 2009   Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package ytex.uima.annotators;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A span of text and its offsets within some larger text
 */
public class SentenceSpan {

	public static String LF = "\n";
	public static String CR = "\r";
	public static String CRLF = "\r\n";
	// private static Pattern periodPattern;
	// private static Pattern splitPattern;
	private static final Logger log = Logger.getLogger(SentenceSpan.class
			.getName());
	// /**
	// * load split pattern from ytex.properties
	// */
	// static {
	// String strPeriodPattern =
	// "(?m)[^Dr|Ms|Mr|Mrs|Ms|\\p{Upper}]\\.\\s+\\p{Upper}";
	// // String strSplitPattern =
	// //
	// "(?im)\\n[\\(\\[]\\s*[yesxno]{0,3}\\s*[\\)\\]]|[\\(\\[]\\s*[yesxno]{0,3}\\s*[\\)\\]]\\s*\\r{0,1}\\n|\\r{0,1}\\n{0,1}[^:\\r\\n]{3,20}\\:[^\\r\\n]{3,20}\\r{0,1}\\n|\\n\\d{1,2}\\.\\s+";
	// String strSplitPattern =
	// "(?im)\\n[\\(\\[]\\s*[yesxno]{0,3}\\s*[\\)\\]]|[\\(\\[]\\s*[yesxno]{0,3}\\s*[\\)\\]]\\s*\\r{0,1}\\n|^[^:\\r\\n]{3,20}\\:[^\\r\\n]{3,20}$|:\\r{0,1}\\n|\\r{0,1}\\n\\r{0,1}\\n";
	// InputStream ytexPropsIn = null;
	// try {
	// ytexPropsIn = SentenceSpan.class
	// .getResourceAsStream("/ytex.properties");
	// Properties ytexProperties = new Properties();
	// ytexProperties.load(ytexPropsIn);
	// strPeriodPattern = ytexProperties.getProperty(
	// "ytex.sentence.PeriodPattern", strPeriodPattern);
	// strSplitPattern = ytexProperties.getProperty(
	// "ytex.sentence.SplitPattern", strSplitPattern);
	// } catch (Exception e) {
	// // shouldn't happen
	// log.log(Level.WARNING, "error loading ytex.properties", e);
	// } finally {
	// if (ytexPropsIn != null) {
	// try {
	// ytexPropsIn.close();
	// } catch (IOException e) {
	// }
	// }
	// }
	// periodPattern = Pattern.compile(strPeriodPattern);
	// splitPattern = Strings.isNullOrEmpty(strSplitPattern) ? null : Pattern
	// .compile(strSplitPattern);
	// }

	private int start; // offset of text within larger text
	private int end; // offset of end of text within larger text
	private String text;

	public SentenceSpan(int s, int e, String t) {
		start = s;
		end = e;
		text = t;
	}

	/**
	 * Set offset of start of this span within the larger text
	 */
	public void setStart(int in) {
		start = in;
	}

	/**
	 * 
	 * Set offset of end of this span within the larger text
	 */
	public void setEnd(int in) {
		end = in;
	}

	public void setText(String in) {
		text = in;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public String getText() {
		return text;
	}

	// /**
	// * If the span contains </code>splitChar</code>,
	// * create a List of the (sub)spans separated by splitChar, and trimmed.
	// * Otherwise return a List containing just this <code>SentenceSpan</code>,
	// trimmed.
	// * @param splitChar (written to be general, but probably newline)
	// * @return List
	// */
	// public List<SentenceSpan> splitSpan(char splitChar) {
	// ArrayList<SentenceSpan> subspans = new ArrayList<SentenceSpan>();
	// int nlPosition;
	//
	// // nlPosition = text.indexOf(splitChar); //JZ
	// // if (nlPosition < 0) {
	// // subspans.add(this); //JZ: should trim as specified in the JavaDoc
	// // return subspans;
	// // }
	//
	// int subspanStart = 0; //
	// int relativeSpanEnd = end-start;
	// int subspanEnd = -1;
	// int trimmedSubspanEnd = -1;
	//
	// try {
	// while (subspanStart < relativeSpanEnd) {
	// String subString = text.substring(subspanStart, relativeSpanEnd);
	// nlPosition = subString.indexOf(splitChar);
	// if (nlPosition < 0) {
	// subspanEnd = relativeSpanEnd;
	// }
	// else {
	// subspanEnd = nlPosition + subspanStart;
	// }
	// String coveredText = text.substring(subspanStart, subspanEnd);
	// coveredText = coveredText.trim();
	// // old len = (ssend-ssstart)
	// // new len = ct.len
	// // new e = ssstart+newlen
	// trimmedSubspanEnd = subspanStart + coveredText.length();
	// subspans.add(new SentenceSpan(subspanStart+start,
	// trimmedSubspanEnd+start, coveredText));
	// subspanStart = subspanEnd+1; // skip past newline
	// }
	// }
	// catch (java.lang.StringIndexOutOfBoundsException iobe) {
	// System.err.println("splitChar as int = " + (int)splitChar);
	// this.toString();
	// System.err.println("subspanStart = " + subspanStart);
	// System.err.println("relativeSpanEnd = " + relativeSpanEnd);
	// System.err.println("subspanEnd = " + subspanEnd);
	// System.err.println("trimmedSubspanEnd = " + trimmedSubspanEnd);
	// System.err.println("splitChar as int = " + (int)splitChar);
	// iobe.printStackTrace();
	// throw iobe;
	// }
	// return subspans;
	// }

	/**
	 * Trim any leading or trailing whitespace. If there are any end-of-line
	 * characters in what's left, split into multiple smaller sentences, and
	 * trim each. If is entirely whitespace, return an empty list
	 * 
	 * @param separatorPattern
	 *            CR LF or CRLF
	 * @return
	 */
	public List<SentenceSpan> splitAtLineBreaksAndTrim(String separatorPattern) {

		ArrayList<SentenceSpan> subspans = new ArrayList<SentenceSpan>();

		// Validate input parameter
		if (!separatorPattern.equals(LF) && !separatorPattern.equals(CR)
				&& !separatorPattern.equals(CRLF)) {

			int len = separatorPattern.length();
			System.err.println("Invalid line break: " + len
					+ " characters long.");

			System.err.print("        line break character values: ");
			for (int i = 0; i < len; i++) {
				System.err.print(Integer.valueOf(separatorPattern.charAt(i)));
				System.err.print(" "); // print a space between values
			}
			System.err.println();

			// System.err.println("Invalid line break: \\0x" +
			// Byte.parseByte(separatorPattern.getBytes("US-ASCII").toString(),16));
			subspans.add(this);
			return subspans;
		}

		// Check first if contains only whitespace, in which case return an
		// empty list
		String coveredText = text.substring(0, end - start);
		String trimmedText = coveredText.trim();
		int trimmedLen = trimmedText.length();
		if (trimmedLen == 0) {
			return subspans;
		}

		// // Split into multiple sentences if contains end-of-line characters
		// // or return just one sentence if no end-of-line characters are
		// within
		// // the trimmed string
		// String spans[] = coveredText.split(separatorPattern);
		// int position = start;
		// for (String s : spans) {
		// String t = s.trim();
		// if (t.length() > 0) {
		// positionOfNonWhiteSpace = s.indexOf(t.charAt(0));
		// } else {
		// positionOfNonWhiteSpace = 0;
		// }
		// // Might have trimmed off some at the beginning of the sentences
		// // other than the 1st (#0)
		// position += positionOfNonWhiteSpace; // sf Bugs artifact 3083903:
		// // For _each_ sentence,
		// // advance past any spaces
		// // at beginning of line
		// subspans.add(new SentenceSpan(position, position + t.length(), t));
		// position += (s.length() - positionOfNonWhiteSpace + separatorPattern
		// .length());
		// }
		//
		// return subspans;
		// If there is any leading or trailing whitespace, determine position of
		// the trimmed section
		int trimmedStart = start;
		// int trimmedEnd = end;
		int positionOfNonWhiteSpace = 0;
		if (trimmedLen != coveredText.length()) {
			// Use indexOf to skip past the white space.
			// Consider looking through looking characters using
			// Character.isWhiteSpace(ch)
			positionOfNonWhiteSpace = coveredText.indexOf(trimmedText);
			trimmedStart = start + positionOfNonWhiteSpace;
			// trimmedEnd = trimmedStart + trimmedLen;
		}

		// Split into multiple sentences if contains end-of-line characters
		// or return just one sentence if no end-of-line characters are within
		// the trimmed string
		// String spans[] = trimmedText.split(separatorPattern);
		String spans[] = new String[] { trimmedText };
		int position = trimmedStart;
		for (String s : spans) {
			String t = s.trim();
			subspans.add(new SentenceSpan(position, position + t.length(), t));
			position += s.length() + separatorPattern.length();
		}

		return subspans;
	}

	/**
	 * vng added
	 * 
	 * @return
	 */
	public List<SentenceSpan> splitAtPeriodAndTrim(Pattern periodPattern,
			Pattern splitPattern) {
		ArrayList<SentenceSpan> subspans = new ArrayList<SentenceSpan>();
		if (periodPattern == null) {
			// don't split at periods
			subspans.add(this);
		} else {
			// Check first if contains only whitespace, in which case return an
			// empty list
			String coveredText = text.substring(0, end - start);
			String trimmedText = coveredText.trim();
			int trimmedLen = trimmedText.length();
			if (trimmedLen == 0) {
				return subspans;
			}

			// If there is any leading or trailing whitespace, determine
			// position of
			// the trimmed section
			int trimmedStart = start;
			// int trimmedEnd = end;
			int positionOfNonWhiteSpace = 0;
			if (trimmedLen != coveredText.length()) {
				// Use indexOf to skip past the white space.
				// Consider looking through looking characters using
				// Character.isWhiteSpace(ch)
				positionOfNonWhiteSpace = coveredText.indexOf(trimmedText);
				trimmedStart = start + positionOfNonWhiteSpace;
				// trimmedEnd = trimmedStart + trimmedLen;
			}

			// Split into multiple sentences if contains end-of-line characters
			// or return just one sentence if no end-of-line characters are
			// within
			// the trimmed string
			String spans[] = periodPattern.split(trimmedText);
			int position = trimmedStart;
			Matcher matcher = periodPattern.matcher(trimmedText);
			int currentStartPos = 0;
			while (matcher.find()) {
				// matcher.start() + 1 because we want to include the "."
				String t = trimmedText.substring(currentStartPos,
						matcher.start() + 1);
				subspans.add(new SentenceSpan(position + currentStartPos,
						position + currentStartPos + t.length() + 1, t));
				// matcher.end() - 1 because we want to include the 1st letter
				// of
				// the sentence
				currentStartPos += (matcher.end() - currentStartPos - 1);
			}
			if (currentStartPos < trimmedText.length()) {
				String t = trimmedText.substring(currentStartPos);
				subspans.add(new SentenceSpan(position + currentStartPos,
						position + currentStartPos + t.length(), t));
			}
		}
		return splitSubspans(subspans, splitPattern);
	}

	/**
	 * vng added
	 * 
	 * @return
	 */
	public List<SentenceSpan> splitSubspans(List<SentenceSpan> subspans,
			Pattern splitPattern) {
		List<SentenceSpan> splitSubspans = new ArrayList<SentenceSpan>();
		if (splitPattern == null) {
			splitSubspans.addAll(subspans);
		} else {
			// Split into multiple sentences if contains end-of-line characters
			// or return just one sentence if no end-of-line characters are
			// within
			// the trimmed string
			for (SentenceSpan span : subspans) {
				String trimmedText = span.getText();
				boolean bSplit = false;
				Matcher matcher = splitPattern.matcher(trimmedText);
				int position = span.getStart();
				int currentStartPos = 0;
				while (matcher.find()) {
					bSplit = true;
					if (matcher.start() > currentStartPos) {
						String t = trimmedText.substring(currentStartPos,
								matcher.start());
						splitSubspans.add(new SentenceSpan(position
								+ currentStartPos, position + currentStartPos
								+ t.length(), t));
						currentStartPos += t.length();
					}
				}
				if (bSplit) {
					if (currentStartPos < trimmedText.length()) {
						String t = trimmedText.substring(currentStartPos);
						splitSubspans.add(new SentenceSpan(position
								+ currentStartPos, position + currentStartPos
								+ t.length(), t));
					}
				} else
					splitSubspans.add(span);
			}
		}
		return splitSubspans;
	}

	public String toString() {
		String s = "(" + start + ", " + end + ") " + text;
		return s;
	}

}
