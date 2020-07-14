package com.lkl.plugin.codegenerator.config;

public interface PipelineStep {
    String type();
    String postfix();
    void postfix(String postfix);
    boolean enabled();
    void enabled(boolean enabled);
}
