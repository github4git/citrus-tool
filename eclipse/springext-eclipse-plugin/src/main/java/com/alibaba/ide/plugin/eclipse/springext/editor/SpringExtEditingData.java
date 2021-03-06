package com.alibaba.ide.plugin.eclipse.springext.editor;

import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import com.alibaba.ide.plugin.eclipse.springext.schema.ISchemaSetChangeListener;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

/**
 * 代表编辑器内的数据。
 * 
 * @author Michael Zhou
 */
public abstract class SpringExtEditingData<S extends IEditorPart> extends PlatformObject implements
        ISchemaSetChangeListener {
    private SpringExtFormEditor<?, ?> editor;
    private IProject project;
    private IEditorInput input;
    private SchemaResourceSet schemas;
    private S sourceEditor;

    public final SpringExtFormEditor<?, ?> getEditor() {
        return editor;
    }

    void setEditor(SpringExtFormEditor<?, ?> editor) {
        this.editor = editor;
    }

    public final S getSourceEditor() {
        return sourceEditor;
    }

    protected void initWithSourceEditor(S sourceEditor) {
        this.sourceEditor = sourceEditor;
    }

    public IProject getProject() {
        return project;
    }

    public IEditorInput getEditorInput() {
        return input;
    }

    public void initWithEditorInput(IEditorInput input) {
        this.input = input;
        this.project = getProjectFromInput(input);
        this.schemas = SchemaResourceSet.getInstance(this.project);

        SchemaResourceSet.addSchemaSetChangeListener(this);
    }

    public SchemaResourceSet getSchemas() {
        return schemas;
    }

    public final void setSchemas(SchemaResourceSet schemas) {
        if (schemas != this.schemas) {
            this.schemas = schemas;

            onSchemaSetChanged();
        }
    }

    protected void onSchemaSetChanged() {
        forceRefreshPages();
    }

    public void forceRefreshPages() {
    }

    /**
     * 当schemas被更新时，此方法被调用。
     * <p/>
     * 例如，用户修改了<code>*.bean-definition-parsers</code>文件，或者调整了classpath。
     * 
     * @see ISchemaSetChangeListener
     */
    public final void onSchemaSetChanged(SchemaSetChangeEvent event) {
        // 仅当发生变化的project和当前所编辑的文件所在的project是同一个时，才作反应。
        if (event.getProject().equals(getProject())) {
            setSchemas(SchemaResourceSet.getInstance(getProject()));
        }
    }

    /**
     * 编辑器被关闭时被调用。
     */
    public void dispose() {
        SchemaResourceSet.removeSchemaSetChangeListener(this);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }

        if (adapter.isAssignableFrom(IProject.class)) {
            return getProject();
        }

        if (adapter.isAssignableFrom(SchemaResourceSet.class)) {
            return getSchemas();
        }

        if (getEditorInput() != null) {
            return getEditorInput().getAdapter(adapter);
        }

        return super.getAdapter(adapter);
    }
}
