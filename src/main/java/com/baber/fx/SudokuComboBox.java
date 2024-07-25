package com.baber.fx;

import javafx.scene.control.ComboBox;

public class SudokuComboBox extends ComboBox<String> {
    public SudokuComboBox() {
        getItems().addAll(" ", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    }

    public void setSelectedValue(int value)
    {
        String selection = " ";
        if (value != 0)
        {
            selection = String.valueOf(value);
        }
        getSelectionModel().select(selection);
    }

    public int getSelectedValue()
    {
        String selection = getSelectionModel().getSelectedItem();
        if (selection.equals(" "))
        {
            return 0;
        }
        return Integer.parseInt(selection);
    }
}
