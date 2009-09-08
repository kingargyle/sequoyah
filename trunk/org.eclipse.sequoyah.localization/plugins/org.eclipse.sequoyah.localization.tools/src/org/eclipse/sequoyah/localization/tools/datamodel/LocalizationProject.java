/********************************************************************************
 * Copyright (c) 2009 Motorola Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * Vinicius Hernandes (Motorola)
 * Matheus Tait Lima (Eldorado)
 * 
 * Contributors:
 * name (company) - description.
 ********************************************************************************/
package org.eclipse.tml.localization.tools.datamodel;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;

/**
 * This class represents a real project and contains other information about it
 */
public class LocalizationProject {

	/*
	 * A reference to the project being represented
	 */
	private IProject project;

	/*
	 * The list of LocalizationFiles which belong to the project
	 */
	private List<LocalizationFile> localizationFiles;

	/*
	 * Whether the data in the model has been modified and differs from the values saved or not
	 */
	private boolean dirty;

	/*
	 * Whether there are changes in the associated meta-data / extra-info or not
	 */
	private boolean dirtyMetaExtraData;

	public LocalizationProject(IProject project, List<LocalizationFile> files) {
		this.project = project;
		this.localizationFiles = files;

		for (Iterator<LocalizationFile> iterator = localizationFiles.iterator(); iterator
				.hasNext();) {
			LocalizationFile localizationFile = iterator.next();
			localizationFile.setLocalizationProject(this);
		}

	}

	/**
	 * Get the project that is being represented 
	 * 
	 * @return the project that is being represented 
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * Set the project that is being represented 
	 *  
	 * @param project the project that is being represented 
	 */
	public void setProject(IProject project) {
		this.project = project;
	}

	/**
	 * Get the list of LocalizationFiles which belong to the project
	 * 
	 * @return the list of LocalizationFiles which belong to the project
	 */
	public List<LocalizationFile> getLocalizationFiles() {
		return localizationFiles;
	}

	/**
	 * Set the list of LocalizationFiles which belong to the project
	 * 
	 * @param localizationFiles the list of LocalizationFiles which belong to the project
	 */
	public void setLocalizationFiles(List<LocalizationFile> localizationFiles) {
		this.localizationFiles = localizationFiles;
	}

	/**
	 * Check whether the data in the model has been modified and differs from the values saved or not
	 * 
	 * @return true if the data in the model has been modified and differs from the values saved, false otherwise
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Set whether the data in the model has been modified and differs from the values saved or not
	 * 
	 * @param dirty true if the data in the model has been modified and differs from the values saved, false otherwise
	 */
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	/**
	 * Check whether there are changes in the associated meta-data / extra-info or not
	 * 
	 * @return true if there are changes in the associated meta-data / extra-info, false otherwise
	 */
	public boolean isDirtyMetaExtraData() {
		return dirtyMetaExtraData;
	}

	/**
	 * Set whether there are changes in the associated meta-data / extra-info or not
	 * 
	 * @param dirtyMetaExtraData true if there are changes in the associated meta-data / extra-info, false otherwise
	 */
	public void setDirtyMetaExtraData(boolean dirtyMetaExtraData) {
		this.dirtyMetaExtraData = dirtyMetaExtraData;
	}

	/**
	 * Get the localization file for a specific locale
	 * 
	 * @param localeInfo the LocaleInfo object that represents the locale
	 * @return the localization file for the given locale
	 */
	public LocalizationFile getLocalizationFile(LocaleInfo localeInfo) {
		LocalizationFile localizationFile = null;

		boolean found = false;
		Iterator<LocalizationFile> iterator = localizationFiles.iterator();
		while (iterator.hasNext() && !found) {
			LocalizationFile file = iterator.next();

			if (file.getLocaleInfo().equals(localeInfo)) {
				localizationFile = file;
				found = true;
			}
		}

		return localizationFile;
	}

	/**
	 * @param localizationFile
	 * @return
	 */
	public boolean addLocalizationFile(LocalizationFile localizationFile) {
		localizationFile.setLocalizationProject(this);
		return localizationFiles.add(localizationFile);
	}

	/**
	 * @param localizationFile
	 * @return
	 */
	public boolean removeLocalizationFile(LocalizationFile localizationFile) {
		return localizationFiles.remove(localizationFile);
	}

}
