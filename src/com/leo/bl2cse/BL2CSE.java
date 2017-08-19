package com.leo.bl2cse;

import java.awt.Component;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class BL2CSE {

	private static JFileChooser fc;

	public static int openFileChooser(Component parent, String title, FileFilter filter, File currentDirectory,
			boolean allowAllFilesFilter, boolean openOrSave) {
		if (fc == null)
			fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		fc.setAcceptAllFileFilterUsed(allowAllFilesFilter);
		fc.setDialogTitle(title);
		fc.setFileFilter(filter);
		fc.setCurrentDirectory(currentDirectory);
		int ret = 0;
		if (openOrSave)
			ret = fc.showSaveDialog(parent);
		else
			ret = fc.showOpenDialog(parent);
		return ret;
	}

	public static class EntityInfo {
		private String shortName1, shortName2, name;
		private Rectangle rect;

		public String getShortName1() {
			return shortName1;
		}

		public void setShortName1(String shortName1) {
			this.shortName1 = shortName1;
		}

		public String getShortName2() {
			return shortName2;
		}

		public void setShortName2(String shortName2) {
			this.shortName2 = shortName2;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Rectangle getRect() {
			return rect;
		}

		public void setRect(Rectangle rect) {
			this.rect = rect;
		}
	}

	private static List<EntityInfo> info;

	private static Rectangle str2Rect(String s) {
		if (!s.contains(":"))
			return new Rectangle(0, 0, 0, 0);
		Scanner sc = new Scanner(s);
		sc.useDelimiter(":");
		int l, u, r, d;
		l = sc.nextInt();
		u = sc.nextInt();
		r = sc.nextInt();
		d = sc.nextInt();
		sc.close();
		return new Rectangle(l, u, r, d);
	}

	private static void error(Throwable e, String message) {
		e.printStackTrace();
		JOptionPane.showMessageDialog(null, message + "\n" + e.getMessage(), "An exception has occured",
				JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

	public static void main(String[] args) {
		int openRes = openFileChooser(null, "Open entityInfo.txt", new FileNameExtensionFilter("Text file", "txt"),
				new File(System.getProperty("user.dir")), false, false);
		if (openRes != JFileChooser.APPROVE_OPTION)
			System.exit(0);
		info = new LinkedList<>();
		File entityInfo = fc.getSelectedFile();
		Scanner sc = null;
		try {
			sc = new Scanner(entityInfo);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.equals("") || line.startsWith("/")) //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			if (line.startsWith("#")) { //$NON-NLS-1$
				try {
					// entity number entry
					line = line.substring(1); // throw away the marker
					line = line.substring(0, line.indexOf(';')); // throw away the trailing garbage
					Scanner lineScan = new Scanner(line);
					lineScan.useDelimiter("\\t+"); //$NON-NLS-1$
					EntityInfo i = new EntityInfo();
					lineScan.nextInt(); // entity number
					i.setShortName1(lineScan.next()); // short name 1
					i.setShortName2(lineScan.next()); // short name 2
					i.setName(lineScan.next()); // long name
					i.setRect(str2Rect(lineScan.next())); // framerect
					info.add(i);
					lineScan.close();
				} catch (Exception err) {
					error(err, "Error while parsing entityInfo.txt");
				}
			}
		}
		sc.close();
		File out = new File(System.getProperty("user.dir") + "/output.js");
		if (out.exists())
			out.delete();
		try {
			out.createNewFile();
		} catch (IOException e1) {
			error(e1, "Error creating output.js");
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
			bw.write("function getEntityFrame(entity) {\n\tswitch (entity.type) {");
			for (int i = 0; i < info.size(); i++) {
				EntityInfo e = info.get(i);
				Rectangle r = e.getRect();
				String sn = e.getShortName1().trim();
				String sn2 = e.getShortName2().trim();
				if (!sn2.isEmpty())
					sn += " " + e.getShortName2().trim();
				bw.write(
						"\n\tcase " + i + ": // " + e.getName().trim() + " (" + sn + ")\n\t\treturn java.awt.Rectangle("
								+ r.x + ", " + r.y + ", " + (r.width - r.x) + ", " + (r.height - r.y) + ");");
			}
			bw.write("\n\tdefault:\n\t\treturn java.awt.Rectangle(0, 0, 0, 0);\n}");
		} catch (IOException e) {
			error(e, "Error writing output");
		}
		JOptionPane.showMessageDialog(null,
				"Generated output.js in app directory (" + System.getProperty("user.dir") + ")", "Done!",
				JOptionPane.INFORMATION_MESSAGE);
		System.exit(0);
	}
}
