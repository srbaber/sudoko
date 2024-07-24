package com.baber.fx;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.controlsfx.control.PropertySheet;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * helper class that knows how to show various dialogs
 */
public class Dialogs
{
    /**
     * don't look at my privates
     */
    private static final Alert WORKING_DIALOG = createInfoDialog();

    /**
     * don't look at my privates
     */
    private static final Alert ERROR_DIALOG = createErrorDialog();

    /**
     * don't look at my privates
     */
    private static final Alert INFO_DIALOG = createInfoDialog();

    /**
     * don't look at my privates
     */
    private static final Alert PROPERTY_DIALOG = createPropertyDialog();

    /**
     * builds the error dialog
     */
    private static Alert createErrorDialog()
    {
        final Alert dialog = new Alert(AlertType.ERROR);
        dialog.setTitle("Error");
        return dialog;
    }

    /**
     * builds the info dialog
     */
    private static Alert createInfoDialog()
    {
        final Alert dialog = new Alert(AlertType.INFORMATION);
        dialog.setTitle("Info");
        return dialog;
    }

    /**
     * builds the property dialog
     */
    private static Alert createPropertyDialog()
    {
        final Alert dialog = new Alert(AlertType.INFORMATION);
        dialog.setTitle("Info");
        dialog.setHeaderText("");
        dialog.getDialogPane().setExpanded(true);
        return dialog;
    }

    /**
     * heper class to build the property items for the property sheet
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PropertySheetItem implements PropertySheet.Item
    {
        /**
         * don't look at my privates
         */
        private String category;

        /**
         * don't look at my privates
         */
        private String description;

        /**
         * don't look at my privates
         */
        private String name;

        /**
         * don't look at my privates
         */
        private Class<String> type;

        /**
         * our value won't change so don't bother with the observable
         */
        @Override
        public Optional<ObservableValue<?>> getObservableValue()
        {
            return Optional.empty();
        }

        /**
         * don't look at my privates
         */
        private String value;

        @Override
        public void setValue(final Object arg0)
        {
            value = arg0.toString();
        }
    }

    /**
     * helper to display the property sheet
     */
    public void propertySheet(final String message, final Properties props)
    {
        final ObservableList<PropertySheet.Item> items = props.entrySet()
            .stream()
            .map(
                    entry -> PropertySheetItem.builder()
                        .name(entry.getKey().toString())
                        .value(entry.getValue().toString())
                        .build())
            .collect(Collectors.toCollection(FXCollections::observableArrayList));

        final PropertySheet sheet = new PropertySheet(items);
        sheet.setModeSwitcherVisible(false);
        sheet.setSearchBoxVisible(false);
        PROPERTY_DIALOG.setResizable(true);
        PROPERTY_DIALOG.setHeaderText(message);
        PROPERTY_DIALOG.getDialogPane().setExpandableContent(sheet);
        PROPERTY_DIALOG.showAndWait();
    }

    /**
     * helper to display the chooser dialogue
     */
    public Optional<String> choose(final String message, final int defaultValue, final List<String> choices)
    {
        final ChoiceDialog<String> inputDialog = new ChoiceDialog<>();
        inputDialog.setTitle("Choose Value");
        inputDialog.setHeaderText(message);
        inputDialog.getItems().addAll(choices);
        inputDialog.setSelectedItem(choices.get(defaultValue));
        return inputDialog.showAndWait();
    }

    /**
     * helper to display the error dialogue
     */
    public void error(final String message)
    {
        ERROR_DIALOG.setHeaderText(message);
        ERROR_DIALOG.showAndWait();
    }

    /**
     * helper to display the info dialogue
     */
    public void info(final String message)
    {
        INFO_DIALOG.setHeaderText(message);
        INFO_DIALOG.showAndWait();
    }

    /**
     * helper to display the info dialogue
     */
    public Alert working(final String message)
    {
        WORKING_DIALOG.setHeaderText(message);
        WORKING_DIALOG.show();
        return WORKING_DIALOG;
    }

    /**
     * helper to prompt the user for input
     */
    public boolean prompt(final String message)
    {
        final Alert errorDialog = new Alert(AlertType.CONFIRMATION);
        errorDialog.setTitle("Confirm");
        errorDialog.setHeaderText(message);
        final Optional<ButtonType> result = errorDialog.showAndWait();
        ButtonType pressedButton = ButtonType.CANCEL;
        if (result.isPresent())
        {
            pressedButton = result.get();
        }
        return ButtonType.OK.equals(pressedButton);
    }

    /**
     * helper to prompt the user for input
     */
    public Optional<Integer> prompt(final String message, final int defaultValue)
    {
        final TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Enter Value");
        inputDialog.setHeaderText(message);
        inputDialog.setResult(String.valueOf(defaultValue));
        final Optional<String> result = inputDialog.showAndWait();
        if (result.isPresent())
        {
            try
            {
                final Integer value = Integer.valueOf(result.get());
                return Optional.of(value);
            }
            catch (final NumberFormatException exc)
            {
                error("Invalid Numeric Value Entered");
            }
        }
        return Optional.empty();
    }

    /**
     * helper to prompt the user for input
     */
    public Optional<String> prompt(final String message, final String defaultValue)
    {
        final TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Enter Value");
        inputDialog.setHeaderText(message);
        inputDialog.setResult(String.valueOf(defaultValue));
        return inputDialog.showAndWait();
    }
}
