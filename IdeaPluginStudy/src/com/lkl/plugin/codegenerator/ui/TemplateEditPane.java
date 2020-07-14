package com.lkl.plugin.codegenerator.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.lkl.plugin.codegenerator.config.ClassSelectionConfig;
import com.lkl.plugin.codegenerator.config.CodeTemplate;
import com.lkl.plugin.codegenerator.config.MemberSelectionConfig;
import com.lkl.plugin.codegenerator.config.PipelineStep;
import org.jetbrains.java.generate.config.DuplicationPolicy;
import org.jetbrains.java.generate.config.InsertWhere;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateEditPane {
    private JPanel templateEdit;
    private JComboBox templateTypeCombo;
    private JTextField templateIdText;
    private JTextField templateNameText;
    private JPanel editorPane;
    private JTextField fileEncodingText;
    private JCheckBox jumpToMethodCheckBox;
    private JRadioButton askRadioButton;
    private JRadioButton replaceExistingRadioButton;
    private JRadioButton generateDuplicateMemberRadioButton;
    private JRadioButton atCaretRadioButton;
    private JRadioButton atEndOfClassRadioButton;
    private JScrollPane settingsPanel;
    private JCheckBox templateEnabledCheckBox;
    private JTabbedPane templateTabbedPane;
    private JButton addMemberButton;
    private JButton addClassButton;
    private JTextField classNameVmText;
    private JCheckBox alwaysPromptForPackageCheckBox;
    private Editor editor;
    private List<SelectionPane> pipeline = new ArrayList<>();

    public TemplateEditPane(CodeTemplate codeTemplate) {
        settingsPanel.getVerticalScrollBar().setUnitIncrement(16); // scroll speed

        templateIdText.setText(codeTemplate.getId());
        templateNameText.setText(codeTemplate.name);
        templateEnabledCheckBox.setSelected(codeTemplate.enabled);
        fileEncodingText.setText(StringUtil.notNullize(codeTemplate.fileEncoding, CodeTemplate.DEFAULT_ENCODING));
        templateTypeCombo.setSelectedItem(codeTemplate.type);
        jumpToMethodCheckBox.setSelected(codeTemplate.jumpToMethod);
        classNameVmText.setText(codeTemplate.classNameVm);
        alwaysPromptForPackageCheckBox.setSelected(codeTemplate.alwaysPromptForPackage);

        askRadioButton.setSelected(false);
        replaceExistingRadioButton.setSelected(false);
        generateDuplicateMemberRadioButton.setSelected(false);
        switch (codeTemplate.whenDuplicatesOption) {
            case ASK:
                askRadioButton.setSelected(true);
                break;
            case REPLACE:
                replaceExistingRadioButton.setSelected(true);
                break;
            case DUPLICATE:
                generateDuplicateMemberRadioButton.setSelected(true);
                break;
        }

        atCaretRadioButton.setSelected(false);
        atEndOfClassRadioButton.setSelected(false);
        switch (codeTemplate.insertNewMethodOption) {
            case AT_CARET:
                atCaretRadioButton.setSelected(true);
                break;
            case AT_THE_END_OF_A_CLASS:
                atEndOfClassRadioButton.setSelected(true);
                break;
            default:
                break;
        }

        codeTemplate.pipeline.forEach(this::addMemberSelection);
        addMemberButton.addActionListener(e -> {
            int currentStep = findMaxStepPostfix(pipeline, "member");
            MemberSelectionConfig config = new MemberSelectionConfig();
            config.postfix = String.valueOf(currentStep + 1);
            addMemberSelection(config);
        });
        addClassButton.addActionListener(e -> {
            int currentStep = findMaxStepPostfix(pipeline, "class");
            ClassSelectionConfig config = new ClassSelectionConfig();
            config.postfix = String.valueOf(currentStep + 1);
            addMemberSelection(config);
        });

        addVmEditor(codeTemplate.template);
    }

    private static int findMaxStepPostfix(List<SelectionPane> pipelinePanes, String type) {
        return pipelinePanes.stream()
                .filter(p -> p.type().equals(type))
                .map(SelectionPane::postfix)
                .filter(str -> str.matches("\\d+"))
                .map(Integer::valueOf)
                .max(Comparator.naturalOrder())
                .orElse(0);
    }

    private void addMemberSelection(PipelineStep step) {
        if (step == null) {
            return;
        }

        String title = "";
        if (step instanceof MemberSelectionConfig) {
            title = "Member";
        } else if (step instanceof ClassSelectionConfig) {
            title = "Class";
        }

        SelectionPane pane = new SelectionPane(step, this);
        pipeline.add(pane);
        templateTabbedPane.addTab(title, pane.getComponent());
    }

    private void addVmEditor(String template) {
        EditorFactory factory = EditorFactory.getInstance();
        Document velocityTemplate = factory.createDocument(template);
        editor = factory.createEditor(velocityTemplate, null, FileTypeManager.getInstance()
                .getFileTypeByExtension("vm"), false);
        GridConstraints constraints = new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(0, 0), null, 0, true);

        editorPane.add(editor.getComponent(), constraints);
    }

    public String id() {
        return templateIdText.getText();
    }

    public String name() {
        return templateNameText.getText();
    }

    public boolean enabled() {
        return templateEnabledCheckBox.isSelected();
    }

    public String template() {
        return editor.getDocument().getText();
    }

    public String fileEncoding() {
        return fileEncodingText.getText();
    }

    public String type() {
        return (String) templateTypeCombo.getSelectedItem();
    }

    public boolean jumpToMethod() {
        return jumpToMethodCheckBox.isSelected();
    }

    public DuplicationPolicy duplicationPolicy() {
        if (askRadioButton.isSelected()) {
            return DuplicationPolicy.ASK;
        } else if (replaceExistingRadioButton.isSelected()) {
            return DuplicationPolicy.REPLACE;
        } else if (generateDuplicateMemberRadioButton.isSelected()) {
            return DuplicationPolicy.DUPLICATE;
        }
        return DuplicationPolicy.ASK;
    }

    public InsertWhere insertWhere() {
        if (atCaretRadioButton.isSelected()) {
            return InsertWhere.AT_CARET;
        } else if (atEndOfClassRadioButton.isSelected()) {
            return InsertWhere.AT_THE_END_OF_A_CLASS;
        }
        return InsertWhere.AT_CARET;
    }

    public JPanel templateEdit() {
        return templateEdit;
    }

    public String classNameVm() {
        return classNameVmText.getText();
    }

    public boolean alwaysPromptForPackage() {
        return this.alwaysPromptForPackageCheckBox.isSelected();
    }

    public String toString() {
        return this.name();
    }

    public CodeTemplate getCodeTemplate() {
        CodeTemplate template = new CodeTemplate(this.id());
        template.name = this.name();
        template.type = this.type();
        template.enabled = this.enabled();
        template.fileEncoding = this.fileEncoding();
        template.template = this.template();
        template.jumpToMethod = this.jumpToMethod();
        template.insertNewMethodOption = this.insertWhere();
        template.whenDuplicatesOption = this.duplicationPolicy();
        template.pipeline = pipeline.stream().map(PipelineStepConfig::getConfig).collect(Collectors.toList());
        template.classNameVm = this.classNameVm();
        template.alwaysPromptForPackage = this.alwaysPromptForPackage();

        return template;
    }

    public void removePipelineStep(PipelineStepConfig stepToRemove) {
        int index = this.pipeline.indexOf(stepToRemove);
        PipelineStepConfig step = this.pipeline.remove(index);
        this.templateTabbedPane.remove(step.getComponent());
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        templateEdit = new JPanel();
        templateEdit.setLayout(new GridLayoutManager(1, 1, new Insets(6, 6, 6, 6), -1, -1));
        templateEdit.setOpaque(true);
        templateEdit.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        templateTabbedPane = new JTabbedPane();
        templateTabbedPane.setTabLayoutPolicy(1);
        templateEdit.add(templateTabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        templateTabbedPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        templateTabbedPane.addTab("Template", panel1);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        templateIdText = new JTextField();
        templateIdText.setVisible(false);
        panel1.add(templateIdText, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 3, new Insets(0, 10, 0, 10), -1, -1));
        panel2.setEnabled(true);
        panel1.add(panel2, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        templateTypeCombo = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("body");
        defaultComboBoxModel1.addElement("class");
        defaultComboBoxModel1.addElement("caret");
        templateTypeCombo.setModel(defaultComboBoxModel1);
        panel2.add(templateTypeCombo, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Write To");
        panel2.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Template Name");
        panel2.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        templateNameText = new JTextField();
        panel2.add(templateNameText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        templateEnabledCheckBox = new JCheckBox();
        templateEnabledCheckBox.setSelected(true);
        templateEnabledCheckBox.setText("Enabled");
        panel2.add(templateEnabledCheckBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Target Class Name(Vm)");
        label3.setToolTipText("The name of the class to create, use Qualified name to specify target package");
        panel2.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        classNameVmText = new JTextField();
        panel2.add(classNameVmText, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        alwaysPromptForPackageCheckBox = new JCheckBox();
        alwaysPromptForPackageCheckBox.setText("Always prompt for package");
        alwaysPromptForPackageCheckBox.setToolTipText("Prompt for package selection even if the target package is found. (Useful in the case when you want to insert class to test/ directory)");
        panel2.add(alwaysPromptForPackageCheckBox, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editorPane = new JPanel();
        editorPane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(editorPane, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        addMemberButton = new JButton();
        addMemberButton.setText("Add Member Selection Dialog");
        panel1.add(addMemberButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addClassButton = new JButton();
        addClassButton.setText("Add Class Selection Dialog");
        panel1.add(addClassButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        settingsPanel = new JScrollPane();
        settingsPanel.setVerticalScrollBarPolicy(20);
        templateTabbedPane.addTab("Settings", settingsPanel);
        settingsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        settingsPanel.setViewportView(panel3);
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(null, "Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label4 = new JLabel();
        label4.setText("File Encoding");
        panel4.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jumpToMethodCheckBox = new JCheckBox();
        jumpToMethodCheckBox.setText("Move caret to generated method");
        panel4.add(jumpToMethodCheckBox, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fileEncodingText = new JTextField();
        panel4.add(fileEncodingText, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(null, "When members exists", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        askRadioButton = new JRadioButton();
        askRadioButton.setEnabled(true);
        askRadioButton.setSelected(true);
        askRadioButton.setText("Ask");
        panel5.add(askRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceExistingRadioButton = new JRadioButton();
        replaceExistingRadioButton.setText("Replace existing");
        panel5.add(replaceExistingRadioButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generateDuplicateMemberRadioButton = new JRadioButton();
        generateDuplicateMemberRadioButton.setText("Generate duplicate member");
        panel5.add(generateDuplicateMemberRadioButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel6, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder(null, "Where to insert", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        atCaretRadioButton = new JRadioButton();
        atCaretRadioButton.setSelected(true);
        atCaretRadioButton.setText("At Caret");
        panel6.add(atCaretRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel6.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        atEndOfClassRadioButton = new JRadioButton();
        atEndOfClassRadioButton.setText("At end of class");
        panel6.add(atEndOfClassRadioButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel3.add(spacer3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(askRadioButton);
        buttonGroup.add(replaceExistingRadioButton);
        buttonGroup.add(generateDuplicateMemberRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(atCaretRadioButton);
        buttonGroup.add(atEndOfClassRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return templateEdit;
    }
}
