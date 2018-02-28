package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.Icon;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

public class CharsetOption implements Presentable {
    public static List<CharsetOption> ALL = new ArrayList<CharsetOption>();
    static {
        for (Charset charset : Charset.availableCharsets().values()){
            ALL.add(new CharsetOption(charset));
        }
    }

    private Charset charset;
    public CharsetOption(Charset charset) {
        this.charset = charset;
    }

    @NotNull
    @Override
    public String getName() {
        return charset.name();
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }

    public static CharsetOption get(Charset charset) {
        for (CharsetOption charsetOption : ALL) {
            if (charsetOption.charset.equals(charset)) {
                return charsetOption;
            }
        }
        return null;
    }

    public Charset getCharset() {
        return charset;
    }
}
