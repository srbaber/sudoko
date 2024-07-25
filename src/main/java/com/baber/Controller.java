package com.baber;

import com.baber.fx.Dialogs;
import com.baber.fx.SudokuComboBox;
import com.google.common.base.Stopwatch;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.baber.Puzzle.CELL_COUNT;

@Slf4j
public class Controller {
    private static final String SAVE_FILE = "sudoku.sav";

    private final Dialogs dialogs = new Dialogs();

    @FXML
    private TextField messageLog;

    @FXML
    public GridPane gridPane;

    private SudokuComboBox[][] values = new SudokuComboBox[CELL_COUNT][CELL_COUNT];

    /**
     * the singleton controller for reference in the Ui
     */
    @Getter
    private static Controller SINGLETON;

    public Controller ()
    {
        SINGLETON = this;
    }

    public void init ()
    {
        for (int x = 0; x< CELL_COUNT; x++) {
            for (int y = 0; y < CELL_COUNT; y++)
            {
                values[x][y] = new SudokuComboBox();
                gridPane.add(values[x][y], y, x);
            }
        }
        clear(null);
        updateMessageLog("");
    }

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

    private void updateMessageLog(String msg)
    {
            messageLog.setText(msg);
    }

    @FXML
    public void solve(final ActionEvent evt) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Puzzle puzzle = new Puzzle(getValues());
        if (!puzzle.isValid())
        {
            dialogs.error("Puzzle is not valid");
            return;
        }

        Puzzle solution = Sudoku.solve(puzzle);
        if (solution != null) {
            updateMessageLog("Solution in " + stopwatch.elapsed(TimeUnit.MILLISECONDS)  + " ms");
            setValues(solution.values);
        } else {
            dialogs.error("No solution found");
        }
    }

    @FXML
    public void clear(final ActionEvent evt) {
        for (int x=0; x<CELL_COUNT; x++) {
            for (int y = 0; y < CELL_COUNT; y++) {
                values[x][y].setSelectedValue(0);
            }
        }
    }

    @FXML
    public void save(final ActionEvent evt) {
        try (FileOutputStream fos = new FileOutputStream(SAVE_FILE))
        {
            int[][] values = getValues();
            for (int x=0; x<CELL_COUNT; x++) {
                for (int y = 0; y < CELL_COUNT; y++) {
                    fos.write(Integer.toString(values[x][y]).getBytes());
                    fos.write('\n');
                }
            }
        } catch (IOException exc)
        {
            log.error("Could not save puzzle to {}", SAVE_FILE, exc);
            dialogs.error("Could not save puzzle to " + SAVE_FILE);
        }
    }

    @FXML
    public void load(final ActionEvent evt) {
        try (BufferedReader fis = new BufferedReader(new FileReader(SAVE_FILE)))
        {
            int[][] values = new int[CELL_COUNT][CELL_COUNT];
            for (int x=0; x<CELL_COUNT; x++) {
                for (int y = 0; y < CELL_COUNT; y++) {
                    String value = fis.readLine();
                    values[x][y] = Integer.parseInt(value);
                }
            }
            setValues(values);
        } catch (IOException | NumberFormatException exc) {
            log.error("Could not load puzzle from {}", SAVE_FILE, exc);
            dialogs.error("Could not load puzzle from " + SAVE_FILE);
        }
    }

    @FXML
    public void about(final ActionEvent evt) {
        dialogs.info("Sudoku Solver V1.0.0");
    }

    public int[][] getValues()
    {
        int[][] selectedValues = new int[CELL_COUNT][CELL_COUNT];
        for (int x=0; x<CELL_COUNT; x++)
        {
            for (int y=0; y<CELL_COUNT; y++)
            {
                selectedValues[x][y] = values[x][y].getSelectedValue();
            }
        }
        return selectedValues;
    }

    public void setValues(int [][] newValues)
    {
        for (int x=0; x<CELL_COUNT; x++) {
            for (int y = 0; y < CELL_COUNT; y++) {
                values[x][y].setSelectedValue(newValues[x][y]);
            }
        }
    }
}
