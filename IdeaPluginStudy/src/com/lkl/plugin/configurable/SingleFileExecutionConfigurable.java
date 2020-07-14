package com.lkl.plugin.configurable;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SingleFileExecutionConfigurable implements SearchableConfigurable {
    private SingleFileExecutionConfigurableGUI mGUI;
    private final SingleFileExecutionConfig mConfig;

    @SuppressWarnings("FieldCanBeLocal")
    private final Project mProject;

    public SingleFileExecutionConfigurable(@NotNull Project project) {
        mProject = project;
        mConfig = SingleFileExecutionConfig.getInstance(project);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Single File Execution Plugin";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "preference.SingleFileExecutionConfigurable";
    }

    @NotNull
    @Override
    public String getId() {
        return "preference.SingleFileExecutionConfigurable";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String s) {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mGUI = new SingleFileExecutionConfigurableGUI();
        mGUI.createUI(mProject);
        return mGUI.getRootPanel();
    }

    @Override
    public boolean isModified() {
        return mGUI.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        mGUI.apply();
    }

    @Override
    public void reset() {
        mGUI.reset();
    }

    @Override
    public void disposeUIResources() {
        mGUI = null;
    }
}
