/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is the Text Trix code.
 *
 * The Initial Developer of the Original Code is
 * Text Flex.
 * Portions created by the Initial Developer are Copyright (C) 2003-4
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): David Young <dvd@textflex.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package com.textflex.texttrix;

import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

/** Plug-in to search documents for specific sequences of text and replace 
    them with alternatives if desired.  The plug-in can search for simple
    sequences or whole words, whether throughout the document, from the
    cursor on, or only within the highlighted selection.  Additionally, the plug-in
    can generate statistical information for either the whole body of text or a 
    specific section of it.

    <p>This search tool creates a dialog window for the user to enter
    the text to find and the text to replace it.  The window
    also allows the user to choose specific options to tailor the
    search before pressing either the "Find" or "Find and replace" button.
    Pressing either button tells <code>TextTrix</code> to run the plug-in
    on the currently selected tab.  The plug-in returns an object containing
    the potentially modified text as well as any positions to highlight.
*/
public class Search extends PlugInWindow {
	private FindDialog diag = null; // the dialog window for user options
	// whether to run find() or replace() after pressing a button,
	// which causes TextTrix to lauch the plug-in's single runPlugIn() command
	private boolean invokeReplace = false;
	private boolean stats = false;

	/** Creates the search plug-in.
	Sets <code>ignoreSelection</code> to <code>false</code> so that
	<code>TextTrix</code> sends the entire body of text.  The plug-in
	chooses which sections on which to work according to user-set
	options.  The constructor also creates the <code>Action</code>s
	and <code>Listener</code>s
	to place in the dialog window so that the plug-in can directly
	listen for user commands to run the search tool.
	*/
	public Search() {
		super(
			"Search and Stats",
			"tools",
			"Finds and replaces words or phrases in the document",
			"desc.html",
			"icon.png",
			"icon-roll.png");
		setAlwaysEntireText(true); // retrieve the entire body of text

		// Runs the search tool in "find" mode if the user hits "Enter" in 
		// the "Find" box;
		KeyAdapter findEnter = new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
					setAllRuns(false);
					runPlugIn();
				}
			}
		};

		// Runs the search tool in "replace" mode if the user hits "Enter" in 
		// the "Find and Replace" box;
		KeyAdapter replaceEnter = new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
					// flag runPlugIn() to run in "replace" mode
					setAllRuns(false);
					invokeReplace = true;
					runPlugIn();
				}
			}
		};

		// Runs the search tool in "find" mode if the user hits the "Find"
		// button;
		// creates a shortcut key (alt-F) as an alternative way to invoke
		// the button
		Action findAction = new AbstractAction("Find", null) {
			public void actionPerformed(ActionEvent e) {
				setAllRuns(false);
				runPlugIn();
			}
		};
		LibTTx.setAcceleratedAction(
			findAction,
			"Find",
			'F',
			KeyStroke.getKeyStroke("alt F"));

		// Runs the search tool in "replace" mode if the user hits the 
		// "Find and Replace" button;
		// creates a shortcut key (alt-R) as an alternative way to invoke
		// the button
		Action replaceAction = new AbstractAction("Find and Replace", null) {
			public void actionPerformed(ActionEvent e) {
				setAllRuns(false);
				invokeReplace = true;
				// flag runPlugIn() to run in "replace" mode
				runPlugIn();
			}
		};
		LibTTx.setAcceleratedAction(
			replaceAction,
			"Statistics",
			'R',
			KeyStroke.getKeyStroke("alt R"));

		// Runs the search tool in "stats" mode if the user hits the 
		// "Statistics" button;
		// creates a shortcut key (alt-S) as an alternative way to invoke
		// the button
		Action statsAction = new AbstractAction("Statistics", null) {
			public void actionPerformed(ActionEvent e) {
				setAllRuns(false);
				stats = true; // flag runPlugIn() to run in "replace" mode
				runPlugIn();
			}
		};
		LibTTx.setAcceleratedAction(
			statsAction,
			"Statistics",
			'S',
			KeyStroke.getKeyStroke("alt S"));

		// Creates the options dialog window
		diag =
			new FindDialog(
				findEnter,
				replaceEnter,
				findAction,
				replaceAction,
				statsAction);
		setWindow(diag);
		//setPanel(diag);
	}

	/** Sets all run-time flags to the given boolean value.
	 * For example, both <code>invokeReplace</code> and <code>stats</code> become
	 * <code>b</code>
	 * @param b boolean value for the flags to become
	 */
	public void setAllRuns(boolean b) {
		invokeReplace = b;
		stats = b;
	}

	/** Gets the normal icon.
	@return normal icon
	*/
	public ImageIcon getIcon() {
		ImageIcon pic = getIcon(getIconPath());
		// getIcon(string) created the icon and retreived a copy of it;
		// now the icon can be set to display in the options window;
		// can't get the icon in the plug-in's constructor b/c the plug-in does not yet
		// know its own path
		diag.setIconImage(pic);
		return pic;
	}

	/** Gets the rollover icon.
	@return rollover icon
	*/
	public ImageIcon getRollIcon() {
		return getRollIcon(getRollIconPath());
	}

	/** Gets the detailed, HTML-formatted description.
	For display as a tool tip.
	@return a buffered reader for the description file
	*/
	public BufferedReader getDetailedDescription() {
		return super.getDetailedDescription(getDetailedDescriptionPath());
	}

	/** Front-end to the <code>run(String, int, int)</code> method,
	assuming that <code>Search</code> will work on the entire body
	of text.
	@param s text to search; all of it will be searched
	@return the modified text and positions to highlight
	*
	public PlugInOutcome run(String s, int caretPosition) {
		return run(s, caretPosition, caretPosition);
	}
	*/

	public PlugInOutcome run(String s) {
		return run(s, 0, 0);
	}

	/** Runs the search tool in the appropriate mode, whether "find" 
	or "replace".
	In "find" mode, the tool searches from the cursor position or at the
	start of the selected section.  If "Wrap" is checked, the search 
	function will wrap
	around to start searching again from the beginning of the text if
	the search sequence has not been found yet.  Choosing the
	"Selected Area Only" confines the search to the selected section.
	"Replace" mode mimics the searching in Find mode" while also 
	replacing the search sequence with a replacement sequence.
	If text has been selected, the Replace function replaces that text
	if it equals the string in the "Replace" box.  If not, Find mode kicks
	in to highlight the string, if found.  Running the Replace function
	again would perform the substitution.  If "Replace All" is chosen,
	the function will replace every instance of the string a) in the 
	selected area if "Selected Area" is checked, b) in the entire
	body of text if "Wrap" is checked, or c) from the cursor or
	start of the selected section if neither box is checked.
	@param s text to search
	@param x start position
	@param y end position, non-inclusive
	@return the modified text and positions to highlight
	*/
	public PlugInOutcome run(String s, int x, int y) {
		// the critical variable to keep in default setting to prevent
		// TextTrix from highlighting when there's nothing to highlight
		int selectionStart = -1;
		int selectionEnd = -1;
		// the string to return; s should be the entire body of text
		String newstr = s;
		// flags whether to employ the find function
		boolean find = false;
		String selectedText = "";
		
		// reset results lable
		diag.setResultsLbl("");
		
		//System.out.println("selected text: " + s.substring(x, y));
		// Acts according to whether the plug-in is set to the 
		// "find" or "replace" modes;
		// only replace if text to replace is already highlighted or
		// replace-all option chosen
		if (invokeReplace && diag.getReplaceAll()) {
			// Replace mode, replace-all
			
			// Check the area in which to replace every instance of
			// the quarry; 
			// by default, replace only within the selected section
			if (diag.getWrap()) {
				// replaces every instance throughout the entire
				// body of text
				x = 0;
				y = s.length();
			} else if (!diag.getSelection()) { 
				// replace within highlighted text only
				y = s.length();
			}
			
			// replace the quarry in the given section of text
			newstr =
				replace(
					s,
					diag.getFindText(),
					diag.getReplaceText(),
					x,
					y,
					diag.getWord(),
					diag.getIgnoreCase());
					
		} else if (invokeReplace 
			&& x != y
			&& ((selectedText = s.substring(x, y)).equalsIgnoreCase(diag.getFindText())
					&& diag.getIgnoreCase())
				|| selectedText.equals(diag.getFindText())) {
			// replaces single instance of quarry, only if already highlighted;
			// otherwise, defaults to find mode to highlight the quarry
			String[] results = new String[] {
				"Replaced " + selectedText + " with " 
					+ diag.getReplaceText() + " once.",
				"Boys and girls, Mr. " + diag.getReplaceText() 
					+ " will be your substitute teacher today.",
				selectedText + ", you're fired!",
				diag.getReplaceText() + ", you're hired!"
			};
			displayResults(results, 4);
			return new PlugInOutcome(
				diag.getReplaceText(), 
				selectionStart, 
				selectionEnd,
				x,
				y);
			//newstr = s.substring(0, x) + diag.getReplaceText() + s.substring(y);
			
		} else if (stats) { // "stats" mode
			// Assume only working from caret position or start of selected 
			// sectiononward
			int start = x;
			int end = s.length();
			
			// Passes only selected text if flagged to do so
			if (diag.getSelection()) { // highlighted text only
				//System.out.println("start: " + x + ", end: " + y);
				selectionStart = start = x;
				selectionEnd = end = y;
			} else if (diag.getWrap()) {
				// entire text
				start = 0;
			}
			
			// gathers the statistics
			//System.out.println("charCount: " + charCount(start, end));
			diag.setCharCountLbl(charCount(start, end) + "");
			diag.setWordCountLbl(wordCount(s, start, end) + "");
			diag.setLineCountLbl(lineCount(s, start, end) + "");
			
		} else {
			// defaults to Find mode
			find = true;
		}
		
		// Find mode
		if (find) {
			String findText = diag.getFindText();
			// as in "replace" mode, "find" mode confines its search to 
			// highlighted text only if the Selection option is checked;
			// if not, "find" starts searching from the start of any 
			// highlighted portion unless it already equals the text to find,
			// often the case when the user scans through repeated words;
			// instead, the function advances its start position by one character
			if (diag.getSelection()) {
				// check within the selected text only
				selectionStart =
					find(
						s,
						findText,
						x,
						y,
						diag.getWord(),
						diag.getIgnoreCase());
						
			} else {
				// check from the cursor or the start of selected region, or one
				// char past the start if the region already highlights the text,
				// to find the quarry
				
				// advance start by one char if the selected region already
				// highlights the quarry
				String selection = s.substring(x, y);
				if (selection.equals(findText)
					|| diag.getIgnoreCase() && selection.equalsIgnoreCase(findText)) x++;
				
				// find the quarry
				selectionStart =
					find(s, findText, x, diag.getWord(), diag.getIgnoreCase());
				// wrap if sequence not yet found and Wrap option
				// checked
				if (selectionStart == -1 && diag.getWrap()) {
					selectionStart =
						find(
							s,
							findText,
							0,
							diag.getWord(),
							diag.getIgnoreCase());
				}
			}
			// determines the ending position to highlight if the
			// sequence has been found
			if (selectionStart != -1)
				selectionEnd = selectionStart + findText.length();
		}
		boolean noTextChange = s.equals(newstr);
		// "find" and "stats" mode won't alter the text
		return new PlugInOutcome(
			newstr,
			selectionStart,
			selectionEnd,
			noTextChange);
	}
	
	private void displayResults(String[] results, int weightFront) {
		int n = (int) (results.length * Math.pow(Math.random(), weightFront));
		diag.setResultsLbl(results[n]);
	}
	
	/**Find a the first occurrence of a given sequence in a string.
	 * @param text string to search
	 * @param quarry sequence to find
	 * @param start index to start searching
	 * @return index of the sequence's start in the string; -1 if not found
	 */
	public int findSeq(String text, String quarry, int start, int end) {
		int loc = -1;
		if (start < text.length()) {
			loc = text.indexOf(quarry, start);
			if (loc < end && loc != -1) {
				String[] results = new String[] {
					"Found " + quarry + ".",
					"Eureka!  I found " + quarry + ".",
					"Caught " + quarry + " red-handed, police officer.",
					"Dr. " + quarry + "-stone, I presume?"
				};
				displayResults(results, 4);
				return loc;
			}
		}
		String[] results = new String[] {
			"Sorry, I couldn't find " + quarry + " here.",
			quarry + " has escaped!",
			"Sir, all I'm picking up is static!"
		};
		displayResults(results, 4);
		return -1;
	}

	/**Front-end to the <code>find</code> methods with the assumption that the search
	 * starts from the caret position.
	 * @param text string to search
	 * @param quarry sequence to search for
	 * @param start index to start searching
	 * @param word if true, treat the sequence as a separate word, with only
	 * non-letters/non-digits surrounding it
	 * @param ignoreCase if true, ignore upper/lower case
	 * @return index of sequence's start in the string; -1 if not found
	 */
	public int find(
		String text,
		String quarry,
		int start,
		boolean word,
		boolean ignoreCase) {
		return find(text, quarry, start, text.length(), word, ignoreCase);
	}

	/**Front-end to the <code>findSeq</code> and <code>findWord</code> methods.
	 * Depending on the options given to it, checks for either a 
	 * any occurrence of a given sequence in a string or only when 
	 * the string occurs as a separate word, ie with only non-letter or 
	 * non-digits surrounding it.  Also can choose to ignore upper/lower 
	 * case.
	 * @param text string to search
	 * @param quarry sequence to search for
	 * @param start index to start searching
	 * @param end index to end searching
	 * @param word if true, treat the sequence as a separate word, with only
	 * non-letters/non-digits surrounding it
	 * @param ignoreCase if true, ignore upper/lower case
	 * @return index of sequence's start in the string; -1 if not found
	 */
	public int find(
		String text,
		String quarry,
		int start,
		int end,
		boolean word,
		boolean ignoreCase) {
		// if ignoring the distinction between upper/lower case, pass both "quarry"
		// and the entire text as lower case
		if (ignoreCase) {
			text = text.toLowerCase();
			quarry = quarry.toLowerCase();
		}
		// if only searching for whole words, use findWord(); otherwise, use findSeq()
		return word
			? findWord(text, quarry, start, end)
			: findSeq(text, quarry, start, end);
	}

	/**Find a given expression as a separate word.
	 * Searches through text to find the given expression so long 
	 * as it is surrounded by non-letter, non-digit characters, such 
	 * as spaces, dashes, or quotation marks.
	 * @param text text to search
	 * @param quarry word to find; can contain letter and/or numbers
	 * @param start index at which to start searching
	 * @return int starting index of matching expression; -1 if not found
	 */
	public int findWord(String text, String quarry, int start, int finish) {
		// compare the word with the quarry to see if they match
		//	    System.out.println("n: " + n + ", end: " + end);
		String word = "";
		while (start < finish
			&& !(word = LibTTx.getWord(text, start, finish)).equals("")) {
			if (word.equals(quarry)) {
				String[] results = new String[] {
					"Found the word" + quarry + ".",
					"Eureka!  I found " + quarry + ".",
					"Caught " + quarry + " red-handed, police officer.",
					"Dr. " + quarry + "stone, I presume?"
				};
				displayResults(results, 4);
				return text.indexOf(quarry, start);
			} else {
				start = text.indexOf(word, start) + word.length();
			}
		}
		String[] results = new String[] {
			"Sorry, I couldn't find " + quarry + " here.  Is it part of another word?",
			quarry + " has escaped!",
			"Sir, all I'm picking up is static!"
		};
		displayResults(results, 4);
		return -1;
	}


	/** Finds and replaces all occurences of a given sequence in the
	 * given region.
	 *	Employs options for specific word searching and ignoring upper/
	 * lower case.
	 * @param text string to search
	 * @param quarry sequence to find
	 * @param replacement sequence with which to substitute
	 * @param start index to start searching
	 * @param end index at which to no longer begin another search; 
	 * a search can continue past it, but cannot start once exceeding it
	 * @param word treat the quarry as a separate word, with only 
	 * non-letters/non-digits surrounding it
	 * @param ignoreCase ignore upper/lower case
	 * @return text with appropriate replacements
	*/
	public String replace(
		String text,
		String quarry,
		String replacement,
		int start,
		int end,
		boolean word,
		boolean ignoreCase) {
		
		//System.out.println("x: " + start + ", y: " + end);
		
		StringBuffer s = new StringBuffer(text.length());
		int n = start;
		int prev = -1;
		
		// append unmodified the text preceding the caret
		s.append(text.substring(0, n));
		
		// continue until the reaching text's end or the quarry has
		// not been found
		int count = 0;
		while (n < end && n != -1) {
			prev = n;
			n = find(text, quarry, n, word, ignoreCase);
			
			// replace the quarry if found and starts within the
			// given region
			if (n != -1) {
				// found within the region;
				// append text at least up to the word
				if (n < end) {
					s.append(text.substring(prev, n) + replacement);
					count++;
				} else {
					// found, but not replaced b/c doesn't start
					// within the region; 
					// apend the quarry, too
					s.append(text.substring(prev, n + quarry.length()));
				}
				
				// advance the find position just past the found quarry
				n += quarry.length();
				
			} else {
				// if not found, append the rest of the text unmodified
				s.append(text.substring(prev));
				text = s.toString();
			}
		}
		
		// append the rest of the text if a quarry occurrence extended 
		// beyond the search boundary
		if (n != -1) {
			s.append(text.substring(n));
			text = s.toString();
		}
		
		String[] results = null;
		if (count > 10) {
			results = new String[] {
				"Replaced " + quarry + " " + count + " times.",
				"Mmm mm!  That felt good.  Gobbled up " + count 
					+ " " + quarry + "\'s.",
				"Whew!  " + count + " occurances of " + quarry + ", all replaced"
			};
		} else {
			results = new String[] {
				"Replaced " + quarry + " " + count + " times.",
				count + " replacements, and I'm still hungry.  Got anymore?",
				"Goodbye, " + quarry + " (" + count + "x)"
			};
		}
		displayResults(results, 4);
		return text;
	}
	
	/*
	public String replace(
		String text,
		String quarry,
		String replacement,
		int start,
		int end,
		boolean word,
		boolean all,
		boolean wrap,
		boolean ignoreCase) {
		
		StringBuffer s = new StringBuffer(text.length());
		int n = start;
		int prev;
		// replace all occurrences of the quarry
		if (all) {
			// append unmodified the text preceding the caret
			s.append(text.substring(0, n));
			// continue until the reaching text's end or the quarry has
			// not been found
			while (n < end && n != -1) {
				prev = n;
				n = find(text, quarry, n, word, ignoreCase);
				// replace the quarry if found
				if (n != -1) {
					s.append(text.substring(prev, n) + replacement);
					n += quarry.length();
					// if not found, append the rest of the text unmodified
				} else {
					s.append(text.substring(prev));
					text = s.toString();
				}
			}
			// append the rest of the text if a quarry occurrence extended 
			// beyond the search boundary
			if (n != -1) {
				s.append(text.substring(n));
				text = s.toString();
			}
			// recursively call the method on the beginning portion of the 
			// text if wrap is enabled
			if (wrap) {
				text =
					replace(
						text,
						quarry,
						replacement,
						0,
						start - 1,
						word,
						all,
						false,
						ignoreCase);
			}
			return text;
			// replace first occurrence only
		} else {
			int quarryLoc = -1;
			// stay within given boundary
			if (n < end) {
				quarryLoc = find(text, quarry, n, word, ignoreCase);
				// replace quarry if found
				if (quarryLoc != -1) {
					text =
						(text.substring(0, quarryLoc)
							+ replacement
							+ text.substring(quarryLoc + quarry.length()));
				}
			}
			// if not found, and wrap is enabled, continue from text beginning;
			// "wrap" overrules "selection"
			if (quarryLoc == -1 && wrap) {
				text =
					replace(
						text,
						quarry,
						replacement,
						0,
						start - 1,
						word,
						all,
						false,
						ignoreCase);
			}
			return text;
		}
	}
	*/

	/** Front end for finding and replacing occurences of a given sequence.
	@param text string to search
	@param quarry sequence to find
	@param replacement sequence with which to substitute
	@param word treat the quarry as a separate word, with only 
	non-letters/non-digits surrounding it
	@param all if true, replace all occurrences of the sequence, starting 
	with the current cursor position and continuing through the text's 
	end, though only wrapping around to the start and back to the cursor 
	if <code>wrap</code> is enabled; 
	if false, replace only the first occurrence
	@param wrap if necessary, continue to the beginning of the text and 
	return to the cursor
	@param ignoreCase ignore upper/lower case
	@return text with appropriate replacements
	@see #findReplace(String, String, String, int, int, boolean, boolean,
	boolean, boolean)
	*
	public String replace(
		String text,
		String quarry,
		String replacement,
		int start,
		boolean word,
		boolean ignoreCase) {
		//System.out.println(
		//	"replace search: " + text.substring(start, text.length()));
		return replace(
			text,
			quarry,
			replacement,
			start,
			text.length(),
			word,
			ignoreCase);
	}
	*/

	/** Counts the number of characters betwen two indices, including the first but not
	 * the last index.
	 * @param start first character to count
	 * @param end first charcter to no longer count
	 * @return number of characters
	 */
	public int charCount(int start, int end) {
		//		System.out.println("char count: " + (end - start));
		return end - start;
	}

	/** Counts the number of words between two indices, inluding the first but not
	 * the last index.
	 * If the starting position is in the middle of a word, that word will still be counted.
	 * 
	 * @param s first position to count
	 * @param start first character to start searching for whole words
	 * @param end first character to stop searching for whole words
	 * @return number of whole words
	 */
	public int wordCount(String s, int start, int end) {
		int n = 0;
		String word = "";
		// ensures that the word does not start after "end" and is not empty;
		// no need to check that getWord() has not skipped over non-word characters and 
		// found a word beyond "end" since getWord() returns "" if the word 
		//characters start after the "end"
		while (start < end && !(word = LibTTx.getWord(s, start, end)).equals("")) {
			start = s.indexOf(word, start) + word.length();
			n++;
		}
		return n;
	}

	/** Counts the number of lines.
	 * 
	 * @param s text to search
	 * @param start starting line
	 * @param end ending line
	 * @return number of lines
	 */
	public int lineCount(String s, int start, int end) {
		int n = 1;
		// must have at least one line, which does not terminate in a "\n"
		while ((start = s.indexOf("\n", start)) >= 0 && start < end) {
			start++;
			n++;
		}
		return n;
	}
}

