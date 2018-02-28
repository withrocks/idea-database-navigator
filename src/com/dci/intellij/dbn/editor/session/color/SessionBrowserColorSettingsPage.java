package com.dci.intellij.dbn.editor.session.color;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.PlainSyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;

public class SessionBrowserColorSettingsPage implements ColorSettingsPage {
    protected final List<AttributesDescriptor> attributeDescriptors = new ArrayList<AttributesDescriptor>();
    protected final List<ColorDescriptor> colorDescriptors = new ArrayList<ColorDescriptor>();

    public SessionBrowserColorSettingsPage() {
        attributeDescriptors.add(new AttributesDescriptor("Active Session",   SessionBrowserTextAttributesKeys.ACTIVE_SESSION));
        attributeDescriptors.add(new AttributesDescriptor("Inactive Session", SessionBrowserTextAttributesKeys.INACTIVE_SESSION));
        attributeDescriptors.add(new AttributesDescriptor("Cached Session",   SessionBrowserTextAttributesKeys.CACHED_SESSION));
        attributeDescriptors.add(new AttributesDescriptor("Sniped Session",   SessionBrowserTextAttributesKeys.SNIPED_SESSION));
        attributeDescriptors.add(new AttributesDescriptor("Killed Session",   SessionBrowserTextAttributesKeys.KILLED_SESSION));
    }

    @Override
    public Icon getIcon() {
        return Icons.FILE_SESSION_BROWSER;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new PlainSyntaxHighlighter();
    }

    @NonNls
    @NotNull
    public final String getDemoText() {
        return " ";
    }

    @NotNull
    public AttributesDescriptor[] getAttributeDescriptors() {
        return attributeDescriptors.toArray(new AttributesDescriptor[attributeDescriptors.size()]);
    }

    @NotNull
    public ColorDescriptor[] getColorDescriptors() {
        return colorDescriptors.toArray(new ColorDescriptor[colorDescriptors.size()]);
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Session Browser (DBN)";
    }

    @Nullable
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }
}
