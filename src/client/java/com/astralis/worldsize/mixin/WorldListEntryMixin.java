package com.astralis.worldsize.mixin;

import com.astralis.worldsize.WorldSizeHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(net.minecraft.client.gui.screen.world.WorldListWidget.WorldEntry.class)
public abstract class WorldListEntryMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("world-size-display");

    @Shadow @Final private LevelSummary level;
    @Shadow @Final private net.minecraft.client.gui.screen.world.WorldListWidget parent;

    private String cachedSizeText = null;
    private boolean sizeCalculating = false;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderWorldSize(DrawContext context, int index, int y,
                                 boolean hovered, float tickDelta, CallbackInfo ci) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();

            boolean isSelected = (parent.getSelectedOrNull() == (Object) this);
            if (!hovered && !isSelected) return;

            if (cachedSizeText == null && !sizeCalculating) {
                sizeCalculating = true;
                CompletableFuture.runAsync(() -> {
                    try {
                        Path savesDir = client.getLevelStorage().getSavesDirectory();
                        Path worldDir = savesDir.resolve(level.getName());
                        long size = WorldSizeHelper.getFolderSize(worldDir);
                        cachedSizeText = "   " + WorldSizeHelper.formatSize(size);
                    } catch (Exception e) {
                        LOGGER.error("WorldSize size calculation error", e);
                        cachedSizeText = "   ?";
                    }
                });
            }

            if (cachedSizeText == null) return;

            List<?> list = parent.children();
            int realIndex = list.indexOf(this);
            if (realIndex == -1) return;

            int realY = parent.getRowTop(realIndex);
            int realX = parent.getRowLeft();
            int nameWidth = client.textRenderer.getWidth(level.getDisplayName());

            context.drawTextWithShadow(
                    client.textRenderer,
                    Text.literal(cachedSizeText),
                    realX + 32 + nameWidth,
                    realY + 3,
                    0xFF888888
            );

        } catch (Exception e) {
            LOGGER.error("WorldSize mixin error", e);
        }
    }
}