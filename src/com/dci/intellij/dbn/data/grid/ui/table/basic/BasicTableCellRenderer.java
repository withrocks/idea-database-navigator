package com.dci.intellij.dbn.data.grid.ui.table.basic;

import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.data.find.DataSearchResult;
import com.dci.intellij.dbn.data.find.DataSearchResultMatch;
import com.dci.intellij.dbn.data.grid.color.BasicTableTextAttributes;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributes;
import com.dci.intellij.dbn.data.grid.options.DataGridTrackingColumnSettings;
import com.dci.intellij.dbn.data.grid.ui.table.sortable.SortableTable;
import com.dci.intellij.dbn.data.model.DataModel;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.value.LargeObjectValue;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.SimpleTextAttributes;


public class BasicTableCellRenderer extends ColoredTableCellRenderer {
    private Map<Color, Border> lineBorderCache = new HashMap<Color, Border>();

    protected Border getLineBorder(Color color) {
        Border border = lineBorderCache.get(color);
        if (border == null) {
            border = new LineBorder(color, 1);
            lineBorderCache.put(color, border);
        }
        return border;
    }

    private DataGridTextAttributes attributes = createTextAttributes();

    protected DataGridTextAttributes createTextAttributes() {
        return new BasicTableTextAttributes();
    }

    public DataGridTextAttributes getAttributes() {
        return attributes;
    }

    protected void customizeCellRenderer(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {

        SortableTable sortableTable = (SortableTable) table;
        boolean isLoading = sortableTable.isLoading();

        boolean isCaretRow = table.getCellSelectionEnabled() && table.getSelectedRow() == rowIndex && table.getSelectedRowCount() == 1;



        DataModelCell cell = (DataModelCell) value;
        if (cell != null && !cell.isDisposed()) {
            boolean isLazyValue = cell.getUserValue() instanceof LargeObjectValue;

            BasicTableTextAttributes attributes = (BasicTableTextAttributes) getAttributes();
            SimpleTextAttributes textAttributes = attributes.getPlainData(false, isCaretRow);


            if (isSelected) {
                textAttributes = attributes.getSelection();
            } else if (isLoading) {
                textAttributes = attributes.getLoadingData(isCaretRow);
            } else if (isLazyValue) {
                textAttributes = attributes.getReadonlyData(false, isCaretRow);
            } else {
                DataGridTrackingColumnSettings trackingColumnSettings = sortableTable.getDataGridSettings().getTrackingColumnSettings();
                boolean trackingColumn = trackingColumnSettings.isTrackingColumn(cell.getColumnInfo().getName());
                if (trackingColumn) {
                    textAttributes = attributes.getTrackingData(false, isCaretRow);
                }
            }

            Color background = CommonUtil.nvl(textAttributes.getBgColor(), table.getBackground());
            Color foreground = CommonUtil.nvl(textAttributes.getFgColor(), table.getForeground());
            Border border = getLineBorder(background);

            setBorder(border);
            setBackground(background);
            setForeground(foreground);
            writeUserValue(cell, textAttributes, attributes);
        }
    }

    protected void writeUserValue(DataModelCell cell, SimpleTextAttributes textAttributes, DataGridTextAttributes configTextAttributes) {
         String formattedUserValue;
         if (cell.getUserValue() instanceof String) {
             formattedUserValue = (String) cell.getUserValue();
             if (formattedUserValue.indexOf('\n') > -1) {
                 formattedUserValue = formattedUserValue.replace('\n', ' ');
             }

         } else {
             formattedUserValue = CommonUtil.nvl(cell.getFormattedUserValue(), "");
         }

         if (cell.isDisposed()) return;
         DataModel model = cell.getRow().getModel();
         if (model.hasSearchResult()) {
             DataSearchResult searchResult = model.getSearchResult();

             Iterator<DataSearchResultMatch> matches = searchResult.getMatches(cell);
             if (matches != null) {
                 int lastEndOffset = 0;
                 SimpleTextAttributes searchResultAttributes = configTextAttributes.getSearchResult();
                 DataSearchResultMatch selectedMatch = searchResult.getSelectedMatch();
                 if (selectedMatch != null && selectedMatch.getCell() == cell) {
                    searchResultAttributes = configTextAttributes.getSelection();
                 }

                 int valueLength = formattedUserValue.length();
                 int lastOffset = Math.max(0, valueLength);
                 while (matches.hasNext()) {
                     DataSearchResultMatch match = matches.next();
                     if (match.getStartOffset() > lastEndOffset) {
                         int startOffset = Math.min(lastOffset, lastEndOffset);
                         int endOffset = Math.min(lastOffset, match.getStartOffset());
                         append(formattedUserValue.substring(startOffset, endOffset), textAttributes);
                     }
                     int startOffset = Math.min(lastOffset, match.getStartOffset());
                     int endOffset = Math.min(lastOffset, match.getEndOffset());
                     append(formattedUserValue.substring(startOffset, endOffset), searchResultAttributes);
                     lastEndOffset = match.getEndOffset();

                 }
                 if (lastEndOffset < valueLength) {
                     append(formattedUserValue.substring(lastEndOffset), textAttributes);
                 }
             } else {
                 append(formattedUserValue, textAttributes);
             }

         } else {
             append(formattedUserValue, textAttributes);
         }
     }

    protected static boolean match(int[] indexes, int index) {
        for (int idx : indexes) {
            if (idx == index) return true;
        }
        return false;
    }
}
