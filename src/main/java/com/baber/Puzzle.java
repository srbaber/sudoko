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
        for (int x = 0; x < CELL_COUNT; x++) {
            for (int y = 0; y < CELL_COUNT; y++) {
                this.values[x][y] = values[x][y];
            }
        }
    }

    public void set(int i, int value) {
        int x = i / CELL_COUNT;
        int y = i % CELL_COUNT;
        set(x, y, value);
    }

    public void set(int x, int y, int value) {
        values[x][y] = value;
    }

    public boolean isSet(int x, int y) {
        return values[x][y] != 0;
    }

    public boolean isUnSet(int x, int y) {
        return !isSet(x, y);
    }

    public int get(int i) {
        int x = i / CELL_COUNT;
        int y = i % CELL_COUNT;
        return get(x, y);
    }

    public int get(int x, int y) {
        return values[x][y];
    }

    public List<Integer> colValues(int y) {
        List<Integer> col = Lists.newArrayList();
        for (int x = 0; x < CELL_COUNT; x++) {
            col.add(get(x, y));
        }
        return col;
    }

    public List<Integer> rowValues(int x) {
        List<Integer> row = Lists.newArrayList();
        for (int y = 0; y < CELL_COUNT; y++) {
            row.add(get(x, y));
        }
        return row;
    }

    public List<Integer> boxValues(int x, int y) {
        List<Integer> box = Lists.newArrayList();
        int baseX = x / 3;
        int baseY = y / 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                box.add(get(baseX * 3 + i, baseY * 3 + j));
            }
        }
        return box;
    }

    public List<Integer> candidates(int x, int y) {
        List<Integer> candidateList = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        candidateList.removeAll(rowValues(x));
        candidateList.removeAll(colValues(y));
        candidateList.removeAll(boxValues(x, y));
        return candidateList;
    }

    public void solveSingleCandidates() {
        boolean hasSingleCandidate;
        do {
            hasSingleCandidate = false;
            for (int x = 0; x < CELL_COUNT; x++) {
                for (int y = 0; y < CELL_COUNT; y++) {
                    List<Integer> candidateList = candidates(x, y);
                    if (isUnSet(x, y) && candidateList.size() == 1) {
                        log.debug("Single Candidate at {}x{} -- {}", x, y, candidateList.get(0));
                        values[x][y] = candidateList.get(0);
                        hasSingleCandidate = true;
                    }
                }
            }
        } while (hasSingleCandidate);
    }

    public boolean isValid() {
        for (int x = 0; x < CELL_COUNT; x++) {
            for (int y = 0; y < CELL_COUNT; y++) {
                if (isSet(x, y)) {
                    int value = get(x, y);
                    boolean unique = hasUniqueValues(rowValues(x), value);
                    unique &= hasUniqueValues(colValues(y), value);
                    unique &= hasUniqueValues(boxValues(x, y), value);

                    if (!unique) {
                        error = Optional.of("Puzzle has errors at cell " + (x+1) + " x " + (y+1));
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
        for (int x = 0; x < CELL_COUNT; x++) {
            for (int y = 0; y < CELL_COUNT; y++) {
                List<Integer> candidateList = candidates(x, y);
                if (isUnSet(x, y) && candidateList.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("UUID " + id + "\n");
        for (int x = 0; x < CELL_COUNT; x++) {
            if (x > 0) {
                str.append("\n");
            }
            if (x % 3 == 0) {
                str.append("\n");
            }

            for (int y = 0; y < CELL_COUNT; y++) {
                if (y > 0) {
                    str.append(" ");
                }
                if (y % 3 == 0) {
                    str.append(" ");
                }
                str.append(values[x][y]);
            }
        }
        return str.toString();
    }

    public List<Puzzle> fork() {
        int bestX = CELL_COUNT;
        int bestY = CELL_COUNT;
        int minCandidateSize = CELL_COUNT;
        for (int x = 0; x < CELL_COUNT; x++) {
            for (int y = 0; y < CELL_COUNT; y++) {
                List<Integer> candidateList = candidates(x, y);
                if (isUnSet(x, y) && candidateList.size() < minCandidateSize) {
                    bestX = x;
                    bestY = y;
                    minCandidateSize = candidateList.size();
                }
            }
        }

        List<Puzzle> forks = Lists.newArrayList();
        if (bestX == CELL_COUNT || bestY == CELL_COUNT) {
            return forks;
        }

        List<Integer> candidateList = candidates(bestX, bestY);
        for (Integer value : candidateList) {
            Puzzle fork = new Puzzle(this.values);
            fork.set(bestX, bestY, value);
            forks.add(fork);
            log.debug("Fork {}x{}\n{}", bestX, bestY, fork);
        }
        return forks;
    }

    public boolean isSolved() {
        for (int x = 0; x < CELL_COUNT; x++) {
            for (int y = 0; y < CELL_COUNT; y++) {
                if (isUnSet(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }
}