/** Find and replace dialog.
    Creates a dialog box accepting input for search and replacement 
    expressions as well as options to tailor the search.
*/
class FindDialog extends JPanel {//JFrame {
	JLabel tips = null; // offers tips on using the plug-in 
	JLabel findLbl = null; // label for the search field
	JTextField find = null; // search expression input
	JLabel replaceLbl = null; // label for the replacement field
	JTextField replace = null; // replacement expression input
	JCheckBox word = null; // treat the search expression as a separate word
	JCheckBox wrap = null; // search to the bottom and start again from the top
	JCheckBox selection = null; // search only within a highlighted section
	JCheckBox replaceAll = null; // replace all instances of search expression
	JCheckBox ignoreCase = null; // ignore upper/lower case
	JLabel resultsTitleLbl = null;
	JLabel resultsLbl = null;
	JButton findBtn = null; // label for the search button
	JButton replaceBtn = null; // label for the replace button
	JButton statsBtn = null; // label for the stats button
	JLabel charLbl = null; // label for the stats char value
	JLabel wordLbl = null; // label for the stats word value
	JLabel lineLbl = null; // label for the stats line value
	JLabel charCountLbl = null; // the actual character count
	JLabel wordCountLbl = null; // the actual word count
	JLabel lineCountLbl = null; // the actual line count

