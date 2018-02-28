package com.dci.intellij.dbn.object.factory;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.object.common.DBObjectType;

import java.util.List;


public class ArgumentFactoryInput extends ObjectFactoryInput{

    private String dataType;
    private boolean isInput;
    private boolean isOutput;

    public ArgumentFactoryInput(ObjectFactoryInput parent, int index, String objectName, String dataType, boolean input, boolean output) {
        super(objectName, DBObjectType.ARGUMENT, parent, index);
        this.dataType = dataType == null ? "" : dataType.trim();
        this.isInput = input;
        this.isOutput = output;
    }

    public String getDataType() {
        return dataType;
    }

    public boolean isInput() {
        return isInput;
    }

    public boolean isOutput() {
        return isOutput;
    }

    public void validate(List<String> errors) {
        if (getObjectName().length() == 0) {
            errors.add("argument name is not specified at index " + getIndex());

        } else if (!StringUtil.isWord(getObjectName())) {
            errors.add("invalid argument name specified at index " + getIndex() + ": \"" + getObjectName() + "\"");
        }

        if (dataType.length() == 0){
            if (getObjectName().length() > 0) {
                errors.add("missing data type for argument \"" + getObjectName() + "\"");
            } else {
                errors.add("missing data type for argument at index " + getIndex());
            }
        }
    }
}
