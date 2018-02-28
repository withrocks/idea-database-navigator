package com.dci.intellij.dbn.code.common.lookup;

import javax.swing.Icon;

import com.dci.intellij.dbn.code.common.completion.CodeCompletionContext;
import com.dci.intellij.dbn.code.common.completion.CodeCompletionContributor;
import com.dci.intellij.dbn.code.common.completion.CodeCompletionLookupConsumer;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.code.common.completion.options.general.CodeCompletionFormatSettings;
import com.dci.intellij.dbn.code.common.style.DBLCodeStyleManager;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.object.DBSynonym;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;

public class ObjectLookupItemBuilder extends LookupItemBuilder {
    private DBLanguage language;
    private DBObjectRef objectRef;

    public ObjectLookupItemBuilder(DBObject object, DBLanguage language) {
        this.objectRef = object.getRef();
        this.language = language;
    }

    @Override
    public CodeCompletionLookupItem createLookupItem(Object source, CodeCompletionLookupConsumer consumer) {
        CodeCompletionLookupItem lookupItem = super.createLookupItem(source, consumer);
        DBObject object = getObject();
        if (object != null && !object.isDisposed() && lookupItem != null) {
            if (object.needsNameQuoting()) {
                DatabaseCompatibilityInterface compatibilityInterface = DatabaseCompatibilityInterface.getInstance(object);
                String lookupString = lookupItem.getLookupString();
                char quoteChar = compatibilityInterface.getIdentifierQuotes();
                lookupString = quoteChar + lookupString + quoteChar;
                lookupItem.setLookupString(lookupString);
            }


/*
            lookupItem.setInsertHandler(consumer.isAddParenthesis() ?
                                BracketsInsertHandler.INSTANCE :
                                BasicInsertHandler.INSTANCE);
*/

        }
        return lookupItem;
    }

    public DBObject getObject() {
        return DBObjectRef.get(objectRef);
    }

    public String getTextHint() {
        DBObject object = getObject();
        if (object != null) {
            DBObject parentObject = object.getParentObject();

            String typePrefix = "";
            if (object instanceof DBSynonym) {
                DBSynonym synonym = (DBSynonym) object;
                DBObject underlyingObject = synonym.getUnderlyingObject();
                if (underlyingObject != null) {
                    typePrefix = underlyingObject.getTypeName() + ' ';
                }
            }

            return parentObject == null ?
                    typePrefix + object.getTypeName() :
                    typePrefix + object.getTypeName() + " (" +
                       parentObject.getTypeName() + ' ' +
                       parentObject.getName() + ')';
        }
        return "";
    }

    public boolean isBold() {
        return false;
    }

    @Override
    public CharSequence getText(CodeCompletionContext context) {
        DBObject object = getObject();
        Project project = context.getFile().getProject();
        CodeStyleCaseSettings styleCaseSettings = DBLCodeStyleManager.getInstance(project).getCodeStyleCaseSettings(language);
        CodeStyleCaseOption caseOption = styleCaseSettings.getObjectCaseOption();
        String text = caseOption.format(objectRef.getObjectName());

        if (object instanceof DBVirtualObject && text.contains(CodeCompletionContributor.DUMMY_TOKEN)) {
            return null;
        }

        String userInput = context.getUserInput();
        CodeCompletionFormatSettings codeCompletionFormatSettings = CodeCompletionSettings.getInstance(project).getFormatSettings();
        if (StringUtil.isNotEmpty(userInput) && !text.startsWith(userInput) && !codeCompletionFormatSettings.isEnforceCodeStyleCase()) {
            char firstInputChar = userInput.charAt(0);
            char firstPresentationChar = text.charAt(0);

            if (Character.toUpperCase(firstInputChar) == Character.toUpperCase(firstPresentationChar)) {
                boolean upperCaseInput = Character.isUpperCase(firstInputChar);
                boolean upperCasePresentation = Character.isUpperCase(firstPresentationChar);

                if (StringUtil.isMixedCase(text)) {
                    if (upperCaseInput != upperCasePresentation) {
                        text = upperCaseInput ?
                                text.toUpperCase() :
                                text.toLowerCase();
                    }
                } else {
                    text = upperCaseInput ?
                            text.toUpperCase() :
                            text.toLowerCase();
                }
            } else {
                return null;
            }
        }

        return text;
    }

    public Icon getIcon() {
        DBObject object = getObject();
        return object == null ? objectRef.getObjectType().getIcon() : object.getIcon();
    }

    @Override
    public void dispose() {
        language = null;
    }
}
