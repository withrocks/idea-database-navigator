package com.dci.intellij.dbn.object.common.ui;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ObjectDetailsForm {
    private JLabel connectionLabel;
    private JPanel objectPanel;
    private JPanel mainPanel;
    private DBObject object;

    public ObjectDetailsForm(DBObject object) {
        this.object = object;
        objectPanel.setLayout(new BoxLayout(objectPanel, BoxLayout.X_AXIS));
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        connectionLabel.setText(connectionHandler.getName());
        connectionLabel.setIcon(connectionHandler.getIcon());
        

        java.util.List<DBObject> chain = new ArrayList<DBObject>();
        while (object != null) {
            chain.add(0, object);
            object = object.getParentObject();
        }

        for (int i=0; i<chain.size(); i++) {
            object = chain.get(i);
            if ( i > 0) objectPanel.add(new JLabel(" > "));

            JLabel objectLabel = new JLabel(object.getName(), object.getIcon(), SwingConstants.LEFT);
            if (object == this.object) {
                Font font = objectLabel.getFont().deriveFont(Font.BOLD);
                objectLabel.setFont(font);
            }
            objectPanel.add(objectLabel);
        }

    }

    public JPanel getComponent() {
        return mainPanel;
    }
}
