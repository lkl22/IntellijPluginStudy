package com.lkl.plugin;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;

/**
 * Created by likunlun on 2020/7/11.
 */
public class MyGroup1 extends DefaultActionGroup {
    @Override
    public void update(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setVisible(true);
        e.getPresentation().setEnabled(editor != null);
        e.getPresentation().setIcon(AllIcons.General.Error);
    }
}
