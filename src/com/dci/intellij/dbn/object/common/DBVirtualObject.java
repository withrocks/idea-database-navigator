package com.dci.intellij.dbn.object.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.IdentifierCategory;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.LeafPsiElement;
import com.dci.intellij.dbn.language.common.psi.QualifiedIdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.TokenPsiElement;
import com.dci.intellij.dbn.language.common.psi.lookup.LookupAdapterCache;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectReferenceLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.SimpleObjectLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.VirtualObjectLookupAdapter;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DBContentVirtualFile;
import com.intellij.ide.util.EditSourceUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;

public class DBVirtualObject extends DBObjectImpl implements PsiReference {
    public static final PsiLookupAdapter CHR_STAR_LOOKUP_ADAPTER = new PsiLookupAdapter() {
        @Override
        public boolean matches(BasePsiElement element) {
            if (element instanceof TokenPsiElement) {
                TokenPsiElement tokenPsiElement = (TokenPsiElement) element;
                return tokenPsiElement.getElementType().getTokenType() == tokenPsiElement.getElementType().getLanguage().getSharedTokenTypes().getChrStar();
            }
            return false;
        }

        @Override
        public boolean accepts(BasePsiElement element) {
            return true;
        }
    };
    public static final ObjectReferenceLookupAdapter DATASET_LOOKUP_ADAPTER = new ObjectReferenceLookupAdapter(null, DBObjectType.DATASET, null);

    private DBObjectType objectType;
    private BasePsiElement underlyingPsiElement;
    private BasePsiElement relevantPsiElement;

