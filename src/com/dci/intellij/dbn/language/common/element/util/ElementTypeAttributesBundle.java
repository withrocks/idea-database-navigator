package com.dci.intellij.dbn.language.common.element.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import java.util.StringTokenizer;

public class ElementTypeAttributesBundle {
    public static final ElementTypeAttributesBundle EMPTY = new ElementTypeAttributesBundle();

    private Set<ElementTypeAttribute> attributes = ElementTypeAttribute.EMPTY_LIST;

    public ElementTypeAttributesBundle(String definition) throws ElementTypeDefinitionException {
        StringTokenizer tokenizer = new StringTokenizer(definition, ",");
        while (tokenizer.hasMoreTokens()) {
            String attributeName = tokenizer.nextToken().trim();
            boolean found = false;
            for (ElementTypeAttribute attribute : ElementTypeAttribute.values()) {
                if (attribute.getName().equals(attributeName)) {
                    if (attributes == ElementTypeAttribute.EMPTY_LIST)
                        attributes = EnumSet.noneOf(ElementTypeAttribute.class);
                    attributes.add(attribute);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new ElementTypeDefinitionException("Invalid element type attribute '" + attributeName + "'");
            }
        }
    }

    private ElementTypeAttributesBundle() {}

    public boolean is(ElementTypeAttribute attribute) {
        return attributes.contains(attribute);
    }

    @Override
    public String toString() {
        return new ArrayList(attributes).toString();
    }
}
