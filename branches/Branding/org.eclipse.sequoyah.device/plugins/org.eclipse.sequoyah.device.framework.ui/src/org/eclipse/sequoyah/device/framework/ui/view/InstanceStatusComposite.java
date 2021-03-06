/********************************************************************************
 * Copyright (c) 2008-2010 Motorola Inc. and Other. All rights reserved
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * Julia Martinez Perdigueiro (Eldorado Research Institute) 
 * [244805] - Improvements on Instance view  
 *
 * Contributors:
 * Julia Martinez Perdigueiro (Eldorado Research Institute) - [244856] - Instance View usability should be improved
 * Julia Martinez Perdigueiro (Eldorado Research Institute) - [247085] - Instance manage view buttons are resizing after applying services filter
 * Julia Martinez Perdigueiro (Eldorado Research Institute) - [247288] - Exceptions after Instance Mgt View is closed
 * Daniel Barboza Franco (Eldorado Research Institute) - Bug [248036] - New Icons for "New Instance" and "Filter services" on Device View
 * Yu-Fen Kuo (MontaVista)  - [236476] - provide a generic device type
 * Daniel Barboza Franco (Eldorado Research Institute) - Bug [250644] - Instance view keeps enabled buttons while performing a service.
 * Daniel Barboza Franco (Eldorado Research Institute) - Bug [252261] - Internal class MobileInstance providing functionalities
 * Daniel Barboza Franco (Eldorado Research Institute) - Bug [259243] - image in the wizards
 * Fabio Fantato (Instituto Eldorado) - [263188] - Create new examples to support tutorial presentation
 * Fabio Fantato (Instituto Eldorado) - [243494] Change the reference implementation to work on Galileo
 * Daniel Barboza Franco (Eldorado Research Institute) - Bug [246082] - Complement bug #245111 by allowing disable of "Properties" option as well
 * Daniel Barboza Franco (Eldorado Research Institute) - Bug [271807] - Improper use of PreferencesUtil.createPropertyDialogOn() on properties editor
 * Daniel Barboza Franco (Eldorado Research Institute) - Bug [274502] - Change labels: Instance Management view and Services label
 * Pablo Cobucci Leite (Eldorado Research Institute) - Bug [274977] - Instance Management View does not ask user before removing a instance
 * Daniel Barboza Franco (Eldorado Research Institute) - Bug [277469] - Device management view blinks when user performs operations
 * Daniel Barboza Franco (Eldorado Research Institute) - Bug [280981] - Add suport for selecting instances programatically 
 * Daniel Barboza Franco (Eldorado Research Institute) - Bug [281425] - Instance Management View does not remove instance listerners properly. 
 * Mauren Brenner (Eldorado) - [281377] Support device types whose instances cannot be created by user
 * Fabio Rigo (Eldorado) - Bug [288006] - Unify features of InstanceManager and InstanceRegistry
 * Daniel Barboza Franco - Bug [287996] - Dont show device selection dialog when there is only one device
 * Eric Cloninger (Motorola) - [287883] Adjust the status column in device management view - Modified relative widths
 * Daniel Pastore (Eldorado) - [289870] Moving and renaming Tml to Sequoyah
 ********************************************************************************/

