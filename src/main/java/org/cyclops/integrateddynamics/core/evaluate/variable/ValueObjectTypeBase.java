package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.util.EnumChatFormatting;
import org.cyclops.cyclopscore.helper.Helpers;

/**
 * Base implementation of a value object type.
 * @author rubensworks
 */
public abstract class ValueObjectTypeBase<V extends IValue> extends ValueTypeBase<V> {

    public ValueObjectTypeBase(String typeName) {
        this(typeName, Helpers.RGBToInt(243, 243, 243), EnumChatFormatting.GRAY.toString());
    }

    public ValueObjectTypeBase(String typeName, int color, String colorFormat) {
        super(typeName, color, colorFormat);
    }

    @Override
    protected String getTypeNamespace() {
        return ".object.";
    }

    @Override
    public boolean isObject() {
        return true;
    }
}
