/*
 * PriceBoardApp.java
 */

package com.yakuc.priceboard;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.LocalStorage;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class PriceBoardApp extends SingleFrameApplication {

    private PriceBoardProperty priceBoardProperty;
    
    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        priceBoardProperty = new PriceBoardProperty();
        show(new PriceBoardView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of PriceBoardApp
     */
    public static PriceBoardApp getApplication() {
        return Application.getInstance(PriceBoardApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(PriceBoardApp.class, args);
    }

    public PriceBoardProperty getPriceBoardProperty() {
        return this.priceBoardProperty;
    }
 
    /**
     * 設定データをファイルに設定する
     */
    public void writePropertyData() {
    }

    public void testLocalStorage() {
        LocalStorage ls = getContext().getLocalStorage();
        try {
            ls.save(this.priceBoardProperty, "test.xml");
        } catch (IOException ex) {
            Logger.getLogger(PriceBoardApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