package org.eclipse.sequoyah.device.framework.ui.view;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.sequoyah.device.framework.DeviceUtils;
import org.eclipse.sequoyah.device.framework.events.IInstanceListener;
import org.eclipse.sequoyah.device.framework.events.InstanceAdapter;
import org.eclipse.sequoyah.device.framework.events.InstanceEvent;
import org.eclipse.sequoyah.device.framework.events.InstanceEventManager;
import org.eclipse.sequoyah.device.framework.factory.DeviceTypeRegistry;
import org.eclipse.sequoyah.device.framework.manager.InstanceManager;
import org.eclipse.sequoyah.device.framework.model.AbstractMobileInstance;
import org.eclipse.sequoyah.device.framework.model.IDeviceType;
import org.eclipse.sequoyah.device.framework.model.IInstance;
import org.eclipse.sequoyah.device.framework.model.IService;
import org.eclipse.sequoyah.device.framework.model.handler.ServiceHandlerAction;
import org.eclipse.sequoyah.device.framework.status.IStatus;
import org.eclipse.sequoyah.device.framework.status.StatusRegistry;
import org.eclipse.sequoyah.device.framework.ui.DeviceUIPlugin;
import org.eclipse.sequoyah.device.framework.ui.view.model.InstanceMgtViewComparator;
import org.eclipse.sequoyah.device.framework.ui.view.model.InstanceSelectionChangeListener;
import org.eclipse.sequoyah.device.framework.ui.view.model.ViewerAbstractNode;
import org.eclipse.sequoyah.device.framework.ui.view.model.ViewerDeviceNode;
import org.eclipse.sequoyah.device.framework.ui.view.model.ViewerInstanceNode;
import org.eclipse.sequoyah.device.framework.ui.view.provider.InstanceMgtViewContentProvider;
import org.eclipse.sequoyah.device.framework.ui.view.provider.InstanceMgtViewLabelProvider;
import org.eclipse.sequoyah.device.framework.ui.wizard.DeviceWizardExtensionManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class InstanceStatusComposite extends Composite
{
	private static final String MENU_DELETE = "Delete"; //$NON-NLS-1$
	private static final String MENU_PROPERTIES = "Properties"; //$NON-NLS-1$
	private static final String MENU_NEW = "New...";  //$NON-NLS-1$
	private static final String TOOLBAR_NEW_TOOLTIP = "New Device"; //$NON-NLS-1$
	private static final String TOOLBAR_DIALOG_MESSAGE = "Select a Device to open the Creation Wizard :"; //$NON-NLS-1$
	private static final String ERROR_DIALOG_TITLE = "Error"; //$NON-NLS-1$
	private static final String ERROR_NO_WIZARD_MESSAGE = "No wizard found for Device "; //$NON-NLS-1$
	private static final int DEFAULT_MENU_IMAGE_SIZE = 16;


	/**
	 * The main viewer of the instance view. 
	 * It is responsible to construct the tree with columns for correct instance visualization 
	 */
	private TreeViewer viewer;

	private IViewSite viewSite;

    /**
     * The wizard actions
     */
    protected Map<String, Action> wizardActions = new TreeMap<String, Action>();
    
    private IInstanceListener listener = new InstanceAdapter()
	{
	    public void instanceLoaded(InstanceEvent e)
	    {
	        InstanceStatusComposite.this.instanceLoaded(e.getInstance());
	    }

	    public void instanceUnloaded(InstanceEvent e)
	    {
	        InstanceStatusComposite.this.instanceUnloaded(e.getInstance());
	    }

	    public void instanceUpdated(InstanceEvent e) {
	    	InstanceStatusComposite.this.instanceTransitioned(e.getInstance());
	    }
	    
	    public void instanceTransitioned(InstanceEvent e)
	    {
	        InstanceStatusComposite.this.instanceTransitioned(e.getInstance());
	    }
	};

	public InstanceStatusComposite(Composite parent, IViewSite viewSite)
	{
		super(parent, SWT.NONE);
		this.viewSite = viewSite;
		createContents();

		InstanceEventManager eventMgr = InstanceEventManager.getInstance();
		eventMgr.addInstanceListener(listener);
	}
	
	protected void addInstanceSelectionChangeListener(InstanceSelectionChangeListener listener)	{
		InstanceMgtView.addInstanceSelectionChangeListener(listener);
	}
	
	protected void removeInstanceSelectionChangeListener(InstanceSelectionChangeListener listener) {
		InstanceMgtView.removeInstanceSelectionChangeListener(listener);
	}
	
	private void notifyInstanceSelectionChangeListeners(IInstance instance)	{
		InstanceMgtView.notifyInstanceSelectionChangeListeners(instance);
	}

	protected IInstance getSelectedInstance()
	{
		IInstance instance = null;
		Object lastSelection = getLastSelection();
		
		if (lastSelection instanceof IInstance)
		{
		    instance = (IInstance) lastSelection;
		}
		
		return instance;
	}
	
	protected IDeviceType getSelectedDevice() {
		IDeviceType device = null;
		
		Object lastSelection = getLastSelection();
		
		if (lastSelection instanceof IDeviceType) {
			device = (IDeviceType) lastSelection;
		}
		
		return device;
	}

	private void createContents()
	{
		setLayout(new FillLayout());
		viewer = new TreeViewer(this, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		Tree tree = viewer.getTree();
		TableLayout layout = new TableLayout();
		tree.setLayout(layout);
		tree.setHeaderVisible(true);

		createColumn("Devices", 10); //$NON-NLS-1$
		createColumn("Status", 9); //$NON-NLS-1$

		InstanceMgtViewLabelProvider labelProvider = new InstanceMgtViewLabelProvider();
		viewer.setLabelProvider(labelProvider);

		viewer.setContentProvider(new InstanceMgtViewContentProvider());
		viewer.getTree().setLinesVisible(true);
		// Use the custom comparator in emulator views
		viewer.setComparator(new InstanceMgtViewComparator(labelProvider));
		viewer.setInput(viewSite);
		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent event) {
				notifyInstanceSelectionChangeListeners(getSelectedInstance());
			}	
		});

        createActions();   
		fillMenuContext();
		fillViewMenu();
		fillViewToolbar();
		//refreshViewer(InstanceMgtView.getSelectedInstance());
		refreshViewer(null);
		viewer.expandAll();
	}

    private void createActions()
    {
        for (final IDeviceType device : DeviceTypeRegistry.getInstance().getDeviceTypes())
        {
        	if ((!device.isAbstract()) && (device.supportsUserInstances())) {
	        	wizardActions.put(device.getLabel(), new Action(device.getLabel())
	            {
	                @Override
	                public void run()
	                {
	                    IWizard wizard = DeviceWizardExtensionManager.getInstance().getDeviceWizard(device.getId());
	                    if (wizard != null)
	                    {
	                        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	                        
	                        // Instantiates the wizard container with the wizard and opens it
	                        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
	                        dialog.create();
	                        dialog.open();
	                    }
	                    else
	                    {
	                        Display.getDefault().asyncExec(new Runnable()
	                        {
	                            public void run()
	                            {
	                                IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	                                MessageDialog.openError(ww.getShell(), ERROR_DIALOG_TITLE, ERROR_NO_WIZARD_MESSAGE + device.getLabel());
	                            }
	                        });
	                    }
	                }
	                
	                @Override
	                public String toString()
	                {
	                    return getText();
	                }
	            });
            }
        }
    }
    private void fillMenuContext()
    {
        final Menu menu = new Menu(viewer.getTree()); 
        viewer.getTree().setMenu(menu); 
        menu.addMenuListener(new MenuAdapter() { 
            public void menuShown(MenuEvent e) { 
                // Get rid of existing menu items 
                MenuItem[] items = menu.getItems(); 
                for (int i = 0; i < items.length; i++) { 
                    ((MenuItem) items[i]).dispose(); 
                }
                fillMenuContext(menu);              

            }
        }); 
    }

    private void fillMenuContext(Menu menu)
    {
        MenuItem newItem = null;

        ISelection selection = viewer.getSelection();
        if (selection instanceof IStructuredSelection)
        {
            IStructuredSelection strSelection = (IStructuredSelection) selection;
            Object firstSelection = strSelection.getFirstElement(); // TODO support multiple selection

            if (firstSelection instanceof ViewerInstanceNode)
            {
                ViewerInstanceNode node = (ViewerInstanceNode) firstSelection;
                if (node.containsInstance())
                {
                    IInstance instance = getSelectedInstance();
                    String statusId = instance.getStatus();
                    IStatus status = StatusRegistry.getInstance().getStatus(statusId);
                    IDeviceType device = DeviceUtils.getDeviceType(instance);
                    String deviceName = device.getLabel();
                    
                    if (device.supportsUserInstances()) {
                    	// menu item "New..."
                    	newItem = new MenuItem(menu, SWT.PUSH);
                    	newItem.setText(MENU_NEW);
                    	newItem.addListener(SWT.Selection, new WizardSelectionListener(deviceName));
                    
                    	newItem = new MenuItem(menu, SWT.SEPARATOR);

                    	// menu item "Delete"
                    	newItem = new MenuItem(menu, SWT.PUSH);
                    	newItem.setText(MENU_DELETE);
                    	newItem.addListener(SWT.Selection, new MenuDeleteListener(instance));
                    	newItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
                    	newItem.setEnabled(status.canDeleteInstance());

                    	newItem = new MenuItem(menu, SWT.SEPARATOR);
                    }

                    // menu item "Properties"
                    newItem = new MenuItem(menu, SWT.PUSH);
                    newItem.setText(MENU_PROPERTIES);
                    newItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(DeviceUIPlugin.ICON_PROPERTY));
                    newItem.addListener(SWT.Selection, new MenuPropertiesListener(instance));
                    newItem.setEnabled(status.canEditProperties());
                    
                    newItem = new MenuItem(menu, SWT.SEPARATOR);
                    
                    for (IService service:device.getServices()){
                        if (service.isVisible()) {
                        	
                        	boolean inTransition = ((AbstractMobileInstance)instance).getStateMachineHandler().isTransitioning();
                        	boolean isServiceEnabled = (service.getStatusTransitions(instance.getStatus())!=null);
                        	isServiceEnabled = isServiceEnabled && !inTransition;
                        	
                            newItem = new MenuItem(menu, SWT.PUSH);  
                            ImageData serviceImageData = service.getImage().getImageData().scaledTo(DEFAULT_MENU_IMAGE_SIZE, DEFAULT_MENU_IMAGE_SIZE);
                            Image serviceImage = new Image(getDisplay(), serviceImageData);
                            newItem.setImage(serviceImage);
                            newItem.setEnabled(isServiceEnabled);
                            newItem.setText(service.getName());
                            newItem.addListener(SWT.Selection,  new ServiceHandlerAction(instance,service.getHandler()));

                            // The listener below updates the services composite
                            final IInstance inst = instance;
                            newItem.addListener(SWT.Selection,  new Listener(){
    							public void handleEvent(Event event) {
    								InstanceMgtView.getInstanceServicesComposite().setSelectedInstance(inst);
    							}
    							
    						} );
                            
                        }
                    }
                }
            }
            else if (firstSelection instanceof ViewerDeviceNode)
            {
            	IDeviceType device = getSelectedDevice();
            	
            	if (device.supportsUserInstances()) {
            		// menu item "New..."
            		newItem = new MenuItem(menu, SWT.PUSH);
            		newItem.setText(MENU_NEW);
            		String deviceName = ((ViewerDeviceNode) firstSelection).getDeviceName();
            		newItem.addListener(SWT.Selection, new WizardSelectionListener(deviceName));
            	}
            }
        }  
    }
    
	private void fillViewMenu()
	{
	    IMenuManager rootMenuManager = viewSite.getActionBars().getMenuManager();
	    IMenuManager wizardSubmenu = new MenuManager(MENU_NEW);
        rootMenuManager.add(wizardSubmenu);
        for (Action action : wizardActions.values())
        {
            wizardSubmenu.add(action);
        }
	}
	
	private void fillViewToolbar()
	{
		    IToolBarManager toolbarManager = viewSite.getActionBars().getToolBarManager();
		    WizardDropDownAction newWizardAction = new WizardDropDownAction();
		    newWizardAction.setToolTipText(TOOLBAR_NEW_TOOLTIP);
		    
			if (wizardActions.size() <= 0) { 
				newWizardAction.setEnabled(false);
			}
	        toolbarManager.add(newWizardAction);
	}
	
	private void createColumn(String columnLabel, int columnWeight)
	{
		Tree tree = viewer.getTree();
		TableLayout layout = (TableLayout) tree.getLayout();
		layout.addColumnData(new ColumnWeightData(columnWeight));
		TreeColumn tc = new TreeColumn(tree, SWT.NONE);
		tc.setMoveable(true);
		tc.setText(columnLabel);
		tc.setResizable(true);

		tc.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				columnSelected(e);
			}
		});
		tc.addControlListener(new ControlAdapter()
		{
			/**
			 * @see org.eclipse.swt.events.ControlListener#controlMoved(ControlEvent)
			 */
			 public void controlMoved(ControlEvent e)
			 {
				 columnMoved(e);
			 }
		});
	}

	protected void refreshViewer(IInstance selectedInstance)
	{
	    Collection<String> expandedDevices = getExpandedDevices();
		viewer.refresh();
		expandToNodeValues(expandedDevices, selectedInstance);		

        notifyInstanceSelectionChangeListeners(getSelectedInstance());
	}
	
	private Collection<String> getExpandedDevices()
	{
	    Object[] expandedElements = viewer.getVisibleExpandedElements();

	    Collection<String> devicesCol = new HashSet<String>();

	    for (Object element : expandedElements)
	    {
	        if (element instanceof ViewerDeviceNode)
	        {
	            ViewerDeviceNode node = (ViewerDeviceNode) element;
	            devicesCol.add(node.getDevice().getId());
	        }
	    }
	    
	    return devicesCol;
	}
	
	private void expandToNodeValues(Collection<String> devices, IInstance instance)
	{
        for (TreeItem treeNode : viewer.getTree().getItems())
        {
            Object node = treeNode.getData();
            if (node instanceof ViewerDeviceNode)
            {
                ViewerDeviceNode deviceNode = (ViewerDeviceNode) node;
                if (devices.contains(deviceNode.getDevice().getId()))
                {
                    treeNode.setExpanded(true);
                    viewer.reveal(treeNode);
                    for (ViewerAbstractNode childNode : deviceNode.getChildren())
                    {
                        viewer.reveal(childNode);
                        
                        if ((instance != null) && (instance.equals(((ViewerInstanceNode)childNode).getInstance())))
                        {
                            treeNode.setExpanded(true);
                            viewer.reveal(treeNode);
                            viewer.setSelection(new StructuredSelection(childNode));
                        }
                    }
                }
            }
        }
	}
	
	private Object getLastSelection()
	{
	    Object lastSelection = null;
	    ISelection selection = viewer.getSelection();
        if (selection instanceof IStructuredSelection)
        {
            IStructuredSelection strSelection = (IStructuredSelection) selection;

            if (strSelection.size() == 1)
            {
                Object firstSelection = strSelection.getFirstElement();

                if (firstSelection instanceof ViewerInstanceNode)
                {
                    ViewerInstanceNode node = (ViewerInstanceNode) firstSelection;
                    lastSelection = node.getInstance();
                }
                else if (firstSelection instanceof ViewerDeviceNode)
                {
                    ViewerDeviceNode node = (ViewerDeviceNode) firstSelection;
                    lastSelection = node.getDevice();
                }
            }
        }
        
        return lastSelection;
	}

	private void columnSelected(SelectionEvent e)
	{
		// When a column is selected by the user, it is used as basis for sorting
		TreeColumn selectedColumn = (TreeColumn) e.widget;
		Tree tree = selectedColumn.getParent();
		int columnIndex = tree.indexOf(selectedColumn);
		InstanceMgtViewComparator comparator = (InstanceMgtViewComparator) viewer.getComparator();
		comparator.setColumnToSort(columnIndex);
		
		if (tree.getSortColumn() == selectedColumn)
		{
			comparator.toggleAscending();
		}

		tree.setSortColumn(selectedColumn);
		if (comparator.isAscending())
		{
			tree.setSortDirection(SWT.UP);
		}
		else
		{
			tree.setSortDirection(SWT.DOWN);
		}
		refreshViewer(getSelectedInstance());
	}

	private void columnMoved(ControlEvent e)
	{
		TreeColumn treeColumn = (TreeColumn) e.getSource();
		Tree tree = treeColumn.getParent();
		int[] order = tree.getColumnOrder();
		InstanceMgtViewLabelProvider provider =
			(InstanceMgtViewLabelProvider) viewer.getLabelProvider();
		int previousFirstColumnIndex = provider.getFirstColumnIndex();

		if (order[0] != previousFirstColumnIndex)
		{
			// This procedure is made only if the movement causes a column to change its
			// position in comparison with other columns. Otherwise, a performance lack
			// is detected.
			provider.setFirstColumnIndex(order[0]);
			refreshViewer(getSelectedInstance());
		}
	}

	/** 
	 * Remove the selected instance.
	 */
	protected void removeSelected() {
		if (viewer.getSelection().isEmpty()) {
			return;
		}
		
		IInstance instance = getSelectedInstance();

		if (instance != null)
		{
		    InstanceManager.deleteInstance(instance);
		}
	}
	
	private void instanceLoaded(final IInstance instance)
	{
	    Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                Collection<String> expandedDevices = getExpandedDevices();
                viewer.refresh();
                expandedDevices.add(instance.getDeviceTypeId());
                expandToNodeValues(expandedDevices, instance);
                
                notifyInstanceSelectionChangeListeners(instance);
            }});
	}
	
	private void instanceUnloaded(IInstance instance)
	{

        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                Collection<String> expandedDevices = getExpandedDevices();
                viewer.refresh();
                expandToNodeValues(expandedDevices, null);
            }});
	}
	
	private void instanceTransitioned(final IInstance instance)
	{
	    Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                ViewerInstanceNode node = getInstanceNode(instance);
                if (node != null)
                {
                    Collection<String> expandedDevices = getExpandedDevices();
                    IInstance selectedInstance = getSelectedInstance();
                    viewer.update(node, null);
                    expandToNodeValues(expandedDevices, selectedInstance);
                    
                    if (instance.equals(selectedInstance))
                    {
                        notifyInstanceSelectionChangeListeners(selectedInstance);
                    }
                }
            }});
	}
	
	private ViewerInstanceNode getInstanceNode(IInstance instance) {
	    ViewerInstanceNode node = null;	    
	    TreeItem[] deviceItems = viewer.getTree().getItems();
	    
	    for (TreeItem devItem : deviceItems) {	        
	        Object data = devItem.getData();
	        
	        if (data instanceof ViewerDeviceNode) {
	            Set<ViewerAbstractNode> children = ((ViewerDeviceNode)data).getChildren();
	            
	            for (ViewerAbstractNode child : children) {
	                if (child instanceof ViewerInstanceNode) {
	                    IInstance nodeInstance = ((ViewerInstanceNode)child).getInstance();
	                    if ((nodeInstance != null)&&(nodeInstance.equals(instance))) {
	                        node = (ViewerInstanceNode) child;
	                        break;
	                    }
	                }
	            }
	            
	            if (node != null) {
	                break;
	            }
	        }
	    }
	    
	    return node;
	}

    /*
     * Menu handler
     */
	private class MenuDeleteListener implements Listener {
		
		private IInstance instance;
		
		public MenuDeleteListener(IInstance instance)
        {
            super();
            this.instance = instance;
        }
		
		public void handleEvent(Event event) {
			final boolean[] result = new boolean[1];
			if(instance != null)
			{
				//Check with User if he really want to remove the instance.
				Display.getDefault().syncExec(new Runnable()
				{
					public void run()
					{
						IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						result[0] = MessageDialog.openQuestion(ww.getShell(), "Instance Removal", "Device Instance '" + instance.getName() + "' will be removed.\nAre you sure?");
					}
				});
				
				if(result[0])
				{
					removeSelected();
				}
			}
		}
	}

    /*
     * Menu handler
     */
    private class MenuPropertiesListener implements Listener {
        private IInstance instance;
        
        public MenuPropertiesListener(IInstance instance)
        {
            super();
            this.instance = instance;
        }
        
        public void handleEvent(Event event) {

            Shell shell = new Shell();
            PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(
                    shell,
                    instance,
                    null,
                    null,
                    null);
            dialog.open();
        }
    }
    
    /*
     * Toolbar handler.
     * 
     * Represents a toolbar item with a drop down menu with the devices listed
     * for opening their respective wizard. The toolbar item itself when clicked
     * opens a list selection dialog presenting the devices for opening the
     * wizard.
     */
	private class WizardDropDownAction extends Action implements IMenuCreator
    {
        private Menu fMenu;

        public WizardDropDownAction()
        {
            ImageDescriptor descriptor= AbstractUIPlugin.imageDescriptorFromPlugin(DeviceUIPlugin.PLUGIN_ID, "icons/full/obj16/new_instance.gif"); //$NON-NLS-1$
            setHoverImageDescriptor(descriptor);
            setImageDescriptor(descriptor); 
            
            setMenuCreator(this);
        }
        
        public void dispose()
        {
            if (fMenu != null) {
                fMenu.dispose();
                fMenu = null;
            }
        }

        public Menu getMenu(Control parent)
        {
            if (fMenu != null) {
                fMenu.dispose();
            }
            fMenu= new Menu(parent);
            
            for (Action action : wizardActions.values())
            {
                ActionContributionItem item = new ActionContributionItem(action);
                item.fill(fMenu, -1);
            }
            
            return fMenu;
        }

        public Menu getMenu(Menu parent)
        {
            return null;
        }
        
        @Override
        public void run()
        {
            // this is run when user clicks the toolbar item itself and
            // not the drop down menu on the toolbar item
        	if (wizardActions.size() > 1) {
        	
	           ListDialog dialog = new ListDialog(viewSite.getShell());
	           dialog.setContentProvider(new ArrayContentProvider());
	           dialog.setLabelProvider(new LabelProvider());
	           dialog.setTitle(TOOLBAR_NEW_TOOLTIP);
	           dialog.setMessage(TOOLBAR_DIALOG_MESSAGE);
	           Action[] input = new Action[wizardActions.size()]; 
	           input = wizardActions.values().toArray(input);
	           dialog.setInput(input);
           
	           if (dialog.open() == Window.OK)
	           {
	               ((Action)dialog.getResult()[0]).run();
	           }
        	}
        	else if (wizardActions.size() == 1){
        		((Action)wizardActions.values().toArray()[0]).run();
        	}
        }
        
    }
	
	/*
	 * Menu Context handler
	 */
	private class WizardSelectionListener implements Listener
	{
	    private String deviceName;
	    
	    public WizardSelectionListener(String deviceName)
	    {
	        this.deviceName = deviceName;	        
	    }
	    
	    public void handleEvent(Event event)
	    {
            Action wizardAction = wizardActions.get(deviceName);
            if (wizardAction != null)
            {
                wizardAction.run();
            }
            else
            {
                Display.getDefault().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                        MessageDialog.openError(ww.getShell(), ERROR_DIALOG_TITLE, ERROR_NO_WIZARD_MESSAGE + deviceName);
                    }
                });
            }
	    }
	}
	
	
	public void selectInstance(IInstance instance) {
		
		ViewerInstanceNode node = getInstanceNode(instance);
		viewer.setSelection(new StructuredSelection(node));
		
	}

	protected void removeListener() {
		InstanceEventManager eventMgr = InstanceEventManager.getInstance();
		eventMgr.removeInstanceListener(listener);
	}
}
