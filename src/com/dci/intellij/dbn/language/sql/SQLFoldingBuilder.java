package com.dci.intellij.dbn.language.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.ChameleonPsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.RootPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.folding.LanguageFolding;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;

public class SQLFoldingBuilder implements FoldingBuilder, DumbAware {

    @NotNull
    public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
        List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>();

        PsiElement child = node.getPsi().getFirstChild();
        while (child != null) {
            if (child instanceof RootPsiElement) {
                RootPsiElement rootPsiElement = (RootPsiElement) child;
                /*FoldingDescriptor rootFoldingDescriptor = new FoldingDescriptor(
                            rootPsiElement.getAstNode(),
                            rootPsiElement.getTextRange()); 
                foldingDescriptors.add(rootFoldingDescriptor);*/

                for (ExecutablePsiElement executablePsiElement : rootPsiElement.getExecutablePsiElements()) {
                    TextRange textRange = executablePsiElement.getTextRange();
                    if (textRange.getLength() > 10) {
                        ASTNode childNode = executablePsiElement.getNode();
                        FoldingDescriptor foldingDescriptor = new FoldingDescriptor(childNode, textRange);
                        descriptors.add(foldingDescriptor);
                    }
                }
            } else if (child instanceof ChameleonPsiElement) {
                ChameleonPsiElement chameleonPsiElement = (ChameleonPsiElement) child;
                FoldingDescriptor foldingDescriptor = new FoldingDescriptor(
                        chameleonPsiElement.getNode(),
                        chameleonPsiElement.getTextRange());
                descriptors.add(foldingDescriptor);


                FoldingBuilder foldingBuilder = LanguageFolding.INSTANCE.forLanguage(chameleonPsiElement.getLanguage());
                FoldingDescriptor[] nestedDescriptors = foldingBuilder.buildFoldRegions(chameleonPsiElement.getNode(), document);
                descriptors.addAll(Arrays.asList(nestedDescriptors));

            }
            child = child.getNextSibling();
        }
        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }

    public String getPlaceholderText(@NotNull ASTNode node) {
        BasePsiElement basePsiElement = (BasePsiElement) node.getPsi();
        Set<IdentifierPsiElement> subjects = new HashSet<IdentifierPsiElement>();
        basePsiElement.collectSubjectPsiElements(subjects);
        StringBuilder buffer = new StringBuilder(basePsiElement.getSpecificElementType().getDescription());
        if (subjects.size() > 0) {
            buffer.append(" (");
            buffer.append(NamingUtil.createNamesList(subjects, 3));
            buffer.append(")");
        }
        return buffer.toString();
    }

    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }

}
