package com.dci.intellij.dbn.object.factory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

public class MethodFactoryInput extends ObjectFactoryInput{
    private List<ArgumentFactoryInput> arguments = new ArrayList<ArgumentFactoryInput>();
    private ArgumentFactoryInput returnArgument;
    private DBObjectRef<DBSchema> schemaRef;

    public MethodFactoryInput(DBSchema schema, String objectName, DBObjectType methodType, int index) {
        super(objectName, methodType, null, index);
        this.schemaRef = DBObjectRef.from(schema);
    }

    public DBSchema getSchema() {
        return DBObjectRef.get(schemaRef);
    }

    public boolean isFunction() {
        return returnArgument != null;
    }

    public List<ArgumentFactoryInput> getArguments() {
        return arguments;
    }

    public ArgumentFactoryInput getReturnArgument() {
        return returnArgument;
    }

    public void setReturnArgument(ArgumentFactoryInput returnArgument) {
        this.returnArgument = returnArgument;
    }

    public void setArguments(List<ArgumentFactoryInput> arguments) {
        this.arguments = arguments;
    }

    public void validate(List<String> errors) {
        if (getObjectName().length() == 0) {
            String hint = getParent() == null ? "" : " at index " + getIndex();
            errors.add(getObjectType().getName() + " name is not specified" + hint);
            
        } else if (!StringUtil.isWord(getObjectName())) {
            errors.add("invalid " + getObjectType().getName() +" name specified" + ": \"" + getObjectName() + "\"");
        }


        if (returnArgument != null) {
            if (returnArgument.getDataType().length() == 0)
                errors.add("missing data type for return argument");
        }

        Set<String> argumentNames = new HashSet<String>();
        for (ArgumentFactoryInput argument : arguments) {
            argument.validate(errors);
            String argumentName = argument.getObjectName();
            if (argumentName.length() > 0 && argumentNames.contains(argumentName)) {
                String hint = getParent() == null ? "" : " for " + getObjectType().getName() + " \"" + getObjectName() + "\"";
                errors.add("dupplicate argument name \"" + argumentName + "\"" + hint);
            }
            argumentNames.add(argumentName);
        }
    }
}
