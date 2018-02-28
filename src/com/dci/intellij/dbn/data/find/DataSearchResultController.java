package com.dci.intellij.dbn.data.find;

import java.awt.Rectangle;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTable;
import com.dci.intellij.dbn.data.model.DataModel;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.dci.intellij.dbn.data.model.basic.BasicDataModel;
import com.intellij.find.FindManager;
import com.intellij.find.FindResult;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

public class DataSearchResultController implements Disposable{
    private SearchableDataComponent searchableComponent;

    public DataSearchResultController(SearchableDataComponent searchableComponent) {
        this.searchableComponent = searchableComponent;
    }

    public void moveCursor(DataSearchDirection direction) {
        BasicTable<? extends BasicDataModel> table = searchableComponent.getTable();
        DataModel dataModel = table.getModel();
        DataSearchResult searchResult = dataModel.getSearchResult();
        DataSearchResultScrollPolicy scrollPolicy = DataSearchResultScrollPolicy.HORIZONTAL;
        DataSearchResultMatch oldSelection = searchResult.getSelectedMatch();
        DataSearchResultMatch selection =
                direction == DataSearchDirection.DOWN ? searchResult.selectNext(scrollPolicy) :
                direction == DataSearchDirection.UP ? searchResult.selectPrevious(scrollPolicy) : null;

        updateSelection(table, oldSelection, selection);
    }

    public void selectFirst(int selectedRowIndex, int selectedColumnIndex) {
        BasicTable<? extends BasicDataModel> table = searchableComponent.getTable();
        DataModel dataModel = table.getModel();
        DataSearchResult searchResult = dataModel.getSearchResult();
        DataSearchResultScrollPolicy scrollPolicy = DataSearchResultScrollPolicy.HORIZONTAL;

        DataSearchResultMatch oldSelection = searchResult.getSelectedMatch();
        DataSearchResultMatch selection = searchResult.selectFirst(selectedRowIndex, selectedColumnIndex, scrollPolicy);

        updateSelection(table, oldSelection, selection);
    }

    private static void updateSelection(BasicTable table, DataSearchResultMatch oldSelection, DataSearchResultMatch selection) {
        if (oldSelection != null) {
            DataModelCell cell = oldSelection.getCell();
            Rectangle cellRectangle = table.getCellRect(cell);
            table.repaint(cellRectangle);
        }

        if (selection != null) {
            DataModelCell cell = selection.getCell();
            Rectangle cellRectangle = table.getCellRect(cell);
            table.repaint(cellRectangle);
            cellRectangle.grow(100, 100);
            table.scrollRectToVisible(cellRectangle);
        }
    }

    public void updateResult(final DataFindModel findModel) {
        new BackgroundTask(searchableComponent.getTable().getProject(), "Updating search results", true) {
            @Override
            public synchronized void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                DataModel dataModel = searchableComponent.getTable().getModel();
                final DataSearchResult searchResult = dataModel.getSearchResult();
                
                long updateTimestamp = System.currentTimeMillis();
                searchResult.startUpdating(updateTimestamp);

                Project project = dataModel.getProject();
                FindManager findManager = FindManager.getInstance(project);

                for (Object r : dataModel.getRows()) {
                    searchResult.checkTimestamp(updateTimestamp);
                    DataModelRow row = (DataModelRow) r;
                    for (Object c : row.getCells()) {
                        searchResult.checkTimestamp(updateTimestamp);
                        DataModelCell cell = (DataModelCell) c;
                        String userValue = cell.getFormattedUserValue();
                        if (userValue != null) {
                            int findOffset = 0;
                            while (true) {
                                FindResult findResult = findManager.findString(userValue, findOffset, findModel);
                                searchResult.checkTimestamp(updateTimestamp);
                                if (findResult.isStringFound()) {
                                    searchResult.addMatch(cell, findResult.getStartOffset(), findResult.getEndOffset());
                                    findOffset = findResult.getEndOffset();
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
                
                searchResult.stopUpdating();

                new SimpleLaterInvocator() {
                    public void execute() {
                        BasicTable table = searchableComponent.getTable();
                        int selectedRowIndex = table.getSelectedRow();
                        int selectedColumnIndex = table.getSelectedRow();
                        if (selectedRowIndex < 0) selectedRowIndex = 0;
                        if (selectedColumnIndex < 0) selectedColumnIndex = 0;
                        searchableComponent.cancelEditActions();

                        table.clearSelection();
                        table.revalidate();
                        table.repaint();

                        selectFirst(selectedRowIndex, selectedColumnIndex);
                        searchResult.notifyListeners();

                    }
                }.start();

            }
        }.start();

    }

    @Override
    public void dispose() {
        searchableComponent = null;
    }
}
