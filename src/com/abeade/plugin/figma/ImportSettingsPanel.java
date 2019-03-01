package com.abeade.plugin.figma;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.util.Comparing;

import javax.swing.*;

public class ImportSettingsPanel {

    private JPanel panel;
    private JTextField prefixField;
    private PropertiesComponent propertiesComponent;

    public JComponent createPanel(PropertiesComponent propertiesComponent) {
        this.propertiesComponent = propertiesComponent;
        return panel;
    }

    public boolean isModified() {
        return !Comparing.equal(prefixField.getText(), getCurrentPrefix());
    }

    public void apply() {
        propertiesComponent.setValue(ImportDialogWrapper.PREFIX_KEY, prefixField.getText());
    }

    public void reset() {
        prefixField.setText(getCurrentPrefix());
    }

    private String getCurrentPrefix() {
        String prefix = propertiesComponent.getValue(ImportDialogWrapper.PREFIX_KEY);
        if (prefix == null) {
            prefix = ImportDialogWrapper.RESOURCE_PREFIX;
        }
        return prefix;
    }
}
