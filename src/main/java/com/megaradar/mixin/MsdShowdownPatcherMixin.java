package com.megaradar.mixin;

import com.megaradar.compat.ShowdownMegaPatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Subito dopo che Mega Showdown ha (ri)scritto i file del motore Showdown, riapplica la modifica
 * delle Mega illimitate. Target per nome: se Mega Showdown non c'è, il mixin semplicemente non si applica.
 */
@Mixin(targets = "com.github.yajatkaul.mega_showdown.utils.ShowdownPatcher", remap = false)
public abstract class MsdShowdownPatcherMixin {
    @Inject(method = "patch", at = @At("RETURN"), remap = false, require = 0)
    private static void megaradar$afterMegaShowdownPatch(CallbackInfo ci) {
        ShowdownMegaPatch.applyNow("dopo Mega Showdown");
    }
}
