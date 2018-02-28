package com.dci.intellij.dbn.debugger.frame;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.code.common.style.DBLCodeStyleManager;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.database.common.debug.DebuggerRuntimeInfo;
import com.dci.intellij.dbn.debugger.DBProgramDebugProcess;
import com.dci.intellij.dbn.debugger.DBProgramDebugUtil;
import com.dci.intellij.dbn.debugger.evaluation.DBProgramDebuggerEvaluator;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.psql.PSQLFile;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.XDebuggerBundle;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import gnu.trove.THashMap;

public class DBProgramDebugStackFrame extends XStackFrame {
    private DBProgramDebugProcess debugProcess;
    private XSourcePosition sourcePosition;
    private boolean inhibitSourcePosition = false;
    private int index;
    private DBProgramDebuggerEvaluator evaluator;
    private Map<String, DBProgramDebugValue> valuesMap;

    public DBProgramDebugStackFrame(DBProgramDebugProcess debugProcess, DebuggerRuntimeInfo runtimeInfo, int index) {
        this.index = index;
        DBEditableObjectVirtualFile databaseFile = debugProcess.getDatabaseFile(runtimeInfo);

        this.debugProcess = debugProcess;
        sourcePosition = XSourcePositionImpl.create(databaseFile, runtimeInfo.getLineNumber());
    }

    public void setInhibitSourcePosition(boolean inhibitSourcePosition) {
        this.inhibitSourcePosition = inhibitSourcePosition;
    }

    public DBProgramDebugProcess getDebugProcess() {
        return debugProcess;
    }

    public int getIndex() {
        return index;
    }

    public DBProgramDebugValue getValue(String variableName) {
        return valuesMap == null ? null : valuesMap.get(variableName.toLowerCase());
    }

    public void setValue(String variableName, DBProgramDebugValue value) {
        if (valuesMap == null) {
            valuesMap =new THashMap<String, DBProgramDebugValue>();
        }
        valuesMap.put(variableName.toLowerCase(), value);
    }

    @Override
    public XDebuggerEvaluator getEvaluator() {
        if (evaluator == null) {
            evaluator = new DBProgramDebuggerEvaluator(this);
        }
        return evaluator;
    }

    @Override
    public XSourcePosition getSourcePosition() {
        return inhibitSourcePosition ? null : sourcePosition;
    }

    public void customizePresentation(@NotNull ColoredTextContainer component) {

        DBSchemaObject object = DBProgramDebugUtil.getObject(sourcePosition);
        String frameName = "";
        Icon frameIcon = Icons.DBO_METHOD;
        if (object != null) {
            frameName = object.getName();
            frameIcon = object.getIcon();
            DBSourceCodeVirtualFile sourceCodeFile = DBProgramDebugUtil.getSourceCodeFile(sourcePosition);
            PSQLFile psiFile = (PSQLFile) PsiUtil.getPsiFile(sourceCodeFile.getProject(), sourceCodeFile);
            if (psiFile != null) {
                Document document = DocumentUtil.getDocument(sourceCodeFile);
                int offset = document.getLineEndOffset(sourcePosition.getLine());
                PsiElement elementAtOffset = psiFile.findElementAt(offset);
                while (elementAtOffset instanceof PsiWhiteSpace || elementAtOffset instanceof PsiComment) {
                    elementAtOffset = elementAtOffset.getNextSibling();
                }

                if (elementAtOffset instanceof BasePsiElement) {
                    BasePsiElement basePsiElement = (BasePsiElement) elementAtOffset;
                    BasePsiElement objectDeclarationPsiElement = basePsiElement.findEnclosingPsiElement(ElementTypeAttribute.OBJECT_DECLARATION);
                    if (objectDeclarationPsiElement != null) {
                        IdentifierPsiElement subjectPsiElement = (IdentifierPsiElement) objectDeclarationPsiElement.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
                        if (subjectPsiElement != null) {
                            frameName = frameName + "." + subjectPsiElement.getChars();
                            frameIcon = subjectPsiElement.getObjectType().getIcon();
                        }
                    }
                }

            }


            component.append(frameName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            component.append(" (line " + (sourcePosition.getLine() + 1) + ") ", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
            component.setIcon(frameIcon);
        } else {
            component.append(XDebuggerBundle.message("invalid.frame"), SimpleTextAttributes.ERROR_ATTRIBUTES);
        }
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        valuesMap = new THashMap<String, DBProgramDebugValue>();

        DBSourceCodeVirtualFile sourceCodeFile = DBProgramDebugUtil.getSourceCodeFile(sourcePosition);
        PSQLFile psiFile = (PSQLFile) PsiUtil.getPsiFile(sourceCodeFile.getProject(), sourceCodeFile);
        if (psiFile != null) {
            Document document = DocumentUtil.getDocument(sourceCodeFile);
            int offset = document.getLineStartOffset(sourcePosition.getLine());
            Set<BasePsiElement> variables = psiFile.lookupVariableDefinition(offset);
            CodeStyleCaseSettings codeStyleCaseSettings = DBLCodeStyleManager.getInstance(psiFile.getProject()).getCodeStyleCaseSettings(PSQLLanguage.INSTANCE);
            CodeStyleCaseOption objectCaseOption = codeStyleCaseSettings.getObjectCaseOption();

            List<DBProgramDebugValue> values = new ArrayList<DBProgramDebugValue>();
            for (final BasePsiElement basePsiElement : variables) {
                String variableName = objectCaseOption.format(basePsiElement.getText());
                //DBObject object = basePsiElement.resolveUnderlyingObject();

                Set<String> childVariableNames = null;
                if (basePsiElement instanceof IdentifierPsiElement) {
                    IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) basePsiElement;
                    List<BasePsiElement> qualifiedUsages = identifierPsiElement.findQualifiedUsages();
                    for (BasePsiElement qualifiedUsage : qualifiedUsages) {
                        if (childVariableNames == null) childVariableNames = new HashSet<String>();

                        String childVariableName = objectCaseOption.format(qualifiedUsage.getText());
                        childVariableNames.add(childVariableName);
                    }
                }
                Icon icon = basePsiElement.getIcon(true);
                DBProgramDebugValue value = new DBProgramDebugValue(debugProcess, null, variableName, childVariableNames, icon, index);
                values.add(value);
                valuesMap.put(variableName.toLowerCase(), value);
            }
            Collections.sort(values);

            XValueChildrenList children = new XValueChildrenList();
            for (DBProgramDebugValue value : values) {
                children.add(value.getVariableName(), value);
            }
            node.addChildren(children, true);
        }
    }
}


