/********************************************************************************
 * Copyright (c) 2009-2010 Motorola Mobility, Inc.
 * All rights reserved. This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * Vinicius Hernandes (Motorola)
 * Matheus Tait Lima (Eldorado)
 * 
 * Contributors:
 * Marcelo Marzola Bossoni (Eldorado) -  Bug [289146] - Performance and Usability Issues
 * Marcelo Marzola Bossoni (Eldorado) - Bug (289236) - Editor Permitting create 2 columns with same id
 * Vinicius Rigoni Hernandes (Eldorado) - Bug [289885] - Localization Editor doesn't recognize external file changes
 * Matheus Tait Lima (Eldorado) - Adapting to accept automatic translation
 * Marcel Gorri (Eldorado) -  Add method to retrieve ISO639 lang ID
 * Paulo Faria (Eldorado) - Add method to retrieve formatted (bold, underline, italics) string
 * Paulo Faria (Eldorado) - Add methods for not to lose comments on save
 * Fabricio Violin (Eldorado) - Bug [317065] - Localization file initialization bug 
 * Daniel Drigo Pastore, Marcel Augusto Gorri (Eldorado) - Bug 312971 - Localization Editor does not accept < and > characters
 * Marcel Augusto Gorri (Eldorado) - Bug 323036 - Add support to other Localizable Resources
 * 
 ********************************************************************************/
package org.eclipse.sequoyah.localization.android;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.sequoyah.device.common.utilities.BasePlugin;
import org.eclipse.sequoyah.device.common.utilities.exception.SequoyahException;
import org.eclipse.sequoyah.device.common.utilities.exception.SequoyahExceptionStatus;
import org.eclipse.sequoyah.localization.android.AndroidLocaleAttribute.AndroidLocaleAttributes;
import org.eclipse.sequoyah.localization.android.datamodel.AndroidStringLocalizationFile;
import org.eclipse.sequoyah.localization.android.i18n.Messages;
import org.eclipse.sequoyah.localization.android.manager.ILocalizationFileManager;
import org.eclipse.sequoyah.localization.android.manager.ImageLocalizationFileManager;
import org.eclipse.sequoyah.localization.android.manager.LocalizationFileManagerFactory;
import org.eclipse.sequoyah.localization.android.manager.SoundLocalizationFileManager;
import org.eclipse.sequoyah.localization.android.manager.StringLocalizationFileManager;
import org.eclipse.sequoyah.localization.android.manager.VideoLocalizationFileManager;
import org.eclipse.sequoyah.localization.stringeditor.datatype.ColumnInfo;
import org.eclipse.sequoyah.localization.stringeditor.datatype.RowInfo;
import org.eclipse.sequoyah.localization.stringeditor.datatype.TranslationInfo;
import org.eclipse.sequoyah.localization.tools.datamodel.ImageLocalizationFile;
import org.eclipse.sequoyah.localization.tools.datamodel.LocaleAttribute;
import org.eclipse.sequoyah.localization.tools.datamodel.LocaleInfo;
import org.eclipse.sequoyah.localization.tools.datamodel.LocalizationFile;
import org.eclipse.sequoyah.localization.tools.datamodel.LocalizationFileBean;
import org.eclipse.sequoyah.localization.tools.datamodel.LocalizationFileFactory;
import org.eclipse.sequoyah.localization.tools.datamodel.SoundLocalizationFile;
import org.eclipse.sequoyah.localization.tools.datamodel.StringLocalizationFile;
import org.eclipse.sequoyah.localization.tools.datamodel.VideoLocalizationFile;
import org.eclipse.sequoyah.localization.tools.datamodel.node.StringArray;
import org.eclipse.sequoyah.localization.tools.datamodel.node.StringNode;
import org.eclipse.sequoyah.localization.tools.extensions.classes.ILocalizationSchema;
import org.eclipse.sequoyah.localization.tools.extensions.implementation.generic.NewRowInputDialog;
import org.eclipse.sequoyah.localization.tools.extensions.implementation.generic.TranslateColumnInputDialog;
import org.eclipse.sequoyah.localization.tools.extensions.implementation.generic.TranslateColumnsInputDialog;
import org.eclipse.sequoyah.localization.tools.managers.LocalizationManager;
import org.eclipse.sequoyah.localization.tools.managers.ProjectLocalizationManager;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The Android Localization Schema provides the localization schema for Android
 * projects.
 */
