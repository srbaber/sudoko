package com.baber;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
class Puzzle {
    public static final int CELL_COUNT = 9;
    String id = UUID.randomUUID().toString();
    int[][] values = new int[CELL_COUNT][CELL_COUNT];
    Optional<String> error = Optional.empty();

    public Puzzle(int... values) {
        if (values.length != CELL_COUNT * CELL_COUNT) {
            log.error("Invalid input");
            System.exit(0);
        }
        for (int i = 0; i < values.length; i++) {
            set(i, values[i]);
        }
    }

    public Puzzle(int[][] values) {
        for (int row = 0; row < CELL_COUNT; row++) {
            for (int col = 0; col < CELL_COUNT; col++) {
                this.values[row][col] = values[row][col];
            }
        }
    }

    public void set(int i, int value) {
        int x = i / CELL_COUNT;
        int y = i % CELL_COUNT;
        set(x, y, value);
    }

    public void set(int row, int col, int value) {
        values[row][col] = value;
    }

    public boolean isSet(int row, int col) {
        return values[row][col] != 0;
    }

    public boolean isUnSet(int row, int col) {
        return !isSet(row, col);
    }

    public int get(int i) {
        int row = i / CELL_COUNT;
        int col = i % CELL_COUNT;
        return get(row, col);
    }

    public int get(int row, int col) {
        return values[row][col];
    }

    public List<Integer> colValues(int col) {
        List<Integer> colValues = Lists.newArrayList();
        for (int row = 0; row < CELL_COUNT; row++) {
            colValues.add(get(row, col));
        }
        return colValues;
    }

    public List<Integer> rowValues(int row) {
        List<Integer> rowValues = Lists.newArrayList();
        for (int col = 0; col < CELL_COUNT; col++) {
            rowValues.add(get(row, col));
        }
        return rowValues;
    }

    public List<Integer> boxValues(int row, int col) {
        List<Integer> box = Lists.newArrayList();
        int baseRow = (row / 3) * 3;
        int baseCol = (col / 3) * 3;
        for (int i = baseRow; i < baseRow + 3; i++) {
            for (int j = baseCol; j < baseCol + 3; j++) {
                box.add(get(i, j));
            }
        }
        return box;
    }

    public List<Integer> candidates(int row, int col) {
        List<Integer> candidateList = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        candidateList.removeAll(rowValues(row));
        candidateList.removeAll(colValues(col));
        candidateList.removeAll(boxValues(row, col));
        return candidateList;
    }

    public void solveSingleCandidates() {
        boolean hasSingleCandidate;
        do {
            hasSingleCandidate = false;
            for (int row = 0; row < CELL_COUNT; row++) {
                for (int col = 0; col < CELL_COUNT; col++) {
                    List<Integer> candidateList = candidates(row, col);
                    if (isUnSet(row, col) && candidateList.size() == 1) {
                        log.debug("Single Candidate at row {} x col {} -- {}", row, col, candidateList.get(0));
                        values[row][col] = candidateList.get(0);
                        hasSingleCandidate = true;
                    }
                }
            }
        } while (hasSingleCandidate);
    }

    private boolean isLastOnColumn(int row, int col) {
        boolean otherColumnsFiled = true;
        int startRow = (row / 3) * 3;
        for (int i = startRow; i < startRow + 3; i++) {
            if (i != row && isUnSet(i, col)) {
                otherColumnsFiled = false;
            }
        }
        return otherColumnsFiled;
    }

    private boolean isLastOnRow(int row, int col) {
        boolean otherRowsFiled = true;
        int startCol = (col / 3) * 3;
        for (int j = startCol; j < startCol + 3; j++) {
            if (j != col && isUnSet(row, j)) {
                otherRowsFiled = false;
            }
        }
        return otherRowsFiled;
    }

    private boolean valueSetOnOtherRows(int row, int value) {
        int startRow = (row / 3) * 3;
        for (int i = startRow; i < startRow + 3; i++) {
            if (i != row && !rowValues(i).contains(value)) {
                return false;
            }
        }
        return true;
    }

    private boolean valueSetOnOtherColumns(int col, int value) {
        int startCol = (col / 3) * 3;
        for (int j = startCol; j < startCol + 3; j++) {
            if (j != col && !colValues(j).contains(value)) {
                return false;
            }
        }
        return true;
    }

