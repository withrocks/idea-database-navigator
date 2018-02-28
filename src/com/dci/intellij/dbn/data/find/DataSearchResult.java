package com.dci.intellij.dbn.data.find;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.list.ReversedList;
import com.dci.intellij.dbn.common.ui.ListUtil;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.intellij.openapi.Disposable;

public class DataSearchResult implements Disposable {
    private Set<DataSearchResultListener> listeners = new HashSet<DataSearchResultListener>();
    private List<DataSearchResultMatch> matches = new ArrayList<DataSearchResultMatch>();
    private DataSearchResultMatch selectedMatch;
    private int matchesLimit;
    private long updateTimestamp = 0;
    private boolean isUpdating;

    public synchronized void clear() {
        selectedMatch = null;
        List<DataSearchResultMatch> oldMatches = matches;
        matches = new ArrayList<DataSearchResultMatch>();
        DisposerUtil.dispose(oldMatches);
    }

    public synchronized int size() {
        return matches.size();
    }

    public synchronized boolean isEmpty() {
        return matches.isEmpty();
    }

    public DataSearchResultMatch getSelectedMatch() {
        return selectedMatch;
    }

    public void setSelectedMatch(DataSearchResultMatch selectedMatch) {
        this.selectedMatch = selectedMatch;
    }

    public void setMatchesLimit(int value) {
        matchesLimit = value;
    }

    public int getMatchesLimit() {
        return matchesLimit;
    }

    public void addListener(DataSearchResultListener listener) {
        listeners.add(listener);
    }

    public void notifyListeners() {
        for (DataSearchResultListener listener : listeners) {
            listener.searchResultUpdated(this);
        }
    }

    public void checkTimestamp(Long updateTimestamp) throws InterruptedException {
        if (this.updateTimestamp != updateTimestamp) {
            throw new InterruptedException("Search result newer than given timestamp");
        }
    }

    public boolean isUpdating() {
        return isUpdating;
    }

    public synchronized void addMatch(DataModelCell cell, int startOffset, int endOffset) {
        DataSearchResultMatch match = new DataSearchResultMatch(cell, startOffset, endOffset);
        matches.add(match);
    }

    public synchronized Iterator<DataSearchResultMatch> getMatches(final DataModelCell cell) {
        final DataSearchResultMatch first = matches.isEmpty() ? null : findMatch(null, cell);
        if (first != null) {
            return new Iterator<DataSearchResultMatch>() {
                private DataSearchResultMatch next = first;

                @Override
                public boolean hasNext() {
                    return next != null;
                }

                @Override
                public DataSearchResultMatch next() {
                    DataSearchResultMatch current = next;
                    next = findMatch(next, cell);
                    return current;
                }

                @Override
                public void remove() {}

            };
        } else {
            return null;
        }
    }

    private synchronized DataSearchResultMatch findMatch(DataSearchResultMatch previous, DataModelCell cell) {
        int index = previous == null ? 0 : matches.indexOf(previous) + 1;
        for (int i = index; i< matches.size(); i++) {
            DataSearchResultMatch match = matches.get(i);
            if (match != null && match.getCell().equals(cell)) {
                return match;

            } else if (previous != null) {
                return null;
            }
        }
        return null;
    }

    public DataSearchResultMatch selectFirst(int fromRowIndex, int fromColumnIndex, DataSearchResultScrollPolicy scrollPolicy) {
        if (isUpdating) return null;
        return getNext(fromRowIndex, fromColumnIndex, scrollPolicy);
    }
    
    public DataSearchResultMatch selectNext(DataSearchResultScrollPolicy scrollPolicy) {
        if (isUpdating) return null;
        int fromRowIndex = 0;
        int fromColumnIndex = 0;
        
        if (selectedMatch != null) {
            fromRowIndex = selectedMatch.getCell().getRow().getIndex();
            fromColumnIndex = selectedMatch.getCell().getIndex();
            switch (scrollPolicy) {
                case VERTICAL: fromRowIndex++; break;
                case HORIZONTAL: fromColumnIndex++; break;
            }
        }
        selectedMatch = getNext(fromRowIndex, fromColumnIndex, scrollPolicy);
        return selectedMatch;
    }

    public DataSearchResultMatch selectPrevious(DataSearchResultScrollPolicy scrollPolicy) {
        if (isUpdating) return null;
        int fromRowIndex = 999999;
        int fromColumnIndex = 999999;

        if (selectedMatch != null) {
            fromRowIndex = selectedMatch.getCell().getRow().getIndex();
            fromColumnIndex = selectedMatch.getCell().getIndex();
            switch (scrollPolicy) {
                case VERTICAL: fromRowIndex--; break;
                case HORIZONTAL: fromColumnIndex--; break;
            }
        }
        selectedMatch = getPrevious(fromRowIndex, fromColumnIndex, scrollPolicy);
        return selectedMatch;
    }

    private synchronized DataSearchResultMatch getNext(int fromRowIndex, int fromColumnIndex, DataSearchResultScrollPolicy scrollPolicy) {
        if (matches.size() > 0) {
            for (DataSearchResultMatch match : matches) {
                int rowIndex = match.getCell().getRow().getIndex();
                int columnIndex = match.getCell().getIndex();

                switch (scrollPolicy) {
                    case HORIZONTAL: {
                        if (rowIndex > fromRowIndex || (rowIndex == fromRowIndex && columnIndex >= fromColumnIndex)) {
                            return match;
                        }
                        break;
                    }

                    case VERTICAL: {
                        if (columnIndex > fromColumnIndex || (columnIndex == fromColumnIndex && rowIndex >= fromRowIndex)) {
                            return match;
                        }
                        break;
                    }
                }

            }
            //reached end of the matches without resolving selection
            // scroll to the beginning
            return ListUtil.getFirst(matches);
        }
        
        return null;
    }
    
    private synchronized DataSearchResultMatch getPrevious(int fromRowIndex, int fromColumnIndex, DataSearchResultScrollPolicy scrollPolicy) {
        if (matches.size() > 0) {
            for (DataSearchResultMatch match : ReversedList.get(matches)) {
                int rowIndex = match.getCell().getRow().getIndex();
                int columnIndex = match.getCell().getIndex();
                switch (scrollPolicy) {
                    case HORIZONTAL: {
                        if (rowIndex < fromRowIndex || (rowIndex == fromRowIndex && columnIndex <= fromColumnIndex)) {
                            return match;
                        }
                        break;
                    }

                    case VERTICAL: {
                        if (columnIndex < fromColumnIndex || (columnIndex == fromColumnIndex && rowIndex <= fromRowIndex)) {
                            return match;
                        }
                        break;
                    }
                }
            }
            //reached beginning of the matches list without resolving selection
            // scroll to the end
            return ListUtil.getLast(matches);
        }
        
        return null;
    }    


    @Override
    public void dispose() {
        DisposerUtil.dispose(matches);
        listeners.clear();
        matches = null;
        listeners = null;

    }

    public void startUpdating(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
        this.isUpdating = true;
        clear();
    }

    public void stopUpdating() {
        this.isUpdating = false;
    }
}
