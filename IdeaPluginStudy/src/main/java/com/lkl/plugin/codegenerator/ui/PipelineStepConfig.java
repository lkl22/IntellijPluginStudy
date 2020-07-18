package com.lkl.plugin.codegenerator.ui;

import com.lkl.plugin.codegenerator.config.PipelineStep;

import javax.swing.*;

public interface PipelineStepConfig {
    PipelineStep getConfig();
    JComponent getComponent();
}
