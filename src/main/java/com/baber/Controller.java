package com.baber;

import com.baber.fx.BackgroundAgent;
import com.baber.fx.Dialogs;
import com.baber.fx.HandlesEvent;
import com.baber.fx.SudokuComboBox;
import com.google.common.base.Stopwatch;
import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.baber.Puzzle.CELL_COUNT;

@Slf4j
public class Controller {
    private static final String SAVE_EXT = "sav";

    private static final String SAVE_FILE = "sudoku.sav";

    private static Stage primaryStage;

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

    public void init (Stage primaryStage)
    {
        this.primaryStage = primaryStage;
        for (int x = 0; x< CELL_COUNT; x++) {
            for (int y = 0; y < CELL_COUNT; y++)
            {
                int xRule = x / 3;
                int yRule = y / 3;
                values[x][y] = new SudokuComboBox();
                gridPane.add(values[x][y], y+yRule, x+xRule);
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

        AtomicReference<Puzzle> solution = new AtomicReference<>();

        EventHandler<WorkerStateEvent> solvedHandler = workerStateEvent -> {
            if (solution.get() != null) {
                updateMessageLog("Solution found in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
                setValues(solution.get().values);
            } else {
                dialogs.error("No solution found");
            }
        };
        EventHandler<WorkerStateEvent> invalidHandler = workerStateEvent -> {
            dialogs.error(puzzle.error.get());
        };
        HandlesEvent<Boolean> backgroundHandler = () -> {
            if (!puzzle.isValid()) {
                return false;
            }

            solution.set(Sudoku.solve(puzzle));
            return true;
        };
        BackgroundAgent<Boolean> solveIt = new BackgroundAgent<Boolean>()
                .andNotifyWhenFailed(invalidHandler)
                .andNotifyWhenComplete(solvedHandler);

        Alert dialog = dialogs.working("Solving");
        solveIt.runInBackground(backgroundHandler);
        dialog.close();
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
        File saveFile = dialogs.fileSave(primaryStage, ".", SAVE_FILE, SAVE_EXT);
        if (saveFile != null) {
            try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                int[][] values = getValues();
                for (int x = 0; x < CELL_COUNT; x++) {
                    for (int y = 0; y < CELL_COUNT; y++) {
                        fos.write(Integer.toString(values[x][y]).getBytes());
                        fos.write('\n');
                    }
                }
            } catch (IOException exc) {
                log.error("Could not save puzzle to {}", saveFile, exc);
                dialogs.error("Could not save puzzle to " + saveFile);
            }
        }
    }

    @FXML
    public void load(final ActionEvent evt) {
        File saveFile = dialogs.fileOpen(primaryStage, ".", SAVE_FILE, SAVE_EXT);
        if (saveFile != null) {
            try (BufferedReader fis = new BufferedReader(new FileReader(saveFile))) {
                int[][] values = new int[CELL_COUNT][CELL_COUNT];
                for (int x = 0; x < CELL_COUNT; x++) {
                    for (int y = 0; y < CELL_COUNT; y++) {
                        String value = fis.readLine();
                        values[x][y] = Integer.parseInt(value);
                    }
                }
                setValues(values);
            } catch (IOException | NumberFormatException exc) {
                log.error("Could not load puzzle from {}", saveFile, exc);
                dialogs.error("Could not load puzzle from " + saveFile);
            }
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
