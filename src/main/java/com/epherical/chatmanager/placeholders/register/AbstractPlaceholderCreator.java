package com.epherical.chatmanager.placeholders.register;

import net.minecraft.resources.ResourceLocation;

public abstract class AbstractPlaceholderCreator {

    private final String id;


    public AbstractPlaceholderCreator(String id) {
        this.id = id;
        registerDefaults();
    }



    protected abstract void registerDefaults();



    public ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(id, path);
    }

}
