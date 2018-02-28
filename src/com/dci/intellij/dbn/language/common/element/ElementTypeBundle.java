package com.dci.intellij.dbn.language.common.element;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.TokenTypeBundle;
import com.dci.intellij.dbn.language.common.element.impl.BasicElementTypeImpl;
import com.dci.intellij.dbn.language.common.element.impl.BlockElementTypeImpl;
import com.dci.intellij.dbn.language.common.element.impl.ExecVariableElementTypeImpl;
import com.dci.intellij.dbn.language.common.element.impl.IdentifierElementTypeImpl;
import com.dci.intellij.dbn.language.common.element.impl.IterationElementTypeImpl;
import com.dci.intellij.dbn.language.common.element.impl.NamedElementTypeImpl;
import com.dci.intellij.dbn.language.common.element.impl.OneOfElementTypeImpl;
import com.dci.intellij.dbn.language.common.element.impl.QualifiedIdentifierElementTypeImpl;
import com.dci.intellij.dbn.language.common.element.impl.SequenceElementTypeImpl;
import com.dci.intellij.dbn.language.common.element.impl.TokenElementTypeImpl;
import com.dci.intellij.dbn.language.common.element.impl.WrapperElementTypeImpl;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinition;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.diagnostic.Logger;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

public class ElementTypeBundle {
    private final Logger log = Logger.getInstance(getClass().getName());
    private TokenTypeBundle tokenTypeBundle;
    private BasicElementType unknownElementType;
    private NamedElementType rootElementType;

    private Set<LeafElementType> leafElementTypes = new THashSet<LeafElementType>();
    private Set<WrapperElementType> wrapperElementTypes = new THashSet<WrapperElementType>();
    private Set<ElementType> wrappedElementTypes = new THashSet<ElementType>();
    private Set<OneOfElementType> oneOfElementTypes = new THashSet<OneOfElementType>();
    private final Map<String, NamedElementType> namedElementTypes = new THashMap<String, NamedElementType>();
    private final DBLanguageDialect languageDialect;
    private boolean rewriteIndexes;

    private int idCursor;

