package com.dci.intellij.dbn.code.common.style.formatting;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.intellij.formatting.Indent;
import org.jdom.Element;

public enum IndentDefinition implements FormattingAttribute<Indent> {
    NORMAL       (new Loader(){Indent load() {return Indent.getNormalIndent(true);}}),
    CONTINUE     (new Loader(){Indent load() {return Indent.getContinuationIndent();}}),
    NONE         (new Loader(){Indent load() {return Indent.getNoneIndent();}}),
    ABSOLUTE_NONE(new Loader(){Indent load() {return Indent.getAbsoluteNoneIndent();}});

    private Indent value;
    private Loader<Indent> loader;

    private IndentDefinition(Loader<Indent> loader) {
        this.loader = loader;
    }

    public Indent getValue() {
        if (value == null && loader != null) {
            value = loader.load();
            loader = null;
        }
        return value;
    }

    public static IndentDefinition get(Element element) {
        return SettingsUtil.getEnumAttribute(element, "formatting-indent", IndentDefinition.class);
    }
}
