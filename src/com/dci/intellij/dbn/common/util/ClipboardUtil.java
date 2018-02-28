package com.dci.intellij.dbn.common.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.jetbrains.annotations.Nullable;

public class ClipboardUtil {

    public static XmlContent createXmlContent(String text) {
        return new XmlContent(text);
    }

    public static class XmlContent implements Transferable {
        private DataFlavor[] dataFlavors;
        private String content;

        public XmlContent(String text) {
            content = text;
            try {
                dataFlavors = new DataFlavor[3];
                dataFlavors[0] = new DataFlavor("text/xml;class=java.lang.String");
                dataFlavors[1] = new DataFlavor("text/rtf;class=java.lang.String");
                dataFlavors[2] = new DataFlavor("text/plain;class=java.lang.String");

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public DataFlavor[] getTransferDataFlavors() {
            return dataFlavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return
                    "text/xml".equals(flavor.getMimeType()) ||
                            "text/rtf".equals(flavor.getMimeType()) ||
                            "text/plain".equals(flavor.getMimeType());
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return content;
        }
    }

    @Nullable
    public static String getStringContent() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Object data = clipboard.getData(DataFlavor.stringFlavor);
            if (data instanceof String) {
                return (String) data;
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }
}