    public ElementTypeBundle(DBLanguageDialect languageDialect, TokenTypeBundle tokenTypeBundle, Document elementTypesDef) {
        this.languageDialect = languageDialect;
        this.tokenTypeBundle = tokenTypeBundle;
        try {
            Element root = elementTypesDef.getRootElement();
            for (Object o : root.getChildren()) {
                Element child = (Element) o;
                createNamedElementType(child);
            }

            NamedElementType unknown = namedElementTypes.get("custom_undefined");
            for(NamedElementType namedElementType : namedElementTypes.values()){
                if (!namedElementType.isDefinitionLoaded()) {
                    namedElementType.update(unknown);
                    //log.info("ERROR: element '" + namedElementType.getId() + "' not defined.");
                    System.out.println("DEBUG - [" + this.languageDialect.getID() + "] undefined element type: " + namedElementType.getId());
/*
                    if (DatabaseNavigator.getInstance().isDebugModeEnabled()) {
                        System.out.println("WARNING - [" + getLanguageDialect().getID() + "] undefined element type: " + namedElementType.getId());
                    }
*/
                }
            }

            for (LeafElementType leafElementType: leafElementTypes) {
                leafElementType.registerLeaf();
            }

            for (WrapperElementType wrapperElementType : wrapperElementTypes) {
                wrapperElementType.getBeginTokenElement().registerLeaf();
                wrapperElementType.getEndTokenElement().registerLeaf();
            }

            for (ElementType wrappedElementType : wrappedElementTypes) {
                WrappingDefinition wrapping = wrappedElementType.getWrapping();
                wrapping.getBeginElementType().registerLeaf();
                wrapping.getEndElementType().registerLeaf();
            }

            if (rewriteIndexes) {
                StringWriter stringWriter = new StringWriter();
                new XMLOutputter().output(elementTypesDef, stringWriter);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                String data = stringWriter.getBuffer().toString();
                System.out.println(data);
                clipboard.setContents(new StringSelection(data), null);

            } else {
            }

            //warnAmbiguousBranches();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TokenTypeBundle getTokenTypeBundle() {
        return tokenTypeBundle;
    }

    public void markIndexesDirty() {
        this.rewriteIndexes = true;
    }

    public boolean isRewriteIndexes() {
        return rewriteIndexes;
    }

    private void createNamedElementType(Element def) throws ElementTypeDefinitionException {
        String id = determineMandatoryAttribute(def, "id", "Invalid definition of named element type.");
        log.debug("Updating complex element definition '" + id + '\'');
        NamedElementType elementType = getNamedElementType(id, null);
        elementType.loadDefinition(def);
        if (elementType.is(ElementTypeAttribute.ROOT)) {
            if (rootElementType == null) {
                rootElementType = elementType;
            } else {
                throw new ElementTypeDefinitionException("Duplicate root definition");
            }
        }
    }


    public static String determineMandatoryAttribute(Element def, String attribute, String message) throws ElementTypeDefinitionException {
        String value = def.getAttributeValue(attribute);
        if (value == null) {
            throw new ElementTypeDefinitionException(message + "Missing '" + attribute + "' attribute.");
        }
        return value;
    }

    public ElementType resolveElementDefinition(Element def, String type, ElementType parent) throws ElementTypeDefinitionException {
        ElementType result;
        if (ElementTypeDefinition.SEQUENCE.is(type)){
            result = new SequenceElementTypeImpl(this, parent, createId(), def);
            log.debug("Created sequence element definition");
        } else if (ElementTypeDefinition.BLOCK.is(type)) {
            result = new BlockElementTypeImpl(this, parent, createId(), def);
            log.debug("Created iteration element definition");
        } else if (ElementTypeDefinition.ITERATION.is(type)) {
            result = new IterationElementTypeImpl(this, parent, createId(), def);
            log.debug("Created iteration element definition");
        } else if (ElementTypeDefinition.ONE_OF.is(type)) {
            result =  new OneOfElementTypeImpl(this, parent, createId(), def);
            oneOfElementTypes.add((OneOfElementType) result);
            log.debug("Created one-of element definition");
        } else if (ElementTypeDefinition.QUALIFIED_IDENTIFIER.is(type)) {
            result =  new QualifiedIdentifierElementTypeImpl(this, parent, createId(), def);
            log.debug("Created qualified identifier element definition");
        } else if (ElementTypeDefinition.WRAPPER.is(type)) {
            result = new WrapperElementTypeImpl(this, parent, createId(), def);
            wrapperElementTypes.add((WrapperElementType) result);
            log.debug("Created wrapper element definition");
        } else if (ElementTypeDefinition.ELEMENT.is(type)) {
            String id = determineMandatoryAttribute(def, "ref-id", "Invalid reference to element.");
            result = getNamedElementType(id, parent);
        } else if (ElementTypeDefinition.TOKEN.is(type)) {
            result = new TokenElementTypeImpl(this, parent, createId(), def);
        } else if (
                ElementTypeDefinition.OBJECT_DEF.is(type) ||
                ElementTypeDefinition.OBJECT_REF.is(type) ||
                ElementTypeDefinition.ALIAS_DEF.is(type) ||
                ElementTypeDefinition.ALIAS_REF.is(type) ||
                ElementTypeDefinition.VARIABLE_DEF.is(type) ||
                ElementTypeDefinition.VARIABLE_REF.is(type)) {
            result = new IdentifierElementTypeImpl(this, parent, createId(), def);
        } else if (ElementTypeDefinition.EXEC_VARIABLE.is(type)) {
            result = new ExecVariableElementTypeImpl(this, parent, createId(), def);            
        }  else {
            throw new ElementTypeDefinitionException("Could not resolve element definition '" + type + '\'');
        }
        if (result instanceof LeafElementType) {
            leafElementTypes.add((LeafElementType) result);
        }
        WrappingDefinition wrapping = result.getWrapping();
        if (wrapping != null) {
            wrappedElementTypes.add(result);
        }
        return result;
    }


    public static DBObjectType resolveObjectType(String name) throws ElementTypeDefinitionException {
        DBObjectType objectType = DBObjectType.getObjectType(name);
        if (objectType == null)
            throw new ElementTypeDefinitionException("Invalid object type '" + name + '\'');
        return objectType;
    }


    /*protected synchronized TokenElementType getTokenElementType(String id) {
        TokenElementType elementType = tokenElementTypes.get(id);
        if (elementType == null) {
            elementType = new TokenElementType(this, id);
            tokenElementTypes.put(id, elementType);
            log.info("Created token element objectType '" + id + "'");
        }
        return elementType;
    }*/

    protected synchronized NamedElementType getNamedElementType(String id, ElementType parent) {
        NamedElementType elementType = namedElementTypes.get(id);
        if (elementType == null) {
            elementType = new NamedElementTypeImpl(this, id);
            namedElementTypes.put(id, elementType);
            log.debug("Created named element type '" + id + '\'');
        }
        if (parent != null) elementType.addParent(parent);
        return elementType;
    }

    public DBLanguageDialect getLanguageDialect() {
        return languageDialect;
    }

    public NamedElementType getRootElementType() {
        return rootElementType;
    }

    public NamedElementType getNamedElementType(String id) {
        return namedElementTypes.get(id);
    }

    public BasicElementType getUnknownElementType() {
        if (unknownElementType == null) {
            unknownElementType = new BasicElementTypeImpl(this);
        }
        return unknownElementType;
    }

    public String createId() {
        String id = Integer.toString(idCursor++);
        StringBuilder buffer = new StringBuilder();
        while (buffer.length() + id.length() < 9) {
            buffer.append('0');
        }
        buffer.append(id);
        return buffer.toString();
    }
}
