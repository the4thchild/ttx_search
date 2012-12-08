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
 * Portions created by the Initial Developer are Copyright (C) 2003-6
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

/** Find and replace dialog.
    Creates a dialog box accepting input for search and replacement 
    expressions as well as options to tailor the search.
*/
public class FindDialog extends JPanel {//JFrame {
	private JLabel tipsTitleLbl = null; // offers tips on using the plug-in 
	private JLabel tipsLbl = null;
	private JLabel findLbl = null; // label for the search field
	private JTextField find = null; // search expression input
	private JLabel replaceLbl = null; // label for the replacement field
	private JTextField replace = null; // replacement expression input
	private JCheckBox word = null; // treat the search expression as a separate word
	private JCheckBox wrap = null; // search to the bottom and start again from the top
	private JCheckBox selection = null; // search only within a highlighted section
	private JCheckBox replaceAll = null; // replace all instances of search expression
	private JCheckBox ignoreCase = null; // ignore upper/lower case
	private JLabel resultsTitleLbl = null;
	private JLabel resultsLbl = null;
	private JButton findBtn = null; // label for the search button
	private JButton replaceBtn = null; // label for the replace button
	private JButton statsBtn = null; // label for the stats button
	private JLabel charLbl = null; // label for the stats char value
	private JLabel wordLbl = null; // label for the stats word value
	private JLabel lineLbl = null; // label for the stats line value
	private JLabel charCountLbl = null; // the actual character count
	private JLabel wordCountLbl = null; // the actual word count
	private JLabel lineCountLbl = null; // the actual line count
	private String[] tips = {
		"Tip: Searches and statistics begin from the cursor or start of selected area",
		"Here's a secret: Use ^t for TABs and ^n for NEWLINEs",
		"Psst!  Stats available for \"Selected area only\", too"
	};
		
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
		setSize(450, 250);
		//Container contentPane = getContentPane();
		//contentPane.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.CENTER;
		String msg = "";
		String lbl = "";
		

		// tips display
		// Now the tips labels are combined into one label that spans the
		// entire row to minimize resizing column widths and button sizes
		tipsLbl = new JLabel("");
		tipsLbl.setHorizontalAlignment(JLabel.RIGHT);
		tipsLbl.setToolTipText("Tips for quicker searching");
		LibTTx.addGridBagComponent(
			tipsLbl,
			constraints,
			0,
			0,
			3,
			1,
			100,
			0,
			this);
		setTipsLbl(4);

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
		
		
		// Results
		/*
		resultsTitleLbl = new JLabel("Results: ");
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
		*/

		// Now the results labels are combined into one label that spans the
		// entire row to minimize resizing column widths and button sizes
		resultsLbl = new JLabel("Results: ");
		resultsLbl.setHorizontalAlignment(JLabel.RIGHT);
		resultsLbl.setToolTipText("Results from the search");
//		resultsLbl.setMaximumSize(new Dimension(100, 25));
		LibTTx.addGridBagComponent(
			resultsLbl,
			constraints,
			0,
			5,
			3,
			1,
			100,
			0,
			this);//contentPane);


		// fires the "find" action
		findBtn = new JButton(findAction);
//		findBtn.setMinimumSize(new Dimension(100, 20));
//		findBtn.setMaximumSize(new Dimension(100, 20));
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
	
	public String getFindTextConverted() {
		return LibTTx.convertEscapeChars(find.getText());
	}

	/** Gets the value of the "replace" text field.
	 * 
	 * @return value of the <code>replace JCheckBox</code>
	 */
	public String getReplaceText() {
		return replace.getText();
	}

	/** Gets the value of the "replace" text field.
	 * 
	 * @return value of the <code>replace JCheckBox</code>
	 */
	public String getReplaceTextConverted() {
		return LibTTx.convertEscapeChars(replace.getText());
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
	
	public void setTipsLbl(int weightedFront) {
		tipsLbl.setText(LibTTx.pickWeightedStr(tips, weightedFront));
	}
	
	public void setStatsLbls(String charCount, String wordCount, String lineCount) {
		setCharCountLbl(charCount);
		setWordCountLbl(wordCount);
		setLineCountLbl(lineCount);
	}
	
	public void resetStatsLbls() {
		setStatsLbls("", "", "");
	}

}