public class AndroidLocalizationSchema extends ILocalizationSchema implements
		IAndroidLocalizationSchemaConstants {
	
	private static final HashMap<Class, Class> typesMap = new HashMap<Class, Class>();
	
	static {
		typesMap.put(StringLocalizationFile.class, StringLocalizationFileManager.class);
		typesMap.put(ImageLocalizationFile.class, ImageLocalizationFileManager.class);
		typesMap.put(SoundLocalizationFile.class, SoundLocalizationFileManager.class);
		typesMap.put(VideoLocalizationFile.class, VideoLocalizationFileManager.class);
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema#getPreferedLanguages()
	 */
	@Override
	public List<String> getPreferedLanguages() {

		List<String> langIDs = new ArrayList<String>();

		// Define XML path
		InputStream xmlStream;
		try {

			xmlStream = AndroidLocalizationPlugin.getDefault().getBundle()
					.getEntry(PREFERED_LANGUAGES_XML_PATH).openStream();

			// Load XML
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(xmlStream);

			/*
			 * Iterate through Startup Groups
			 */
			Element rootNode = document.getDocumentElement();
			NodeList languages = rootNode.getElementsByTagName("language"); //$NON-NLS-1$
			for (int i = 0; i < languages.getLength(); i++) {

				Element language = (Element) languages.item(i);
				langIDs.add(language.getAttributeNode("id").getNodeValue()); //$NON-NLS-1$
			}

		} catch (Exception e) {

			BasePlugin
					.logError("Could not load prefered languages for Android Localization Schema"); //$NON-NLS-1$

		}

		return langIDs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema#getIDforLanguage(java.lang.String)
	 */
	@Override
	public String getIDforLanguage(String langID) {
		String columnID = LOCALIZATION_FILES_FOLDER;

		if (langID.contains("-")) { //$NON-NLS-1$
			String[] langParts = langID.split("-"); //$NON-NLS-1$
			columnID += "-" + langParts[0] + "-" + "r" + langParts[1]; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			columnID += "-" + langID; //$NON-NLS-1$
		}

		return columnID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema #isValueValid(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public IStatus isValueValid(String localeID, String key, String value) {
		Status result = new Status(IStatus.OK,
				AndroidLocalizationPlugin.PLUGIN_ID, ""); //$NON-NLS-1$

		if (localeID.toLowerCase().equals(MANDATORY_ID.toLowerCase())) {

			if ((value == null)) {
				result = new Status(IStatus.ERROR,
						AndroidLocalizationPlugin.PLUGIN_ID,
						Messages.EmptyKey_Discouraged);
			}

			if ((value == null) || (value.length() == 0)) {
				result = new Status(IStatus.WARNING,
						AndroidLocalizationPlugin.PLUGIN_ID,
						Messages.EmptyKey_Discouraged);
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema #promptCollumnName()
	 */
	@Override
	public ColumnInfo promptCollumnName(final IProject project) {
		ColumnInfo newColumn = null;

		// Ask user for the ID
		InputDialog dialog = new InputDialog(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), NEW_COLUMN_TITLE,
				NEW_COLUMN_DESCRIPTION, NEW_COLUMN_TEXT, //$NON-NLS-2$
				new IInputValidator() {

					public String isValid(String newText) {
						return isValid2(newText, project);
					};
				});

		if (dialog.open() == IDialogConstants.OK_ID) {
			newColumn = new ColumnInfo(dialog.getValue(), dialog.getValue(),
					null, true);
		}

		return newColumn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema#promptRowName(org.eclipse.core.resources.IProject)
	 */
	@Override
	public RowInfo[] promptRowName(final IProject iProject) {
		RowInfo[] rowInfo = null;

		NewRowInputDialog dialog = new NewRowInputDialog(PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getShell(),
				iProject, NEW_ROW_TITLE);

		if (dialog.open() == IDialogConstants.OK_ID) {
			rowInfo = new RowInfo[dialog.getNumEntries()];
			String key = dialog.getKey();
			boolean isArray = dialog.isArray();
			for (int i = 0; i < dialog.getNumEntries(); i++) {
				rowInfo[i] = new RowInfo(key, isArray, null);
			}
		}

		return rowInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema #promptTranslatedCollumnName()
	 */
	@Override
	public TranslationInfo promptTranslatedCollumnName(final IProject project,
			String selectedColumn) {
		TranslationInfo newColumn = null;

		// Ask user for the ID
		TranslateColumnInputDialog dialog = new TranslateColumnInputDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				project, selectedColumn, NEW_TRANSLATE_COLUMN_TITLE,
				NEW_COLUMN_DESCRIPTION, NEW_COLUMN_TEXT, new IInputValidator() {

					public String isValid(String newText) {
						return isValid2(newText, project);
					};
				});

		if (dialog.open() == IDialogConstants.OK_ID) {

			newColumn = new TranslationInfo(dialog.getValue(),
					dialog.getValue(), null, true, dialog.getFromLanguage(),
					dialog.getToLanguage(), null, dialog.getTranslator());
		}

		return newColumn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema #promptTranslatedCollumnName()
	 */
	@Override
	public TranslationInfo[] promptTranslatedCollumnsName(
			final IProject project, String selectedColumn,
			String[] selectedKeys, String[] selectedCells, TableColumn[] columns) {
		TranslationInfo[] newColumns = null;

		// Ask user for the ID
		TranslateColumnsInputDialog dialog = new TranslateColumnsInputDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				project, selectedColumn, selectedCells, columns,
				TRANSLATE_CELLS_TITLE);

		if (dialog.open() == IDialogConstants.OK_ID) {
			List<TranslateColumnsInputDialog.DestinationColumn> destinationColumns = dialog
					.getDestinationColumns();
			newColumns = new TranslationInfo[selectedCells.length
					* destinationColumns.size()];

			int count = 0;
			for (int i = 0; i < selectedCells.length; i++) {
				String selectedCell = selectedCells[i];
				String selectedKey = selectedKeys[i];
				for (int j = 0; j < destinationColumns.size(); j++) {
					TranslateColumnsInputDialog.DestinationColumn destColumn = destinationColumns
							.get(j);
					newColumns[count] = new TranslationInfo(
							destColumn.getText(), destColumn.getText(), null,
							true, dialog.getFromLanguage(),
							destColumn.getLang(), selectedCell,
							dialog.getTranslator());
					newColumns[count].setFromKey(selectedKey);
					newColumns[count].setToColumn(destColumn.getText());
					count++;
				}
			}

		}

		return newColumns;
	}

	/**
	 * Checks if the current input is a valid column name for a new column
	 * 
	 * @param value
	 *            the new column name
	 * @return true if it is a valid column name
	 */
	private String isValid2(String value, IProject project) {

		String result = null;

		if (value.startsWith(LOCALIZATION_FILES_FOLDER)) {

			AndroidLocalizationSchema schema = new AndroidLocalizationSchema();
			String id = value.replace(LOCALIZATION_FILES_FOLDER, ""); //$NON-NLS-1$
			LocaleInfo info = schema.getLocaleInfoFromID(id);
			ProjectLocalizationManager manager = null;
			try {
				manager = LocalizationManager.getInstance()
						.getProjectLocalizationManager(project, false);
			} catch (IOException e) {
			}

			if ((info.getLocaleAttributes().size() > 0)
					|| (value
							.equalsIgnoreCase(AndroidLocalizationSchema.LOCALIZATION_FILES_FOLDER))) {
				StringLocalizationFile file = manager.getLocalizationProject()
						.getLocalizationFile(info);
				if (file != null && !file.isToBeDeleted()) {
					result = Messages.AndroidNewColumnProvider_Dialog_FileAlreadyExists;
				}
			} else {
				result = NEW_COLUMN_INVALID_ID;
			}

		} else {
			result = NEW_COLUMN_INVALID_ID;
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema #getEditorName()
	 */
	@Override
	public String getEditorName() {
		return Messages.AndroidStringEditorInput_EditorTooltip;
	}

	/**
	 * Create an Android string localization file. It's a XML which has the
	 * following format:
	 * 
	 * <?xml version="1.0" encoding="utf-8"?> <resources> <string
	 * name="KEY">VALUE</string> ... </resources>
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.ILocalizationSchema
	 *      #createStringFile(org.eclipse.sequoyah.localization.tools.datamodel.LocaleInfo)
	 */
	public void createLocalizationFile(LocalizationFile localizationFile)
			throws SequoyahException {

		// try {
		// String filePath = localizationFile.getFile().getFullPath()
		// .toOSString();
		//
		// if (!localizationFile.getFile().exists()) {
		// localizationFile.getFile().getLocation();
		// IPath fileToSave = null;
		// if (localizationFile.getLocalizationProject() != null) {
		// fileToSave = new Path(localizationFile
		// .getLocalizationProject().getProject()
		// .getLocation()
		// + filePath);
		// } else {
		// fileToSave = localizationFile.getFile().getLocation();
		// }
		//
		// fileToSave.removeLastSegments(1).toFile().mkdirs();
		// fileToSave.toFile().createNewFile();
		//
		// if (localizationFile.getLocalizationProject() != null) {
		// IFile iFile = localizationFile.getLocalizationProject()
		// .getProject().getFile(new Path(filePath));
		// localizationFile.setFile(iFile);
		// }
		// }
		//
		// DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// DocumentBuilder db = dbf.newDocumentBuilder();
		// Document document = db.newDocument();
		//
		// /*
		// * Create XML nodes
		// */
		// Element resources = document.createElement(XML_RESOURCES_TAG);
		//
		// // Simple entries
		// for (StringNode stringNode : ((StringLocalizationFile)
		// localizationFile)
		// .getStringNodes()) {
		// addSingleEntry(document, resources, stringNode);
		// }
		//
		// // Arrays
		// for (StringArray stringArray : ((StringLocalizationFile)
		// localizationFile)
		// .getStringArrays()) {
		// addArrayEntry(document, resources, stringArray);
		// }
		//
		// document.appendChild(resources);
		//
		// saveXMLDocument(localizationFile.getFile().getLocation().toFile(),
		// document);
		// ((AndroidStringLocalizationFile) localizationFile)
		// .setSavedXMLDocument(document);
		// localizationFile
		// .getFile()
		// .getProject()
		// .refreshLocal(IResource.DEPTH_INFINITE,
		// new NullProgressMonitor());
		// //
		// loadAllFiles(localizationFile.getLocalizationProject().getProject());
		//
		// } catch (Exception e) {
		// throw new SequoyahException();
		// }

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema #getLocaleToolTip(org.eclipse.core.runtime.Path)
	 */
	@Override
	public String getLocaleToolTip(IPath path) {
		LocaleInfo locale = getLocaleInfoFromPath(path);
		List<LocaleAttribute> attributes = locale.getLocaleAttributes();
		String result = ""; //$NON-NLS-1$
		for (Iterator<LocaleAttribute> iterator = attributes.iterator(); iterator
				.hasNext();) {
			if (result.length() > 0) {
				result = result + "\n "; //$NON-NLS-1$
			}
			LocaleAttribute localeAttribute = iterator.next();
			result = result
					+ ((AndroidLocaleAttribute) localeAttribute)
							.getDisplayName()
					+ ": " //$NON-NLS-1$
					+ ((AndroidLocaleAttribute) localeAttribute)
							.getDisplayValue();
		}

		if (result.length() == 0) {
			result = DEFAULT_LOCALE_TOOLTIP;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema #getLocaleAttributes()
	 */
	@Override
	public List<LocaleAttribute> getLocaleAttributes() {
		List<LocaleAttribute> localeAttributes = new ArrayList<LocaleAttribute>();

		localeAttributes.add(new AndroidLocaleAttribute(new Integer(123),
				AndroidLocaleAttributes.COUNTRY_CODE.ordinal()));
		localeAttributes.add(new AndroidLocaleAttribute(new Integer(000),
				AndroidLocaleAttributes.NETWORK_CODE.ordinal()));
		localeAttributes.add(new AndroidLocaleAttribute(null,
				AndroidLocaleAttributes.LANGUAGE.ordinal()));
		localeAttributes.add(new AndroidLocaleAttribute(null,
				AndroidLocaleAttributes.REGION.ordinal()));
		localeAttributes.add(new AndroidLocaleAttribute(null,
				AndroidLocaleAttributes.SCREEN_SIZE.ordinal()));
		localeAttributes.add(new AndroidLocaleAttribute(null,
				AndroidLocaleAttributes.SCREEN_ORIENTATION.ordinal()));
		localeAttributes.add(new AndroidLocaleAttribute(new Integer(12),
				AndroidLocaleAttributes.PIXEL_DENSITY.ordinal()));
		localeAttributes.add(new AndroidLocaleAttribute(null,
				AndroidLocaleAttributes.TOUCH_TYPE.ordinal()));
		localeAttributes.add(new AndroidLocaleAttribute(null,
				AndroidLocaleAttributes.KEYBOARD_STATE.ordinal()));
		localeAttributes.add(new AndroidLocaleAttribute(null,
				AndroidLocaleAttributes.TEXT_INPUT_METHOD.ordinal()));
		localeAttributes.add(new AndroidLocaleAttribute(null,
				AndroidLocaleAttributes.NAVIGATION_METHOD.ordinal()));
		localeAttributes.add(new AndroidLocaleAttribute(new Dimension(1, 1),
				AndroidLocaleAttributes.SCREEN_DIMENSION.ordinal()));

		return localeAttributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema #getLocalizationFileExtensions()
	 */
	@Override
	public List<String> getLocalizationFileExtensions() {
		List<String> localizationFileExtensions = new ArrayList<String>();
		localizationFileExtensions.add(FILE_EXTENSION);
		return localizationFileExtensions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema
	 * #getLocalizationFiles(org.eclipse.core.resources.IProject)
	 */
	@Override
	public Map<LocaleInfo, IFile> getLocalizationFiles(IProject project) {

		Map<LocaleInfo, IFile> localizationFiles = new LinkedHashMap<LocaleInfo, IFile>();
		boolean hasDefault = false;
		try {

			IResource resourcesFolder = project.findMember(RESOURCES_FOLDER);
			if ((resourcesFolder != null)
					&& (resourcesFolder instanceof IFolder)) {

				IResource[] folders = ((IFolder) resourcesFolder).members();
				if (folders != null) {
					/*
					 * Iterate in folders inside the resources folder
					 */
					for (IResource folder : folders) {
						if (folder.getName().startsWith(
								LOCALIZATION_FILES_FOLDER)) {
							IResource[] files = ((IFolder) folder).members();
							for (IResource file : files) {

								if ((file instanceof IFile)
										&& (isLocalizationFile((IFile) file))) {
									// TODO WARNING 2 put LocalizationFiles in the map, not an IFile
									localizationFiles.put(
											getLocaleInfoFromPath(file
													.getProjectRelativePath()),
											(IFile) file);
									if (folder.getName().equals(
											LOCALIZATION_FILES_FOLDER)) {
										hasDefault = true;
									}
								}
							}
						}
					}
				}
			}
			// if a default file (typically values/strings.xml does not exists,
			// create it
			if (!hasDefault) {
				if (resourcesFolder instanceof IFolder) {
					IFolder folder = (IFolder) resourcesFolder;
					final IFolder valuesFolder = folder
							.getFolder(LOCALIZATION_FILES_FOLDER);
					try {
						if (!valuesFolder.exists()) {

							// try to create the folder
							PlatformUI
									.getWorkbench()
									.getProgressService()
									.run(false, false,
											new IRunnableWithProgress() {

												public void run(
														IProgressMonitor monitor)
														throws InvocationTargetException,
														InterruptedException {
													try {
														valuesFolder.create(
																true, true,
																monitor);
													} catch (CoreException e) {
														// do nothing
													}

												}
											});
						}
						// check if folder was created
						// create the default file
						if (valuesFolder.exists()) {
							IFile valuesFile = valuesFolder
									.getFile(LOCALIZATION_FILE_NAME);
							localizationFiles.put(
									getLocaleInfoFromPath(valuesFile
											.getProjectRelativePath()),
									valuesFile);
						}

					} catch (Exception e) {
						// do nothing, just exit
					}

				}
			}
		} catch (CoreException e) {
			//
		}
		return localizationFiles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema #isLocalizationFile(org.eclipse.core.resources.IFile)
	 */
	@Override
	public boolean isLocalizationFile(IFile file) {

		boolean result = false;
		if (file != null) {
			// TODO WARNING 1 fix regular expression for each type of localizable resource
			if (file.getProjectRelativePath().toString()
					.matches(LF_REGULAR_EXPRESSION)) {

				result = true;
			}
		}
		return result;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema #loadAllFiles()
	 */
	@Override
	public Map<LocaleInfo, LocalizationFile> loadAllFiles(IProject project)
			throws SequoyahException {

		Map<LocaleInfo, LocalizationFile> filesMap = new LinkedHashMap<LocaleInfo, LocalizationFile>();

		Map<LocaleInfo, IFile> localizationFiles = getLocalizationFiles(project);

		for (Map.Entry<LocaleInfo, IFile> entry : localizationFiles.entrySet()) {
			// TODO WARNING 3 fix entries: based on the type of the resource, choose a specific manager
			filesMap.put(entry.getKey(), loadFile("", entry.getValue()));
		}

		return filesMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema #loadFile()
	 */
	@Override
	public LocalizationFile loadFile(String type, IFile file)
			throws SequoyahException {
		LocalizationFile localizationFile = new LocalizationFile();
		localizationFile
				.setLocaleInfo(getLocaleInfoFromPath(file.getFullPath()));
		localizationFile.setFile(file);

		ILocalizationFileManager manager = LocalizationFileManagerFactory
				.getInstance().createLocalizationFileManager(type);
		
		LocalizationFile locFile = manager.loadFile(localizationFile);

		return locFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema #
	 * updateFile(org.eclipse.sequoyah.localization.tools.datamodel
	 * .LocalizationFile)
	 */
	@Override
	public void updateFile(LocalizationFile localizationFile)
			throws SequoyahException {
		
		Class aClass = typesMap.get(localizationFile);
		ILocalizationFileManager manager = LocalizationFileManagerFactory
		.getInstance().createLocalizationFileManager(aClass.getName());
		manager.updateFile(localizationFile);		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema
	 * #createLocalizationFile(org.eclipse.core.resources.IFile,
	 * org.eclipse.sequoyah.localization.tools.datamodel.LocaleInfo,
	 * java.util.List, java.util.List)
	 */
	@Override
	public LocalizationFile createLocalizationFile(LocalizationFileBean bean) {
		return LocalizationFileFactory.getInstance().createLocalizationFile(
				bean);
	}

	@Override
	public Object getLocalizationFileContent(LocalizationFile locFile) {
		Class aClass = typesMap.get(locFile);
		ILocalizationFileManager manager = LocalizationFileManagerFactory
		.getInstance().createLocalizationFileManager(aClass.getName());
		return manager.getLocalizationFileContent(locFile);	
	}

	@Override
	public void updateLocalizationFileContent(
			LocalizationFile locFile, String content)
			throws SequoyahException {
		Class aClass = typesMap.get(locFile);
		ILocalizationFileManager manager = LocalizationFileManagerFactory
		.getInstance().createLocalizationFileManager(aClass.getName());
		manager.updateLocalizationFileContent(locFile, content);	
	}	
	

	/*
	 * 
	 */
	private boolean isKnownNode(Node visitingNode) {
		return visitingNode.getNodeName().equals(XML_STRING_TAG)
				|| visitingNode.getNodeName().equals(XML_STRING_ARRAY_TAG);
	}

	/**
	 * Given a localization file path, returns the language information
	 * (attributes) of this localization file according to the file name.
	 * 
	 * @return LanguageInfo object with the attributes of this language
	 */
	private LocaleInfo getLocaleInfoFromPath(IPath path) {

		IPath folder = path;
		folder = path.removeLastSegments(1);
		String folderName = folder.lastSegment();
		String id = folderName.replace(LOCALIZATION_FILES_FOLDER, ""); //$NON-NLS-1$

		return getLocaleInfoFromID(id);

	}

	/**
	 * @param lang
	 * @return
	 */
	@Override
	public String getPathFromLocaleInfo(LocaleInfo lang) {

		String result;
		if (lang.getLocaleAttributes().size() > 0) {
			// There are qualifiers to concatenate in the folder name
			result = RESOURCES_FOLDER + File.separator
					+ LOCALIZATION_FILES_FOLDER + QUALIFIER_SEP
					+ getLocaleID(lang) + File.separator
					+ LOCALIZATION_FILE_NAME;
		} else {
			// It is a default location file (no language qualifier)
			result = RESOURCES_FOLDER + File.separator
					+ LOCALIZATION_FILES_FOLDER + File.separator
					+ LOCALIZATION_FILE_NAME;

		}
		return result;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema
	 * #getLocaleID(org.eclipse.sequoyah.localization.tools.datamodel
	 * .LocaleInfo)
	 */
	@Override
	public String getLocaleID(LocaleInfo localeInfo) {
		String localeID = ""; //$NON-NLS-1$
		List<LocaleAttribute> localeAttributes = localeInfo
				.getLocaleAttributes();
		for (Iterator<LocaleAttribute> iterator = localeAttributes.iterator(); iterator
				.hasNext();) {
			LocaleAttribute localeAttribute = iterator.next();
			if (((AndroidLocaleAttribute) localeAttribute).isSet()) {
				if (localeID.length() != 0) {
					localeID = localeID + QUALIFIER_SEP;
				}
				localeID = localeID + localeAttribute.getFolderValue();
			}
		}
		return localeID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.sequoyah.localization.tools.extensions.classes.
	 * ILocalizationSchema #getLocaleInfoFromID(java.lang.String)
	 */
	@Override
	public LocaleInfo getLocaleInfoFromID(String ID) {

		LocaleInfo result = new LocaleInfo();

		String[] segments = ID.split(QUALIFIER_SEP);
		int lastQualifier = -1;

		List<LocaleAttribute> localeAttributes = new ArrayList<LocaleAttribute>();

		for (int i = 1; i < segments.length; i++) {

			if (segments[i].equals("")) { //$NON-NLS-1$
				// Do nothiing
			} else if (isCountryCodeSegment(segments[i])
					&& (lastQualifier < AndroidLocaleAttributes.COUNTRY_CODE
							.ordinal())) {
				lastQualifier = AndroidLocaleAttributes.COUNTRY_CODE.ordinal();
				localeAttributes.add(new AndroidLocaleAttribute(segments[i],
						AndroidLocaleAttributes.COUNTRY_CODE.ordinal()));
			} else if (isNetworkCodeSegment(segments[i])
					&& (lastQualifier < AndroidLocaleAttributes.NETWORK_CODE
							.ordinal())) {
				lastQualifier = AndroidLocaleAttributes.NETWORK_CODE.ordinal();
				localeAttributes.add(new AndroidLocaleAttribute(segments[i],
						AndroidLocaleAttributes.NETWORK_CODE.ordinal()));
			} else if (isLanguageSegment(segments[i])
					&& (lastQualifier < AndroidLocaleAttributes.LANGUAGE
							.ordinal())) {
				lastQualifier = AndroidLocaleAttributes.LANGUAGE.ordinal();
				localeAttributes.add(new AndroidLocaleAttribute(segments[i],
						AndroidLocaleAttributes.LANGUAGE.ordinal()));
			} else if (isRegionSegment(segments[i])
					&& (lastQualifier < AndroidLocaleAttributes.REGION
							.ordinal())) {
				lastQualifier = AndroidLocaleAttributes.REGION.ordinal();
				localeAttributes.add(new AndroidLocaleAttribute(segments[i],
						AndroidLocaleAttributes.REGION.ordinal()));
			} else if (isScreenSizeSegment(segments[i])
					&& lastQualifier < AndroidLocaleAttributes.SCREEN_SIZE
							.ordinal()) {
				lastQualifier = AndroidLocaleAttributes.SCREEN_SIZE.ordinal();
				localeAttributes.add(new AndroidLocaleAttribute(segments[i],
						AndroidLocaleAttributes.SCREEN_SIZE.ordinal()));
			} else if (isOrientationSegment(segments[i])
					&& (lastQualifier < AndroidLocaleAttributes.SCREEN_ORIENTATION
							.ordinal())) {
				lastQualifier = AndroidLocaleAttributes.SCREEN_ORIENTATION
						.ordinal();
				localeAttributes.add(new AndroidLocaleAttribute(segments[i],
						AndroidLocaleAttributes.SCREEN_ORIENTATION.ordinal()));
			} else if (isPixelDensitySegment(segments[i])
					&& (lastQualifier < AndroidLocaleAttributes.PIXEL_DENSITY
							.ordinal())) {
				lastQualifier = AndroidLocaleAttributes.PIXEL_DENSITY.ordinal();
				localeAttributes.add(new AndroidLocaleAttribute(segments[i],
						AndroidLocaleAttributes.PIXEL_DENSITY.ordinal()));
			} else if (isTouchTypeSegment(segments[i])
					&& (lastQualifier < AndroidLocaleAttributes.TOUCH_TYPE
							.ordinal())) {
				lastQualifier = AndroidLocaleAttributes.TOUCH_TYPE.ordinal();
				localeAttributes.add(new AndroidLocaleAttribute(segments[i],
						AndroidLocaleAttributes.TOUCH_TYPE.ordinal()));
			} else if (isKeyboardStateSegment(segments[i])
					&& (lastQualifier < AndroidLocaleAttributes.KEYBOARD_STATE
							.ordinal())) {
				lastQualifier = AndroidLocaleAttributes.KEYBOARD_STATE
						.ordinal();
				localeAttributes.add(new AndroidLocaleAttribute(segments[i],
						AndroidLocaleAttributes.KEYBOARD_STATE.ordinal()));
			} else if (isTextInputSegment(segments[i])
					&& (lastQualifier < AndroidLocaleAttributes.TEXT_INPUT_METHOD
							.ordinal())) {
				lastQualifier = AndroidLocaleAttributes.TEXT_INPUT_METHOD
						.ordinal();
				localeAttributes.add(new AndroidLocaleAttribute(segments[i],
						AndroidLocaleAttributes.TEXT_INPUT_METHOD.ordinal()));
			} else if (isNavigationSegment(segments[i])
					&& (lastQualifier < AndroidLocaleAttributes.NAVIGATION_METHOD
							.ordinal())) {
				lastQualifier = AndroidLocaleAttributes.NAVIGATION_METHOD
						.ordinal();
				localeAttributes.add(new AndroidLocaleAttribute(segments[i],
						AndroidLocaleAttributes.NAVIGATION_METHOD.ordinal()));
			} else if (isDimensionSegment(segments[i])
					&& (lastQualifier < AndroidLocaleAttributes.SCREEN_DIMENSION
							.ordinal())) {
				lastQualifier = AndroidLocaleAttributes.SCREEN_DIMENSION
						.ordinal();
				localeAttributes.add(new AndroidLocaleAttribute(segments[i],
						AndroidLocaleAttributes.SCREEN_DIMENSION.ordinal()));
			} else if (isAPIVersionSegment(segments[i])
					&& (lastQualifier < AndroidLocaleAttributes.API_VERSION
							.ordinal())) {
				lastQualifier = AndroidLocaleAttributes.API_VERSION.ordinal();
				localeAttributes.add(new AndroidLocaleAttribute(segments[i],
						AndroidLocaleAttributes.API_VERSION.ordinal()));
			} else {
				localeAttributes = new ArrayList<LocaleAttribute>();
			}

		}

		result.setLocaleAttributes(localeAttributes);

		return result;
	}

	@Override
	public String getISO639LangFromID(String ID) {
		String iso639 = null;

		LocaleInfo localeInfo = getLocaleInfoFromID(ID);
		for (LocaleAttribute locAtt : localeInfo.getLocaleAttributes()) {
			if (locAtt.getDisplayName().equals("Language")) { //$NON-NLS-1$
				iso639 = locAtt.getFolderValue();
			}
		}

		return iso639;
	}

	@Override
	public String getDefaultID() {
		return LOCALIZATION_FILES_FOLDER;
	}

	private boolean isNetworkCodeSegment(String value) {
		return value.startsWith("mnc"); //$NON-NLS-1$

	}

	private boolean isLanguageSegment(String value) {
		return (value.length() == 2);

	}

	private boolean isRegionSegment(String value) {
		return ((value.startsWith("r") && (value.length() == 3))); //$NON-NLS-1$

	}

	private boolean isOrientationSegment(String value) {
		return ((value.equalsIgnoreCase("port") //$NON-NLS-1$
				|| value.equalsIgnoreCase("land") || value //$NON-NLS-1$
				.equalsIgnoreCase("square"))); //$NON-NLS-1$

	}

	private boolean isPixelDensitySegment(String value) {
		return (value.endsWith("dpi")); //$NON-NLS-1$

	}

	private boolean isTouchTypeSegment(String value) {
		return ((value.equalsIgnoreCase("notouch") //$NON-NLS-1$
				|| value.equalsIgnoreCase("stylus") || value //$NON-NLS-1$
				.equalsIgnoreCase("finger"))); //$NON-NLS-1$
	}

	private boolean isKeyboardStateSegment(String value) {
		return ((value.equalsIgnoreCase("keysexposed") || value //$NON-NLS-1$
				.equalsIgnoreCase("keyshidden"))); //$NON-NLS-1$

	}

	private boolean isTextInputSegment(String value) {
		return ((value.equalsIgnoreCase("nokeys") //$NON-NLS-1$
				|| value.equalsIgnoreCase("qwerty") || value //$NON-NLS-1$
				.equalsIgnoreCase("12key"))); //$NON-NLS-1$

	}

	private boolean isNavigationSegment(String value) {
		return ((value.equalsIgnoreCase("dpad") //$NON-NLS-1$
				|| value.equalsIgnoreCase("trackball") //$NON-NLS-1$
				|| value.equalsIgnoreCase("wheel") || value //$NON-NLS-1$
				.equalsIgnoreCase("nonav"))); //$NON-NLS-1$

	}

	private boolean isDimensionSegment(String value) {
		boolean result = false;
		if (value.contains("x")) { //$NON-NLS-1$
			String[] ints = value.split("x"); //$NON-NLS-1$
			if (ints.length == 2) {
				result = true;
			}
		}
		return result;

	}

	private boolean isCountryCodeSegment(String value) {
		return value.startsWith("mcc"); //$NON-NLS-1$

	}

	private boolean isScreenSizeSegment(String value) {
		return value.equalsIgnoreCase("large") //$NON-NLS-1$
				|| value.equalsIgnoreCase("normal") //$NON-NLS-1$
				|| value.equalsIgnoreCase("small"); //$NON-NLS-1$
	}

	private boolean isAPIVersionSegment(String value) {
		return value.startsWith("v"); //$NON-NLS-1$
	}
}