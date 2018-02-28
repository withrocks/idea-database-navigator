package com.dci.intellij.dbn.common.environment;

import javax.swing.Icon;
import java.awt.Color;
import java.util.UUID;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.ColorIcon;
import com.intellij.util.ui.UIUtil;

public class EnvironmentType extends CommonUtil implements Cloneable, PersistentConfiguration, Presentable {

    public static final Color DEFAULT_REGULAR_COLOR = Color.LIGHT_GRAY;
    public static final Color DEFAULT_DARK_COLOR = Color.DARK_GRAY;

    public interface EnvironmentColor {
/*        JBColor DEVELOPMENT = new JBColor(new Color(-2430209), new Color(0x445F80));
        JBColor INTEGRATION = new JBColor(new Color(-2621494), new Color(0x466646));
        JBColor PRODUCTION = new JBColor(new Color(-11574), new Color(0x634544));
        JBColor OTHER = new JBColor(new Color(-1576), new Color(0x5C5B41));*/
        JBColor NONE = new JBColor(new Color(0xffffff), Color.DARK_GRAY);
    }

    public static final EnvironmentType DEFAULT     = new EnvironmentType("default", "", "", null, null);
    public static final EnvironmentType DEVELOPMENT = new EnvironmentType("development", "Development", "Development environment", new Color(-2430209), new Color(0x445F80));
    public static final EnvironmentType INTEGRATION = new EnvironmentType("integration", "Integration", "Integration environment", new Color(-2621494), new Color(0x466646));
    public static final EnvironmentType PRODUCTION  = new EnvironmentType("production", "Production", "Productive environment", new Color(-11574), new Color(0x634544));
    public static final EnvironmentType OTHER       = new EnvironmentType("other", "Other", "", new Color(-1576), new Color(0x5C5B41));
    public static final EnvironmentType[] DEFAULT_ENVIRONMENT_TYPES = new EnvironmentType[] {
            DEVELOPMENT,
            INTEGRATION,
            PRODUCTION,
            OTHER};

    private String id;
    private String name;
    private String description;
    private Color regularColor;
    private Color darkColor;
    private transient JBColor color;
    private boolean isDarkScheme = UIUtil.isUnderDarcula();

    public static EnvironmentType forName(String name) {
        for (EnvironmentType environmentType : DEFAULT_ENVIRONMENT_TYPES){
            if (environmentType.name.equals(name)) {
                return environmentType;
            }
        }
        return null;
    }

    public EnvironmentType() {
        id = UUID.randomUUID().toString();
    }

    public EnvironmentType(String id, String name, String description, Color regularColor, Color darkColor) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.regularColor = regularColor;
        this.darkColor = darkColor;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        JBColor color = getColor();
        return color == null ? null : new ColorIcon(12, color);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Nullable
    public JBColor getColor() {
        if (isDarkScheme != UIUtil.isUnderDarcula()) {
            isDarkScheme = UIUtil.isUnderDarcula();
            color = null;
        }

        if (color == null) {
            if (isDarkScheme && darkColor != null) {
                Color regularColor = nvl(this.regularColor, DEFAULT_REGULAR_COLOR);
                color = new JBColor(regularColor, darkColor);
            } else if (!isDarkScheme && regularColor != null) {
                Color darkColor = nvl(this.darkColor, DEFAULT_DARK_COLOR);
                this.color = new JBColor(regularColor, darkColor);
            }
        }

        return color;
    }

    public void setColor(Color color) {
        if (UIUtil.isUnderDarcula())
            darkColor = color; else
            regularColor = color;
        this.color = null;
    }

    public EnvironmentType clone() {
        return new EnvironmentType(id, name, description, regularColor, darkColor);
    }
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnvironmentType that = (EnvironmentType) o;

        if (darkColor != null ? !darkColor.equals(that.darkColor) : that.darkColor != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (!id.equals(that.id)) return false;
        if (!name.equals(that.name)) return false;
        if (regularColor != null ? !regularColor.equals(that.regularColor) : that.regularColor != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (regularColor != null ? regularColor.hashCode() : 0);
        result = 31 * result + (darkColor != null ? darkColor.hashCode() : 0);
        return result;
    }

    @Override
    public void readConfiguration(Element element) {
        id = element.getAttributeValue("id");
        name = element.getAttributeValue("name");
        description = element.getAttributeValue("description");

        String value = element.getAttributeValue("color");
        if (StringUtil.isNotEmpty(value)) {
            int index = value.indexOf('/');
            if (index > -1) {
                String regularRgb = value.substring(0, index);
                String darkRgb = value.substring(index + 1);
                regularColor = StringUtil.isEmpty(regularRgb) ? null : new Color(Integer.parseInt(regularRgb));
                darkColor = StringUtil.isEmpty(darkRgb) ? null : new Color(Integer.parseInt(darkRgb));
            }
        }

        EnvironmentType defaultEnvironmentType = forName(name);
        if (id == null && defaultEnvironmentType != null) {
            id = defaultEnvironmentType.id;
        }
        if (id == null) id = name.toLowerCase();
    }

    @Override
    public void writeConfiguration(Element element) {
        element.setAttribute("id", id);
        element.setAttribute("name", name);
        element.setAttribute("description", CommonUtil.nvl(description, ""));
        element.setAttribute("color",
                (regularColor != null ? regularColor.getRGB() : "") + "/" +
                (darkColor != null ? darkColor.getRGB() : ""));
    }
}