	/**Construct a find/replace dialog box
	 * @param owner frame to which the dialog box will be attached; 
	 * can be null
	 */
	public FindDialog(
		KeyAdapter findEnter,
		KeyAdapter replaceEnter,
		Action findAction,
		Action replaceAction,
		Action statsAction) {
		//super("Search and Stats");
		super(new GridBagLayout());
		setSize(400, 200);
		//Container contentPane = getContentPane();
		//contentPane.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.CENTER;
		String msg = "";

		// tips display
		String lbl = "Tip: Searches and statistics begin from the cursor or start of selected area";
		tips = new JLabel(lbl);
		LibTTx.addGridBagComponent(
			tips,
			constraints,
			0,
			0,
			3,
			1,
			100,
			0,
			this);//contentPane);

		// search expression input
		findLbl = new JLabel("Find:");
		LibTTx.addGridBagComponent(
			findLbl,
			constraints,
			0,
			1,
			1,
			1,
			100,
			0,
			this);//contentPane);
		find = new JTextField(20);
		LibTTx.addGridBagComponent(
			find,
			constraints,
			1,
			1,
			2,
			1,
			100,
			0,
			this);//contentPane);
		find.addKeyListener(findEnter);

		// replace expression input
		replaceLbl = new JLabel("Replace:");
		LibTTx.addGridBagComponent(
			replaceLbl,
			constraints,
			0,
			2,
			1,
			1,
			100,
			0,
			this);//contentPane);
		replace = new JTextField(20);
		LibTTx.addGridBagComponent(
			replace,
			constraints,
			1,
			2,
			2,
			1,
			100,
			0,
			this);//contentPane);
		replace.addKeyListener(replaceEnter);

		// treat search expression as a separate word
		lbl = "Whole word only";
		word = new JCheckBox(lbl);
		LibTTx.addGridBagComponent(
			word,
			constraints,
			0,
			3,
			1,
			1,
			100,
			0,
			this);//contentPane);
		word.setMnemonic(KeyEvent.VK_W);
		msg = "Searches for the expression as a separate word";
		word.setToolTipText(msg);

		// wrap search through start of text if necessary
		wrap = new JCheckBox("Wrap", true);
		LibTTx.addGridBagComponent(
			wrap,
			constraints,
			2,
			3,
			1,
			1,
			100,
			0,
			this);//contentPane);
		wrap.setMnemonic(KeyEvent.VK_A);
		msg = "Starts searching from the cursor and wraps back to it";
		wrap.setToolTipText(msg);
		wrap.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (wrap.isSelected() && selection.isSelected()) {
					selection.setSelected(false);
				}
			}
		});

		// replace all instances within highlighted section
		lbl = "Selected area only";
		selection = new JCheckBox(lbl);
		LibTTx.addGridBagComponent(
			selection,
			constraints,
			1,
			3,
			1,
			1,
			100,
			0,
			this);//contentPane);
		selection.setMnemonic(KeyEvent.VK_A);
		msg =
			"Searches, replaces text, or generates statistics only within the highlighted section";
		selection.setToolTipText(msg);
		selection.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (wrap.isSelected() && selection.isSelected()) {
					wrap.setSelected(false);
				}
			}
		});

		// replace all instances from cursor to end of text unless 
		// combined with wrap, where replace all instances in whole text
		replaceAll = new JCheckBox("Replace all");
		LibTTx.addGridBagComponent(
			replaceAll,
			constraints,
			0,
			4,
			1,
			1,
			100,
			0,
			this);//contentPane);
		replaceAll.setMnemonic(KeyEvent.VK_L);
		msg = "Replace all instances of the expression";
		replaceAll.setToolTipText(msg);

		// ignore upper/lower case while searching
		ignoreCase = new JCheckBox("Ignore case", true);
		LibTTx.addGridBagComponent(
			ignoreCase,
			constraints,
			1,
			4,
			1,
			1,
			100,
			0,
			this);//contentPane);
		ignoreCase.setMnemonic(KeyEvent.VK_I);
		msg =
			"Searches for both lower and upper case versions of the expression";
		ignoreCase.setToolTipText(msg);
		
		
		resultsTitleLbl = new JLabel("Reults: ");
		LibTTx.addGridBagComponent(
			resultsTitleLbl,
			constraints,
			0,
			5,
			1,
			1,
			100,
			0,
			this);//contentPane);

		resultsLbl = new JLabel("");
		resultsLbl.setHorizontalAlignment(JLabel.RIGHT);
		LibTTx.addGridBagComponent(
			resultsLbl,
			constraints,
			1,
			5,
			2,
			1,
			100,
			0,
			this);//contentPane);

		// fires the "find" action
		findBtn = new JButton(findAction);
		LibTTx.addGridBagComponent(
			findBtn,
			constraints,
			0,
			6,
			1,
			1,
			100,
			0,
			this);//contentPane);

		// find and replace action, using appropriate options above
		replaceBtn = new JButton(replaceAction);
		LibTTx.addGridBagComponent(
			replaceBtn,
			constraints,
			1,
			6,
			1,
			1,
			100,
			0,
			this);//contentPane);

		// fires the "stats" action
		statsBtn = new JButton(statsAction);
		LibTTx.addGridBagComponent(
			statsBtn,
			constraints,
			2,
			6,
			1,
			1,
			100,
			0,
			this);//contentPane);

		// search expression input
		charLbl = new JLabel("Characters:");
		LibTTx.addGridBagComponent(
			charLbl,
			constraints,
			0,
			7,
			2,
			1,
			100,
			0,
			this);//contentPane);
		charCountLbl = new JLabel("");
		charCountLbl.setHorizontalAlignment(JLabel.RIGHT);
		LibTTx.addGridBagComponent(
			charCountLbl,
			constraints,
			2,
			7,
			1,
			1,
			100,
			0,
			this);//contentPane);

		// search expression input
		wordLbl = new JLabel("Words:");
		LibTTx.addGridBagComponent(
			wordLbl,
			constraints,
			0,
			8,
			2,
			1,
			100,
			0,
			this);//contentPane);
		wordCountLbl = new JLabel("");
		wordCountLbl.setHorizontalAlignment(JLabel.RIGHT);
		LibTTx.addGridBagComponent(
			wordCountLbl,
			constraints,
			2,
			8,
			1,
			1,
			100,
			0,
			this);//contentPane);

		// search expression input
		lineLbl = new JLabel("Lines:");
		LibTTx.addGridBagComponent(
			lineLbl,
			constraints,
			0,
			9,
			2,
			1,
			100,
			0,
			this);//contentPane);
		lineCountLbl = new JLabel("");
		lineCountLbl.setHorizontalAlignment(JLabel.RIGHT);
		LibTTx.addGridBagComponent(
			lineCountLbl,
			constraints,
			2,
			9,
			1,
			1,
			100,
			0,
			this);//contentPane);
	}

