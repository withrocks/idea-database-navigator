package com.dci.intellij.dbn.code.common.completion;

import com.dci.intellij.dbn.code.common.lookup.CodeCompletionLookupItem;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.psi.LeafPsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;

public class BasicInsertHandler implements InsertHandler<CodeCompletionLookupItem> {
    public static final BasicInsertHandler INSTANCE = new BasicInsertHandler();

    public void handleInsert(InsertionContext insertionContext, CodeCompletionLookupItem lookupElement) {
        char completionChar = insertionContext.getCompletionChar();

        Object lookupElementObject = lookupElement.getObject();
        if (lookupElementObject instanceof TokenElementType) {
            TokenElementType tokenElementType = (TokenElementType) lookupElementObject;
            if(tokenElementType.getTokenType().isReservedWord()) {
                Editor editor = insertionContext.getEditor();
                CaretModel caretModel = editor.getCaretModel();

                LeafPsiElement leafPsiElement = PsiUtil.lookupLeafAtOffset(insertionContext.getFile(), insertionContext.getTailOffset());
                if (leafPsiElement == null || leafPsiElement.getTextOffset() != caretModel.getOffset()) {
                    if (completionChar == '\t' || completionChar == '\u0000' || completionChar == '\n') {
                        insertionContext.getDocument().insertString(insertionContext.getTailOffset(), " ");
                        caretModel.moveCaretRelatively(1, 0, false, false, false);
                    }
                }
            }
        }

/*        if (completionChar == ' ' || completionChar == '\t' || completionChar == '\u0000') {
            Editor editor = insertionContext.getEditor();
            CaretModel caretModel = editor.getCaretModel();
            caretModel.moveCaretRelatively(1, 0, false, false, false);
        }*/


    }

    protected static boolean shouldInsertCharacter(char chr) {
        return chr != '\t' && chr != '\n' && chr!='\u0000';
    }
}
