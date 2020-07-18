package com.lkl.plugin.base;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
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
        public void actionPerformed(@NotNull AnActionEvent e) {
            Editor editor = e.getData(PlatformDataKeys.EDITOR);
            if (editor == null)
                return;

            SelectionModel selectionModel = editor.getSelectionModel();
            CaretModel caretModel=editor.getCaretModel();
        }
    }
}
