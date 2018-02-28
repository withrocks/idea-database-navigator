package com.dci.intellij.dbn.editor.session.color;

import java.awt.Color;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.TextAttributesUtil;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributes;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributesKeys;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.ui.SimpleTextAttributes;

public class SessionBrowserTextAttributes extends CommonUtil implements DataGridTextAttributes {
    private SimpleTextAttributes activeSession;
    private SimpleTextAttributes inactiveSession;
    private SimpleTextAttributes cachedSession;
    private SimpleTextAttributes snipedSession;
    private SimpleTextAttributes killedSession;
    private SimpleTextAttributes selection;
    private SimpleTextAttributes searchResult;
    private SimpleTextAttributes activeSessionAtCaretRow;
    private SimpleTextAttributes inactiveSessionAtCaretRow;
    private SimpleTextAttributes cachedSessionAtCaretRow;
    private SimpleTextAttributes snipedSessionAtCaretRow;
    private SimpleTextAttributes killedSessionAtCaretRow;

    private SimpleTextAttributes loadingData;
    private SimpleTextAttributes loadingDataAtCaretRow;


    private Color caretRowBgColor;

    public SessionBrowserTextAttributes() {
        load();
    }

    public void load() {
        EditorColorsScheme globalScheme = EditorColorsManager.getInstance().getGlobalScheme();
        caretRowBgColor = globalScheme.getColor(DataGridTextAttributesKeys.CARET_ROW_BACKGROUND);

        activeSession = TextAttributesUtil.getSimpleTextAttributes(SessionBrowserTextAttributesKeys.ACTIVE_SESSION);
        inactiveSession = TextAttributesUtil.getSimpleTextAttributes(SessionBrowserTextAttributesKeys.INACTIVE_SESSION);
        cachedSession = TextAttributesUtil.getSimpleTextAttributes(SessionBrowserTextAttributesKeys.CACHED_SESSION);
        snipedSession = TextAttributesUtil.getSimpleTextAttributes(SessionBrowserTextAttributesKeys.SNIPED_SESSION);
        killedSession = TextAttributesUtil.getSimpleTextAttributes(SessionBrowserTextAttributesKeys.KILLED_SESSION);
        loadingData = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.LOADING_DATA);

        activeSessionAtCaretRow = new SimpleTextAttributes(caretRowBgColor, activeSession.getFgColor(), null, activeSession.getStyle());
        inactiveSessionAtCaretRow = new SimpleTextAttributes(caretRowBgColor, inactiveSession.getFgColor(), null, inactiveSession.getStyle());
        cachedSessionAtCaretRow = new SimpleTextAttributes(caretRowBgColor, cachedSession.getFgColor(), null, cachedSession.getStyle());
        snipedSessionAtCaretRow = new SimpleTextAttributes(caretRowBgColor, snipedSession.getFgColor(), null, snipedSession.getStyle());
        killedSessionAtCaretRow = new SimpleTextAttributes(caretRowBgColor, killedSession.getFgColor(), null, killedSession.getStyle());
        loadingDataAtCaretRow = new SimpleTextAttributes(caretRowBgColor, loadingData.getFgColor(), null, loadingData.getFontStyle());

        selection = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.SELECTION);
        searchResult = TextAttributesUtil.getSimpleTextAttributes(EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES);
    }

    public SimpleTextAttributes getActiveSession(boolean atCaretRow) {
        return atCaretRow ? activeSessionAtCaretRow : activeSession;
    }

    public SimpleTextAttributes getInactiveSession(boolean atCaretRow) {
        return atCaretRow ? inactiveSessionAtCaretRow : inactiveSession;
    }

    public SimpleTextAttributes getCachedSession(boolean atCaretRow) {
        return atCaretRow ? cachedSessionAtCaretRow : cachedSession;
    }

    public SimpleTextAttributes getSnipedSession(boolean atCaretRow) {
        return atCaretRow ? snipedSessionAtCaretRow : snipedSession;
    }

    public SimpleTextAttributes getKilledSession(boolean atCaretRow) {
        return atCaretRow ? killedSessionAtCaretRow : killedSession;
    }

    public SimpleTextAttributes getLoadingData(boolean atCaretRow) {
        return atCaretRow ? loadingDataAtCaretRow : loadingData;
    }

    public SimpleTextAttributes getSelection() {
        return selection;
    }

    public SimpleTextAttributes getSearchResult() {
        return searchResult;
    }

    public Color getCaretRowBgColor() {
        return caretRowBgColor;
    }

    @Override
    public SimpleTextAttributes getPlainData(boolean modified, boolean atCaretRow) {
        return getActiveSession(atCaretRow);
    }
}
