package com.brokenkeyboard.simplemusket.datagen;

import com.brokenkeyboard.simplemusket.SimpleMusket;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static com.brokenkeyboard.simplemusket.SimpleMusket.BULLET;

public class Tags extends DamageTypeTagsProvider {
    public Tags(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper fileHelper) {
        super(output, provider, SimpleMusket.MOD_ID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(DamageTypeTags.BYPASSES_ARMOR).add(BULLET);
        this.tag(DamageTypeTags.BYPASSES_SHIELD).add(BULLET);
        this.tag(DamageTypeTags.IS_PROJECTILE).add(BULLET);
    }
}