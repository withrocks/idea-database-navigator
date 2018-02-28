package com.dci.intellij.dbn.language.common.psi;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingAttributes;
import com.dci.intellij.dbn.code.common.style.formatting.FormattingProviderPsiElement;
import com.dci.intellij.dbn.code.common.style.presets.CodeStylePreset;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.language.common.element.ChameleonElementType;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.formatting.Indent;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

public class ChameleonPsiElement extends ASTWrapperPsiElement implements ExecutableBundlePsiElement, FormattingProviderPsiElement {
    public static final FormattingAttributes FORMATTING_ATTRIBUTES = new FormattingAttributes(null, Indent.getAbsoluteNoneIndent(), CodeStylePreset.SPACING_MIN_ONE_LINE, null);


    private ChameleonElementType elementType;
    public ChameleonPsiElement(@NotNull ASTNode node, ChameleonElementType elementType) {
        super(node);
        this.elementType = elementType;
    }

    public List<ExecutablePsiElement> getExecutablePsiElements() {
        List<ExecutablePsiElement> bucket = new ArrayList<ExecutablePsiElement>();
        collectExecutablePsiElements(bucket, this);
        return bucket;
    }

    private static void collectExecutablePsiElements(List<ExecutablePsiElement> bucket, PsiElement element) {
        PsiElement child = element.getFirstChild();
        while (child != null) {
            if (child instanceof ExecutablePsiElement) {
                ExecutablePsiElement executablePsiElement = (ExecutablePsiElement) child;
                bucket.add(executablePsiElement);
            } else {
                collectExecutablePsiElements(bucket, child);
            }
            child = child.getNextSibling();
        }
    }

    @Override
    public String toString() {
        return elementType.getDebugName();
    }

    @Override
    public Icon getIcon(int flags) {
        return Icons.FILE_BLOCK_PSQL; // todo make this dynamic
    }

    @Override
    public FormattingAttributes getFormattingAttributes() {
        return FORMATTING_ATTRIBUTES;
    }

    @Override
    public FormattingAttributes getFormattingAttributesRecursive(boolean left) {
        return FORMATTING_ATTRIBUTES;
    }
}