    public DBVirtualObject(DBObjectType objectType, BasePsiElement psiElement) {
        super(psiElement.getActiveConnection() == null ? null :
                psiElement.getActiveConnection().getObjectBundle(), psiElement.getText());

        underlyingPsiElement = psiElement;
        relevantPsiElement = psiElement;
        this.objectType = objectType;

        if (objectType == DBObjectType.COLUMN) {
            PsiLookupAdapter lookupAdapter = LookupAdapterCache.ALIAS_DEFINITION.get(objectType);
            BasePsiElement relevantPsiElement = lookupAdapter.findInElement(psiElement);

            if (relevantPsiElement == null) {
                lookupAdapter = new SimpleObjectLookupAdapter(null, objectType);
                relevantPsiElement = lookupAdapter.findInElement(psiElement);
            }

            if (relevantPsiElement != null) {
                this.relevantPsiElement = relevantPsiElement;
                this.name = relevantPsiElement.getText();
            }
        } else if (objectType == DBObjectType.TYPE || objectType == DBObjectType.TYPE_ATTRIBUTE || objectType == DBObjectType.CURSOR) {
            BasePsiElement relevantPsiElement = psiElement.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
            if (relevantPsiElement != null) {
                this.relevantPsiElement = relevantPsiElement;
                this.name = relevantPsiElement.getText();
            }
        } else if (objectType == DBObjectType.DATASET) {
            ObjectLookupAdapter lookupAdapter = new ObjectLookupAdapter(null, IdentifierCategory.REFERENCE, DBObjectType.DATASET);
            Set<BasePsiElement> basePsiElements = lookupAdapter.collectInElement(psiElement, null);
            List<String> tableNames = new ArrayList<String>();
            if (basePsiElements != null) {
                for (BasePsiElement basePsiElement : basePsiElements) {
                    if (basePsiElement instanceof IdentifierPsiElement) {
                        IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) basePsiElement;
                        String tableName = identifierPsiElement.getText().toUpperCase();
                        if (!tableNames.contains(tableName)) {
                            tableNames.add(tableName);
                        }
                    }
                }
            }
            Collections.sort(tableNames);

            StringBuilder name = new StringBuilder();
            for (CharSequence tableName : tableNames) {
                if (name.length() > 0) name.append(", ");
                name.append(tableName);
            }

            this.name = "subquery " + name;
        }
        objectRef = new DBObjectRef(this);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
    }

    public boolean isValid() {
        if (underlyingPsiElement.isValid()) {
            if (objectType == DBObjectType.DATASET) {
                return true;
            }
            if (name.equalsIgnoreCase(relevantPsiElement.getText())) {
                if (relevantPsiElement instanceof IdentifierPsiElement) {
                    IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) relevantPsiElement;
                    if (identifierPsiElement.getObjectType() != objectType) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @NotNull
    public List<DBObject> getChildObjects(DBObjectType objectType) {
        return getChildObjectList(objectType).getObjects();
    }

    public DBObject getChildObject(DBObjectType objectType, String name, int overload, boolean lookupHidden) {
        return getChildObjectList(objectType).getObject(name, overload);
    }

    public synchronized DBObjectList<DBObject> getChildObjectList(DBObjectType objectType) {
        DBObjectListContainer childObjects = initChildObjects();
        DBObjectList<DBObject> objectList = childObjects.getObjectList(objectType);
        if (objectList != null) {
            for (DBObject object : objectList.getObjects()) {
                if (!object.isValid()) {
                    objectList = null;
                    break;
                }
            }
        }

        if (objectList == null) {
            objectList = childObjects.createObjectList(objectType, this, DynamicContentLoader.VOID_CONTENT_LOADER, false, false);
        }

        if (objectList.size() == 0) {
            VirtualObjectLookupAdapter lookupAdapter = new VirtualObjectLookupAdapter(this.objectType, objectType);
            Set<BasePsiElement> children = underlyingPsiElement.collectPsiElements(lookupAdapter, null, 100);
            if (children != null) {
                for (BasePsiElement child : children) {

                    // handle STAR column
                    if (objectType == DBObjectType.COLUMN) {
                        LeafPsiElement starPsiElement = (LeafPsiElement) CHR_STAR_LOOKUP_ADAPTER.findInElement(child);
                        if (starPsiElement != null) {
                            if (starPsiElement.getParent() instanceof QualifiedIdentifierPsiElement) {
                                QualifiedIdentifierPsiElement qualifiedIdentifierPsiElement = (QualifiedIdentifierPsiElement) starPsiElement.getParent();
                                int index = qualifiedIdentifierPsiElement.getIndexOf(starPsiElement);
                                if (index > 0) {
                                    IdentifierPsiElement parentPsiElement = qualifiedIdentifierPsiElement.getLeafAtIndex(index - 1);
                                    DBObject object = parentPsiElement.resolveUnderlyingObject();
                                    if (object != null && object.getObjectType().matches(DBObjectType.DATASET)) {
                                        List<DBObject> columns = object.getChildObjects(DBObjectType.COLUMN);
                                        for (DBObject column : columns) {
                                            objectList.addObject(column);
                                        }
                                    }
                                }
                            } else {
                                Set<BasePsiElement> basePsiElements = DATASET_LOOKUP_ADAPTER.collectInElement(underlyingPsiElement, null);
                                if (basePsiElements != null) {
                                    for (BasePsiElement basePsiElement : basePsiElements) {
                                        DBObject object = basePsiElement.resolveUnderlyingObject();
                                        if (object != null && object != this && object.getObjectType().matches(DBObjectType.DATASET)) {
                                            List<DBObject> columns = object.getChildObjects(DBObjectType.COLUMN);
                                            for (DBObject column : columns) {
                                                objectList.addObject(column);
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }

                    DBObject object = child.resolveUnderlyingObject();
                    if (object != null && object.getObjectType().isChildOf(this.objectType) && !objectList.getElements().contains(object)) {
                        if (object instanceof DBVirtualObject) {
                            DBVirtualObject virtualObject = (DBVirtualObject) object;
                            virtualObject.setParentObject(this);
                        }
                        objectList.addObject(object);
                    }

                }
            }
        }
        return objectList;
    }

    public String getQualifiedNameWithType() {
        return getName();
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        DBLanguagePsiFile file = underlyingPsiElement.getFile();
        return file == null ? null : file.getActiveConnection();
    }

    @Override
    public DBObject getParentObject() {
        return DBObjectRef.get(parentObject);
    }

    public void setParentObject(DBVirtualObject virtualObject) {
        parentObject = DBObjectRef.from(virtualObject);
    }

    @NotNull
    public Project getProject() {
        if (underlyingPsiElement.isValid()) {
            return underlyingPsiElement.getProject();
        } else{
            throw new ProcessCanceledException();
        }
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
    }

    public void navigate(boolean requestFocus) {
        PsiFile containingFile = getContainingFile();
        if (containingFile != null) {
            VirtualFile virtualFile = containingFile.getVirtualFile();
            if(virtualFile instanceof DBContentVirtualFile) {
                Document document = DocumentUtil.getDocument(containingFile);
                Editor[] editors =  EditorFactory.getInstance().getEditors(document);
                OpenFileDescriptor descriptor = (OpenFileDescriptor) EditSourceUtil.getDescriptor(relevantPsiElement);
                if (descriptor != null) descriptor.navigateIn(editors[0]);

            } else{
                relevantPsiElement.navigate(requestFocus);
            }
        }
    }
    
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return relevantPsiElement.isValid() ? relevantPsiElement.getContainingFile() : null;
    }

    /*********************************************************
     *                       PsiReference                    *
     *********************************************************/
    public PsiElement getElement() {
        return null;
    }

    public TextRange getRangeInElement() {
        return new TextRange(0, getTextLength());
    }

    public PsiElement resolve() {
        return underlyingPsiElement;
    }

    public BasePsiElement getUnderlyingPsiElement() {
        return underlyingPsiElement;
    }

    public BasePsiElement getRelevantPsiElement() {
        return relevantPsiElement;
    }

    @NotNull
    public String getCanonicalText() {
        return null;
    }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        return null;
    }

    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        return null;
    }

    public boolean isReferenceTo(PsiElement element) {
        return underlyingPsiElement == element;
    }

    @NotNull
    public Object[] getVariants() {
        return new Object[0];
    }

    public boolean isSoft() {
        return false;
    }

}
