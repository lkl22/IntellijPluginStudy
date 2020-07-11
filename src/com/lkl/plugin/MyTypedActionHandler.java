package com.lkl.plugin;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Created by likunlun on 2020/7/11.
 */
public class MyTypedActionHandler implements TypedActionHandler {
    private TypedActionHandler oldHandler;
    private boolean isBegin = true;
    private int caretLine = 0;

    @Override
    public void execute(@NotNull Editor editor, char c, @NotNull DataContext dataContext) {
        if (oldHandler != null)
            oldHandler.execute(editor, c, dataContext);

        Document document = editor.getDocument();
        CaretModel caretModel = editor.getCaretModel();
        int caretOffset = caretModel.getOffset();
        int line = document.getLineNumber(caretOffset);
        if (isBegin) {
            document.insertString(document.getLineStartOffset(line), String.valueOf(c) + "\n");
            caretLine = line + 1;
            isBegin = false;
        } else {
            if (line != caretLine) {
                isBegin = true;
                execute(editor, c, dataContext);
            } else {
                document.insertString(document.getLineEndOffset(line - 1), String.valueOf(c));
            }
        }
        System.out.println(caretLine + "," + line);

    }

    public void setOldHandler(TypedActionHandler oldHandler) {
        this.oldHandler = oldHandler;
    }
}
