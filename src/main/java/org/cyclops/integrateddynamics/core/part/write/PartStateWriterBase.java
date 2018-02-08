package org.cyclops.integrateddynamics.core.part.write;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.helper.CollectionHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.part.PartStateActiveVariableBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A default implementation of the {@link IPartTypeWriter}.
 * @author rubensworks
 */
public class PartStateWriterBase<P extends IPartTypeWriter>
        extends PartStateActiveVariableBase<P> implements IPartStateWriter<P> {

    private IAspectWrite activeAspect = null;
    private Map<String, List<L10NHelpers.UnlocalizedString>> errorMessages = Maps.newHashMap();
    private boolean firstTick = true;
    private Set<String> transientErrors = Sets.newHashSet();

    public PartStateWriterBase(int inventorySize) {
        super(inventorySize);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        if (this.activeAspect != null) tag.setString("activeAspectName", this.activeAspect.getUnlocalizedName());
        NBTClassType.getType(Map.class, this.errorMessages).writePersistedField("errorMessages", this.errorMessages, tag);
        NBTClassType.getType(Set.class, this.transientErrors).writePersistedField("transientErrors", this.transientErrors, tag);
        super.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        IAspect aspect = Aspects.REGISTRY.getAspect(tag.getString("activeAspectName"));
        if (aspect instanceof IAspectWrite) {
            this.activeAspect = (IAspectWrite) aspect;
        }
        this.errorMessages = (Map<String, List<L10NHelpers.UnlocalizedString>>) NBTClassType.getType(Map.class, this.errorMessages).readPersistedField("errorMessages", tag);
        this.transientErrors = (Set<String>) NBTClassType.getType(Set.class, this.transientErrors).readPersistedField("transientErrors", tag);
        super.readFromNBT(tag);
    }

    @Override
    protected void validate(IPartNetwork network) {
        // Note that this is only called server-side, so these errors are sent via NBT to the client(s).
        if(getActiveAspect() != null) {
            this.currentVariableFacade.validate(network,
                    new PartStateWriterBase.Validator(this, getActiveAspect()), getActiveAspect().getValueType());
        }
    }

    @Override
    protected void onCorruptedState() {
        super.onCorruptedState();
        this.activeAspect = null;
    }

    @Override
    public boolean hasVariable() {
        return getActiveAspect() != null && getErrors(getActiveAspect()).isEmpty() && super.hasVariable();
    }

    @Override
    public void triggerAspectInfoUpdate(P partType, PartTarget target, IAspectWrite newAspect) {
        onVariableContentsUpdated(partType, target);
        IAspectWrite activeAspect = getActiveAspect();
        if(activeAspect != null && activeAspect != newAspect) {
            activeAspect.onDeactivate(partType, target, this);
        }
        if(newAspect != null && activeAspect != newAspect) {
            newAspect.onActivate(partType, target, this);
        }
        this.activeAspect = newAspect;
    }

    @Override
    public void onVariableContentsUpdated(P partType, PartTarget target) {
        // Resets the errors for this aspect
        super.onVariableContentsUpdated(partType, target);
        IAspectWrite activeAspect = getActiveAspect();
        if(activeAspect != null) {
            addError(activeAspect, null, false);
        }
    }

    @Override
    public IAspectWrite getActiveAspect() {
        return activeAspect;
    }

    @Override
    public List<L10NHelpers.UnlocalizedString> getErrors(IAspectWrite aspect) {
        List<L10NHelpers.UnlocalizedString> errors = errorMessages.get(aspect.getUnlocalizedName());
        if(errors == null) {
            return Collections.emptyList();
        }
        return errors;
    }

    @Override
    public boolean isTransientError(IAspectWrite aspect) {
        return transientErrors.contains(aspect.getUnlocalizedName());
    }

    @Override
    public void addError(IAspectWrite aspect, L10NHelpers.UnlocalizedString error, boolean transientError) {
        if(error == null) {
            errorMessages.remove(aspect.getUnlocalizedName());
            transientErrors.remove(aspect.getUnlocalizedName());
        } else {
            CollectionHelpers.addToMapList(errorMessages, aspect.getUnlocalizedName(), error);
            if (transientError) {
                transientErrors.add(aspect.getUnlocalizedName());
            } else {
                transientErrors.remove(aspect.getUnlocalizedName());
            }
        }
        onDirty();
        sendUpdate(); // We want this error messages to be sent to the client(s).
    }

    @Override
    public boolean checkAndResetFirstTick() {
        if(firstTick) {
            firstTick = false;
            return true;
        }
        return false;
    }

    public static class Validator implements IVariableFacade.IValidator {

        private final IPartStateWriter state;
        private final IAspectWrite aspect;

        /**
         * Make a new instance
         * @param state The part state.
         * @param aspect The aspect to set the error for.
         */
        public Validator(IPartStateWriter state, IAspectWrite aspect) {
            this.state = state;
            this.aspect = aspect;
        }

        @Override
        public void addError(L10NHelpers.UnlocalizedString error, boolean transientError) {
            this.state.addError(aspect, error, transientError);
        }

    }

}
