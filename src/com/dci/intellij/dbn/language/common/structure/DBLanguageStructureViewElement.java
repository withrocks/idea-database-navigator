package com.dci.intellij.dbn.language.common.structure;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;

public abstract class DBLanguageStructureViewElement<T> implements StructureViewTreeElement {
    private PsiElement psiElement;

    public DBLanguageStructureViewElement(PsiElement psiElement) {
        this.psiElement = psiElement;
    }

    public Object getValue() {
        return psiElement;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    @NotNull
    public ItemPresentation getPresentation() {
        if (psiElement instanceof BasePsiElement) return (ItemPresentation) psiElement;
        return new ItemPresentation() {
            public String getPresentableText() {
                if (psiElement instanceof DBLanguagePsiFile) {
                    DBLanguagePsiFile file = (DBLanguagePsiFile) psiElement;
                    return file.getName();
                }
                return psiElement.getText();
            }

            @Nullable
            public String getLocationString() {
                return null;
            }

            @Nullable
            public Icon getIcon(boolean open) {
                return psiElement.isValid() ? psiElement.getIcon(open ? Iconable.ICON_FLAG_OPEN : Iconable.ICON_FLAG_CLOSED) : null;
            }

            @Nullable
            public TextAttributesKey getTextAttributesKey() {
                return null;
            }
        };
    }

    @NotNull
    public StructureViewTreeElement[] getChildren() {
        List<T> elements = getChildren(psiElement, null);
        return elements == null ?
                EMPTY_ARRAY :
                elements.toArray(new StructureViewTreeElement[elements.size()]);
    }

    private List<T> getChildren(PsiElement parent, List<T> elements) {
        PsiElement child = parent.getFirstChild();
        while (child != null) {
            elements = visitChild(child, elements);
            child = child.getNextSibling();
        }
        return elements;
    }

    protected List<T> visitChild(PsiElement child, List<T> elements) {
        if (child instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) child;
            if (basePsiElement.is(ElementTypeAttribute.STRUCTURE)) {
                if (elements == null) {
                    elements = new ArrayList<T>();
                }
                elements.add(createChildElement(child));
            } else {
                elements = getChildren(basePsiElement, elements);
            }
        }
        return elements;
    }

    protected abstract T createChildElement(PsiElement child);

    public void navigate(boolean requestFocus) {
        if (psiElement instanceof NavigationItem) {
            NavigationItem navigationItem = (NavigationItem) psiElement;
            navigationItem.navigate(requestFocus);
        }
    }

    public boolean canNavigate() {
        return true;
    }

    public boolean canNavigateToSource() {
        return true;
    }
}
