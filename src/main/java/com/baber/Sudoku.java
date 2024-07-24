package com.baber;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Sudoku {
    static Puzzle puzzle = new Puzzle(6,3,0,1,0,0,0,9,0,0,0,8,9,5,4,1,0,0,0,0,0,0,0,0,2,5,7,1,0,0,0,9,8,0,0,6,7,0,0,2,4,6,0,0,9,9,0,6,0,0,0,5,2,4,5,4,0,8,6,0,3,0,0,0,0,2,4,0,9,0,8,0,0,6,7,0,0,0,0,0,2);

    public static Puzzle solve(Puzzle root)
    {
        List<Puzzle> puzzleSteps = Lists.newArrayList();
        puzzleSteps.add(root);
        while (!puzzleSteps.isEmpty()) {
            for (int i = 0; i< puzzleSteps.size(); i++) {
                Puzzle puzzle = puzzleSteps.get(i);
                log.info("Evalulate {} of {}\n{}", i+1, puzzleSteps.size(), puzzle);
                puzzle.solveSingleCandidates();
            }

            List<Puzzle> forks = Lists.newArrayList();
            for (Puzzle puzzle : puzzleSteps) {
                if (puzzle.isSolved()) {
                    return puzzle;
                } else if (puzzle.isSolvable()) {
                    forks.addAll(puzzle.fork());
                } else {
                    log.debug("Abandoned UUID {}", puzzle.id);
                }
            }
            puzzleSteps = forks;
        }
        return null;
    }

    public static void main(final String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Puzzle solution = solve(puzzle);
        log.info("Solution in {} ms\n{}", stopwatch.elapsed(TimeUnit.MILLISECONDS), solution);
    }

    static class Puzzle {
        static final int CELL_COUNT = 9;
        String id = UUID.randomUUID().toString();
        int[][] values = new int[CELL_COUNT][CELL_COUNT];

        public Puzzle(int... values) {
            if (values.length != CELL_COUNT * CELL_COUNT)
            {
                log.error("Invalid input");
                System.exit(0);
            }
            for (int i = 0; i < values.length; i++) {
                set(i, values[i]);
            }
        }

        public Puzzle clone()
        {
            Puzzle clone = new Puzzle();
            clone.values = this.values.clone();
            return clone;
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
            int bestX=CELL_COUNT;
            int bestY=CELL_COUNT;
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
            List<Integer> candidateList = candidates(bestX, bestY);
            for (Integer value : candidateList)
            {
                Puzzle fork = clone();
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
}
