package me.wolfyscript.utilities.compatibility.plugins.executableblocks;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.ssomar.score.api.executableblocks.config.ExecutableBlocksManagerInterface;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import me.wolfyscript.utilities.api.inventory.custom_items.references.APIReference;
import me.wolfyscript.utilities.compatibility.plugins.ExecutableBlocksIntegration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ExecutableBlocksRef extends APIReference {

    private final ExecutableBlocksIntegration integration;
    private final ExecutableBlocksManagerInterface manager;
    private final String id;

    public ExecutableBlocksRef(ExecutableBlocksIntegration integration, ExecutableBlocksManagerInterface manager, String id) {
        this.id = id;
        this.manager = manager;
        this.integration = integration;
    }

    private ExecutableBlocksRef(ExecutableBlocksRef other) {
        this.id = other.id;
        this.manager = other.manager;
        this.integration = other.integration;
    }

    @Override
    public ItemStack getLinkedItem() {
        return manager.getExecutableBlock(id).map(eb -> eb.buildItem(amount, Optional.empty())).orElseGet(() -> new ItemStack(Material.AIR));
    }

    @Override
    public boolean isValidItem(ItemStack itemStack) {
        return integration.getExecutableBlock(itemStack).map(eB -> eB.equals(id)).orElse(false);
    }

    @Override
    public void serialize(JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStringField(ExecutableBlocksIntegration.PLUGIN_NAME.toLowerCase(Locale.ROOT), id);
    }

    @Override
    public APIReference clone() {
        return new ExecutableBlocksRef(this);
    }

    public static class Parser extends PluginParser<ExecutableBlocksRef> {

        private final ExecutableBlocksIntegration integration;
        private final ExecutableBlocksManagerInterface manager;

        public Parser(ExecutableBlocksIntegration integration, ExecutableBlocksManagerInterface manager) {
            super(ExecutableBlocksIntegration.PLUGIN_NAME, ExecutableBlocksIntegration.PLUGIN_NAME.toLowerCase(Locale.ROOT), 1000);
            this.integration = integration;
            this.manager = manager;
        }

        @Override
        public @Nullable ExecutableBlocksRef construct(ItemStack itemStack) {
            return integration.getExecutableBlock(itemStack).map(ebID -> new ExecutableBlocksRef(integration, manager, ebID)).orElse(null);
        }

        @Override
        public @Nullable ExecutableBlocksRef parse(JsonNode element) {
            return new ExecutableBlocksRef(integration, manager, element.asText());
        }
    }

}
