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
 * Portions created by the Initial Developer are Copyright (C) 2003
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
public class Search extends PlugIn {
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
				if (evt.getKeyCode() == KeyEvent.VK_ENTER)
					setAllRuns(false);
				runPlugIn();
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

		// Runs the search tool in "replace" mode if the user hits the 
		// "Find and Replace" button;
		// creates a shortcut key (alt-R) as an alternative way to invoke
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
	In "find" mode, the tool searches from the current caret position
	or after any highlighted text unless the "Search within selection"
	option box is checked.  The search only searches the text preceding
	the cursor if "Wrap" is checked, which causes the search to wrap
	around to start searching again from the beginning of the text if
	the search sequence has not been found yet.
	"Replace" mode mimics the searching in "find" mode" while also 
	replacing the search sequence with a replacement sequence.
	The "replace" mode also gives the option of finding and replacing
	multiple instances of the search sequence at once.
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
		String newstr = s;

		// Acts according to whether the plug-in is set to the 
		// "find" or "replace" modes
		if (invokeReplace) { // "replace" mode
			// confines the search to within the highlighted section
			// only if the Selection option is checked; 
			// otherwise, searches the entire text from the caret
			// position or the end of the highlighted section forward
			
			//			System.out.println("x: " + x + ", y: " + y);
			
			// Passes only the selected section if flagged to do so
			if (diag.getSelection()) { // highlighted text only
				newstr =
					replace(
						s,
						diag.getFindText(),
						diag.getReplaceText(),
						x,
						y,
						diag.getWord(),
						diag.getReplaceAll(),
						diag.getWrap(),
						diag.getIgnoreCase());
			} else { // from caret onward; replace() determines whether to wrap
				newstr =
					replace(
						s,
						diag.getFindText(),
						diag.getReplaceText(),
						y,
						diag.getWord(),
						diag.getReplaceAll(),
						diag.getWrap(),
						diag.getIgnoreCase());
			}

		} else if (stats) { // "stats" mode
			// Assume only working from caret position onward
			int start = x;
			int end = s.length();
			// Passes only selected text if flagged to do so
			if (diag.getSelection()) { // highlighted text only
				start = x;
				end = y;
			} else if (diag.getWrap()) { // entire text
				start = 0;
			}
			diag.setCharCountLbl(charCount(start, end) + "");
			diag.setWordCountLbl(wordCount(s, start, end) + "");
			diag.setLineCountLbl(lineCount(s, start, end) + "");
		} else { // "find" mode
			String findText = diag.getFindText();
			// as in "replace" mode, "find" mode confines its search to 
			// highlighted text only if the Selection option is checked;
			// if not, "find" must start searching after any highlighted
			// portion in case the user is using the tool for a repeated
			// word, which would highlight the word each time and would
			// force the user to deselect the text otherwise
			if (diag.getSelection()) { // selected text only
				selectionStart =
					find(
						s,
						findText,
						x,
						y,
						diag.getWord(),
						diag.getIgnoreCase());
			} else { // post-caret or post-selected text
				selectionStart =
					find(s, findText, y, diag.getWord(), diag.getIgnoreCase());
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
		boolean noTextChange = s.equals(newstr); // "find" and "stats" mode don't alter the text
		return new PlugInOutcome(newstr, selectionStart, selectionEnd, noTextChange);
	}

	/** Starts the plug-in by displaing the options dialog for users
	to choose their search options, enter the text to search or
	with which to replace, and start the search.
	*/
	public void startPlugIn() {
		diag.show();
	}

	/**Find a the first occurrence of a given sequence in a string.
	 * @param text string to search
	 * @param quarry sequence to find
	 * @param start index to start searching
	 * @return index of the sequence's start in the string; -1 if not found
	 */
	public int findSeq(String text, String quarry, int start, int end) {
		int loc = -1;
		if (start < text.length())
			return (loc = text.indexOf(quarry, start)) >= end ? -1 : loc;
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
			&& !(word = getWord(text, start, finish)).equals("")) {
//			System.out.println("word: " + word);
			if (word.equals(quarry)) {
				return text.indexOf(quarry, start);
			} else {
				start = text.indexOf(word, start) + word.length();
			}
		}
		return -1;
	}
	
