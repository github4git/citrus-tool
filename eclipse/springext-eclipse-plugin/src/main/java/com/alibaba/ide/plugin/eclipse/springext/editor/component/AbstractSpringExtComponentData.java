package com.alibaba.ide.plugin.eclipse.springext.editor.component;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.alibaba.ide.plugin.eclipse.springext.editor.SpringExtEditingData;

@SuppressWarnings("restriction")
public abstract class AbstractSpringExtComponentData<C> extends SpringExtEditingData<PropertiesFileEditor> {
    private DocumentViewer documentViewer;
    private IManagedForm managedForm;
    private IDocument document;

    public AbstractSpringExtComponentData() {
        this.documentViewer = createDocumentViewer();
    }

    public void initWithManagedForm(IManagedForm managedForm) {
        this.managedForm = managedForm;
    }

    @Override
    protected void initWithSourceEditor(PropertiesFileEditor sourceEditor) {
        super.initWithSourceEditor(sourceEditor);
        document = sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput());
        document.addDocumentListener(getDocumentViewer());
    }

    public DocumentViewer getDocumentViewer() {
        return documentViewer;
    }

    public IDocument getDocument() {
        return document;
    }

    @Override
    public void forceRefreshPages() {
        if (managedForm != null) {
            for (IFormPart part : managedForm.getParts()) {
                if (part instanceof AbstractFormPart) {
                    ((AbstractFormPart) part).markStale();
                }
            }
        }
    }

    @Override
    public void dispose() {
        if (document != null) {
            document.removeDocumentListener(getDocumentViewer());
        }

        super.dispose();
    }

    protected abstract DocumentViewer createDocumentViewer();

    public abstract class DocumentViewer implements ModifyListener, IDocumentListener {
        private final AtomicBoolean updatingLock = new AtomicBoolean();
        private final AtomicBoolean refreshingLock = new AtomicBoolean();

        public abstract void createContent(Composite parent, FormToolkit toolkit);

        public void documentAboutToBeChanged(DocumentEvent event) {
        }

        /**
         * 当用户直接修改了文件时。
         */
        public void documentChanged(DocumentEvent event) {
            refresh();
        }

        public void modifyText(ModifyEvent e) {
            update();
        }

        /**
         * 将字段的修改写入文件中。
         */
        public final void update() {
            if (refreshingLock.get()) {
                return;
            }

            try {
                updatingLock.set(true);
                doUpdate();
            } finally {
                updatingLock.set(false);
            }
        }

        protected abstract void doUpdate();

        /**
         * 将文件的内容更新到字段中。
         */
        public final void refresh() {
            if (updatingLock.get()) {
                return;
            }

            try {
                refreshingLock.set(true);
                doRefresh();
            } finally {
                refreshingLock.set(false);
            }
        }

        protected abstract void doRefresh();
    }
}
