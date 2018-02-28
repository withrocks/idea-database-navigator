package com.dci.intellij.dbn.code.common.style.formatting;

import com.dci.intellij.dbn.code.common.style.presets.CodeStylePreset;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.intellij.formatting.Wrap;
import org.jdom.Element;

public enum WrapDefinition implements FormattingAttribute<Wrap>{
    NONE    (new Loader(){Wrap load(){return CodeStylePreset.WRAP_NONE;}}),
    NORMAL  (new Loader(){Wrap load(){return CodeStylePreset.WRAP_NORMAL;}}),
    ALWAYS  (new Loader(){Wrap load(){return CodeStylePreset.WRAP_ALWAYS;}}),
    IF_LONG (new Loader(){Wrap load(){return CodeStylePreset.WRAP_IF_LONG;}});

    private Wrap value;
    private Loader<Wrap> loader;

    private WrapDefinition(Loader<Wrap> loader) {
        this.loader = loader;
    }

    public Wrap getValue() {
        if (value == null && loader != null) {
            value = loader.load();
            loader = null;
        }
        return value;
    }

    public static WrapDefinition get(Element element) {
        return SettingsUtil.getEnumAttribute(element, "formatting-wrap", WrapDefinition.class);
    }
}
