package com.dci.intellij.dbn.language.common.element.path;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.tree.IElementType;

public class ASTPathNode implements PathNode{
    private ASTNode astNode;

    public ASTPathNode(ASTNode astNode) {
        this.astNode = astNode;
    }

    public PathNode getParent() {
        ASTNode treeParent = astNode.getTreeParent();
        if (treeParent != null && !(treeParent instanceof FileElement)) {
            return new ASTPathNode(treeParent);
        }
        return null;
    }

    public int getIndexInParent() {
        ASTNode parentAstNode = astNode.getTreeParent();
        if (parentAstNode.getElementType() instanceof SequenceElementType) {
            SequenceElementType sequenceElementType = (SequenceElementType) parentAstNode.getElementType();
            int index = 0;
            ASTNode child = parentAstNode.getFirstChildNode();
            while (child != null) {
                if (astNode == child) {
                    break;
                }
                index++;
                child = child.getTreeNext();
                if (child instanceof PsiWhiteSpace){
                    child = child.getTreeNext();
                }
            }
            IElementType elementType = astNode.getElementType();
            if (elementType instanceof ElementType) {
                return sequenceElementType.indexOf((ElementType) elementType, index);
            }

        }
        return 0;
    }

    @Nullable
    public ElementType getElementType() {
        IElementType elementType = astNode.getElementType();

        return elementType instanceof ElementType ? (ElementType) elementType : null;
    }

    public PathNode getRootPathNode() {
        return null;
    }

    public boolean isRecursive() {
        return false; 
    }

    public boolean isRecursive(ElementType elementType) {
        return false;
    }

    @Override
    public void detach() {

    }
}
