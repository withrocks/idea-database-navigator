package com.dci.intellij.dbn.navigation.psi;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.language.common.psi.EmptySearchScope;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.vfs.DBObjectListVirtualFile;
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

public class DBObjectListPsiDirectory implements PsiDirectory, Disposable {
    private DBObjectListVirtualFile virtualFile;

    public DBObjectListPsiDirectory(DBObjectList objectList) {
        virtualFile = new DBObjectListVirtualFile(objectList);
    }

    public DBObjectList getObjectList() {
        return virtualFile == null ? null : virtualFile.getObjectList();
    }

    @NotNull
    public VirtualFile getVirtualFile() {
        return FailsafeUtil.nvl(virtualFile);
    }

    @Override
    public void dispose() {
        DisposerUtil.dispose(virtualFile);
        virtualFile = null;
    }

    /*********************************************************
     *                      PsiElement                       *
     *********************************************************/
    @NotNull
    public String getName() {
        return NamingUtil.capitalize(getObjectList().getName());
    }

    public ItemPresentation getPresentation() {
        return getObjectList().getPresentation();
    }

    public FileStatus getFileStatus() {
        return FileStatus.NOT_CHANGED;
    }

    @NotNull
    public Project getProject() throws PsiInvalidElementAccessException {
        DBObjectList objectList = getObjectList();
        Project project = objectList == null ? null : objectList.getProject();
        return FailsafeUtil.nvl(project);
    }

    @NotNull
    public Language getLanguage() {
        return Language.ANY;
    }

    public PsiDirectory getParent() {
        GenericDatabaseElement parent = getObjectList().getTreeParent();
        if (parent instanceof DBObject) {
            DBObject parentObject = (DBObject) parent;
            return NavigationPsiCache.getPsiDirectory(parentObject);
        }

        if (parent instanceof DBObjectBundle) {
            DBObjectBundle objectBundle = (DBObjectBundle) parent;
            return NavigationPsiCache.getPsiDirectory(objectBundle.getConnectionHandler());
        }

        return null;
    }

    public ASTNode getNode() {
        return null;
    }

    public void navigate(boolean requestFocus) {
        getObjectList().navigate(requestFocus);
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
        List<PsiElement> children = new ArrayList<PsiElement>();        
        for (Object obj : getObjectList().getObjects()) {
            DBObject object = (DBObject) obj;
            if (object instanceof DBSchemaObject) {
                children.add(NavigationPsiCache.getPsiFile(object));    
            } else {
                children.add(NavigationPsiCache.getPsiDirectory(object));                
            }
        }
        return children.toArray(new PsiElement[children.size()]);
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
        return null;
    }

    public <T> T getUserData(@NotNull Key<T> key) {
        return null;
    }

    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }

    /*********************************************************
     *                        PsiDirectory                   *
     *********************************************************/
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
