package com.dci.intellij.dbn.common.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.object.common.DBObject;

public class NamingUtil {

    public static String getNextNumberedName(String numberedIdentifier, boolean insertWhitespace) {
        StringBuilder text = new StringBuilder();
        StringBuilder number = new StringBuilder();
        for (int i=numberedIdentifier.length() -1; i >= 0; i--) {
            char chr = numberedIdentifier.charAt(i);
            if ('0' <= chr && chr <= '9') {
                number.insert(0, chr);
            } else {
                text.append(numberedIdentifier.substring(0, i+1));
                break;
            }
        }
        int nr = number.length() == 0 ? 0 : Integer.parseInt(number.toString());
        nr++;
        if (insertWhitespace && nr == 1) text.append(" ");
        return text.toString() + nr;
    }

    public static String createNamesList(Set<IdentifierPsiElement> identifiers, int maxItems) {
        boolean partial = false;
        Set<String> names = new HashSet<String>();
        for (IdentifierPsiElement identifier : identifiers) {
            names.add(identifier.getUnquotedText().toString().toUpperCase());
            if (names.size() >= maxItems) {
                partial = identifiers.size() > maxItems;
                break;
            }
        }

        StringBuilder buffer = new StringBuilder();
        for (String name : names) {
            if (buffer.length() > 0) buffer.append(", ");
            buffer.append(name);
        }

        if (partial) {
            buffer.append("...");
        }
        return buffer.toString();
    }

    public static String[] createAliasNames(DBObject object) {
        if (object != null) {
            return createAliasNames(object.getName());
        }
        return new String[0];
    }

    public static String[] createAliasNames(CharSequence  objectName) {
        return new String[]{createAliasName(objectName)};
    }

    public static String createAliasName(CharSequence objectName) {
        StringBuilder camelBuffer = new StringBuilder();

        camelBuffer.append(objectName.charAt(0));

        for (int i = 1; i < objectName.length(); i++) {
            char previous = objectName.charAt(i - 1);
            char current = objectName.charAt(i);
            if (!Character.isLetter(previous) && Character.isLetter(current)) {
                camelBuffer.append(current);
            }
        }
        return camelBuffer.toString().toLowerCase();
    }

    public static String createCommaSeparatedList(DBObject[] objects) {
        StringBuilder buffer = new StringBuilder();
        for (DBObject column : objects) {
            buffer.append(column.getName());
            if (column != objects[objects.length-1]) {
                buffer.append(", ");
            }
        }
        return buffer.toString();
    }

    public static String createCommaSeparatedList(List<? extends DBObject> objects) {
        StringBuilder buffer = new StringBuilder();
        for (DBObject column : objects) {
            buffer.append(column.getName());
            if (column != objects.get(objects.size()-1)) {
                buffer.append(", ");
            }
        }
        return buffer.toString();
    }


    public static boolean isVowel(char chr){
        chr  = Character.toLowerCase(chr);
        String vowels = "a����e���i���o����u����";
        return vowels.indexOf(chr) > -1;
    }

    public static void main(String[] args) {
        System.out.println(enhanceUnderscoresForDisplay("_abc_def_xyz_"));
    }

    public static String createFriendlyName(String name) {
        StringBuilder friendlyName = new StringBuilder(name.replace('_', ' '));
        for (int i=0; i<friendlyName.length(); i++) {
            if (i>0 && Character.isLetter(friendlyName.charAt(i-1))){
                char chr = friendlyName.charAt(i);
                chr = Character.toLowerCase(chr);
                friendlyName.setCharAt(i, chr);
            }
        }
        return friendlyName.toString();
    }

    public static String enhanceUnderscoresForDisplay(String name) {
        return duplicateCharacter(name, '_');
    }

    public static String enhanceAndsForDisplay(String name) {
        return duplicateCharacter(name, '&');
    }

    public static String enhanceNameForDisplay(String name) {
        return enhanceAndsForDisplay(enhanceUnderscoresForDisplay(name));
    }

    private static String duplicateCharacter(String name, char chr) {
        if (name != null) {
            int index = name.indexOf(chr);
            if (index > -1) {
                int startIndex = 0;
                StringBuilder buffer  = new StringBuilder();
                while(index > -1) {
                    buffer.append(name.substring(startIndex, index+1));
                    buffer.append(chr);
                    startIndex = index + 1;
                    index = name.indexOf(chr, startIndex);
                }
                buffer.append(name.substring(startIndex));
                return buffer.toString();
            }
        }
        return name;
    }

    public static String capitalize(String string) {
        string = string.toLowerCase();
        string = Character.toUpperCase(string.charAt(0)) + string.substring(1);
        return string;
    }

    public static String capitalizeWords(String string) {
        StringBuilder result = new StringBuilder(string.toLowerCase());
        for (int i=0; i<result.length(); i++) {
            if (i == 0 || !Character.isLetter(result.charAt(i-1))) {
                result.setCharAt(i, Character.toUpperCase(result.charAt(i)));
            }
        }
        return result.toString();
    }

    public static String unquote(String string) {
        if (string.length() > 1) {
            char firstChar = string.charAt(0);
            char lastChar = string.charAt(string.length() - 1);
            if ((firstChar =='"' && lastChar == '"') || (firstChar == '`' && lastChar == '`')) {
                return string.substring(1, string.length() - 1);
            }
            return string;
        } else {
            return string;
        }
    }

    public static String getClassName(Class clazz) {
        int index = clazz.getName().lastIndexOf('.');
        return clazz.getName().substring(index + 1);
    }
}
