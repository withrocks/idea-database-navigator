package com.dci.intellij.dbn.code.sql.style.options;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleFormattingOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleFormattingSettings;
import com.dci.intellij.dbn.code.common.style.presets.clause.ClauseChopDownAlwaysPreset;
import com.dci.intellij.dbn.code.common.style.presets.clause.ClauseChopDownIfLongPreset;
import com.dci.intellij.dbn.code.common.style.presets.clause.ClauseChopDownIfLongStatementPreset;
import com.dci.intellij.dbn.code.common.style.presets.clause.ClauseChopDownNeverPreset;
import com.dci.intellij.dbn.code.common.style.presets.clause.ClauseIgnoreWrappingPreset;
import com.dci.intellij.dbn.code.common.style.presets.iteration.IterationChopDownAlwaysPreset;
import com.dci.intellij.dbn.code.common.style.presets.iteration.IterationChopDownIfLongPreset;
import com.dci.intellij.dbn.code.common.style.presets.iteration.IterationChopDownIfLongStatementPreset;
import com.dci.intellij.dbn.code.common.style.presets.iteration.IterationChopDownIfNotSinglePreset;
import com.dci.intellij.dbn.code.common.style.presets.iteration.IterationIgnoreWrappingPreset;
import com.dci.intellij.dbn.code.common.style.presets.iteration.IterationNoWrappingPreset;
import com.dci.intellij.dbn.code.common.style.presets.statement.StatementIgnoreSpacingPreset;
import com.dci.intellij.dbn.code.common.style.presets.statement.StatementLineBreakAtLeastPreset;
import com.dci.intellij.dbn.code.common.style.presets.statement.StatementLineBreakPreset;
import com.dci.intellij.dbn.code.common.style.presets.statement.StatementOneLineSpacingAtLeastPreset;
import com.dci.intellij.dbn.code.common.style.presets.statement.StatementOneLineSpacingPreset;

public class SQLCodeStyleFormattingSettings extends CodeStyleFormattingSettings {
    public SQLCodeStyleFormattingSettings() {
        CodeStyleFormattingOption statementSpacing =
                new CodeStyleFormattingOption("STATEMENT_SPACING", "Statement spacing");
        statementSpacing.addPreset(new StatementLineBreakPreset());
        statementSpacing.addPreset(new StatementLineBreakAtLeastPreset());
        statementSpacing.addPreset(new StatementOneLineSpacingPreset(), true);
        statementSpacing.addPreset(new StatementOneLineSpacingAtLeastPreset());
        statementSpacing.addPreset(new StatementIgnoreSpacingPreset());
        addOption(statementSpacing);

        CodeStyleFormattingOption clauseChopDown =
                new CodeStyleFormattingOption("CLAUSE_CHOP_DOWN", "Clause chop down");
        clauseChopDown.addPreset(new ClauseChopDownAlwaysPreset());
        clauseChopDown.addPreset(new ClauseChopDownIfLongPreset());
        clauseChopDown.addPreset(new ClauseChopDownIfLongStatementPreset(), true);
        clauseChopDown.addPreset(new ClauseChopDownNeverPreset());
        clauseChopDown.addPreset(new ClauseIgnoreWrappingPreset());
        addOption(clauseChopDown);

        CodeStyleFormattingOption iterationsWrapOption =
                new CodeStyleFormattingOption("ITERATION_ELEMENTS_WRAPPING", "Iteration elements wrapping");
        iterationsWrapOption.addPreset(new IterationChopDownAlwaysPreset());
        iterationsWrapOption.addPreset(new IterationChopDownIfLongPreset());
        iterationsWrapOption.addPreset(new IterationChopDownIfLongStatementPreset());
        iterationsWrapOption.addPreset(new IterationChopDownIfNotSinglePreset(), true);
        iterationsWrapOption.addPreset(new IterationNoWrappingPreset());
        iterationsWrapOption.addPreset(new IterationIgnoreWrappingPreset());
        addOption(iterationsWrapOption);
    }
}
