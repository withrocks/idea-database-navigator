package com.dci.intellij.dbn.language.common.psi;

import java.util.Iterator;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.thread.ConditionalReadActionRunner;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.lookup.IdentifierLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectLookupAdapter;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageDialect;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiWhiteSpace;
import gnu.trove.THashSet;

public class PsiUtil {

    public static DBSchema getCurrentSchema(PsiElement psiElement) {
        DBSchema currentSchema = null;
        if (psiElement instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) psiElement;
            currentSchema = basePsiElement.getCurrentSchema();
        }
        if (currentSchema == null) {
            VirtualFile virtualFile = getVirtualFileForElement(psiElement);
            if (virtualFile != null) {
                FileConnectionMappingManager mappingManager = FileConnectionMappingManager.getInstance(psiElement.getProject());
                currentSchema = mappingManager.getCurrentSchema(virtualFile);
            }
        }
        return currentSchema;
    }

    @Nullable
    public static VirtualFile getVirtualFileForElement(PsiElement psiElement) {
        PsiFile psiFile = null;
        try {
            psiFile = psiElement.getContainingFile().getOriginalFile();
            if (psiFile == null) psiFile = psiElement.getContainingFile();
        } catch (PsiInvalidElementAccessException e) {
            System.out.println("");
        }
        return psiFile == null ? null : psiFile.getVirtualFile();
    }

    public static BasePsiElement resolveAliasedEntityElement(IdentifierPsiElement aliasElement) {
        PsiElement psiElement = aliasElement.isReference() ? aliasElement.resolve() : aliasElement; 
        if (psiElement instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) psiElement;
            BasePsiElement scope = basePsiElement.findEnclosingNamedPsiElement();

            DBObjectType objectType = aliasElement.getObjectType();
            IdentifierLookupAdapter lookupInput = new IdentifierLookupAdapter(aliasElement, null, null, objectType, null);

            BasePsiElement objectPsiElement = lookupInput.findInScope(scope);
            if (objectPsiElement == null) {
                scope = scope.findEnclosingSequencePsiElement();
                if (scope != null)
                    objectPsiElement = lookupInput.findInScope(scope);
            }
            if (objectPsiElement != null) {
                Set<BasePsiElement> virtualObjectPsiElements = new THashSet<BasePsiElement>();
                scope.collectVirtualObjectPsiElements(virtualObjectPsiElements, objectType);
                for (BasePsiElement virtualObjectPsiElement : virtualObjectPsiElements) {
                    if (virtualObjectPsiElement.containsPsiElement(objectPsiElement))
                        return virtualObjectPsiElement;

                }
            }
            return objectPsiElement;

        }
        return null;
    }

    public static IdentifierPsiElement lookupObjectPriorTo(BasePsiElement element, DBObjectType objectType) {
        SequencePsiElement scope = element.findEnclosingSequencePsiElement();

        Iterator<PsiElement> children = PsiUtil.getChildrenIterator(scope);
        while (children.hasNext()) {
            PsiElement child = children.next();
            if (child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                ObjectLookupAdapter lookupInput = new ObjectLookupAdapter(null, objectType);
                BasePsiElement objectPsiElement = lookupInput.findInScope(basePsiElement);
                if (objectPsiElement != null && objectPsiElement instanceof IdentifierPsiElement) {
                    return (IdentifierPsiElement) objectPsiElement;
                }                                        
            }
            if (child == element) break;
        }
        return null;
    }

    @Nullable
    public static ExecutablePsiElement lookupExecutableAtCaret(Editor editor, boolean lenient) {
        // GTK: PsiElement psiElement = PsiFile.findElementA(offset)

        int offset = editor.getCaretModel().getOffset();

        PsiFile file = DocumentUtil.getFile(editor);
        PsiElement current = null;

        if (lenient) {
            int lineStart = editor.getCaretModel().getVisualLineStart();
            int lineEnd = editor.getCaretModel().getVisualLineEnd();
            current = file.findElementAt(lineStart);
            while (ignore(current)) {
                offset = current.getTextOffset() + current.getTextLength();
                if (offset >= lineEnd) break;
                current = file.findElementAt(offset);
            }
        } else {
            current = file.findElementAt(offset);
        }

        if (current != null) {
            PsiElement parent = current.getParent();
            while (parent != null) {
                if (parent instanceof ExecutablePsiElement){
                    ExecutablePsiElement executable = (ExecutablePsiElement) parent;
                    if (!executable.isNestedExecutable()) {
                        return executable;
                    }

                }
                parent = parent.getParent();
            }
        }
        return null;
    }

    public static BasePsiElement lookupElementAtOffset(PsiFile file, ElementTypeAttribute typeAttribute, int offset) {
        PsiElement psiElement = file.findElementAt(offset);
        while (psiElement != null) {
            if (psiElement instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) psiElement;
                if (basePsiElement.getElementType().is(typeAttribute)) {
                    return basePsiElement;
                }
            }
            psiElement = psiElement.getParent();
        }
        return null;
    }

    @Nullable
    public static LeafPsiElement lookupLeafBeforeOffset(PsiFile file, int originalOffset) {
        int offset = originalOffset;
        if (offset > 0 && offset == file.getTextLength()) {
            offset--;
        }
        PsiElement element = file.findElementAt(offset);
        while (element != null && offset >= 0) {
            int elementEndOffset = element.getTextOffset() + element.getTextLength();
            PsiElement parent = element.getParent();
            if (elementEndOffset <= originalOffset && parent instanceof LeafPsiElement) {
                LeafPsiElement leafPsiElement = (LeafPsiElement) parent;
                if (leafPsiElement instanceof IdentifierPsiElement) {
                    if (elementEndOffset < originalOffset) {
                        return leafPsiElement;
                    }
                } else {
                    return (LeafPsiElement) parent;
                }
            }
            offset = element.getTextOffset() - 1;
            element = file.findElementAt(offset);
        }
        return null;
    }

    private static boolean ignore(PsiElement element) {
        return element instanceof PsiWhiteSpace || element instanceof PsiComment;
    }


    public static LeafPsiElement lookupLeafAtOffset(PsiFile file, int originalOffset) {
        int offset = originalOffset;
        PsiElement element = file.findElementAt(offset);
        while (element != null && offset >= 0) {
            int elementEndOffset = element.getTextOffset() + element.getTextLength();
            if (element.getParent() instanceof LeafPsiElement) {
                LeafPsiElement leafPsiElement = (LeafPsiElement) element.getParent();
                if (leafPsiElement instanceof IdentifierPsiElement) {
                    if (elementEndOffset < originalOffset) {
                        return leafPsiElement;
                    }
                } else {
                    return (LeafPsiElement) element.getParent();
                }
            }
            offset = element.getTextOffset() - 1;
            element = file.findElementAt(offset);
        }
        return null;
    }

    public static void moveCaretOutsideExecutable(Editor editor) {
        ExecutablePsiElement executablePsiElement = lookupExecutableAtCaret(editor, false);
        if (executablePsiElement != null) {
            int offset = executablePsiElement.getTextOffset();
            editor.getCaretModel().moveToOffset(offset);
        }
    }

    public static Iterator<PsiElement> getChildrenIterator(final PsiElement element) {
        return new Iterator<PsiElement>() {
            private PsiElement current = element.getFirstChild();
            public boolean hasNext() {
                return current != null;
            }

            public PsiElement next() {
                PsiElement oldCurrent = current;
                current = current.getNextSibling();
                return oldCurrent;
            }

            public void remove() {

            }
        };
    }

    public static int getChildrenCount(PsiElement element) {
        int count = 0;
        PsiElement current = element.getFirstChild();
        while (current != null) {
            count ++ ;
            current = current.getNextSibling();
        }
        return count;
    }

    public static PsiElement getNextSibling(PsiElement psiElement) {
        PsiElement nextPsiElement = psiElement.getNextSibling();
        while (ignore(nextPsiElement)) {
            nextPsiElement = nextPsiElement.getNextSibling();
        }
        return nextPsiElement;
    }

    public static PsiFile getPsiFile(Project project, Document document) {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        return psiDocumentManager == null ? null : psiDocumentManager.getPsiFile(document);
    }

    public static PsiFile getPsiFile(final Project project, final VirtualFile virtualFile) {
        return new ConditionalReadActionRunner<PsiFile>() {
            @Override
            protected PsiFile run() {
                return PsiManager.getInstance(project).findFile(virtualFile);
            }
        }.start();
    }


    public static BasePsiElement getBasePsiElement(PsiElement psiElement) {
        while (psiElement != null) {
            if (psiElement instanceof BasePsiElement) {
                return (BasePsiElement) psiElement;
            }
            psiElement = psiElement.getParent();
        }

        return null;
    }

    @Nullable
    public static ElementType getElementType(PsiElement psiElement) {
        if (psiElement instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) psiElement;
            return basePsiElement.getElementType();
        }
        return null;
    }

    public static Language getLanguage(PsiElement element) {
        Language language = element.getLanguage();
        if (language instanceof LanguageDialect) {
            LanguageDialect languageDialect = (LanguageDialect) language;
            language = languageDialect.getBaseLanguage();
        }
        return language;
    }
}
