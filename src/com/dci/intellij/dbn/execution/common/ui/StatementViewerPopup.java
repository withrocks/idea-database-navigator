package com.dci.intellij.dbn.execution.common.ui;

import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupAdapter;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.psi.PsiFile;

public class StatementViewerPopup implements Disposable {
    private EditorEx viewer;
    private String resultName;

    public StatementViewerPopup(ExecutionResult executionResult) {
        this.resultName = executionResult.getName();
        Project project = executionResult.getProject();

        PsiFile previewFile = executionResult.createPreviewFile();
        Document document = DocumentUtil.getDocument(previewFile);
        viewer = (EditorEx) EditorFactory.getInstance().createViewer(document, project);
        viewer.setEmbeddedIntoDialogWrapper(true);
        viewer.getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        viewer.getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        viewer.setHighlighter(HighlighterFactory.createHighlighter(project, SQLFileType.INSTANCE));
        viewer.setBackgroundColor(viewer.getColorsScheme().getColor(ColorKey.find("CARET_ROW_COLOR")));
        viewer.getScrollPane().setViewportBorder(new LineBorder(CompatibilityUtil.getEditorBackgroundColor(viewer), 4, false));
        viewer.getScrollPane().setBorder(null);


        EditorSettings settings = viewer.getSettings();
        settings.setFoldingOutlineShown(false);
        settings.setLineMarkerAreaShown(false);
        settings.setLineNumbersShown(false);
        settings.setVirtualSpace(false);
        settings.setDndEnabled(false);
        settings.setAdditionalLinesCount(2);
        settings.setRightMarginShown(false);

        //mainPanel.setBorder(new LineBorder(Color.BLACK, 1, false));
    }

    public void show(Component component) {
        JBPopup popup = createPopup();
        popup.showInScreenCoordinates(component,
                new Point(
                        (int) (component.getLocationOnScreen().getX() + component.getWidth() +8),
                        (int) component.getLocationOnScreen().getY()));
    }

    public void show(Component component, Point point) {
        JBPopup popup = createPopup();
        point.setLocation(
                point.getX() + component.getLocationOnScreen().getX() + 16,
                point.getY() + component.getLocationOnScreen().getY() + 16);

        popup.showInScreenCoordinates(component, point);
    }

    private JBPopup createPopup() {
        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(viewer.getComponent(), viewer.getContentComponent());
        popupBuilder.setMovable(true);
        popupBuilder.setResizable(true);
        popupBuilder.setRequestFocus(true);
        popupBuilder.setTitle("<html>" + resultName + "</html>");
        JBPopup popup = popupBuilder.createPopup();

        Dimension dimension = EditorUtil.calculatePreferredSize(viewer);
        //Dimension dimension = ((EditorImpl) viewer).getPreferredSize();
        dimension.setSize(Math.min(dimension.getWidth() + 20, 1000), Math.min(dimension.getHeight() + 70, 800) );
        popup.setSize(dimension);

        popup.addListener(new JBPopupAdapter() {
            @Override
            public void onClosed(LightweightWindowEvent event) {
                dispose();
            }
        });
        return popup;
    }

    public void dispose() {
        if (viewer != null) {
            EditorFactory.getInstance().releaseEditor(viewer);
            viewer = null;
        }
    }
}
