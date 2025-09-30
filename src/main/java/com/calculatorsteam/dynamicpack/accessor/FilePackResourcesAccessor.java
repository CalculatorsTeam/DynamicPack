package com.calculatorsteam.dynamicpack.accessor;

import java.io.File;

public interface FilePackResourcesAccessor {
    boolean dynamicpack$isClosed();
    File dynamicpack$getFile();
    void dynamicpack$close();
}