    public void solveWhenOtherRowsSolved() {
        for (int row = 0; row < CELL_COUNT; row++) {
            for (int col = 0; col < CELL_COUNT; col++) {
                if (isUnSet(row, col)) {
                    List<Integer> candidateList = candidates(row, col);
                    for (int candidate=0; candidate< candidateList.size(); candidate++) {
                        int value = candidateList.get(candidate);
                        if (isLastOnColumn(row, col) && valueSetOnOtherColumns(col, value)) {
                            log.info("last one on the column row {} x col {} --", row, col, value);
                            set(row, col, value);
                            return;
                        } else if (isLastOnRow(row, col) && valueSetOnOtherRows(row, value)) {
                            log.info("last one on the row row {} x col {} -- {}", row, col, value);
                            set(row, col, value);
                            return;
                        } else if (valueSetOnOtherColumns(col, value) && valueSetOnOtherRows(row, value))
                        {
                            log.info("last one on the row & column row {} x col {} -- {}", row, col, value);
                            set(row, col, value);
                            return;
                        }
                    }
                }
            }
        }
    }

    public boolean isValid() {
        for (int row = 0; row < CELL_COUNT; row++) {
            for (int col = 0; col < CELL_COUNT; col++) {
                if (isSet(row, col)) {
                    int value = get(row, col);
                    boolean unique = hasUniqueValues(rowValues(row), value);
                    unique &= hasUniqueValues(colValues(col), value);
                    unique &= hasUniqueValues(boxValues(row, col), value);

                    if (!unique) {
                        error = Optional.of("Puzzle has errors at cell row " + (row + 1) + " x col " + (col + 1));
                        return false;
                    }
                }
            }
        }
        error = Optional.empty();
        return true;
    }

    private boolean hasUniqueValues(List<Integer> newValues, int cellValue) {
        boolean foundOnce = false;
        for (int value : newValues) {
            if (cellValue == value) {
                if (foundOnce) {
                    return false;
                } else {
                    foundOnce = true;
                }
            }
        }
        return true;
    }

    public boolean isSolvable() {
        for (int row = 0; row < CELL_COUNT; row++) {
            for (int col = 0; col < CELL_COUNT; col++) {
                List<Integer> candidateList = candidates(row, col);
                if (isUnSet(row, col) && candidateList.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("UUID " + id + "\n");
        for (int row = 0; row < CELL_COUNT; row++) {
            if (row > 0) {
                str.append("\n");
            }
            if (row % 3 == 0) {
                str.append("\n");
            }

            for (int col = 0; col < CELL_COUNT; col++) {
                if (col > 0) {
                    str.append(" ");
                }
                if (col % 3 == 0) {
                    str.append(" ");
                }
                str.append(values[row][col]);
            }
        }
        return str.toString();
    }

    public List<Puzzle> fork() {
        int bestRow = CELL_COUNT;
        int bestCol = CELL_COUNT;
        int minCandidateSize = CELL_COUNT;
        for (int row = 0; row < CELL_COUNT; row++) {
            for (int col = 0; col < CELL_COUNT; col++) {
                List<Integer> candidateList = candidates(row, col);
                if (isUnSet(row, col) && candidateList.size() < minCandidateSize) {
                    bestRow = row;
                    bestCol = col;
                    minCandidateSize = candidateList.size();
                }
            }
        }

        List<Puzzle> forks = Lists.newArrayList();
        if (bestRow == CELL_COUNT || bestCol == CELL_COUNT) {
            return forks;
        }

        List<Integer> candidateList = candidates(bestRow, bestCol);
        for (Integer value : candidateList) {
            Puzzle fork = new Puzzle(this.values);
            fork.set(bestRow, bestCol, value);
            forks.add(fork);
            log.debug("Fork row {} x col {}\n{}", bestRow, bestCol, fork);
        }
        return forks;
    }

    public boolean isSolved() {
        for (int row = 0; row < CELL_COUNT; row++) {
            for (int col = 0; col < CELL_COUNT; col++) {
                if (isUnSet(row, col)) {
                    return false;
                }
            }
        }
        return true;
    }
}
