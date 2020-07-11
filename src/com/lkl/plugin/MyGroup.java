package com.lkl.plugin;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by likunlun on 2020/7/11.
 */
public class MyGroup extends ActionGroup {
    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        return new AnAction[]{new CustomAction("first"),new CustomAction("second")};
    }

    class CustomAction extends AnAction {
        public CustomAction(String text) {
            super(text);
        }
        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        }
    }
}
