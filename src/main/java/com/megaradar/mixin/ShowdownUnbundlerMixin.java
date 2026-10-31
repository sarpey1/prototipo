package com.megaradar.mixin;

import com.megaradar.compat.ShowdownMegaPatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Alla fine dell'estrazione del motore Showdown di Cobblemon (prima che venga caricato). */
@Mixin(targets = "com.cobblemon.mod.common.battles.runner.graal.GraalShowdownUnbundler", remap = false, priority = 2000)
public abstract class ShowdownUnbundlerMixin {
    @Inject(method = "attemptUnbundle", at = @At("TAIL"), remap = false, require = 0)
    private void megaradar$afterUnbundle(CallbackInfo ci) {
        ShowdownMegaPatch.applyNow("dopo l'estrazione di Cobblemon");
    }
}
