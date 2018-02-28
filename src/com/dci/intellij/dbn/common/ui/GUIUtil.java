package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.Colors;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.lang.reflect.Method;
import java.util.EventListener;

public class GUIUtil{
    public static final Font REGULAR_FONT = com.intellij.util.ui.UIUtil.getLabelFont();
    public static final Font BOLD_FONT = new Font(REGULAR_FONT.getName(), Font.BOLD, REGULAR_FONT.getSize());
    public static final String DARK_LAF_NAME = "Darcula";

    public static void updateSplitterProportion(final JComponent root, final float proportion) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (root instanceof Splitter) {
                    Splitter splitter = (Splitter) root;
                    splitter.setProportion(proportion);
                } else {
                    Component[] components = root.getComponents();
                    for (Component component : components) {
                        if (component instanceof JComponent) {
                            updateSplitterProportion((JComponent) component, proportion);
                        }
                    }
                }
            }
        });

    }
    
    public static Point getRelativeMouseLocation(Component component) {
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo == null) {
            return new Point();
        } else {
            Point mouseLocation = pointerInfo.getLocation();
            return getRelativeLocation(mouseLocation, component);
        }
    }
    
    public static Point getRelativeLocation(Point locationOnScreen, Component component) {
        Point componentLocation = component.getLocationOnScreen();
        Point relativeLocation = locationOnScreen.getLocation();
        relativeLocation.move(
                (int) (locationOnScreen.getX() - componentLocation.getX()), 
                (int) (locationOnScreen.getY() - componentLocation.getY()));
        return relativeLocation;
    }

    public static boolean isChildOf(Component component, Component child) {
        Component parent = child == null ? null : child.getParent();
        while (parent != null) {
            if (parent == component) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public static boolean isFocused(Component component, boolean recoursive) {
        if (component.isFocusOwner()) return true;
        if (recoursive && component instanceof JComponent) {
            JComponent parentComponent = (JComponent) component;
            for (Component childComponent : parentComponent.getComponents()) {
                if (isFocused(childComponent, recoursive)) {
                    return true;
                }
            }
        }
        return false;
    }


    public static boolean isDarkLookAndFeel() {
        return UIManager.getLookAndFeel().getName().contains(DARK_LAF_NAME);
    }

    public static boolean supportsDarkLookAndFeel() {
        if (isDarkLookAndFeel()) return true;
        for (UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels()) {
            if (lookAndFeelInfo.getName().contains(DARK_LAF_NAME)) return true;
        }
        return false;
    }

    public static void updateBorderTitleForeground(JPanel panel) {
        Border border = panel.getBorder();
        if (border instanceof TitledBorder) {
            TitledBorder titledBorder = (TitledBorder) border;
            //titledBorder.setTitleColor(com.intellij.util.ui.GUIUtil.getLabelForeground());
            titledBorder.setTitleColor(Colors.HINT_COLOR);
        }
    }

    public static void removeListeners(Component comp) {
        Method[] methods = comp.getClass().getMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("remove") && name.endsWith("Listener")) {

                Class[] params = method.getParameterTypes();
                if (params.length == 1) {
                    EventListener[] listeners;
                    try {
                        listeners = comp.getListeners(params[0]);
                    } catch (Exception e) {
                        // It is possible that someone could create a listener
                        // that doesn't extend from EventListener.  If so, ignore it
                        System.out.println("Listener " + params[0] + " does not extend EventListener");
                        continue;
                    }
                    for (EventListener listener : listeners) {
                        try {
                            method.invoke(comp, listener);
                            //System.out.println("removed Listener " + name + " for comp " + comp + "\n");
                        } catch (Exception e) {
                            System.out.println("Cannot invoke removeListener method " + e);
                            // Continue on.  The reason for removing all listeners is to
                            // make sure that we don't have a listener holding on to something
                            // which will keep it from being garbage collected. We want to
                            // continue freeing listeners to make sure we can free as much
                            // memory has possible
                        }
                    }
                } else {
                    // The only Listener method that I know of that has more than
                    // one argument is removePropertyChangeListener.  If it is
                    // something other than that, flag it and move on.
                    if (!name.equals("removePropertyChangeListener"))
                        System.out.println("    Wrong number of Args " + name);
                }
            }
        }
    }

    public static void showUnderneathOf(@NotNull JBPopup popup, @NotNull Component sourceComponent, int verticalShift, int maxHeight) {
        JComponent popupContent = popup.getContent();
        Dimension preferredSize = popupContent.getPreferredSize();
        int width = Math.max((int) preferredSize.getWidth(), sourceComponent.getWidth());
        int height = (int) Math.min(maxHeight, preferredSize.getHeight());
        popupContent.setPreferredSize(new Dimension(width, height));

        popup.show(new RelativePoint(sourceComponent, new Point(0, sourceComponent.getHeight() + verticalShift)));
    }

}
