package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.NamedElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.parser.Branch;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import gnu.trove.THashSet;

public class ElementLookupContext {
    public static double MAX_DB_VERSION = 9999;
    private Set<NamedElementType> scannedElements = new THashSet<NamedElementType>();
    protected Set<Branch> branches;
    private Map<Branch, NamedElementType> branchMarkers = new HashMap<Branch, NamedElementType>();

    protected double databaseVersion = MAX_DB_VERSION;

    @Deprecated
    public ElementLookupContext() {}

    public ElementLookupContext(double version) {
        this.databaseVersion = version;
    }

    public ElementLookupContext(Set<Branch> branches, double version) {
        this.branches = branches;
        this.databaseVersion = version;
    }

    public boolean check(ElementTypeRef elementTypeRef) {
        return elementTypeRef.check(branches, databaseVersion);
    }

    public void addBranchMarker(ASTNode astNode, @NotNull Branch branch) {
        NamedElementType namedElementType = getNamedElement(astNode);
        if (namedElementType != null) {
            branchMarkers.put(branch, namedElementType);
            this.branches = branchMarkers.keySet();
        }
    }

    public void addBranchMarker(PathNode pathNode, @NotNull Branch branch) {
        NamedElementType namedElementType = getNamedElement(pathNode);
        if (namedElementType != null) {
            branchMarkers.put(branch, namedElementType);
            this.branches = branchMarkers.keySet();
        }
    }

    @Nullable
    private static NamedElementType getNamedElement(@NotNull ASTNode astNode) {
        astNode = astNode.getTreeParent();
        while (astNode != null) {
            IElementType elementType = astNode.getElementType();
            if (elementType instanceof NamedElementType) {
                return (NamedElementType) elementType;
            }
            astNode = astNode.getTreeParent();
        }
        return null;
    }

    @Nullable
    private static NamedElementType getNamedElement(@NotNull PathNode pathNode) {
        pathNode = pathNode.getParent();
        while (pathNode != null) {
            ElementType elementType = pathNode.getElementType();
            if (elementType instanceof NamedElementType) {
                return (NamedElementType) elementType;
            }
            pathNode = pathNode.getParent();
        }
        return null;
    }

    public void removeBranchMarkers(@NotNull PathNode pathNode) {
        ElementType elementType = pathNode.getElementType();
        if (elementType instanceof NamedElementType) {
            removeBranchMarkers((NamedElementType) elementType);
        }
    }

    public void removeBranchMarkers(NamedElementType elementType) {
        if (branchMarkers.size() > 0 && branchMarkers.containsValue(elementType)) {
            Iterator<Branch> iterator = branchMarkers.keySet().iterator();
            while (iterator.hasNext()) {
                Branch key = iterator.next();
                if (branchMarkers.get(key) == elementType) {
                    iterator.remove();
                }
            }
        }
        branches = branchMarkers.size() == 0 ? null : branchMarkers.keySet();
    }


    public ElementLookupContext reset() {
        scannedElements.clear();
        return this;
    }

    public boolean isScanned(NamedElementType elementType) {
        return scannedElements.contains(elementType);
    }

    public void markScanned(NamedElementType elementType) {
        scannedElements.add(elementType);
    }
}
