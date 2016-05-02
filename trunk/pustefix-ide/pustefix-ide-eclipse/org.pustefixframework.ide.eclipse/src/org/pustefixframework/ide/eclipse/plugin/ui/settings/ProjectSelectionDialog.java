package org.pustefixframework.ide.eclipse.plugin.ui.settings;

import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementSorter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

import org.pustefixframework.ide.eclipse.plugin.Activator;
import org.pustefixframework.ide.eclipse.plugin.ui.util.StatusInfo;


public class ProjectSelectionDialog extends SelectionStatusDialog {

	private TableViewer tableViewer;
	private Set projectsWithSpecifics;

	private final static int SELECTION_WIDGET_HEIGHT= 250;
	private final static int SELECTION_WIDGET_WIDTH= 300;
	
	private final static String DIALOG_SETTINGS_SHOW_ALL= "ProjectSelectionDialog.show_all"; //$NON-NLS-1$

	private ViewerFilter fFilter;

	public ProjectSelectionDialog(Shell parentShell, Set<IJavaProject> projectsWithSpecificSettings) {
		super(parentShell);
		setTitle("Project Specific Configuration");  
		setMessage("&Select the project to configure:"); 
		projectsWithSpecifics= projectsWithSpecificSettings;
		
        int shellStyle = getShellStyle();
        setShellStyle(shellStyle | SWT.MAX | SWT.RESIZE);
		
        if(projectsWithSpecifics!=null) {
			fFilter= new ViewerFilter() {
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					return projectsWithSpecifics.contains(element);
				}
			};
        }
		
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite= (Composite) super.createDialogArea(parent);

		Font font= parent.getFont();
		composite.setFont(font);

		createMessageArea(composite);

		tableViewer= new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				doSelectionChanged(((IStructuredSelection) event.getSelection()).toArray());
			}
		});
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
                okPressed();
			}
		});
		GridData data= new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint=SELECTION_WIDGET_HEIGHT;
		data.widthHint=SELECTION_WIDGET_WIDTH;
		tableViewer.getTable().setLayoutData(data);

		tableViewer.setLabelProvider(new JavaElementLabelProvider());
		tableViewer.setContentProvider(new ProjectElementContentProvider());
		tableViewer.setSorter(new JavaElementSorter());
		tableViewer.getControl().setFont(font);

		
		if(projectsWithSpecifics!=null) {
			Button checkbox= new Button(composite, SWT.CHECK);
			checkbox.setText("&Filter projects with no specific settings"); 
			checkbox.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
			checkbox.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					updateFilter(((Button) e.widget).getSelection());
				}
				public void widgetDefaultSelected(SelectionEvent e) {
					updateFilter(((Button) e.widget).getSelection());
				}
			});
			IDialogSettings dialogSettings= Activator.getDefault().getDialogSettings();
			boolean doFilter= !dialogSettings.getBoolean(DIALOG_SETTINGS_SHOW_ALL) && !projectsWithSpecifics.isEmpty();
			checkbox.setSelection(doFilter);
			updateFilter(doFilter);
		}
		
		IJavaModel input= JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
		tableViewer.setInput(input);
		
		doSelectionChanged(new Object[0]);
		Dialog.applyDialogFont(composite);
		return composite;
	}
	
	protected void updateFilter(boolean selected) {
		if (selected) {
			tableViewer.addFilter(fFilter);
		} else {
			tableViewer.removeFilter(fFilter);
		}
		Activator.getDefault().getDialogSettings().put(DIALOG_SETTINGS_SHOW_ALL, !selected);
	}

	private void doSelectionChanged(Object[] objects) {
		if (objects.length != 1) {
			updateStatus(new StatusInfo(IStatus.ERROR, "")); //$NON-NLS-1$
			setSelectionResult(null);
		} else {
			updateStatus(new StatusInfo()); 
			setSelectionResult(objects);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
	 */
	protected void computeResult() {
	}
}