package com.apr.main;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import com.jme3.water.SimpleWaterProcessor;

/**
 *
 * @author n.beaudrot
 */
public class World {

    private AssetManager assetManager;
    private Node rootNode;
    private BulletAppState bulletAppState;
    private ViewPort viewPort;
    
    private static final Sphere sphere;
    private static final Box floor;
    
    /** Prepare Materials */
    private Material stone_mat;
    private Material floor_mat;
    
    static {
        /** Initialize the cannon ball geometry */
        sphere = new Sphere(32, 32, 0.4f, true, false);
        sphere.setTextureMode(TextureMode.Projected);
        /** Initialize the floor geometry */
        floor = new Box(Vector3f.ZERO, 200f, 0.1f, 20f);
        floor.scaleTextureCoordinates(new Vector2f(3, 6));
    }
    
    public World(AssetManager assetManager, Node rootNode, BulletAppState bulletAppState, ViewPort viewPort) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.bulletAppState = bulletAppState;
        this.viewPort = viewPort;
        
        initMaterials();
    }
    
    /** Initialize the materials used in this scene. */
    private void initMaterials() {
        stone_mat = new Material(assetManager, "MatDefs/Misc/Unshaded.j3md");
        TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.png");
        key2.setGenerateMips(true);
        Texture tex2 = assetManager.loadTexture(key2);
        stone_mat.setTexture("ColorMap", tex2);

        floor_mat = new Material(assetManager, "MatDefs/Misc/Unshaded.j3md");
        TextureKey key3 = new TextureKey("Textures/Terrain/Road/Road.jpg");
        key3.setGenerateMips(true);
        Texture tex3 = assetManager.loadTexture(key3);
        tex3.setWrap(WrapMode.Repeat);
        floor_mat.setTexture("ColorMap", tex3);
        
    }

    /** 
     * Make a solid floor and add it to the scene.
     */
    public void createFloor() {
        Geometry floor_geo = new Geometry("Floor", floor);
        floor_geo.setMaterial(floor_mat);
        floor_geo.setLocalTranslation(0, -0.1f, 0);
        this.rootNode.attachChild(floor_geo);
        /* Make the floor physical with mass 0.0f! */
        RigidBodyControl floor_phy = new RigidBodyControl(0.0f);
        floor_geo.addControl(floor_phy);
        bulletAppState.getPhysicsSpace().add(floor_phy);
        
        Box ground = new Box(Vector3f.ZERO, 1000f, 0.1f, 500f);
        Geometry ground_geo = new Geometry("Ground", ground);
        RigidBodyControl ground_phy = new RigidBodyControl(0.0f);
        ground_geo.addControl(ground_phy);
        bulletAppState.getPhysicsSpace().add(ground_phy);
    }
    
    /**
     * Create a skybox
     */
    public void createSky() {
        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/skyboxsun5deg2.png", false));
        
    }
    
    /**
     * Make water
     */
    public void createWater() {
        SimpleWaterProcessor waterProcessor = new SimpleWaterProcessor(assetManager);
        waterProcessor.setReflectionScene(rootNode);
        waterProcessor.setDebug(false);
        waterProcessor.setRefractionClippingOffset(1.0f);

        //setting the water plane
        Vector3f waterLocation=new Vector3f(0,-20,0);
        waterProcessor.setPlane(new Plane(Vector3f.UNIT_Y, waterLocation.dot(Vector3f.UNIT_Y)));
        
        waterProcessor.setWaterColor(ColorRGBA.Brown);
        waterProcessor.setDebug(true);
        //lower render size for higher performance
//        waterProcessor.setRenderSize(128,128);
        //raise depth to see through water
//        waterProcessor.setWaterDepth(20);
        //lower the distortion scale if the waves appear too strong
//        waterProcessor.setDistortionScale(0.1f);
        //lower the speed of the waves if they are too fast
//        waterProcessor.setWaveSpeed(0.01f);

        Quad quad = new Quad(400,400);

        //the texture coordinates define the general size of the waves
        quad.scaleTextureCoordinates(new Vector2f(6f,6f));

        Geometry water=new Geometry("water", quad);
        water.setShadowMode(ShadowMode.Receive);
        water.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
        water.setMaterial(waterProcessor.getMaterial());
        //water.setLocalTranslation(-200, -20, 250);

        rootNode.attachChild(water);
        viewPort.addProcessor(waterProcessor);
    }
}
