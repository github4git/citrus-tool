package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace;

import static com.alibaba.ide.plugin.eclipse.springext.util.DomDocumentUtil.*;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ContributionItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.NamespaceItem;

public class NamespacesMasterPart extends SectionPart {
    private final NamespacesPage page;
    private final FormToolkit toolkit;
    private CheckboxTreeViewer treeViewer;

    public NamespacesMasterPart(Composite parent, NamespacesPage page) {
        super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | Section.TITLE_BAR);

        this.page = page;
        this.toolkit = page.getManagedForm().getToolkit();
    }

    public CheckboxTreeViewer getViewer() {
        return treeViewer;
    }

    public void createContents() {
        // section
        Section section = getSection();

        section.setText("Select Namespaces");
        section.setDescription("The list contains namepsaces that are available to SpringExt/Spring configuration file");
        section.marginWidth = 10;
        section.marginHeight = 5;

        // section/client
        Composite client = toolkit.createComposite(section, SWT.WRAP);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginWidth = 2;
        layout.marginHeight = 2;
        client.setLayout(layout);

        // section/client/tree
        FilteredCheckboxTree tree = new FilteredCheckboxTree(toolkit, client, SWT.CHECK | SWT.SINGLE | SWT.V_SCROLL
                | SWT.H_SCROLL | SWT.BORDER, new SchemaPatternfilter(), true);

        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 20;
        gd.widthHint = 100;
        tree.setLayoutData(gd);
        toolkit.paintBordersFor(client);

        section.setClient(client);

        // section/client/tree viewer
        treeViewer = tree.getViewer();

        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                getManagedForm().fireSelectionChanged(NamespacesMasterPart.this, event.getSelection());
            }
        });

        treeViewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (event.getElement() instanceof NamespaceItem) {
                    updateNamespaceDefinition(page.getConfig(), (NamespaceItem) event.getElement(), event.getChecked());
                }
            }
        });

        NamespacesProvider provider = new NamespacesProvider(page.getConfig());

        treeViewer.setContentProvider(provider);
        treeViewer.setLabelProvider(provider);
        treeViewer.setCheckStateProvider(provider);
        treeViewer.setInput(page.getConfig().getDomDocument());
    }

    @Override
    public void refresh() {
        page.getConfig().getNamespacesTreeViewer().refresh();
        super.refresh();
    }

    private static class SchemaPatternfilter extends PatternFilter {
        public SchemaPatternfilter() {
            setIncludeLeadingWildcard(true);
        }

        @Override
        public boolean isElementSelectable(Object element) {
            return element != null && !(element instanceof ContributionItem);
        }
    }

    private static class FilteredCheckboxTree extends FilteredTree {
        private FilteredCheckboxTree(FormToolkit toolkit, Composite parent, int treeStyle, PatternFilter filter,
                                     boolean useNewLook) {
            super(parent, treeStyle, filter, useNewLook);
            toolkit.adapt(this, false, false);
        }

        @Override
        public CheckboxTreeViewer getViewer() {
            return (CheckboxTreeViewer) super.getViewer();
        }

        @Override
        protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
            return new CheckboxTreeViewer(parent, style);
        }
    }
}