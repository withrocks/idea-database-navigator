package com.dci.intellij.dbn.language.common.element.impl;

import java.util.Set;

import com.dci.intellij.dbn.common.ChainElement;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.Branch;
import com.dci.intellij.dbn.language.common.element.parser.BranchCheck;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;

public class ElementTypeRef extends ChainElement<ElementTypeRef> {
    private ElementType parentElementType;
    private ElementType elementType;
    private boolean optional;
    private double version;
    private Set<BranchCheck> branchChecks;

    public ElementTypeRef(ElementTypeRef previous, ElementType parentElementType, ElementType elementType, boolean optional, double version, Set<BranchCheck> branchChecks) {
        super(previous);
        this.parentElementType = parentElementType;
        this.elementType = elementType;
        this.optional = optional;
        this.version = version;
        this.branchChecks = branchChecks;
    }

    public boolean check(Set<Branch> branches, double currentVersion) {
        if (version > currentVersion) {
            return false;
        }

        if (branches != null && !branches.isEmpty() && branchChecks != null) {
            boolean finalCheckValue = true;
            for (Branch branch : branches) {
                for (BranchCheck branchCheck : branchChecks) {
                    boolean checkValue = branchCheck.check(branch, currentVersion);
                    if (branchCheck.getType() == BranchCheck.Type.FORBIDDEN) {
                        if (!checkValue) return false;
                    } else if (branchCheck.getType() == BranchCheck.Type.ALLOWED) {
                        finalCheckValue = finalCheckValue || checkValue;
                    }
                }
            }
            return finalCheckValue;
        }


/*
        if (branches != null) {
            Set<Branch> checkedBranches = getParentElementType().getCheckedBranches();
            if (checkedBranches != null) {
                if (supportedBranches != null) {
                    for (Branch branch : branches) {
                        if (checkedBranches.contains(branch)) {
                            for (Branch supportedBranch : supportedBranches) {
                                if (supportedBranch.equals(branch) && currentVersion >= supportedBranch.getVersion()) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        }
*/

        return true;
    }

    public ElementType getParentElementType() {
        return parentElementType;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isOptionalToHere() {
        if (getIndex() == 0) return false;

        ElementTypeRef previous = getPrevious();
        while (previous != null) {
            if (!previous.optional) {
                return false;
            }
            previous = previous.getPrevious();
        }
        return true;
    }

    public boolean isOptionalFromHere() {
        ElementTypeRef next = getNext();
        while (next != null) {
            if (!next.optional) {
                return false;
            }
            next = next.getNext();
        }
        return true;
    }

    public double getVersion() {
        return version;
    }

    public ElementTypeLookupCache getLookupCache() {
        return elementType.getLookupCache();
    }

    public ElementTypeParser getParser() {
        return elementType.getParser();
    }

    @Override
    public String toString() {
        return elementType.toString();
    }
}
