package com.dci.intellij.dbn.code.common.style.formatting;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCustomSettings;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleFormattingOption;
import com.dci.intellij.dbn.code.common.style.presets.CodeStyleDefaultPresets;
import com.dci.intellij.dbn.code.common.style.presets.CodeStylePreset;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.SimpleTokenType;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.WrapperElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.ChameleonPsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.NamedPsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.common.psi.TokenPsiElement;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.impl.source.tree.TreeElement;

public class FormattingBlock implements Block {
    private PsiElement psiElement;
    private CodeStyleSettings codeStyleSettings;
    private CodeStyleCustomSettings codeStyleCustomSettings;
    private static final List<Block> EMPTY_LIST = new ArrayList<Block>(0);
    private List<Block> childBlocks;
    private FormattingBlock parentBlock;
    private int index;

    public FormattingBlock(
            CodeStyleSettings codeStyleSettings,
            CodeStyleCustomSettings codeStyleCustomSettings,
            PsiElement psiElement,
            FormattingBlock parentBlock,
            int index) {
        this.psiElement = psiElement;
        this.parentBlock = parentBlock;
        this.index = index;
        this.codeStyleSettings = codeStyleSettings;
        this.codeStyleCustomSettings = codeStyleCustomSettings;
    }

    private FormattingAttributes getFormattingAttributes() {
        if (psiElement instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) psiElement;
            return basePsiElement.getFormattingAttributes();
        }
        return null;
    }


    private Indent getIndentAttribute() {
        return (Indent) getAttribute(FormattingAttributes.Type.INDENT);
    }

    private Wrap getWrapAttribute() {
        return (Wrap) getAttribute(FormattingAttributes.Type.WRAP);
    }

    private Spacing getSpacingBeforeAttribute() {
        return (Spacing) getAttribute(FormattingAttributes.Type.SPACING_BEFORE);
    }

    private Spacing getSpacingAfterAttribute() {
        return (Spacing) getAttribute(FormattingAttributes.Type.SPACING_AFTER);
    }


    private Object getAttribute(FormattingAttributes.Type type) {
        if (psiElement instanceof FormattingProviderPsiElement) {
            FormattingProviderPsiElement psiElement = (FormattingProviderPsiElement) this.psiElement;
            FormattingAttributes attributes = psiElement.getFormattingAttributes();
            Object attribute = FormattingAttributes.getAttribute(attributes, type);
            if (attribute != null) {
                return attribute;
            }

            if (type == FormattingAttributes.Type.SPACING_BEFORE || type == FormattingAttributes.Type.SPACING_AFTER) {
                PsiElement parent = psiElement.getParent();
                PsiElement child = type.isLeft() ? parent.getFirstChild() : parent.getLastChild();
                if (child != psiElement) {
                    attributes = psiElement.getFormattingAttributesRecursive(type.isLeft());
                    attribute = FormattingAttributes.getAttribute(attributes, type);
                    if (attribute != null) {
                        return attribute;
                    }
                }
            }
        }
        return null;
    }


    @Nullable
    public Indent getIndent() {
        if (psiElement instanceof PsiComment) {
            return null;
        }

        if (psiElement.getParent() instanceof DBLanguagePsiFile) {
            return Indent.getAbsoluteNoneIndent();
        }

        Indent indent = getIndentAttribute();
        if (indent == null) {
            if (psiElement instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) psiElement;
                BasePsiElement parentPsiElement = getParentPsiElement(basePsiElement);
                if (parentPsiElement != null && parentPsiElement.getElementType() instanceof WrapperElementType) {
                    WrapperElementType wrapperElementType = (WrapperElementType) parentPsiElement.getElementType();
                    SharedTokenTypeBundle sharedTokenTypes = parentPsiElement.getLanguage().getSharedTokenTypes();
                    if (wrapperElementType.getBeginTokenElement().getTokenType() == sharedTokenTypes.getChrLeftParenthesis()) {
                        //FormattingBlock parentStatementBlock = getParentBlock(this, ElementTypeAttribute.STATEMENT);
                        //Indent parentStatementIndent = parentStatementBlock.getIndent();
                        //return Indent.getIndent(Indent.Type.SPACES, -1, false, false);
                        return Indent.getNoneIndent();
                    }
                }
            }

            return Indent.getNoneIndent();
        } else {
            return indent;
        }
    }

    @Nullable
    public Wrap getWrap() {
        if (psiElement instanceof PsiComment) {
            return null;
        }

        if (psiElement instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) psiElement;

            Wrap wrap = getWrapAttribute();
            if (wrap != null) {
                return wrap;
            }

/*            FormattingAttributes attributes = getFormattingAttributes();
            if (attributes != null && attributes.getWrap() != null) {
                Wrap wrap = attributes.getWrap();

                if (wrap == CodeStylePreset.WRAP_IF_LONG && basePsiElement.lookupEnclosingNamedPsiElement().approximateLength() > codeStyleSettings.RIGHT_MARGIN) {
                    wrap = CodeStylePreset.WRAP_ALWAYS;
                }
                return wrap;
            }*/

            for (CodeStyleFormattingOption option : codeStyleCustomSettings.getFormattingSettings().getOptions()) {
                CodeStylePreset preset = option.getPreset();
                if (preset.accepts(basePsiElement)) {
                    return preset.getWrap(basePsiElement, codeStyleSettings);
                }
            }

            for (CodeStylePreset preset : CodeStyleDefaultPresets.PRESETS) {
                if (preset.accepts(basePsiElement)) {
                    return preset.getWrap(basePsiElement, codeStyleSettings);
                }
            }
        }


        return CodeStylePreset.WRAP_NONE;
    }

    @Nullable
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        FormattingBlock leftBlock = (FormattingBlock) child1;
        FormattingBlock rightBlock = (FormattingBlock) child2;

        if (leftBlock == null) {
            return null;
        }

        PsiElement leftPsiElement = leftBlock.psiElement;
        PsiElement rightPsiElement = rightBlock.psiElement;

        if (leftPsiElement instanceof PsiComment || rightPsiElement instanceof PsiComment) {
            return null;
        }

        DBLanguage language = getLanguage();
        if (language != null) {

            // DOT, COMMA spacing
            SharedTokenTypeBundle sharedTokenTypes = language.getSharedTokenTypes();
            SimpleTokenType chrDot = sharedTokenTypes.getChrDot();
            SimpleTokenType chrComma = sharedTokenTypes.getChrComma();
            SimpleTokenType leftParenthesis = sharedTokenTypes.getChrLeftParenthesis();
            SimpleTokenType rightParenthesis = sharedTokenTypes.getChrRightParenthesis();

            if (is(leftPsiElement, chrDot)) return SpacingDefinition.NO_SPACE.getValue();
            if (is(rightPsiElement, chrDot)) return SpacingDefinition.NO_SPACE.getValue();

            if (is(leftPsiElement, chrComma)) return SpacingDefinition.MIN_ONE_SPACE.getValue();
            if (is(rightPsiElement, chrComma)) return SpacingDefinition.NO_SPACE.getValue();

            if (is(leftPsiElement, leftParenthesis)) return SpacingDefinition.NO_SPACE.getValue();
            if (is(rightPsiElement, rightParenthesis)) return SpacingDefinition.NO_SPACE.getValue();

        }

        Spacing spacingAfter = leftBlock.getSpacingAfterAttribute();
        if (spacingAfter != null) {
            return spacingAfter;
        }

        Spacing spacingBefore = rightBlock.getSpacingBeforeAttribute();
        if (spacingBefore != null) {
            return spacingBefore;
        }


        if (rightPsiElement instanceof BasePsiElement) {
            BasePsiElement rightBasePsiElement = (BasePsiElement) rightPsiElement;

            if (rightBasePsiElement instanceof TokenPsiElement || rightBasePsiElement instanceof NamedPsiElement) {
                return null;
            }


            List<CodeStyleFormattingOption> formattingOptions = codeStyleCustomSettings.getFormattingSettings().getOptions();
            for (CodeStyleFormattingOption formattingOption : formattingOptions) {
                CodeStylePreset preset = formattingOption.getPreset();
                if (preset.accepts(rightBasePsiElement)) {
                    return preset.getSpacing(rightBasePsiElement, codeStyleSettings);
                }
            }

            for (CodeStylePreset preset : CodeStyleDefaultPresets.PRESETS) {
                if (preset.accepts(rightBasePsiElement)) {
                    return preset.getSpacing(rightBasePsiElement, codeStyleSettings);
                }
            }
        }
        return SpacingDefinition.ONE_SPACE.getValue();
    }

    private static boolean is(PsiElement psiElement, TokenType tokenType) {
        if (psiElement instanceof TreeElement) {
            TreeElement treeElement = (TreeElement) psiElement;
            return treeElement.getElementType() == tokenType;
        }
        return false;
    }

    @Nullable
    private DBLanguage getLanguage() {
        if (psiElement instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile databasePsiFile = (DBLanguagePsiFile) psiElement;
            return databasePsiFile.getDBLanguage();
        }

        if (psiElement instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) psiElement;
            return basePsiElement.getLanguage();
        }
        return null;
    }

    private static BasePsiElement getParentPsiElement(PsiElement psiElement) {
        PsiElement parentPsiElement = psiElement.getParent();
        if (parentPsiElement instanceof BasePsiElement) {
            return (BasePsiElement) parentPsiElement;
        }
        return null;
    }

    private static FormattingBlock getParentBlock(FormattingBlock block, ElementTypeAttribute typeAttribute) {
        if (block.parentBlock != null) {
            PsiElement psiElement = block.parentBlock.psiElement;
            if (psiElement instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) psiElement;
                if (basePsiElement.getElementType().is(typeAttribute)) {
                    return block.parentBlock;
                }
                return getParentBlock(block.parentBlock, typeAttribute);
            }
        }
        return null;
    }


    private static ElementType getParentElementType(PsiElement psiElement) {
        BasePsiElement parentPsiElement = getParentPsiElement(psiElement);
        if (parentPsiElement != null) {
            return parentPsiElement.getElementType();
        }
        return null;
    }

    @NotNull
    public TextRange getTextRange() {
        return psiElement.getTextRange();
    }

    @NotNull
    public synchronized List<Block> getSubBlocks() {
        if (childBlocks == null) {
            PsiElement child = psiElement.getFirstChild();
            while (child != null) {
                if (!(child instanceof PsiWhiteSpace) /*&& !(child instanceof PsiErrorElement)*/ && child.getTextLength() > 0) {
                    if (childBlocks == null) childBlocks = new ArrayList<Block>();
                    CodeStyleCustomSettings codeStyleCustomSettings = getCodeStyleSettings(child);
                    FormattingBlock childBlock = new FormattingBlock(codeStyleSettings, codeStyleCustomSettings, child, this, index);
                    childBlocks.add(childBlock);
                }
                child = child.getNextSibling();
            }

            if (childBlocks == null) childBlocks = EMPTY_LIST;
        }
        return childBlocks;
    }

    private CodeStyleCustomSettings getCodeStyleSettings(PsiElement child) {
        CodeStyleCustomSettings codeStyleCustomSettings = this.codeStyleCustomSettings;
        if (child instanceof ChameleonPsiElement) {
            ChameleonPsiElement element = (ChameleonPsiElement) child;
            DBLanguage language = (DBLanguage) PsiUtil.getLanguage(element);
            codeStyleCustomSettings = language.getCodeStyleSettings(psiElement.getProject());
        }
        return codeStyleCustomSettings;
    }

    @Nullable
    public Alignment getAlignment() {
        return Alignment.createAlignment();
    }

    @NotNull
    public ChildAttributes getChildAttributes(final int newChildIndex) {
        List<Block> subBlocks = getSubBlocks();
        if (newChildIndex > subBlocks.size() - 1) {
            return new ChildAttributes(Indent.getNoneIndent(), Alignment.createAlignment());
        } else {
            Block child = getSubBlocks().get(newChildIndex);
            return new ChildAttributes(child.getIndent(), child.getAlignment());
        }
    }

    public boolean isIncomplete() {
        if (psiElement instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) psiElement;
            return basePsiElement.hasErrors();
        }
        return false;
    }

    private boolean isPreviousIncomplete() {
        Block previous = getPreviousBlockInParent();
        return previous != null && previous.isIncomplete();
    }

    private boolean isParentIncomplete() {
        return parentBlock != null && parentBlock.isIncomplete();
    }

    public boolean isLeaf() {
        return psiElement instanceof IdentifierPsiElement ||
                psiElement instanceof TokenPsiElement ||
                psiElement instanceof PsiWhiteSpace;
    }


    public String toString() {
        return psiElement.toString();
    }


    public PsiElement getPsiElement() {
        return psiElement;
    }

    private Block getPreviousBlockInParent() {
        if (parentBlock != null) {
            return index > 0 ? parentBlock.getSubBlocks().get(index - 1) : null;
        }
        return null;
    }
}
