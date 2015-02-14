/*
 * IJ BAR: https://github.com/tferr/Scripts
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation
 * (http://www.gnu.org/licenses/gpl.txt).
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 */

package bar.plugin;

import fiji.Debug;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.text.TextPanel;
import ij.text.TextWindow;

import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bar.Utils;

/**
 * This class implements a file browser modeled after CommandFinder[1] by Mark
 * Longair and FunctionFinder[2] by Jerome Mutterer. It is also influenced by
 * Johannes Schindelin's Recent_Commands plugin[3].
 *
 * [1] https://github.com/imagej/ImageJA/blob/master/src/main/java/ij/plugin/CommandFinder.java
 * [2] https://github.com/imagej/ImageJA/blob/master/src/main/java/ij/macro/FunctionFinder.java
 * [3] https://github.com/fiji/Fiji_Plugins/blob/master/src/main/java/fiji/util/Recent_Commands.java
 *
 */
public class Opener implements PlugIn, FileFilter, ActionListener,
		DocumentListener, KeyListener, ListSelectionListener, MouseListener,
		WindowListener {

	/* Defaults for "Reset" option */
	private static final String DEF_PATH = Utils.getBARDir();
	private static final int DEF_MAX_SIZE = 200;
	private static final boolean DEF_CLOSE_ON_OPEN = false;
	private static final boolean DEF_IJM_LEGACY = false;
	private static final String PROMPT_PLACEHOLDER = "Search or press <!> for console";

	/* Parameters */
	private String path = DEF_PATH;
	private int maxSize = DEF_MAX_SIZE;
	private boolean closeOnOpen = DEF_CLOSE_ON_OPEN;
	private boolean ijmLegacy = DEF_IJM_LEGACY;
	private String matchingString = "";

	/** Flag that monitors if file list reached maximum size */
	private boolean truncatedList = false;
	/** Flag that toggles changes to status bar messages */
	private boolean freezeStatusBar = false;

	private Dialog dialog;
	private JTextField prompt;
	private JScrollPane listPane;
	private JList list;
	private JLabel status, pathBar;
	private Button optionsButton, openButton, closeButton;
	private PopupMenu optionsMenu;
	private Vector<String> filenames;
	private String selectedItem;
	ArrayList<String> bookmarks;


	public static void main(final String[] args) { Debug.run("BAR Opener",""); }


	public void run(final String arg) {
		new Thread() {
			public void run() {
				if (Utils.fileExists(path))
					runInteractively();
			}
		}.start();
	}

	public void runInteractively() {

		// Initialize file list and favorites list
		filenames = new Vector<String>();
		bookmarks = new ArrayList<String>();

		// Build dialog
		dialog = new Dialog(IJ.getInstance(), "BAR Opener");
		dialog.addWindowListener(this);
		dialog.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = c.gridx = 0;

		// Add search prompt
		prompt = new JTextField(PROMPT_PLACEHOLDER, 20);
		prompt.selectAll();
		prompt.setToolTipText("<html>Prompt shortcuts:<br>"
				+ "&emsp;! &emsp; Console mode<br>"
				+ "&emsp;&crarr;&ensp; Open filtered item <br>"
				+ "&emsp;&darr;&emsp; Move to list</html>");
		prompt.setFont(prompt.getFont().deriveFont(
				prompt.getFont().getSize() + 2f));
		prompt.getDocument().addDocumentListener(this);
		prompt.addActionListener(this);
		prompt.addKeyListener(this);
		c.gridy++; c.gridx = 0;
		dialog.add(prompt, c);

		// Add file list
		listPane = new JScrollPane();
		list = new JList();
		list.setVisibleRowCount(16);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setToolTipText("<html>Navigation shortcuts:<br>"
				+ "&emsp;&uarr; &darr;&ensp; Select items<br>"
				+ "&emsp;&crarr;&emsp; Open item<br>"
				+ "&emsp;&larr;&emsp; Parent directory<br>"
				+ "&emsp;&rarr;&emsp; Expand selected folder<br>"
				+ "Alt &uarr;, &lArr;&ensp;Search prompt</html>");
		list.addKeyListener(this);
		list.addMouseListener(this);
		list.addListSelectionListener(this);
		listPane.getViewport().setView(list);
		c.gridy++; c.gridx = 0;
		dialog.add(listPane, c);

		// Add path bar
		pathBar = new JLabel();
		pathBar.setFont(pathBar.getFont().deriveFont(
				pathBar.getFont().getSize() - 1f));
		pathBar.setForeground(Color.DARK_GRAY);
		setPath(path);
		c.gridy++;
		c.gridx = 0;
		dialog.add(pathBar, c);

		// Add status bar
		status = new JLabel();
		status.addMouseListener(this);
		c.gridy++; c.gridx = 0;
		dialog.add(status, c);
		updateStatus();
		setTooltips();

		// Add buttons
		final Panel buttonPanel = new Panel();
		optionsButton = new Button(". . .");
		optionsMenu = createOptionsMenu();
		optionsButton.add(optionsMenu);
		optionsButton.addActionListener(this);
		optionsButton.addMouseListener(this);
		buttonPanel.add(optionsButton);
		closeButton = new Button("Dismiss");
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);
		openButton = new Button("Open");
		openButton.addActionListener(this);
		buttonPanel.add(openButton);
		c.gridy++; c.gridx = 0;
		dialog.add(buttonPanel, c);

		// Populate file list and display dialog
		updateList();
		dialog.pack();
		dialog.setResizable(false);
		dialog.setVisible(true);
		prompt.requestFocus();

	}

	void addBookmark() {
		if (!bookmarks.contains(path)) {
			if (bookmarks.size() > 0)
				optionsMenu.remove(optionsMenu.getItemCount() - 1);
			bookmarks.add(path);
			optionsMenu.add(createBookmarkMenu());
			log("New bookmark: "+ path);
		} else
			error("Already bookmarked "+ path);
	}

	void cdToDirectory(final String defaultpath) {
		try {
			Class.forName("fiji.util.gui.GenericDialogPlus");
			final GenericDialogPlus gd = new GenericDialogPlus("Change directory");
			gd.addDirectoryField("cd to..", defaultpath, 50);
			gd.setOKLabel("    Set Path    ");
			gd.addDialogListener(new DialogListener() {
				@Override
				public boolean dialogItemChanged(final GenericDialog gd, final AWTEvent e) {
					final TextField tf = (TextField) gd.getStringFields().elementAt(0);
					final Button[] buttons = gd.getButtons();
					if (new File(gd.getNextString()).exists()) {
						tf.setForeground(Color.BLACK);
						buttons[0].setLabel("    Set Path    ");
						return true;
					} else {
						tf.setForeground(Color.RED);
						buttons[0].setLabel("Invalid Path");
						return false;
					}
				}
			});
			gd.showDialog();
			final String newPath = gd.getNextString();
			if (!gd.wasCanceled() && !newPath.isEmpty()) {
				changeDirectory(newPath);
			} else {
				error("cd to... not executed");
			}
		} catch (final ClassNotFoundException e) {
			error("cd to... not executed");
			IJ.error("Dependencies Missing", "Error: This command requires fiji-lib.");
		}
	}

	void changeDirectory(String newDir) {
		if (newDir.isEmpty())
			newDir = IJ.getDirectory("Choose new Directory");
		if (newDir == null)
			return;
		if (Utils.fileExists(newDir))
			setPath(newDir);
		else
			error("Path unavailable: "+ newDir);
		resetFileList();
	}

	void clearBookmarks() {
		optionsMenu.remove(optionsMenu.getItemCount() - 1);
		bookmarks.clear();
	}

	PopupMenu createOptionsMenu() {
		final PopupMenu popup = new PopupMenu();

		MenuItem mi = new MenuItem("Add to Favorites");
		mi.addActionListener(this);
		popup.add(mi);
		mi = new MenuItem("Reveal Path");
		mi.addActionListener(this);
		popup.add(mi);
		mi = new MenuItem("Go To...");
		mi.addActionListener(this);
		popup.add(mi);
		popup.addSeparator();
		mi = new MenuItem("Refresh File List");
		mi.addActionListener(this);
		popup.add(mi);
		mi = new MenuItem("Print Current List");
		mi.addActionListener(this);
		popup.add(mi);
		popup.addSeparator();

		mi = new MenuItem("Enter Console Mode");
		mi.addActionListener(this);
		popup.add(mi);
		mi = new MenuItem("Options...");
		mi.addActionListener(this);
		popup.add(mi);
		popup.addSeparator();

		return popup;
	}

	Menu createBookmarkMenu() {
		final Menu menu = new Menu("Favorites");
		MenuItem mi;
		for (final String bookmark : bookmarks) {
			mi = new MenuItem(bookmark);
			mi.addActionListener(this);
			menu.add(mi);
		}
		return menu;
	}

	/**
	 * Displays an error message. When triggered in console mode the message is
	 * displayed during 5s. This is achieved through a timerTask that keeps the
	 * freezeStatusBar flag set to true during five seconds.
	 * 
	 * @see log
	 */
	void error(final String errorMsg) {
		status.setForeground(Color.RED);
		status.setText(errorMsg);
		if (isConsoleMode()) {
			freezeStatusBar = true;
			final Timer timer = new Timer();
			final TimerTask task = new TimerTask() {
				@Override
				public void run() {
					freezeStatusBar = false;
					timer.cancel();
				}
			};
			try {
				timer.schedule(task, 5 * 1000);
			} catch (final Exception e) {
				timer.cancel();
			}
		}
	}

	/**
	 * Displays an informational message if status bar is not "frozen"
	 * (freezeStatusBar is false). Does nothing if freezeStatusBar is true
	 * (an error is being displayed).
	 * 
	 * @see error
	 */
	void log(final String msg) {
		if (!freezeStatusBar)
			status.setText(msg);
	}

	void interpretCommand(final String cmd) {
		if (cmd.isEmpty()) // just a spacer in command list
			return;

		final String result = execCommand(cmd);

		// Case null: cmd encoded a path and encoded directory was not found
		if (result == null) {
			if (cmd.startsWith("image")) {
				error("Image directory unknown");
				IJ.error("Unknown path", "Could not determine path of active image.");
			} else if (cmd.startsWith("current")) {
				error("Working directory unknown");
				IJ.error("Unknown path", "Working directory is set upon a valid I/O operation.");
			} else if (!cmd.startsWith("new")) {
				error("Directory not found");
				IJ.error("Error", "The requested directory could not be found.");
			}
			resetCommandList();
			return;
		}

		// Case 0: cmd encoded a non-verbose self-contained instruction
		if (result.equals(String.valueOf(0))) {
			resetFileList();
			prompt.requestFocus();
			return;
		}

		// Case 1: cmd encoded a self-contained instruction that output a status message
		if (result.equals(String.valueOf(1))) {
			setConsoleMode(false);
			resetFileListWithoutStatusChange();
			prompt.requestFocus();
			return;
		}

		// Remaining cases: cmd encodes a new path
		changeDirectory(result);
		prompt.requestFocus();

	}

	String execCommand(final String cmd) {

		// Case "0": Self-contained commands that need no follow-up
		String exitStatus = String.valueOf(0);
		if (cmd.startsWith("close")) {
			dialog.dispose();
			return exitStatus;
		} else if (cmd.startsWith("help")) {
			showHelp();
			return exitStatus;
		} else if (cmd.startsWith("ls")) {
			printList();
			return exitStatus;
		} else if (cmd.equals("options")) {
			showOptionsDialog();
			return exitStatus;
		} else if (cmd.startsWith("refresh")) {
			freezeStatusBar = false;
			return exitStatus;
		} else if (cmd.startsWith("reveal")) {
			Utils.revealFile(path);
			return exitStatus;
		}

		// Case "1": Self-contained commands that produce a status
		// bar message and expect it to persist after execution
		exitStatus = String.valueOf(1);
		if (cmd.equals("..")) {
			selectParentDirectory(path);
			return exitStatus;
		} else if (cmd.equals("bookmark")) {
			addBookmark();
			return exitStatus;
		} else if (cmd.equals("cd")) {
			cdToDirectory(path);
			return exitStatus;
		} else if (cmd.startsWith("info")) {
			showInfo();
			return exitStatus;
		}

		// Remaining cases: Commands that only retrieve paths
		exitStatus = null;
		if (cmd.equals("new")) {
			exitStatus = IJ.getDirectory("Choose new directory");
		} else if (cmd.startsWith("home")) {
			exitStatus = IJ.getDirectory("home");
		} else if (cmd.startsWith("image")) {
			exitStatus = IJ.getDirectory("image");
		} else if (cmd.equals("luts")) {
			exitStatus = IJ.getDirectory(cmd);
		} else if (cmd.equals("macros")) {
			exitStatus = IJ.getDirectory(cmd);
		} else if (cmd.equals("plugins")) {
			exitStatus = IJ.getDirectory(cmd);
		} else if (cmd.startsWith("current")) {
			exitStatus = OpenDialog.getDefaultDirectory();
		} else if (cmd.equals("ij")) {
			exitStatus = IJ.getDirectory("imagej");
		} else if (cmd.startsWith("temp")) {
			exitStatus = IJ.getDirectory("temp");
		} else if (cmd.equals("snip")) {
			exitStatus = Utils.getSnippetsDir();
		} else if (cmd.equals("lib")) {
			exitStatus = Utils.getLibDir();
		} else if (cmd.equals("bar")) {
			exitStatus = Utils.getBARDir();
		} else if (cmd.equals("samples")) {
			exitStatus = IJ.getDirectory("imagej") + cmd;
		} else if (cmd.equals("scripts")) {
			exitStatus = IJ.getDirectory("imagej") + cmd;
		} else if (cmd.equals("tools/")) {
			exitStatus = IJ.getDirectory("macros") + "tools";
		} else if (cmd.equals("toolsets")) {
			exitStatus = IJ.getDirectory("macros") + cmd;
		}
		return exitStatus;

	}

	private void showInfo() {
		try {
			resetFileList();
			final File f = new File(path);
			final String writable = (f.canWrite()) ? "writable"
					: "non writable";
			final String bookmarked = (bookmarks.contains(path)) ? "bookmarked"
					: "not bookmarked";
			int subfolders = 0;
			for (final String item : filenames)
				if (item.endsWith(File.separator))
					subfolders++;
			final int files = filenames.size() - subfolders;

			final StringBuffer sb = new StringBuffer();
			sb.append("<html>");
			sb.append("<dl>");
			sb.append("<dt>Path (").append(writable).append("):</dt>");
			sb.append("<dd>");
			sb.append(f.getAbsolutePath()).append(File.separator);
			sb.append("</dd>");
			sb.append("<dt>Last modified:</dt>");
			sb.append("<dd>");
			sb.append(new Date(f.lastModified()));
			sb.append("</dd>");
			sb.append("<dt>Listed contents:</dt>");
			sb.append("<dd>");
			sb.append(subfolders).append(" subfolder(s), ").append(files)
					.append(" file(s)");
			sb.append("</dd>");
			sb.append("<dt>Maximum list size:</dt>");
			sb.append("<dd>");
			sb.append("Currently set to ").append(maxSize).append(" items");
			sb.append("</dd>");
			sb.append("<dt>Favorite?</dt>");
			sb.append("<dd>");
			sb.append("Folder is ").append(bookmarked);
			sb.append("</dd>");
			sb.append("</dl>");
			sb.append("</html>");
			IJ.showMessage(f.getName(), sb.toString());
		} catch (final Exception e) {
			IJ.error("Could not retrieve information from current path");
		}
	}

	void resetCommandList() {
		setMatchingString("!");
		prompt.setText("!");
		prompt.selectAll();
		updateList();
	}

	void resetFileList() {
		setMatchingString("");
		prompt.setText("");
		updateList();
		list.setSelectedIndex(0);
	}

	boolean isOpenable(String path) {
		if (path == null)
			return false;
		if (!isConsoleMode()) {
			final String[] compiledExtensions = { ".jar", ".class", ".pyc" };
			path = path.toLowerCase();
			for (final String ext : compiledExtensions) {
				if (path.endsWith(ext))
					return false;
			}
		}
		return true;
	}

	boolean isFolder(final String path) {
		return path != null && path.endsWith(File.separator);
	}

	void openItem(final String filename) {

		if (!isOpenable(filename))
			return;

		if (isConsoleMode()) {
			try {
				final int idx1 = filename.indexOf("<b>") + 4;
				final int idx2 = filename.indexOf("</b>");
				selectedItem = filename.substring(idx1, idx2);
				interpretCommand(selectedItem);
			} catch (final Exception e) {
				error("Error: malformed command");
			}
			return;
		}

		if (isFolder(filename)) {
			selectSubDirectory(filename);
			return;
		}

		new Thread() {
			public void run() {
				if (!Utils.fileExists(path + filename)) {
					error(filename + " unavailable...");
					return;
				}
				if (isScript(filename)) {
					if (ijmLegacy && filename.toLowerCase().endsWith("ijm"))
						Utils.openIJ1Script(path, filename);
					else
						Utils.openScript(path, filename);
				} else {
					IJ.open(path + filename);
				}
			}
		}.start();

		if (closeOnOpen)
			dialog.dispose();

	}

	boolean isScript(final String file) {
		final String[] EXTS = { ".txt", ".bsh", ".clj", ".groovy", ".ijm",
				".js", ".py", ".rb", ".java" };
		for (final String ext : EXTS)
			if (file.endsWith(ext))
				return true;
		return false;
	}

	void printList() {
		if (isConsoleMode()) {
			Utils.listDirectory(path);
			return;
		}
		if (filenames.size() < 2) {
			error("No files in current list");
			return;
		}
		final TextWindow tw = new TextWindow(
				path + " [" + matchingString + "]", "", 550, 200);
		final TextPanel tp = tw.getTextPanel();
		tp.setColumnHeadings("Double-click on a filename to open it");
		int counter = 1;
		final int padLngth = (int) (Math.log10(filenames.size()) + 1);
		for (final String f : filenames)
			tw.append("" + IJ.pad(counter++, padLngth) + ": " + path + f);
		tp.updateDisplay();
		log("" + (counter - 1) + " items listed");
	}

	void selectParentDirectory(final String currentDir) {
		final File parent = new File(currentDir).getParentFile();
		if (parent == null)
			error("Parent directory unavailable");
		else
			changeDirectory(parent.getAbsolutePath());
	}

	void selectSubDirectory(final String subdir) {
		changeDirectory(path + subdir);
	}

	void setCommandList() {
		final String spacer = "<html><span style='color:white;'><b>!</b></span></html>";
		final String cmds[] = {
				"!..@Parent directory of current path",
				"!cd@Change directory to...",
				"!current, !pwd@ImageJ <i>working</i> directory",
				"!image, !imp@Folder of active image",
				"!home, !~@"+ System.getProperty("user.name") +"'s home directory",
				"!new@Choose new path in file manager",
				"!temp, !tmp@Temporary directory",
				spacer,
				"!ij@<i>ImageJ</i> directory",
				"!luts@<i>Lookup Tables</i> directory",
				"!macros@<i>Macros</i> directory",
				"!plugins@<i>Plugins</i> directory",
				"!samples@Cached File&gt;Open Samples&gt;",
				"!scripts@<i>Scripts</i> directory",
				"!tools/@Macro <i>tools</i> directory", // slash makes command unique
				"!toolsets@Macro <i>toolsets</i> directory",
				spacer,
				"!bar@BAR root directory",
				"lib@Path to <i>BAR/lib</i>",
				"!snip@Path to <i>BAR/Snippets</i>",
				spacer,
				"!bookmark@Add path to <i>Favorites</i>",
				"!close !quit@Dismiss this window",
				"!info@Details on current path",
				"!ls !print@List contents of current path",
				"!help@Display built-in help",
				"!options@Prompt for settings",
				"!refresh@Refresh (reload) list"
			};

		truncatedList = false;
		filenames.removeAllElements();
		for (String cmd : cmds) {
			if (cmd.indexOf(matchingString) >= 0) {
				if (!cmd.equals(spacer)) {
					final String[] items = cmd.split("@");
					cmd = "<html><span style='color:blue;'>"
							+ "<b>" + items[0] + "</b>"
							+ "</span>&ensp;" + items[1] + "</html>";
				}
				filenames.add(cmd);
			}
		}
	}

	void setFileList() {
		truncatedList = false;
		filenames.removeAllElements();
		final File dir = new File(path);
		final int rootIdx = path.length();
		for (final File f : dir.listFiles(this)) {
			if (filenames.size() >= maxSize) {
				truncatedList = true;
				break;
			}
			String name = f.getAbsolutePath().substring(rootIdx);
			if (name.length() == 0)
				continue;
			if (f.isDirectory())
				name += File.separator;
			filenames.add(name);
		}
		// Collections.sort(filenames);
	}

	void setMatchingString(final String newMatchingString) {
		this.matchingString = newMatchingString.toLowerCase(Locale.US);
	}

	void setPath(String newPath) {
		if (!newPath.endsWith(File.separator))
			newPath += File.separator;
		path = newPath;
		pathBar.setText(path);
	}

	void setSelectedItem(final int index) {
		selectedItem = filenames.elementAt(Math.max(0, index));
	}

	private void showHelp() {
		// TODO Auto-generated method stub
		IJ.error("Not yet implemented");
	}

	void showOptionsDialog() {
		boolean hardReset = false;
		final GenericDialog gd = new GenericDialog("Commander Settings");
		gd.addNumericField("Maximum number of items in list", maxSize, 0);
		gd.addCheckbox("Close window after opening selected file", closeOnOpen);
		gd.addCheckbox("Open IJM files in built-in (legacy) editor", ijmLegacy);
		gd.addCheckbox("Clear Favorites list", false);
		gd.enableYesNoCancel("OK", "Reset All Settings");
		gd.showDialog();
		if (gd.wasCanceled()) {
			dialog.toFront();
			return;
		} else if (gd.wasOKed()) {
			maxSize = (int) Math.max(1, gd.getNextNumber());
			closeOnOpen = gd.getNextBoolean();
			ijmLegacy = gd.getNextBoolean();
			if (gd.getNextBoolean()) clearBookmarks();
			dialog.toFront();
		} else {
			hardReset = true;
			maxSize = DEF_MAX_SIZE;
			ijmLegacy = DEF_IJM_LEGACY;
			closeOnOpen = DEF_CLOSE_ON_OPEN;
			path = DEF_PATH;
			showOptionsDialog();
		}
		if (hardReset)
			resetFileList();
		else
			updateList();
	}

	void updateList() {
		if (isConsoleMode()) {
			setCommandList();
		else
			setFileList();
		updatingList = true;
		list.setListData(filenames);
		updatingList = false;
		updateStatus(path);
	}

	void setTooltips() {
		String tip;
		if (isConsoleMode()) {
			tip = "Double-click to exit console mode.";
		} else {
			if (filenames.size() == 0) {
				tip = "Reset search or type <tt>!refresh</tt>.";
			} else if (truncatedList) {
				tip = "Double-click to change list size or type <tt>!options</tt>.";
			} else {
				tip = "Double-click to change directory or type <tt>!new</tt>.";
			}
		}
		status.setToolTipText("<html>" + tip + "<br>Current path:<br><pre>"
				+ path + "</pre></html>");
	}

	void updateStatus() {
		String msg;
		if (!dialog.isShowing()) { // Startup message
			msg = "Waiting for input...";

		} else { // Interactive mode
			final int hits = filenames.size();

			if (isConsoleMode()) { // Default look for console
				prompt.setForeground(Color.BLUE);
				status.setForeground(Color.BLUE);
				msg = "Console enabled...";

			} else { // Default look for file list
				prompt.setForeground(Color.BLACK);
				status.setForeground(Color.DARK_GRAY);

				if (hits == 0) { // Empty folder or no matches?
					if (matchingString.isEmpty()) {
						msg = "No items in folder...";
					} else {
						prompt.setForeground(Color.RED);
						status.setForeground(Color.RED);
						msg = "No matches found...";
					}

				} else if (truncatedList) { // Some files may not be displayed
					msg = String.valueOf(maxSize) + " items limit reached...";

				} else { // All files are being filtered
					final String label = (hits == 1) ? String.valueOf(hits)
							+ " item, " : String.valueOf(hits) + " items, ";
					msg = label + path;
				}
			}
		}
		status.setText(msg);
	}

	void validateOptionsMenu() {
		// Toggle "Add to Favorites", "Reveal Path", "Print Current List", etc.
		for (int i = 0; i < optionsMenu.getItemCount(); i++) {
			final MenuItem item = optionsMenu.getItem(i);
			final String label = item.getLabel();
			if (label.equals("Add to Favorites") || label.equals("Reveal Path")
					|| label.equals("Print Current List")
					|| label.equals("Refresh File List")) {
				item.setEnabled(!isConsoleMode());
			} else if (label.contains("Console")) {
				final String cLabel = (isConsoleMode()) ? "Exit Console Mode"
						: "Enter Console Mode";
				item.setLabel(cLabel);
			}
		}
	}


	boolean isConsoleMode() {
		return matchingString.startsWith("!");
	}

	/* FileFilter Methods */
	@Override
	public boolean accept(final File file) {
		if (file.isHidden())
			return false;
		String name = file.getName().toLowerCase();
		if (file.isDirectory())
			name += File.separator;
		return (name.indexOf(matchingString) >= 0);
	}

	/* ActionEvent Methods */
	@Override
	public void actionPerformed(final ActionEvent e) {
		final Object b = e.getSource();
		if (b == prompt) {
			//setMatchingString(prompt.getText());
			if (filenames.size() == 1) {
				setSelectedItem(0);
				openItem(selectedItem);
			}
		} else if (b == optionsButton) {
			validateOptionsMenu();
			final Rectangle r = optionsButton.getBounds();
			optionsMenu.show(optionsButton, r.x, r.y);
		} else if (b == openButton) {
			openItem(selectedItem);
		} else if (b == closeButton) {
			dialog.dispose();
		} else { // An entry in the optionsMenu has been selected
			final String command = e.getActionCommand();
			if (command.equals("Options...")) {
				showOptionsDialog();
			} else if (command.equals("Go To...")) {
				changeDirectory("");
			} else if (command.equals("Add to Favorites")) {
				addBookmark();
			} else if (command.equals("Print Current List")) {
				printList();
			} else if (command.equals("Refresh File List")) {
				resetFileList();
			} else if (command.contains("Console")) {
				if (isConsoleMode())
					resetFileList();
				else
					resetCommandList();
			} else if (command.equals("Reveal Path")) {
				Utils.revealFile(path);
			} else { // A bookmark was selected
				changeDirectory(command);
			}
		}
	}


	/* DocumentListener Methods */
	@Override
	public void insertUpdate(final DocumentEvent ev) {
		setMatchingString(prompt.getText());
		updateList();
	}

	@Override
	public void removeUpdate(final DocumentEvent ev) {
		setMatchingString(prompt.getText());
		updateList();
	}

	// Plain text should not trigger this event
	@Override
	public void changedUpdate(final DocumentEvent ev) {
		setMatchingString(prompt.getText());
		updateList();
	}

	/* ListSelectionListener Methods */
	@Override
	public void valueChanged(final ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;
		setSelectedItem(list.getSelectedIndex());
		openButton.setEnabled(isOpenable(selectedItem));
	}

	/* KeyListener Methods */
	public void keyPressed(final KeyEvent ke) {
		final int key = ke.getKeyCode();
		final Object source = ke.getSource();

		if ((ke.isMetaDown() && key == KeyEvent.VK_W)
				|| key == KeyEvent.VK_ESCAPE) {
			dialog.dispose();

		} else if (source == prompt) {

			if (key == KeyEvent.VK_DOWN
					|| (ke.isAltDown() && key == KeyEvent.VK_TAB)) {
				list.setSelectedIndex(0);
				list.requestFocus();
			}

		} else if (source == list) {

			if (key == KeyEvent.VK_LEFT) {

				selectParentDirectory(path);
				setSelectedItem(list.getSelectedIndex());

			} else if (key == KeyEvent.VK_RIGHT) {

				setSelectedItem(list.getSelectedIndex());
				if (isFolder(selectedItem))
					selectSubDirectory(selectedItem);

			} else if (key == KeyEvent.VK_ENTER) {

				setSelectedItem(list.getSelectedIndex());
				openItem(selectedItem);

			} else if (key == KeyEvent.VK_BACK_SPACE
					|| (key == KeyEvent.VK_TAB) && ke.isAltDown()) {

				prompt.requestFocus();

			} else if (key == KeyEvent.VK_UP) {

				if (ke.isAltDown())
					prompt.requestFocus();
				else if (list.getSelectedIndex() == 0)
					list.setSelectedIndex(list.getModel().getSize() - 1);

			} else if (key == KeyEvent.VK_DOWN) {

				if (list.getSelectedIndex() == list.getModel().getSize() - 1)
					list.setSelectedIndex(0);

			}

		}

	}

	public void keyReleased(final KeyEvent ke) {
	}

	public void keyTyped(final KeyEvent ke) {
	}


	/* MouseListener Methods */
	@Override
	public void mouseClicked(final MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == list) {
				setSelectedItem(list.getSelectedIndex());
				openItem(selectedItem);
			} else if (e.getSource() == status) {
				if (consoleMode())
					resetFileList();
				else if (truncatedList)
					showOptionsDialog();
				else
					changeDirectory("");
			}
		}
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (e.getSource() != list && e.isPopupTrigger()) {
			validateOptionsMenu();
			optionsMenu.show(optionsButton, e.getX(), e.getY());
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (e.getSource() != list && e.isPopupTrigger()){
			validateOptionsMenu();
			optionsMenu.show(optionsButton, e.getX(), e.getY());
		}
	}

	public void mouseEntered(final MouseEvent e) {
	}

	public void mouseExited(final MouseEvent e) {
	}

	/* WindowListener Methods */
	public void windowClosing(final WindowEvent e) {
		dialog.dispose();
	}

	public void windowActivated(final WindowEvent e) {
	}

	public void windowClosed(final WindowEvent e) {
	}

	public void windowDeactivated(final WindowEvent e) {
	}

	public void windowDeiconified(final WindowEvent e) {
	}

	public void windowIconified(final WindowEvent e) {
	}

	public void windowOpened(final WindowEvent e) {
	}

}
