package com.lkl.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.TypedAction;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;

/**
 * Created by likunlun on 2020/7/11.
 */
public class InsertCharAction extends AnAction {
    public InsertCharAction() {
        final EditorActionManager actionManager = EditorActionManager.getInstance();
        final TypedAction typedAction = actionManager.getTypedAction();
        MyTypedActionHandler handler = new MyTypedActionHandler();
        //将自定义的TypedActionHandler设置进去后，
        //返回旧的TypedActionHandler，即IDEA自身的TypedActionHandler
        TypedActionHandler oldHandler = typedAction.setupHandler(handler);
        handler.setOldHandler(oldHandler);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

    }
}
