package phanastrae.arachne.editor;

import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.editor.editor_tabs.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class EditorTabHandler {

    EditorInstance editorInstance;

    HashMap<String, EditorTab> tabs = new HashMap<>();
    List<EditorTab> tabsOrdered = new ArrayList<>();

    EditorTab activeTab = null;

    public EditorTabHandler(EditorInstance editorInstance) {
        this.editorInstance = editorInstance;
        setupTabs();
    }

    public void setupTabs() {
        clearTabs();
        addTab(new MainTab("main"));
        addTab(new PhysicsMaterialTab("physicsMaterial"));
        addTab(new RenderMaterialTab("renderMaterial"));
        addTab(new PreviewTab("preview"));
        addTab(new ConfigTab("config"));
        addTab(new DebugTab("debug"));
    }

    public void clearTabs() {
        this.tabs.clear();
        this.tabsOrdered.clear();
    }

    public boolean addTab(EditorTab tab) {
        String name = tab.getId();
        if(tabs.containsKey(name)) {
            return false;
        } else {
            tabs.put(name, tab);
            tabsOrdered.add(tab);
            return true;
        }
    }

    @Nullable
    public EditorTab getActiveTab() {
        return this.activeTab;
    }

    public void setTab(@Nullable EditorTab tab) {
        if(this.activeTab == tab) return;

        this.activeTab = tab;
        this.editorInstance.getScreen().setupForTab(tab);
    }

    public void setTab(String id) {
        EditorTab tab = this.getTab(id);
        this.setTab(tab);
    }

    public Collection<EditorTab> getTabs() {
        return this.tabsOrdered;
    }

    @Nullable
    public EditorTab getTab(String id) {
        return this.tabs.getOrDefault(id, null);
    }
}
