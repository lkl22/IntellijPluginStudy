package com.lkl.plugin.configurable;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SingleFileExecutionConfigurable implements SearchableConfigurable {
    SingleFileExecutionConfigurableGUI mGUI;
    Project mProject;
    SingleFileExecutionConfig mConfig;

    public SingleFileExecutionConfigurable(@NotNull Project project) {
        // you can get project instance as an argument of constructor
        mProject = project;
        mConfig = SingleFileExecutionConfig.getInstance(project);
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mGUI = new SingleFileExecutionConfigurableGUI();
        return mGUI.getRootPanel();
    }

    @Override
    public void disposeUIResources() {
        mGUI = null;
    }

    @NotNull
    @Override
    public String getId() {
        return "preference.SingleFileExecutionConfigurable";
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Single File Execution Plugin";
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "preference.SingleFileExecutionConfigurable";
    }

    @Override
    public void reset() {

    }
}