	/** Gets the next whole word from a given position, assuming that the position
	 * is not in the middle of a word.
	 * @param text text to search
	 * @param start index at which to start
	 * @param finish first index at which to stop searching, though the word may extend 
	 * to or past this index
	 * @return the whole word
	 */
	public String getWord(String text, int start, int finish) {
		int n = start; // becomes position of start of word
		int end = start + 1; // becomes first character after word
		//int len = end;
		String specialChars = "_\'";
		String word = "";
		// skip over non-letters/non-digits
		char c = 0;
		while (n < finish
			&& !Character.isLetterOrDigit(c = text.charAt(n))
			&& specialChars.indexOf(c) < 0) {
			//		System.out.println("skipped char: " + c);
			n++;
		}
		if (n >= finish)
			return "";
		// progress to the end of a word
		end = n + 1;
		while (end < finish
			&& (Character.isLetterOrDigit(c = text.charAt(end))
				|| specialChars.indexOf(c) >= 0)) {
			//		System.out.println("included char: " + c);
			end++;
		}
		//		}
		return text.substring(n, end);
	}

	/** Find and replace occurences of a given sequence.
	Employs options for specific word searching, replacing all 
	occurrences, wrap around to the text's beginning, and ignoring upper/
	lower case.
	@param text string to search
	@param quarry sequence to find
	@param replacement sequence with which to substitute
	@param start index to start searching
	@param end index at which to no longer begin another search; 
	a search can continue past it, but cannot start once exceeding it
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
	@see #findReplace(String, String, String, boolean, boolean,
	boolean, boolean)
	*/
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
	*/
	public String replace(
		String text,
		String quarry,
		String replacement,
		int start,
		boolean word,
		boolean all,
		boolean wrap,
		boolean ignoreCase) {
		System.out.println(
			"replace search: " + text.substring(start, text.length()));
		return replace(
			text,
			quarry,
			replacement,
			start,
			text.length(),
			word,
			all,
			wrap,
			ignoreCase);
	}

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
		while (start < end && !(word = getWord(s, start, end)).equals("")) {
			start = s.indexOf(word, start) + word.length();
//			System.out.println("word: " + word);
			n++;
		}
//		System.out.println("word count: " + n);
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
//		System.out.println("line count: " + n);
		return n;
	}
}

/** Find and replace dialog.
    Creates a dialog box accepting input for search and replacement 
    expressions as well as options to tailor the search.
*/
class FindDialog extends JFrame {
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
		super("Search and Stats");
		setSize(400, 200);
		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.CENTER;
		String msg = "";

		// tips display
		String lbl = "Tip: All searches and statistics start from the caret";
		tips = new JLabel(lbl);
		LibTTx.addGridBagComponent(tips, constraints, 0, 0, 3, 1, 100, 0, contentPane);

		// search expression input
		findLbl = new JLabel("Find:");
		LibTTx.addGridBagComponent(findLbl, constraints, 0, 1, 1, 1, 100, 0, contentPane);
		find = new JTextField(20);
		LibTTx.addGridBagComponent(find, constraints, 1, 1, 2, 1, 100, 0, contentPane);
		find.addKeyListener(findEnter);

		// replace expression input
		replaceLbl = new JLabel("Replace:");
		LibTTx.addGridBagComponent(replaceLbl, constraints, 0, 2, 1, 1, 100, 0, contentPane);
		replace = new JTextField(20);
		LibTTx.addGridBagComponent(replace, constraints, 1, 2, 2, 1, 100, 0, contentPane);
		replace.addKeyListener(replaceEnter);

