package dev.quantumfusion.dashloader.def.data;


import dev.quantumfusion.dashloader.core.api.DashObject;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.def.mixin.accessor.ModelIdentifierAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataFixedArraySize;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

@Data
@DashObject(ModelIdentifier.class)
public class DashModelIdentifier implements DashIdentifierInterface {
	public final String @DataFixedArraySize(3) [] strings;

	public DashModelIdentifier(String[] strings) {
		this.strings = strings;
	}

	public DashModelIdentifier(ModelIdentifier identifier) {
		strings = new String[3];
		strings[0] = identifier.getNamespace();
		strings[1] = identifier.getPath();
		strings[2] = identifier.getVariant();
	}

	@Override
	public Identifier export(RegistryReader exportHandler) {
		return ModelIdentifierAccessor.init(strings);
	}
}
