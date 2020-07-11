package com.lkl.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;

/**
 * Created by likunlun on 2020/7/11.
 */
public class GetterAndSetter extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        //获取Editor和Project对象
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (editor == null||project==null)
            return;

        //获取SelectionModel和Document对象
        SelectionModel selectionModel = editor.getSelectionModel();
        Document document = editor.getDocument();

        //拿到选中部分字符串
        String selectedText = selectionModel.getSelectedText();

        //得到选中字符串的起始和结束位置
        int startOffset = selectionModel.getSelectionStart();
        int endOffset = selectionModel.getSelectionEnd();

        //得到最大插入字符串（即生成的Getter和Setter函数字符串）位置
        int maxOffset = document.getTextLength() - 1;

        //计算选中字符串所在的行号，并通过行号得到下一行的第一个字符的起始偏移量
        int curLineNumber = document.getLineNumber(endOffset);
        int nextLineStartOffset = document.getLineStartOffset(curLineNumber + 1);

        //计算字符串的插入位置
        int insertOffset = maxOffset > nextLineStartOffset ? nextLineStartOffset : maxOffset;

        //得到选中字符串在Java类中对应的字段的类型
        String type = getSelectedType(document, startOffset);

        //对文档进行操作部分代码，需要放入Runnable接口中实现，由IDEA在内部将其通过一个新线程执行
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //genGetterAndSetter为生成getter和setter函数部分
                document.insertString(insertOffset, genGetterAndSetter(selectedText, type));
            }
        };

        //加入任务，由IDEA调度执行这个任务
        WriteCommandAction.runWriteCommandAction(project, runnable);

    }

    @Override
    public void update(AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        SelectionModel selectionModel = editor.getSelectionModel();

        //如果没有字符串被选中，那么无需显示该Action
        e.getPresentation().setVisible(editor != null && selectionModel.hasSelection());
    }

    private String getSelectedType(Document document, int startOffset) {

        String text = document.getText().substring(0, startOffset).trim();
        int startIndex = text.lastIndexOf(' ');

        return text.substring(startIndex + 1);
    }

    private String genGetterAndSetter(String field, String type) {
        if (field == null || (field = field.trim()).equals(""))
            return "";
        String upperField = field;
        char first = field.charAt(0);
        if (first <= 'z' && first >= 'a') {
            upperField = String.valueOf(first).toUpperCase() + field.substring(1);
        }
        String getter = "\tpublic TYPE getUpperField(){ \n\t\treturn this.FIELD;\n\t}";
        String setter = "\tpublic void setUpperField(TYPE FIELD){\n\t\tthis.FIELD=FIELD;\n\t}";

        String myGetter = getter.replaceAll("TYPE", type).replaceAll("UpperField", upperField).replaceAll("FIELD", field);
        String mySetter = setter.replaceAll("TYPE", type).replaceAll("UpperField", upperField).replaceAll("FIELD", field);

        return "\n"+myGetter + "\n" + mySetter + "\n";
    }
}
