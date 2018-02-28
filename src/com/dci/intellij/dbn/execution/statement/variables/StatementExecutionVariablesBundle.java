package com.dci.intellij.dbn.execution.statement.variables;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.IdentifierCategory;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecVariablePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectLookupAdapter;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.util.text.StringUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

public class StatementExecutionVariablesBundle implements Disposable{
    public static final Comparator<StatementExecutionVariable> NAME_LENGTH_COMPARATOR = new Comparator<StatementExecutionVariable>() {
        @Override
        public int compare(StatementExecutionVariable o1, StatementExecutionVariable o2) {
            return o2.getName().length() - o1.getName().length();
        }
    };
    public static final Comparator<StatementExecutionVariable> OFFSET_COMPARATOR = new Comparator<StatementExecutionVariable>() {
        @Override
        public int compare(StatementExecutionVariable o1, StatementExecutionVariable o2) {
            return o1.getOffset() - o2.getOffset();
        }
    };
    private Map<StatementExecutionVariable, String> errorMap;
    private Set<StatementExecutionVariable> variables = new THashSet<StatementExecutionVariable>();

    public StatementExecutionVariablesBundle(Set<ExecVariablePsiElement> variablePsiElements) {
        initialize(variablePsiElements);
    }

    public void initialize(Set<ExecVariablePsiElement> variablePsiElements) {
        Set<StatementExecutionVariable> newVariables = new THashSet<StatementExecutionVariable>();
        for (ExecVariablePsiElement variablePsiElement : variablePsiElements) {
            StatementExecutionVariable variable = getVariable(variablePsiElement.getText());
            if (variable == null) {
                variable = new StatementExecutionVariable(variablePsiElement);
            } else {
                variable.setOffset(variablePsiElement.getTextOffset());
            }

            if (variable.getDataType() == null) {
                DBDataType dataType = lookupDataType(variablePsiElement);
                if (dataType != null && dataType.isNative()) {
                    variable.setDataType(dataType.getGenericDataType());
                } else {
                    variable.setDataType(GenericDataType.LITERAL);
                }
            }
            newVariables.add(variable);
        }
        variables = newVariables;
    }

    public boolean isIncomplete() {
        for (StatementExecutionVariable variable : variables) {
            if (StringUtil.isEmpty(variable.getValue())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasErrors() {
        return errorMap != null && errorMap.size() > 0;
    }

    private static DBDataType lookupDataType(ExecVariablePsiElement variablePsiElement) {
        BasePsiElement conditionPsiElement = variablePsiElement.findEnclosingPsiElement(ElementTypeAttribute.CONDITION);

        if (conditionPsiElement != null) {
            ObjectLookupAdapter lookupAdapter = new ObjectLookupAdapter(variablePsiElement, IdentifierCategory.REFERENCE, DBObjectType.COLUMN);
            BasePsiElement basePsiElement = lookupAdapter.findInScope(conditionPsiElement);
            if (basePsiElement instanceof IdentifierPsiElement) {
                IdentifierPsiElement columnPsiElement = (IdentifierPsiElement) basePsiElement;
                DBObject object = columnPsiElement.resolveUnderlyingObject();
                if (object != null && object instanceof DBColumn) {
                    DBColumn column = (DBColumn) object;
                    return column.getDataType();
                }
            }
        }
        return null;
    }

    @Nullable
    public StatementExecutionVariable getVariable(String name) {
        for (StatementExecutionVariable variable : variables) {
            if (variable.getName().equalsIgnoreCase(name)) {
                return variable;
            }
        }
        return null;
    }

    public Set<StatementExecutionVariable> getVariables() {
        return variables;
    }

    public String prepareStatementText(ConnectionHandler connectionHandler, String statementText, boolean forPreview) {
        errorMap = null;
        List<StatementExecutionVariable> variables = new ArrayList<StatementExecutionVariable>(this.variables);
        Collections.sort(variables, NAME_LENGTH_COMPARATOR);
        for (StatementExecutionVariable variable : variables) {
            String value = forPreview ? variable.getPreviewValueProvider().getValue() : variable.getValue();
            GenericDataType genericDataType = forPreview ? variable.getPreviewValueProvider().getDataType() : variable.getDataType();

            if (!StringUtil.isEmpty(value)) {
                RegionalSettings regionalSettings = RegionalSettings.getInstance(connectionHandler.getProject());

                if (genericDataType == GenericDataType.LITERAL) {
                    value = StringUtil.replace(value, "'", "''");
                    value = '\'' + value + '\'';
                } else if (genericDataType == GenericDataType.DATE_TIME){
                    DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
                    try {
                        Date date = regionalSettings.getFormatter().parseDateTime(value);
                        value = metadataInterface.createDateString(date);
                    } catch (ParseException e) {
                        try {
                            Date date = regionalSettings.getFormatter().parseDate(value);
                            value = metadataInterface.createDateString(date);
                        } catch (ParseException e1) {
                            addError(variable, "Invalid date");
                        }
                    }
                } else if (genericDataType == GenericDataType.NUMERIC){
                    try {
                        regionalSettings.getFormatter().parseNumber(value);
                    } catch (ParseException e) {
                        addError(variable, "Invalid number");
                    }

                } else {
                    throw new IllegalArgumentException("Data type " + genericDataType.getName() + " not supported with execution variables.");
                }

                statementText = StringUtil.replaceIgnoreCase(statementText, variable.getName(), value);
            }
        }
        return statementText;
    }

    private void addError(StatementExecutionVariable variable, String value) {
        if (errorMap == null) {
            errorMap = new THashMap<StatementExecutionVariable, String>();
        }
        errorMap.put(variable, value);
    }

    public String getError(StatementExecutionVariable variable) {
        return errorMap == null ? null : errorMap.get(variable);
    }

    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        disposed = true;
    }
}
