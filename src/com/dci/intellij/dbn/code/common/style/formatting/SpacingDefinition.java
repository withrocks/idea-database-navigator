package com.dci.intellij.dbn.code.common.style.formatting;

import org.jdom.Element;

import com.dci.intellij.dbn.code.common.style.presets.CodeStylePreset;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.intellij.formatting.Spacing;

public enum SpacingDefinition implements FormattingAttribute<Spacing>{
    NO_SPACE  (new Loader(){Spacing load(){return CodeStylePreset.SPACING_NO_SPACE;}}),
    ONE_SPACE (new Loader(){Spacing load(){return CodeStylePreset.SPACING_ONE_SPACE;}}),

    LINE_BREAK (new Loader(){Spacing load(){return CodeStylePreset.SPACING_LINE_BREAK;}}),
    ONE_LINE  (new Loader(){Spacing load(){return CodeStylePreset.SPACING_ONE_LINE;}}),

    MIN_LINE_BREAK (new Loader(){Spacing load(){return CodeStylePreset.SPACING_MIN_LINE_BREAK;}}),
    MIN_ONE_LINE  (new Loader(){Spacing load(){return CodeStylePreset.SPACING_MIN_ONE_LINE;}}),
    MIN_ONE_SPACE  (new Loader(){Spacing load(){return CodeStylePreset.SPACING_MIN_ONE_SPACE;}}),
    ;

    private Spacing value;
    private Loader<Spacing> loader;

    private SpacingDefinition(Loader<Spacing> loader) {
        this.loader = loader;
    }

    public Spacing getValue() {
        if (value == null && loader != null) {
            value = loader.load();
            loader = null;
        }
        return value;
    }

    public static SpacingDefinition get(Element element, boolean before) {
        return before ?
                SettingsUtil.getEnumAttribute(element, "formatting-spacing-before", SpacingDefinition.class) :
                SettingsUtil.getEnumAttribute(element, "formatting-spacing-after", SpacingDefinition.class);
    }
}
