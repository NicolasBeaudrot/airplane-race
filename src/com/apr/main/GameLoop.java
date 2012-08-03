package com.apr.main;

import com.apr.entities.AirPlane;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;

/**
 *
 * @author n.beaudrot
 */
public class GameLoop extends SimpleApplication {

    /** Node for the plane **/
    private AirPlane planeNode;

    public GameLoop() {
        AppSettings localSettings = new AppSettings(true);
        localSettings.setTitle("Airplane Race");
        localSettings.setResolution(1024, 768);
        //settings.setFullscreen(true);
        localSettings.setRenderer(AppSettings.LWJGL_OPENGL_ANY);

        this.setSettings(localSettings);
        this.setShowSettings(false);
        this.setDisplayStatView(false);
        this.start();
    }

    @Override
    public void simpleInitApp() {
        /** Set up Physics Engine */
        BulletAppState bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);

        planeNode = new AirPlane(assetManager, bulletAppState);
        rootNode.attachChild(planeNode);

        initListener();
        initCrossHairs();
        setupChaseCamera();

        World world = new World(assetManager, rootNode, bulletAppState, viewPort);
        world.createFloor();
        world.createSky();
                
        Picture radar_img = new Picture("Radar Picture");
        radar_img.setImage(assetManager, "Interface/Radar.png", true);
        radar_img.setWidth(100f);
        radar_img.setHeight(100f);
        radar_img.setPosition(settings.getWidth() - 100, settings.getHeight() - 100);
        guiNode.attachChild(radar_img);

        //world.createWater();

        planeNode.setLocalTranslation(-200, 0, 0);
    }

    @Override
    public void simpleUpdate(float tpf) {
        planeNode.update();
    }

    protected void initListener() {
        //Plane Listener
        inputManager.addMapping("jump",
                new KeyTrigger(KeyInput.KEY_J));
        inputManager.addListener(planeNode, "jump");
        inputManager.addMapping("moveF",
                new KeyTrigger(KeyInput.KEY_Z));
        inputManager.addListener(planeNode, "moveF");
        inputManager.addMapping("moveB",
                new KeyTrigger(KeyInput.KEY_S));
        inputManager.addListener(planeNode, "moveB");
        inputManager.addMapping("moveL",
                new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addListener(planeNode, "moveL");
        inputManager.addMapping("moveR",
                new KeyTrigger(KeyInput.KEY_D));
        inputManager.addListener(planeNode, "moveR");
        inputManager.addMapping("fire", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addListener(planeNode, "fire");
    }

    protected void initCrossHairs() {
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+");        // fake crosshairs :)
        ch.setLocalTranslation( // center
                settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
                settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }

    private void setupChaseCamera() {
        // Disable the default flyby cam
        flyCam.setEnabled(false);

        ChaseCamera chaseCamera = new ChaseCamera(cam, planeNode, inputManager);
        //chaseCamera.setSmoothMotion(true);
    }
}
