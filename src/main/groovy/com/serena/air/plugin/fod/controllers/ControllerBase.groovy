package com.serena.air.plugin.fod.controllers

import com.serena.air.plugin.fod.FodApi;

class ControllerBase {
    public FodApi api;

    /**
     * Base constructor for all api controllers
     * @param api api object (containing client etc.) of controller
     */
    ControllerBase(FodApi api) { this.api = api; }
}
