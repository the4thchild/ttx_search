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
    FindDialog diag = null; // the dialog window for user options
    // whether to run find() or replace() after pressing a button,
    // which causes TextTrix to lauch the plug-in's single runPlugIn() command
    boolean invokeReplace = false; 

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
	super("Search",
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
		    runPlugIn();
		    //		    find();
		}
	    };
	LibTTx.setAcceleratedAction(findAction, "Find", 'F', 
			     KeyStroke.getKeyStroke("alt F"));
	   
	// Runs the search tool in "replace" mode if the user hits the 
	// "Find and Replace" button;
	// creates a shortcut key (alt-R) as an alternative way to invoke
	// the button
	Action replaceAction 
	    = new AbstractAction("Find and Replace", null) {
		    public void actionPerformed(ActionEvent e) {
			// flag runPlugIn() to run in "replace" mode
			invokeReplace = true;
			runPlugIn();
			//			findReplace();
		    }
		};
	LibTTx.setAcceleratedAction(replaceAction, "Find and replace", 'R', 
			     KeyStroke.getKeyStroke("alt R"));

	// Creates the options dialog window
	diag = new FindDialog(findEnter, replaceEnter, 
			      findAction, replaceAction);
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

	// Acts according to whether the plug-in is set to the 
	// "find" or "replace" modes
	if (invokeReplace) { // "replace" mode
	    //	    selectionStart = x;
	    // confines the search to within the highlighted section
	    // only if the Selection option is checked; 
	    // otherwise, searches the entire text from the caret
	    // position or the end of the highlighted section forward
	    selectionEnd = diag.getSelection() ? y : s.length();
	    s = replace(s, diag.getFindText(), diag.getReplaceText(), 
			x, selectionEnd, diag.getWord(), 
			diag.getReplaceAll(), diag.getWrap(), 
			diag.getIgnoreCase());
	} else { // "find" mode
	    String findText = diag.getFindText();
	    // as in "replace" mode, "find" mode confines its search to 
	    // highlighted text only if the Selection option is checked;
	    // if not, "find" must start searching after any highlighted
	    // portion in case the user is using the tool for a repeated
	    // word, which would highlight the word each time and would
	    // force the user to deselect the text otherwise
	    if (diag.getSelection()) { // selected text only
		selectionStart 
		    = find(s, findText, x, y,
			   diag.getWord(), diag.getIgnoreCase());
	    } else { // post-caret or post-selected text
		selectionStart 
		    = find(s, findText, y,
			   diag.getWord(), diag.getIgnoreCase());
		// wrap if sequence not yet found and Wrap option
		// checked
		if (selectionStart == -1 && diag.getWrap()) {
		    selectionStart
			= find(s, findText, 0, diag.getWord(), 
			       diag.getIgnoreCase());
		}
	    }
	    // determines the ending position to highlight if the
	    // sequence has been found
	    if (selectionStart != -1) 
		selectionEnd = selectionStart + findText.length();
	}
	invokeReplace = false; // reset the dialog box
	return new PlugInOutcome(s, selectionStart, selectionEnd);
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

    public int find(String text, String quarry, int start, boolean word, 
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
     * @param n index to start searching
     * @param word if true, treat the sequence as a separate word, with only
     * non-letters/non-digits surrounding it
     * @param ignoreCase if true, ignore upper/lower case
     * @return index of sequence's start in the string; -1 if not found
     */
    public int find(String text, String quarry, int start, int end, 
		    boolean word, boolean ignoreCase) {
	if (ignoreCase) {
	    text = text.toLowerCase();
	    quarry = quarry.toLowerCase();
	}
	return word ? findWord(text, quarry, start, end) 
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
	int n = start;
	int end = start + 1;
	int len = end;
	//	System.out.println(text);
	while (n < len) {
	    // skip over non-letters/non-digits
	    while (n < len && !Character.isLetterOrDigit(text.charAt(n)))
		n++;
	    // progress to the end of a word
	    end = n + 1;
	    while (end < len && Character.isLetterOrDigit(text.charAt(end)))
		end++;
	    // compare the word with the quarry to see if they match
	    //	    System.out.println("n: " + n + ", end: " + end);
	    if (end <= len && text.substring(n, end).equals(quarry) 
		&& n < finish) {
		return n;
		// continue search with next word if no match yet
	    } else {
		n = end;
		//		end++;
	    }
	}
	return -1;
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
    public String replace(String text, String quarry, 
			      String replacement, int start, int end, 
			      boolean word, boolean all, boolean wrap, 
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
		text = replace(text, quarry, replacement, 0, start - 1,
				   word, all, false, ignoreCase);
	    }
	    return text;
	    // replace first occurrence only
	} else {
	    int quarryLoc = -1;
	    // stay within given boundary
	    if (n < end) {
		quarryLoc = find(text, quarry, n, word, ignoreCase);
		// replace quarry if found
		if (quarryLoc!= -1) {
		    text = (text.substring(0, quarryLoc) + replacement
			    + text.substring(quarryLoc + quarry.length()));
		}
	    }
	    // if not found, and wrap is enabled, continue from text beginning;
	    // "wrap" overrules "selection"
	    if (quarryLoc == -1 && wrap) {
		text = replace(text, quarry, replacement, 0, start - 1, 
				   word, all, false, ignoreCase);
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
    public String replace(String text, String quarry, 
			      String replacement, boolean word, 
			      boolean all, boolean wrap, 
			      boolean ignoreCase) {
	return replace(text, quarry, replacement, 0, text.length(), word,
			   all, wrap, ignoreCase);
    }
}


/** Find and replace dialog.
    Creates a dialog box accepting input for search and replacement 
    expressions as well as options to tailor the search.
*/
class FindDialog extends JDialog {
    JTextField find; // search expression input
    JTextField replace; // replacement expression input
    JCheckBox word; // treat the search expression as a separate word
    JCheckBox wrap; // search to the bottom and start again from the top
    JCheckBox selection; // search only within a highlighted section
    JCheckBox replaceAll; // replace all instances of search expression
    JCheckBox ignoreCase; // ignore upper/lower case
    /*
      String text = null;
      int selectionStart = -1;
      int selectionEnd = -1;
    *

    public void setInvokeReplace(boolean aInvokeReplace) { 
	invokeReplace = aInvokeReplace;
    }
    */

    public boolean getWord() { return word.isSelected(); }
    public boolean getWrap() { return wrap.isSelected(); }
    public boolean getSelection() { return selection.isSelected(); }
    public boolean getReplaceAll() { return replaceAll.isSelected(); }
    public boolean getIgnoreCase() { return ignoreCase.isSelected(); }
    public String getFindText() { return find.getText(); }
    public String getReplaceText() { return replace.getText(); }



    /**Construct a find/replace dialog box
     * @param owner frame to which the dialog box will be attached; 
     * can be null
     */
    public FindDialog(KeyAdapter findEnter, KeyAdapter replaceEnter,
		      Action findAction, Action replaceAction) {
	super();
	setSize(400, 150);
	Container contentPane = getContentPane();
	contentPane.setLayout(new GridBagLayout());
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	String msg = "";
			
	// search expression input
	add(new JLabel("Find:"), constraints, 0, 0, 1, 1, 100, 0);
	add(find = new JTextField(20), constraints, 1, 0, 2, 1, 100, 0);
	find.addKeyListener(findEnter);

	// replace expression input
	add(new JLabel("Replace:"), constraints, 0, 1, 1, 1, 100, 0);
	add(replace = new JTextField(20), constraints, 1, 1, 2, 1, 100, 0);
	replace.addKeyListener(replaceEnter);
			
	// treat search expression as a separate word
	add(word = new JCheckBox("Whole word only"), 
	    constraints, 0, 2, 1, 1, 100, 0);
	word.setMnemonic(KeyEvent.VK_N);
	msg = "Search for the expression as a separate word";
	word.setToolTipText(msg);
			
	// wrap search through start of text if necessary
	add(wrap = new JCheckBox("Wrap"), constraints, 2, 2, 1, 1, 100, 0);
	wrap.setMnemonic(KeyEvent.VK_A);
	msg = "Start searching from the cursor and wrap back to it";
	wrap.setToolTipText(msg);
			
	// replace all instances within highlighted section
	add(selection = new JCheckBox("Search within selection"), 
	    constraints, 1, 2, 1, 1, 100, 0);
	selection.setMnemonic(KeyEvent.VK_S);
	msg = "Search and replace text within only the "
	    + "highlighted section";
	selection.setToolTipText(msg);
			
	// replace all instances from cursor to end of text unless 
	// combined with wrap, where replace all instances in whole text
	add(replaceAll = new JCheckBox("Replace all"), 
	    constraints, 0, 3, 1, 1, 100, 0);
	replaceAll.setMnemonic(KeyEvent.VK_L);
	msg = "Replace all instances of the expression";
	replaceAll.setToolTipText(msg);
			
	// ignore upper/lower case while searching
	add(ignoreCase = new JCheckBox("Ignore case"), 
	    constraints, 1, 3, 1, 1, 100, 0);
	ignoreCase.setMnemonic(KeyEvent.VK_I);
	msg = "Search for both lower and upper case versions "
	    + "of the expression";
	ignoreCase.setToolTipText(msg);

	add(new JButton(findAction), constraints, 0, 4, 1, 1, 100, 0);

	// find and replace action, using appropriate options above
	add(new JButton(replaceAction), 
	    constraints, 1, 4, 1, 1, 100, 0);
    }

    /**Adds a new component to the <code>GridBagLayout</code> manager.
     * @param c component to add
     * @param constraints layout constraints object
     * @param x column number
     * @param y row number
     * @param w number of columns to span
     * @param h number of rows to span
     * @param wx column weight
     * @param wy row weight
     * */
    public void add(Component c, GridBagConstraints constraints,
		    int x, int y, int w, int h, 
		    int wx, int wy) {
	constraints.gridx = x;
	constraints.gridy = y;
	constraints.gridwidth = w;
	constraints.gridheight = h;
	constraints.weightx = wx;
	constraints.weighty = wy;
	getContentPane().add(c, constraints);
    }
}