		// treat search expression as a separate word
		lbl = "Whole word only";
		word = new JCheckBox(lbl);
		LibTTx.addGridBagComponent(word, constraints, 0, 3, 1, 1, 100, 0, contentPane);
		word.setMnemonic(KeyEvent.VK_W);
		msg = "Searches for the expression as a separate word";
		word.setToolTipText(msg);

		// wrap search through start of text if necessary
		wrap = new JCheckBox("Wrap");
		LibTTx.addGridBagComponent(wrap, constraints, 2, 3, 1, 1, 100, 0, contentPane);
		wrap.setMnemonic(KeyEvent.VK_A);
		msg = "Starts searching from the cursor and wraps back to it";
		wrap.setToolTipText(msg);

		// replace all instances within highlighted section
		lbl = "Selected area only";
		selection = new JCheckBox(lbl);
		LibTTx.addGridBagComponent(selection, constraints, 1, 3, 1, 1, 100, 0, contentPane);
		selection.setMnemonic(KeyEvent.VK_A);
		msg =
			"Searches, replaces text, or generates statistics only within the highlighted section";
		selection.setToolTipText(msg);

		// replace all instances from cursor to end of text unless 
		// combined with wrap, where replace all instances in whole text
		replaceAll = new JCheckBox("Replace all");
		LibTTx.addGridBagComponent(replaceAll,	constraints, 0, 4, 1, 1, 100, 0, contentPane);
		replaceAll.setMnemonic(KeyEvent.VK_L);
		msg = "Replace all instances of the expression";
		replaceAll.setToolTipText(msg);

		// ignore upper/lower case while searching
		ignoreCase = new JCheckBox("Ignore case");
		LibTTx.addGridBagComponent(ignoreCase, constraints, 1, 4, 1, 1, 100, 0, contentPane);
		ignoreCase.setMnemonic(KeyEvent.VK_I);
		msg =
			"Searches for both lower and upper case versions of the expression";
		ignoreCase.setToolTipText(msg);
		
		// fires the "find" action
		findBtn = new JButton(findAction);
		LibTTx.addGridBagComponent(findBtn, constraints, 0, 5, 1, 1, 100, 0, contentPane);

		// find and replace action, using appropriate options above
		replaceBtn = new JButton(replaceAction);
		LibTTx.addGridBagComponent(replaceBtn, constraints, 1, 5, 1, 1, 100, 0, contentPane);
		
		// fires the "stats" action
		statsBtn = new JButton(statsAction);
		LibTTx.addGridBagComponent(statsBtn, constraints, 2, 5, 1, 1, 100, 0, contentPane);

		// search expression input
		charLbl = new JLabel("Characters:");
		LibTTx.addGridBagComponent(charLbl, constraints, 0, 6, 2, 1, 100, 0, contentPane);
		charCountLbl = new JLabel("");
		LibTTx.addGridBagComponent(charCountLbl, constraints, 2, 6, 1, 1, 100, 0, contentPane);

		// search expression input
		wordLbl = new JLabel("Words:");
		LibTTx.addGridBagComponent(wordLbl, constraints, 0, 7, 2, 1, 100, 0, contentPane);
		wordCountLbl = new JLabel("");
		LibTTx.addGridBagComponent(wordCountLbl, constraints, 2, 7, 1, 1, 100, 0, contentPane);

		// search expression input
		lineLbl = new JLabel("Lines:");
		LibTTx.addGridBagComponent(lineLbl, constraints, 0, 8, 2, 1, 100, 0, contentPane);
		lineCountLbl = new JLabel("");
		LibTTx.addGridBagComponent(lineCountLbl, constraints, 2, 8, 1, 1, 100, 0, contentPane);
	}
	
	/** Sets the window's icon.
	 * 
	 * @param pic icon to display
	 */
	public void setIconImage(ImageIcon pic) {
		// can't insert within the constructor in case the plug-in creates the object before
		// receiving the plug-in's path to generate the icon
		setIconImage(pic.getImage());
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

}
