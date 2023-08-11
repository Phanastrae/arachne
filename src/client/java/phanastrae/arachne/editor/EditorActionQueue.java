package phanastrae.arachne.editor;

import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.editor.editor_actions.EditorAction;
import phanastrae.arachne.editor.editor_actions.ModifiableAction;

import java.util.ArrayList;

public class EditorActionQueue {

    EditorInstance editorInstance;

    public EditorActionQueue(EditorInstance editorInstance) {
        this.editorInstance = editorInstance;
    }

    ArrayList<EditorAction> editorActions = new ArrayList<>();
    int nextAction = 0;

    @Nullable
    EditorAction currentAction = null;

    public ArrayList<EditorAction> getActionQueue() {
        return this.editorActions;
    }

    public int getNextAction() {
        return this.nextAction;
    }

    // returns false if at bottom of queue, otherwise true
    public boolean undo() {
        if(nextAction <= 0) {
            return false;
        } else {
            finaliseCurrentAction();
            this.editorActions.get(nextAction - 1).undo(this.editorInstance);
            nextAction--;
            return true;
        }
    }

    // returns false if at top of queue, otherwise true
    public boolean redo() {
        if(this.nextAction >= editorActions.size()) {
            return false;
        } else {
            this.editorActions.get(this.nextAction).act(this.editorInstance);
            nextAction++;
            return true;
        }
    }

    public void clearRedos() {
        if (this.editorActions.size() > this.nextAction) {
            editorActions.subList(this.nextAction, this.editorActions.size()).clear();
        }
    }

    public void doNewAction(EditorAction action) {
        finaliseCurrentAction();
        clearRedos();

        this.editorActions.add(action);
        if(action instanceof ModifiableAction) {
            this.currentAction = action;
        }
        action.act(this.editorInstance);
        this.nextAction++;
    }

    public void clearQueue() {
        finaliseCurrentAction();

        this.editorActions.clear();
        this.nextAction = 0;
    }

    public void updateLast(Runnable r) {
        // undoes current action, modifies, then redoes (if all is applicable)
        EditorAction current = this.currentAction;

        if(current instanceof ModifiableAction ma) {
            if(!ma.canEdit()) return;

            current.undo(this.editorInstance);
            r.run();
            current.act(this.editorInstance);
        }
    }

    public void finaliseCurrentAction() {
        if(this.currentAction instanceof ModifiableAction maction) {
            if(maction.canEdit()) {
                maction.undo(this.editorInstance);
                maction.finaliseEdit();
                maction.act(this.editorInstance);
            }
            this.currentAction = null;
        }
    }
}
