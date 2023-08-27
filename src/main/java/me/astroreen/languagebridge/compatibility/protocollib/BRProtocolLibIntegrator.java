package me.astroreen.languagebridge.compatibility.protocollib;

import me.astroreen.astrolibs.api.compatibility.Integrator;
import me.astroreen.astrolibs.exception.HookException;

public class BRProtocolLibIntegrator implements Integrator {
    @Override
    public void hook() throws HookException {
        ProtocolLibManager.setup();
    }

    @Override
    public void reload() {

    }

    @Override
    public void close() {
        ProtocolLibManager.disable();
    }
}