/*
	public void addWindowAdapter(WindowAdapter adapter) {
		addWindowListener(adapter);
	}
*/
	/** Sets the window's icon.
	 * 
	 * @param pic icon to display
	 */
	public void setIconImage(ImageIcon pic) {
		// can't insert within the constructor in case the plug-in creates the object before
		// receiving the plug-in's path to generate the icon
		//setIconImage(pic.getImage());
	}

	/** Gets the value of the "word" check box.
	 * 
	 * @return value of the <code>word JCheckBox</code>
	 */
	public boolean getWord() {
		return word.isSelected();
	}

	/** Gets the value of the "wrap" check box.
	 * 
	 * @return value of the <code>warp JCheckBox</code>
	 */
	public boolean getWrap() {
		return wrap.isSelected();
	}

	/** Gets the value of the "selection" check box.
	 * 
	 * @return value of the <code>selection JCheckBox</code>
	 */
	public boolean getSelection() {
		return selection.isSelected();
	}

	/** Gets the value of the "replaceAll" check box.
	 * 
	 * @return value of the <code>replaceAll JCheckBox</code>
	 */
	public boolean getReplaceAll() {
		return replaceAll.isSelected();
	}

	/** Gets the value of the "ignoreCase" check box.
	 * 
	 * @return value of the <code>ignoreCase JCheckBox</code>
	 */
	public boolean getIgnoreCase() {
		return ignoreCase.isSelected();
	}

	/** Gets the value in the "find" text field.
	 * 
	 * @return value in the <code>find JFrame</code>
	 */
	public String getFindText() {
		return find.getText();
	}

	/** Gets the value of the "replace" text field.
	 * 
	 * @return value of the <code>replace JCheckBox</code>
	 */
	public String getReplaceText() {
		return replace.getText();
	}

	/** Sets the value of the "charCountLbl" counter.
	 * 
	 * @return value of the <code>charCountLbl JLbl</code>
	 */
	public void setCharCountLbl(String s) {
		charCountLbl.setText(s);
	}

	/** Gets the value of the "wordCountLbl" counter.
	 * 
	 * @return value of the <code>wordCountLbl JLbl</code>
	 */
	public void setWordCountLbl(String s) {
		wordCountLbl.setText(s);
	}

	/** Gets the value of the "lineCountLbl" counter.
	 * 
	 * @return value of the <code>lineCountLbl JLbl</code>
	 */
	public void setLineCountLbl(String s) {
		lineCountLbl.setText(s);
	}
	
	/**Sets the results label.
	 * @param the results summary
	*/
	public void setResultsLbl(String s) {
		resultsLbl.setText(s);
	}

}
