package com.lkl.plugin.codegenerator.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.lkl.plugin.codegenerator.config.CodeGeneratorSettings;
import com.lkl.plugin.codegenerator.config.CodeTemplate;
import com.lkl.plugin.codegenerator.config.CodeTemplateList;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CodeGeneratorConfig {
    private JPanel mainPane;
    private JButton addTemplateButton;
    private JSplitPane splitPane;
    private JList<TemplateEditPane> templateList;
    private DefaultListModel<TemplateEditPane> templateListModel;
    private JButton deleteTemplateButton;
    private JPanel splitRightPane;
    private JScrollPane scrollPane;
    private JButton importButton;
    private JButton exportButton;
    private JButton exportAllButton;

    private static String DEFAULT_EXPORT_PATH = "code-generator.xml";

    public CodeGeneratorConfig(CodeGeneratorSettings settings) {
        this.templateListModel = new DefaultListModel<>();
        this.templateList.setModel(templateListModel);

        templateList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }

            int length = templateListModel.getSize();
            int index = templateList.getSelectedIndex();
            if (length < 0 || index < 0 || index >= length) {
                splitPane.setRightComponent(splitRightPane);
                deleteTemplateButton.setEnabled(false);
                return;
            }

            TemplateEditPane pane = templateListModel.get(templateList.getSelectedIndex());
            deleteTemplateButton.setEnabled(true);
            splitPane.setRightComponent(pane.templateEdit());
        });

        addTemplateButton.addActionListener(e -> {
            CodeTemplate template = new CodeTemplate();
            template.name = "Untitled";
            TemplateEditPane editPane = new TemplateEditPane(template);
            DefaultListModel<TemplateEditPane> model = (DefaultListModel<TemplateEditPane>) templateList.getModel();
            model.addElement(editPane);
            templateList.setSelectedIndex(model.getSize() - 1);
        });

        deleteTemplateButton.addActionListener(e -> {
            int index = templateList.getSelectedIndex();
            int size = templateListModel.getSize();
            if (index >= 0 && index < size) {
                int result = Messages.showYesNoDialog("Delete this template?", "Delete", null);
                if (result == Messages.OK) {
                    int lastIndex = templateList.getAnchorSelectionIndex();
                    templateListModel.remove(index);

                    int nextIndex = -1;
                    if (lastIndex >= 0 && lastIndex < index || lastIndex == index && index < size - 1) {
                        nextIndex = lastIndex;
                    } else if (lastIndex == index || lastIndex > index && lastIndex < size - 1) {
                        nextIndex = lastIndex - 1;
                    } else if (lastIndex >= index) {
                        nextIndex = size - 2; // should not be here
                    }
                    templateList.setSelectedIndex(nextIndex);
                }
            }
        });

        exportButton.addActionListener((ActionEvent e) -> {
            int index = templateList.getSelectedIndex();
            TemplateEditPane template = templateListModel.get(index);

            String xml = CodeTemplateList.toXML(template.getCodeTemplate());
            saveToFile(xml);
        });

        exportAllButton.addActionListener((ActionEvent e) -> {
            List<CodeTemplate> templates = new ArrayList<>();
            for (int i = 0; i < templateListModel.getSize(); i++) {
                templates.add(templateListModel.get(i).getCodeTemplate());
            }

            String xml = CodeTemplateList.toXML(templates);
            saveToFile(xml);
        });

        importButton.addActionListener(e -> {
            readFromFile().thenAccept(xml -> {
                try {
                    List<CodeTemplate> templates = CodeTemplateList.fromXML(xml);
                    List<CodeTemplate> currentTemplates = getTabTemplates();
                    currentTemplates.addAll(templates);
                    refresh(currentTemplates);
                    Messages.showMessageDialog("Import finished!", "Import", null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Messages.showMessageDialog("Fail to import\n" + ex.getMessage(), "Import Error", null);
                }
            });
        });

        resetTabPane(settings.getCodeTemplates());
    }

    public void refresh(List<CodeTemplate> templates) {
        templateListModel.removeAllElements();
        resetTabPane(templates);
    }

    private void saveToFile(String content) {
        final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleLocalFileDescriptor();
        descriptor.setTitle("Choose Directory to Export");
        descriptor.setDescription("save to directory/" + DEFAULT_EXPORT_PATH + " or the file to overwrite");
        FileChooser.chooseFile(descriptor, null, mainPane, null, virtualFile -> {
            String targetPath;
            if (virtualFile.isDirectory()) {
                targetPath = virtualFile.getPath() + '/' + DEFAULT_EXPORT_PATH;
            } else {
                targetPath = virtualFile.getPath();
            }

            Path path = Paths.get(targetPath);
            if (virtualFile.isDirectory() && Files.exists(path)) {
                int result = Messages.showYesNoDialog("Overwrite the file?\n" + path, "Overwrite", null);
                if (result != Messages.OK) {
                    return;
                }
            }

            try {
                Files.write(path, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                Messages.showMessageDialog("Exported to \n" + path, "Export Successful", null);
            } catch (IOException e) {
                e.printStackTrace();
                Messages.showMessageDialog("Error occurred\n" + e.getMessage(), "Export Error", null);
            }
        });
    }

    private CompletableFuture<String> readFromFile() {
        final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("xml");
        descriptor.setTitle("Choose File to Import");
        final CompletableFuture<String> result = new CompletableFuture<>();
        FileChooser.chooseFile(descriptor, null, mainPane, null, virtualFile -> result.complete(FileDocumentManager.getInstance().getDocument(virtualFile).getText()));
        return result;
    }

    private void resetTabPane(List<CodeTemplate> templates) {
        templates.forEach(template -> {
            if (template == null) return;
            TemplateEditPane editPane = new TemplateEditPane(template);
            templateListModel.addElement(editPane);
        });

        // select first item
        templateList.setSelectedIndex(0);
    }

    public List<CodeTemplate> getTabTemplates() {
        List<CodeTemplate> ret = new ArrayList<>();
        for (int i = 0; i < templateListModel.getSize(); i++) {
            TemplateEditPane value = templateListModel.get(i);
            ret.add(value.getCodeTemplate());
        }

        return ret;
    }

    /**
     * Getter method for property <tt>mainPane</tt>.
     *
     * @return property value of mainPane
     */
    public JPanel getMainPane() {
        return mainPane;
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
        mainPane = new JPanel();
        mainPane.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPane.setOpaque(true);
        splitPane = new JSplitPane();
        splitPane.setContinuousLayout(false);
        splitPane.setOrientation(1);
        mainPane.add(splitPane, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        splitRightPane = new JPanel();
        splitRightPane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane.setRightComponent(splitRightPane);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        splitPane.setLeftComponent(panel1);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setBorderPainted(false);
        toolBar1.setFloatable(false);
        toolBar1.setFocusable(false);
        toolBar1.setForeground(new Color(-12828863));
        toolBar1.setInheritsPopupMenu(false);
        toolBar1.setOpaque(false);
        toolBar1.setRollover(false);
        toolBar1.putClientProperty("JToolBar.isRollover", Boolean.FALSE);
        panel1.add(toolBar1, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addTemplateButton = new JButton();
        addTemplateButton.setBorderPainted(false);
        addTemplateButton.setContentAreaFilled(true);
        addTemplateButton.setOpaque(false);
        addTemplateButton.setText("Add");
        addTemplateButton.setToolTipText("Add a new template");
        addTemplateButton.setVisible(true);
        toolBar1.add(addTemplateButton);
        deleteTemplateButton = new JButton();
        deleteTemplateButton.setBorderPainted(false);
        deleteTemplateButton.setEnabled(false);
        deleteTemplateButton.setHideActionText(false);
        deleteTemplateButton.setOpaque(false);
        deleteTemplateButton.setText("Remove");
        deleteTemplateButton.setToolTipText("Remove template");
        toolBar1.add(deleteTemplateButton);
        final Spacer spacer1 = new Spacer();
        toolBar1.add(spacer1);
        scrollPane = new JScrollPane();
        scrollPane.setAutoscrolls(true);
        scrollPane.setHorizontalScrollBarPolicy(30);
        scrollPane.setMinimumSize(new Dimension(170, 19));
        scrollPane.setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(20);
        panel1.add(scrollPane, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        templateList = new JList();
        templateList.setLayoutOrientation(0);
        templateList.setVisibleRowCount(4);
        scrollPane.setViewportView(templateList);
        importButton = new JButton();
        importButton.setBorderPainted(true);
        importButton.setText("Import");
        importButton.setVisible(true);
        panel1.add(importButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        exportButton = new JButton();
        exportButton.setBorderPainted(true);
        exportButton.setText("Export");
        exportButton.setVisible(true);
        panel1.add(exportButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        exportAllButton = new JButton();
        exportAllButton.setBorderPainted(true);
        exportAllButton.setText("Export ALL");
        panel1.add(exportAllButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPane;
    }
}
