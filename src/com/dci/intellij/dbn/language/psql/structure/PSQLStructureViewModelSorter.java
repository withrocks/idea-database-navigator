package com.dci.intellij.dbn.language.psql.structure;

import javax.swing.Icon;
import java.util.Comparator;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.psi.PsiElement;

public class PSQLStructureViewModelSorter implements Sorter {

    public Comparator getComparator() {
        return COMPARATOR;    
    }

    public boolean isVisible() {
        return true;
    }

    @NotNull
    public ActionPresentation getPresentation() {
        return ACTION_PRESENTATION;
    }

    @NotNull
    public String getName() {
        return "Sort by Name";
    }

    private static final ActionPresentation ACTION_PRESENTATION = new ActionPresentation() {
        @NotNull
        public String getText() {
            return "Sort by Name";
        }

        public String getDescription() {
            return "Sort elements alphabetically by name";
        }

        public Icon getIcon() {
            return Icons.ACTION_SORT_ALPHA;
        }
    };

    private static final Comparator COMPARATOR = new Comparator() {
        public int compare(Object object1, Object object2) {

            if (object1 instanceof PSQLStructureViewElement && object2 instanceof PSQLStructureViewElement) {
                PSQLStructureViewElement structureViewElement1 = (PSQLStructureViewElement) object1;
                PSQLStructureViewElement structureViewElement2 = (PSQLStructureViewElement) object2;
                PsiElement psiElement1 = (PsiElement) structureViewElement1.getValue();
                PsiElement psiElement2 = (PsiElement) structureViewElement2.getValue();
                if (psiElement1 instanceof BasePsiElement && psiElement2 instanceof BasePsiElement) {
                    BasePsiElement namedPsiElement1 = (BasePsiElement) psiElement1;
                    BasePsiElement namedPsiElement2 = (BasePsiElement) psiElement2;
                    BasePsiElement subjectPsiElement1 = namedPsiElement1.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
                    BasePsiElement subjectPsiElement2 = namedPsiElement2.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
                    if (subjectPsiElement1 != null && subjectPsiElement2 != null) {
                        return subjectPsiElement1.getText().toUpperCase().compareTo(subjectPsiElement2.getText().toUpperCase());
                    }
                }
                return 0;
            } else {
                return object1 instanceof PSQLStructureViewElement ? 1 : -1;
            }
        }
    };
}
