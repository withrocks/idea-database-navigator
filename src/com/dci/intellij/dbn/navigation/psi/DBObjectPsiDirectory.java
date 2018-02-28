package com.dci.intellij.dbn.navigation.psi;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.language.common.psi.EmptySearchScope;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DBObjectVirtualFile;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;

public class DBObjectPsiDirectory implements PsiDirectory, Disposable{
    private DBObjectRef objectRef;

    public DBObjectPsiDirectory(DBObjectRef objectRef) {
        this.objectRef = objectRef;
    }

    @Nullable
    public DBObject getObject() {
        return objectRef.get();
    }

    @Override
    public void dispose() {
    }

    /*********************************************************
     *                      PsiElement                       *
     *********************************************************/
    @NotNull
    public String getName() {
        return objectRef.getObjectName();
    }

    public ItemPresentation getPresentation() {
        return getObject();
    }

    public FileStatus getFileStatus() {
        return FileStatus.NOT_CHANGED;
    }

    @NotNull
    public Project getProject() throws PsiInvalidElementAccessException {
        DBObject object = getObject();
        Project project = object == null ? null : object.getProject();
        return FailsafeUtil.nvl(project);
    }

    @NotNull
    public Language getLanguage() {
        return Language.ANY;
    }

    public PsiDirectory getParent() {
        DBObject object = getObject();
        if (object != null) {
            GenericDatabaseElement parent = object.getTreeParent();
            if (parent instanceof DBObjectList) {
                DBObjectList objectList = (DBObjectList) parent;
                return NavigationPsiCache.getPsiDirectory(objectList);
            }
        }

        return null;
    }

    public ASTNode getNode() {
        return null;
    }

    public void navigate(boolean requestFocus) {
        DBObject object = getObject();
        if (object != null) {
            object.navigate(requestFocus);
        }

    }

    public boolean canNavigate() {
        return true;
    }

    public boolean canNavigateToSource() {
        return false;
    }

    public PsiManager getManager() {
        return PsiManager.getInstance(getProject());
    }

    @NotNull
    public PsiElement[] getChildren() {
        DBObject object = getObject();
        if (object != null) {
            List<PsiElement> children = new ArrayList<PsiElement>();
            DBObjectListContainer childObjects = object.getChildObjects();
            if (childObjects != null) {
                Collection<DBObjectList<DBObject>> objectLists = childObjects.getObjectLists();
                if (objectLists != null) {
                    for (DBObjectList objectList : objectLists) {
                        children.add(NavigationPsiCache.getPsiDirectory(objectList));
                    }
                    return children.toArray(new PsiElement[children.size()]);
                }
            }
        }
        return new PsiElement[0];
    }

    public PsiElement getFirstChild() {
        return null;
    }

    public PsiElement getLastChild() {
        return null;
    }

    public PsiElement getNextSibling() {
        return null;
    }

