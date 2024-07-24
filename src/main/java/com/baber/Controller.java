package com.baber;

import com.baber.fx.Dialogs;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import lombok.NoArgsConstructor;
import lombok.NonNull;


@NoArgsConstructor
public class Controller {
    private final Dialogs dialogs = new Dialogs();

    @SuppressWarnings("PMD.DoNotCallSystemExit")
    public void exit(final @NonNull Event evt) {
        if (dialogs.prompt("Exit Program?")) {
            Platform.exit();
            System.exit(0);
        } else {
            evt.consume();
        }
    }

    @FXML
    public void fileClose(final ActionEvent evt) {
        exit(evt);
    }

    @FXML
    public void about(final ActionEvent evt) {
        dialogs.info("Sudoku Solver V1.0.0");
    }
}
