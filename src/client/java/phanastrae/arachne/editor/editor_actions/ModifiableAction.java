package phanastrae.arachne.editor.editor_actions;

public abstract class ModifiableAction implements EditorAction {
    boolean canEdit = true;

    public void finaliseEdit() {
        this.canEdit = false;
    }

    public boolean canEdit() {
        return this.canEdit;
    }
}
