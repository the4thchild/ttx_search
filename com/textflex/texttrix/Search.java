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
    cursor on, or only within the highlighted selection.

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
			"Search",
			"tools",
			"Finds and replaces words or phrases in the document",
			"desc.html",
			"icon.png",
			"icon-roll.png");
		setAlwaysEntireText(true); // retrieve the entire body of text

		// Runs the search tool in "find" mode if the user hits "Enter" in 
		// the "Find" box;
		// ASSUMES: invokeReplace == false
		KeyAdapter findEnter = new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER)
					setAllRuns(false);
				runPlugIn();
				//			find();
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
					//			findReplace();
				}
			}
		};

		// Runs the search tool in "find" mode if the user hits the "Find"
		// button;
		// creates a shortcut key (alt-F) as an alternative way to invoke
		// the button
		// ASSUMES: invokeReplace == false
		Action findAction = new AbstractAction("Find", null) {
			public void actionPerformed(ActionEvent e) {
				setAllRuns(false);
				runPlugIn();
				//		    find();
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
				//			findReplace();
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
				//			findReplace();
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

	public void setAllRuns(boolean b) {
		invokeReplace = b;
		stats = b;
	}

	/** Gets the normal icon.
	@return normal icon
	*/
	public ImageIcon getIcon() {
		return super.getIcon(getIconPath());
	}

	/** Gets the rollover icon.
	@return rollover icon
	*/
	public ImageIcon getRollIcon() {
		return super.getIcon(getRollIconPath());
	}

	/** Gets the detailed, HTML-formatted description.
	For display as a tool tip.
	@return a buffered reader for the description file
	*/
	public BufferedReader getDetailedDescription() {
		return super.getDetailedDescription(getDetailedDescriptionPath());
	}

	/*    
	public static void invokeRun() {
	runPlugIn();
	}
	*/

	/** Front-end to the <code>run(String, int, int)</code> method,
	assuming that <code>Search</code> will work on the entire body
	of text.
	@param s text to search; all of it will be searched
	@return the modified text and positions to highlight
	*/
	public PlugInOutcome run(String s, int caretPosition) {
		return run(s, caretPosition, caretPosition);
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
		//	find.setText(s);
		// the critical variable to keep in default setting to prevent
		// TextTrix from highlighting when there's nothing to highlight
		int selectionStart = -1;
		int selectionEnd = -1;
		boolean noTextChange = false;
		String newstr = s;

		// Acts according to whether the plug-in is set to the 
		// "find" or "replace" modes
		if (invokeReplace) { // "replace" mode
			//	    selectionStart = x;
			// confines the search to within the highlighted section
			// only if the Selection option is checked; 
			// otherwise, searches the entire text from the caret
			// position or the end of the highlighted section forward
			//			selectionEnd = diag.getSelection() ? y : s.length();
			//			System.out.println("x: " + x + ", y: " + y);
			if (diag.getSelection()) {
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
			} else {
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

		} else if (stats) {
			int start = x;
			int end = s.length();
			if (diag.getSelection()) {
				start = x;
				end = y;
			} else if (diag.getWrap()) {
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
		//		invokeReplace = false; // reset the dialog box
		noTextChange = s.equals(newstr);
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
	 *
	public int find(String text, String quarry, int start) {
	if (start < text.length())
	    return text.indexOf(quarry, start);
	return -1;
	}
	*/

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
		if (ignoreCase) {
			text = text.toLowerCase();
			quarry = quarry.toLowerCase();
		}
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
			System.out.println("word: " + word);
			if (word.equals(quarry)) {
				return text.indexOf(quarry, start);
			} else {
				start = text.indexOf(word, start) + word.length();
			}
		}
		/*
		if (end <= finish
			&& text.substring(n, end).equals(quarry)
			&& n < finish) {
			return n;
			// continue search with next word if no match yet
		} else {
			n = end;
			//		end++;
		}
		}
		*/
		return -1;
	}

	public String getWord(String text, int start, int finish) {
		int n = start; // becomes position of start of word
		int end = start + 1; // becomes first character after word
		//int len = end;
		String specialChars = "_\'";
		String word = "";
		//	System.out.println(text);
		//		while (n < finish) {
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

	public int charCount(int start, int end) {
		System.out.println("char count: " + (end - start));
		return end - start;
	}

	public int wordCount(String s, int start, int end) {
		int n = 0;
		String word = "";
		while (start < end && !(word = getWord(s, start, end)).equals("")) {
			start = s.indexOf(word, start) + word.length();
			System.out.println("word: " + word);
			n++;
		}
		System.out.println("word count: " + n);
		return n;
	}

	public int lineCount(String s, int start, int end) {
		int n = 1;
		// must have at least one line, which does not terminate in a "\n"
		while ((start = s.indexOf("\n", start)) >= 0 && start < end) {
			start++;
			n++;
		}
		System.out.println("line count: " + n);
		return n;
	}
}

/** Find and replace dialog.
    Creates a dialog box accepting input for search and replacement 
    expressions as well as options to tailor the search.
*/
class FindDialog extends JDialog {
	JLabel tips = null; // offers tips on using the plug-in 
	JLabel findLbl = null;
	JTextField find = null; // search expression input
	JLabel replaceLbl = null;
	JTextField replace = null; // replacement expression input
	JCheckBox word = null; // treat the search expression as a separate word
	JCheckBox wrap = null; // search to the bottom and start again from the top
	JCheckBox selection = null; // search only within a highlighted section
	JCheckBox replaceAll = null; // replace all instances of search expression
	JCheckBox ignoreCase = null; // ignore upper/lower case
	JButton findBtn = null;
	JButton replaceBtn = null;
	JButton statsBtn = null;
	JLabel charLbl = null;
	JLabel wordLbl = null;
	JLabel lineLbl = null;
	JLabel charCountLbl = null;
	JLabel wordCountLbl = null;
	JLabel lineCountLbl = null;
	/*
	  String text = null;
	  int selectionStart = -1;
	  int selectionEnd = -1;
	*
	
	public void setInvokeReplace(boolean aInvokeReplace) { 
	invokeReplace = aInvokeReplace;
	}
	*/

	public boolean getWord() {
		return word.isSelected();
	}
	public boolean getWrap() {
		return wrap.isSelected();
	}
	public boolean getSelection() {
		return selection.isSelected();
	}
	public boolean getReplaceAll() {
		return replaceAll.isSelected();
	}
	public boolean getIgnoreCase() {
		return ignoreCase.isSelected();
	}
	public String getFindText() {
		return find.getText();
	}
	public String getReplaceText() {
		return replace.getText();
	}

	public void setCharCountLbl(String s) {
		charCountLbl.setText(s);
	}
	public void setWordCountLbl(String s) {
		wordCountLbl.setText(s);
	}
	public void setLineCountLbl(String s) {
		lineCountLbl.setText(s);
	}

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
		super();
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
		
		findBtn = new JButton(findAction);
		LibTTx.addGridBagComponent(findBtn, constraints, 0, 5, 1, 1, 100, 0, contentPane);

		// find and replace action, using appropriate options above
		replaceBtn = new JButton(replaceAction);
		LibTTx.addGridBagComponent(replaceBtn, constraints, 1, 5, 1, 1, 100, 0, contentPane);

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
}
