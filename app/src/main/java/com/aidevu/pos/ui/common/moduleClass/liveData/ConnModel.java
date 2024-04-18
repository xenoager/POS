package com.aidevu.pos.ui.common.moduleClass.liveData;

public class ConnModel {

    private int type;
    private boolean isConnected;

    public ConnModel(int type, boolean isConnected) {
        this.type = type;
        this.isConnected = isConnected;
    }

    public int getType() {
        return type;
    }

    public boolean getIsConnected() {
        return isConnected;
    }

}
