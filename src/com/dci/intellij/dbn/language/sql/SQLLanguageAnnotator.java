package com.dci.intellij.dbn.language.sql;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.content.DatabaseLoadMonitor;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.statement.StatementGutterRenderer;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.language.common.psi.ChameleonPsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.NamedPsiElement;
import com.dci.intellij.dbn.language.common.psi.TokenPsiElement;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;

public class SQLLanguageAnnotator implements Annotator {
    public static final SQLLanguageAnnotator INSTANCE = new SQLLanguageAnnotator();

    public void annotate(@NotNull final PsiElement psiElement, @NotNull final AnnotationHolder holder) {
        if (psiElement instanceof ExecutablePsiElement)  {
            annotateExecutable((ExecutablePsiElement) psiElement, holder);

        } else if (psiElement instanceof ChameleonPsiElement)  {
            annotateChameleon(psiElement, holder);

        } else if (psiElement instanceof TokenPsiElement) {
            annotateToken((TokenPsiElement) psiElement, holder);

        } else if (psiElement instanceof IdentifierPsiElement) {
            IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) psiElement;
            ConnectionHandler connectionHandler = identifierPsiElement.getActiveConnection();
            if (connectionHandler != null && !connectionHandler.isVirtual()) {
                annotateIdentifier(identifierPsiElement, holder);
            }
        }



        if (psiElement instanceof NamedPsiElement) {
            NamedPsiElement namedPsiElement = (NamedPsiElement) psiElement;
            if (namedPsiElement.hasErrors()) {
                holder.createErrorAnnotation(namedPsiElement, "Invalid " + namedPsiElement.getElementType().getDescription());
            }
        }
    }

    private static void annotateToken(TokenPsiElement tokenPsiElement, AnnotationHolder holder) {
        TokenTypeCategory flavor = tokenPsiElement.getElementType().getFlavor();
        if (flavor != null) {
            Annotation annotation = holder.createInfoAnnotation(tokenPsiElement, null);
            switch (flavor) {
                case DATATYPE: annotation.setTextAttributes(SQLTextAttributesKeys.DATA_TYPE); break;
                case FUNCTION: annotation.setTextAttributes(SQLTextAttributesKeys.FUNCTION); break;
                case KEYWORD: annotation.setTextAttributes(SQLTextAttributesKeys.KEYWORD); break;
                case IDENTIFIER: annotation.setTextAttributes(SQLTextAttributesKeys.IDENTIFIER); break;
            }
        }
    }

    private void annotateIdentifier(IdentifierPsiElement identifierPsiElement, final AnnotationHolder holder) {
        if (identifierPsiElement.getLanguageDialect().isReservedWord(identifierPsiElement.getText())) {
            Annotation annotation = holder.createInfoAnnotation(identifierPsiElement, null);
            annotation.setTextAttributes(SQLTextAttributesKeys.IDENTIFIER);
        }
        if (identifierPsiElement.isObject()) {
            boolean ensureDataLoaded = DatabaseLoadMonitor.isEnsureDataLoaded();
            DatabaseLoadMonitor.setEnsureDataLoaded(false);
            try {
                annotateObject(identifierPsiElement, holder);
            } finally {
                DatabaseLoadMonitor.setEnsureDataLoaded(ensureDataLoaded);
            }

        } else if (identifierPsiElement.isAlias()) {
            if (identifierPsiElement.isReference())
                annotateAliasRef(identifierPsiElement, holder); else
                annotateAliasDef(identifierPsiElement, holder);
        }
    }

    private static void annotateAliasRef(IdentifierPsiElement aliasReference, AnnotationHolder holder) {
        if (aliasReference.resolve() == null &&  aliasReference.getResolveTrialsCount() > 3) {
            Annotation annotation = holder.createWarningAnnotation(aliasReference, "Unknown identifier");
            annotation.setTextAttributes(SQLTextAttributesKeys.UNKNOWN_IDENTIFIER);
        } else {
            Annotation annotation = holder.createInfoAnnotation(aliasReference, null);
            annotation.setTextAttributes(SQLTextAttributesKeys.ALIAS);
        }
    }

    private void annotateAliasDef(IdentifierPsiElement aliasDefinition, AnnotationHolder holder) {
        /*Set<BasePsiElement> aliasDefinitions = new HashSet<BasePsiElement>();
        BasePsiElement scope = aliasDefinition.getEnclosingScopePsiElement();
        scope.collectAliasDefinitionPsiElements(aliasDefinitions, aliasDefinition.getUnquotedText(), DBObjectType.ANY);
        if (aliasDefinitions.size() > 1) {
            holder.createWarningAnnotation(aliasDefinition, "Duplicate alias definition: " + aliasDefinition.getUnquotedText());
        }
        Annotation annotation = holder.createInfoAnnotation(aliasDefinition, null);
        annotation.setTextAttributes(SQLTextAttributesKeys.ALIAS);*/
    }

    private static void annotateObject(IdentifierPsiElement objectReference, AnnotationHolder holder) {
        if (!objectReference.isResolving() && !objectReference.isDefinition()) {
            PsiElement reference = objectReference.resolve();
            if (reference == null && checkConnection(objectReference)) {
                if (objectReference.getResolveTrialsCount() > 3) {
                    Annotation annotation = holder.createWarningAnnotation(objectReference.getNode(),
                            "Unknown identifier");
                    annotation.setTextAttributes(SQLTextAttributesKeys.UNKNOWN_IDENTIFIER);
                }
            }
        }
    }

    private static boolean checkConnection(IdentifierPsiElement objectReference) {
        ConnectionHandler connectionHandler = objectReference.getActiveConnection();
        return connectionHandler != null && !connectionHandler.isVirtual() && connectionHandler.canConnect() && connectionHandler.getConnectionStatus().isValid() && !connectionHandler.getLoadMonitor().isLoading();
    }

    private static void annotateExecutable(ExecutablePsiElement executablePsiElement, AnnotationHolder holder) {
        if (!executablePsiElement.isNestedExecutable()) {
            Annotation annotation = holder.createInfoAnnotation(executablePsiElement, null);
            annotation.setGutterIconRenderer(new StatementGutterRenderer(executablePsiElement));
        }
    }

    private static void annotateChameleon(PsiElement psiElement, AnnotationHolder holder) {
        ChameleonPsiElement executable = (ChameleonPsiElement) psiElement;
/*
        if (!executable.isNestedExecutable()) {
            StatementExecutionProcessor executionProcessor = executable.getExecutionProcessor();
            if (executionProcessor != null) {
                Annotation annotation = holder.createInfoAnnotation(psiElement, null);
                annotation.setGutterIconRenderer(new StatementGutterRenderer(executionProcessor));
            }
        }
*/
    }
}
