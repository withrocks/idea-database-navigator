package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.language.common.element.BlockElementType;
import com.intellij.lang.ASTNode;

public class BlockPsiElement extends SequencePsiElement {
    public BlockPsiElement(ASTNode astNode, BlockElementType elementType) {
        super(astNode, elementType);
    }


    public BlockElementType getElementType() {
        return (BlockElementType) super.getElementType();
    }
}
