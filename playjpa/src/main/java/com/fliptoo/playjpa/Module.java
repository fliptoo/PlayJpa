package com.fliptoo.playjpa;

import com.google.inject.AbstractModule;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(PlayJpa.class).toProvider(PlayJpa.Provider.class).asEagerSingleton();
    }
}

