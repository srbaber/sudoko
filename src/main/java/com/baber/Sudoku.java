package com.baber;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
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
                log.info("Evaluate Single Candidates {} of {}\n{}", i + 1, puzzleSteps.size(), puzzle);

                while (puzzle.solveSingleCandidates() || puzzle.solveWhenCelLRequiresValue());
                if (puzzle.isSolved()) {
                    return puzzle;
                }
            }

            List<Puzzle> forks = Lists.newArrayList();
            for (Puzzle puzzle : puzzleSteps) {
                 if (puzzle.isSolvable()) {
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
}
