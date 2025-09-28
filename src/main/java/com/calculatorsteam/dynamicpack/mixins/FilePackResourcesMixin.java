package com.calculatorsteam.dynamicpack.mixins;

import com.calculatorsteam.dynamicpack.accessor.FilePackResourcesAccessor;
import com.calculatorsteam.dynamicpack.util.LockUtils;
import net.minecraft.server.packs.FilePackResources;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.zip.ZipFile;

@Mixin(FilePackResources./*? if >=1.21 {*/SharedZipFileAccess./*?}*/class)
public abstract class FilePackResourcesMixin implements FilePackResourcesAccessor {

    @Unique private boolean dynamicpack$opened;

    @Shadow public abstract void close();

    @Shadow @Final
    //? if < 1.21
    /*private*/
    File file;

    @Inject(at = @At("RETURN"), method = "getOrCreateZipFile")
    public void dynamicpack$return$getOrCreateZipFile(CallbackInfoReturnable<ZipFile> cir) {
        ZipFile ret = cir.getReturnValue();
        if (ret != null) {
            dynamicpack$opened = true; // теперь действительно открыт
            LockUtils.addFileToOpened(this);
        }
    }

    @Inject(at = @At("RETURN"), method = "close")
    public void dynamicpack$return$close(CallbackInfo ci) {
        dynamicpack$opened = false;
    }

    @Override
    public boolean dynamicpack$isClosed() {
        return !dynamicpack$opened;
    }

    @Override
    public File dynamicpack$getFile() {
        return file;
    }

    @Override
    public void dynamicpack$close() {
        close();
    }
}