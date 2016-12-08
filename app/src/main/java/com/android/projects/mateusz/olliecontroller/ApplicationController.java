package com.android.projects.mateusz.olliecontroller;

import com.android.projects.mateusz.olliecontroller.stateEnums.TCPStates;

/**
 * Created by Mateusz on 22.11.2016.
 */

public final class ApplicationController {
    private static TCPStates clientState;


    public static TCPStates getClientState() {
        return clientState;
    }

    public static void setClientState(TCPStates clientState) {
        ApplicationController.clientState = clientState;
    }
}
