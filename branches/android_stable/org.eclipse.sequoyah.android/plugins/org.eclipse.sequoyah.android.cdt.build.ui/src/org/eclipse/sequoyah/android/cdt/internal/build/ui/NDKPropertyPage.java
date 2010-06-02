package org.eclipse.sequoyah.android.cdt.internal.build.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.sequoyah.android.cdt.build.core.INDKService;
import org.eclipse.sequoyah.android.cdt.build.core.NDKUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * DESCRIPTION: 
 * This class represents the properties page for Android NDK.
 * It gives the user the option to set gcc version, platform, 
 * source object and library paths.
 * */
public class NDKPropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{

    //UI fields
    private Text libraryTxt;

    //selected project
    private IProject project = null;

    @Override
    protected Control createContents(Composite parent)
    {
        IProjectNature nature = null;
        IAdaptable apt = getElement();
        //get selected project
        if (apt instanceof IProject)
        {
            if (apt instanceof IJavaProject)
            {
                IJavaProject resource = (IJavaProject) getElement();
                project = resource.getProject();
            }
            else
            {
                project = (IProject) apt;
            }

            try
            {
                nature = project.getNature("org.eclipse.cdt.core.cnature");
            }
            catch (CoreException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        Composite main = new Composite(parent, SWT.FILL);
        main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        main.setLayout(new GridLayout(1, false));

        Composite auxComp = new Composite(main, SWT.FILL);
        auxComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        auxComp.setLayout(new GridLayout(6, true));

        //platform fields
        Label libraryLabel = new Label(auxComp, SWT.FILL);
        libraryLabel.setText("Native Library Name");
        libraryTxt = new Text(auxComp, SWT.FILL | SWT.BORDER);
        libraryTxt.setEditable(true);
        libraryTxt.setText(getInitialValue(INDKService.libName));
        libraryTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        if (nature == null)
        {
            libraryTxt.setEnabled(false);
        }
        else
        {
            libraryTxt.setEnabled(true);
        }

        Label dummyLabel2 = new Label(auxComp, SWT.None);
        dummyLabel2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 4, 1));

        return main;
    }

    /**initialize properties values
     * 
     * @param propertyId
     * @return
     */
    private String getInitialValue(QualifiedName propertyId)
    {
        String returnValue = "";
        try
        {
            if (project.getPersistentProperty(propertyId) != null)
            {
                returnValue = project.getPersistentProperty(propertyId);
            }
        }
        catch (CoreException e1)
        {
            returnValue = "";
        }
        return returnValue;
    }

    @Override
    /**called when apply button is pressed
     * 
     */
    public void performApply()
    {
        try
        {
            // Check if the value was changed and generate a new Android make file accordingly
            if ((project.getPersistentProperty(INDKService.libName) == null)
                    || !project.getPersistentProperty(INDKService.libName).equals(
                            libraryTxt.getText().trim()))
            {
                project.setPersistentProperty(INDKService.libName, libraryTxt.getText().trim());
                // Generate a new makefile
                NDKUtils.generateAndroidMakeFile(project, libraryTxt.getText().trim());
            }
        }
        catch (CoreException e1)
        {

        }
    }

    @Override
    /**called when ok button is pressed
     * 
     */
    public boolean performOk()
    {

        performApply();

        return super.performOk();
    }
}