    public PsiElement getPrevSibling() {
        return null;
    }

    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return null;
    }

    public TextRange getTextRange() {
        return null;
    }

    public int getStartOffsetInParent() {
        return 0;
    }

    public int getTextLength() {
        return 0;
    }

    public PsiElement findElementAt(int offset) {
        return null;
    }

    public PsiReference findReferenceAt(int offset) {
        return null;
    }

    public int getTextOffset() {
        return 0;
    }

    public String getText() {
        return null;
    }

    @NotNull
    public char[] textToCharArray() {
        return new char[0];
    }

    public PsiElement getNavigationElement() {
        return this;
    }

    public PsiElement getOriginalElement() {
        return this;
    }

    public boolean textMatches(@NotNull CharSequence text) {
        return false;
    }

    public boolean textMatches(@NotNull PsiElement element) {
        return false;
    }

    public boolean textContains(char c) {
        return false;
    }

    public void accept(@NotNull PsiElementVisitor visitor) {

    }

    public void acceptChildren(@NotNull PsiElementVisitor visitor) {

    }

    public PsiElement copy() {
        return null;
    }

    public PsiElement add(@NotNull PsiElement element) throws IncorrectOperationException {
        throw new IncorrectOperationException("Operation not supported");
    }

    public PsiElement addBefore(@NotNull PsiElement element, PsiElement anchor) throws IncorrectOperationException {
        throw new IncorrectOperationException("Operation not supported");
    }

    public PsiElement addAfter(@NotNull PsiElement element, PsiElement anchor) throws IncorrectOperationException {
        throw new IncorrectOperationException("Operation not supported");
    }

    public void checkAdd(@NotNull PsiElement element) throws IncorrectOperationException {
        throw new IncorrectOperationException("Operation not supported");
    }

    public PsiElement addRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        throw new IncorrectOperationException("Operation not supported");
    }

    public PsiElement addRangeBefore(@NotNull PsiElement first, @NotNull PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        throw new IncorrectOperationException("Operation not supported");
    }

    public PsiElement addRangeAfter(PsiElement first, PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        throw new IncorrectOperationException("Operation not supported");
    }

    public void delete() throws IncorrectOperationException {

    }

    public void checkDelete() throws IncorrectOperationException {

    }

    public void deleteChildRange(PsiElement first, PsiElement last) throws IncorrectOperationException {

    }

    public PsiElement replace(@NotNull PsiElement newElement) throws IncorrectOperationException {
        return null;
    }

    public boolean isValid() {
        return true;
    }

    public boolean isWritable() {
        return false;
    }

    public PsiReference getReference() {
        return null;
    }

    @NotNull
    public PsiReference[] getReferences() {
        return new PsiReference[0];
    }

    public <T> T getCopyableUserData(Key<T> key) {
        return null;
    }

    public <T> void putCopyableUserData(Key<T> key, T value) {

    }

    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, @Nullable PsiElement lastParent, @NotNull PsiElement place) {
        return false;
    }

    public PsiElement getContext() {
        return null;
    }

    public boolean isPhysical() {
        return true;
    }

    @NotNull
    public GlobalSearchScope getResolveScope() {
        return EmptySearchScope.INSTANCE;
    }

    @NotNull
    public SearchScope getUseScope() {
        return EmptySearchScope.INSTANCE;
    }

    public boolean isEquivalentTo(PsiElement another) {
        return false;
    }

    public Icon getIcon(int flags) {
        DBObject object = getObject();
        return object == null ? null : object.getIcon();
    }

    public <T> T getUserData(@NotNull Key<T> key) {
        return null;
    }

    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }

    /*********************************************************
     *                        PsiDirectory                   *
     *********************************************************/
    @NotNull
    public VirtualFile getVirtualFile() {
        DBObject object = getObject();
        DBObjectVirtualFile virtualFile = object == null ? null : object.getVirtualFile();
        return FailsafeUtil.nvl(virtualFile);
    }

    public boolean processChildren(PsiElementProcessor<PsiFileSystemItem> processor) {
        return false;
    }

    @NotNull
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        throw new IncorrectOperationException("Operation not supported");
    }

    public boolean isDirectory() {
        return true;
    }

    public void checkSetName(String name) throws IncorrectOperationException {

    }

    @Override
    public PsiDirectory getParentDirectory() {
        return getParent();
    }

    @NotNull
    @Override
    public PsiDirectory[] getSubdirectories() {
        return new PsiDirectory[0];
    }

    @NotNull
    @Override
    public PsiFile[] getFiles() {
        return new PsiFile[0];
    }

    @Override
    public PsiDirectory findSubdirectory(@NotNull String s) {
        return null;
    }

    @Override
    public PsiFile findFile(@NotNull String s) {
        return null;
    }

    @NotNull
    @Override
    public PsiDirectory createSubdirectory(@NotNull String s) throws IncorrectOperationException {
        throw new IncorrectOperationException("Operation not supported");
    }

    @Override
    public void checkCreateSubdirectory(@NotNull String s) throws IncorrectOperationException {
        throw new IncorrectOperationException("Operation not supported");
    }

    @NotNull
    @Override
    public PsiFile createFile(@NotNull String s) throws IncorrectOperationException {
        throw new IncorrectOperationException("Operation not supported");
    }

    @NotNull
    @Override
    public PsiFile copyFileFrom(@NotNull String s, @NotNull PsiFile psiFile) throws IncorrectOperationException {
        throw new IncorrectOperationException("Operation not supported");
    }

    @Override
    public void checkCreateFile(@NotNull String s) throws IncorrectOperationException {
        throw new IncorrectOperationException("Operation not supported");
    }
}
