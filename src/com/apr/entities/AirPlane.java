package com.apr.entities;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.input.controls.ActionListener;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import com.jme3.texture.Texture;

/**
 *
 * @author n.beaudrot
 */
public class AirPlane extends Node implements ActionListener, PhysicsCollisionListener {

    private AssetManager assetManager;
    private BulletAppState bulletAppState;
    private VehicleControl planeControl;
    
    private final float accelerationForce = -400.0f;
    private final float brakeForce = 100.0f;
    private float steeringValue = 0;
    private float accelerationValue = 0;
    
    /**
     * Constructor
     * 
     * @param assetManager
     * @param bulletAppState 
     */
    public AirPlane(final AssetManager assetManager, final BulletAppState bulletAppState) {
        this.assetManager = assetManager;
        this.bulletAppState = bulletAppState;
        //this.bulletAppState.getPhysicsSpace().addCollisionListener(this);
        this.setName("plane");
        
        createPlane();
        makeEngine();
    }

    @Override
    public void onAction(String name, boolean keyPressed, float tpf) {
        if (name.equals("jump") && !keyPressed) {

            planeControl.setLinearVelocity(new Vector3f(0, 5, 0));

        } else if (name.equals("moveF") && !keyPressed) {
            if (keyPressed) {
                accelerationValue += accelerationForce;
            } else {
                accelerationValue -= accelerationForce;
            }
            
            planeControl.accelerate(accelerationValue);
            Vector3f direction=new Vector3f(0,2,0);
            planeControl.getPhysicsRotation().multLocal(direction);
            planeControl.applyForce(direction, Vector3f.ZERO);

        } else if (name.equals("moveB") && !keyPressed) {
            if (keyPressed) {
                planeControl.brake(brakeForce);
            } else {
                planeControl.brake(0f);
            }
        } else if (name.equals("moveL") && !keyPressed) {
            if (keyPressed) {
                steeringValue += -.2f;
            } else {
                steeringValue += .2f;
            }
            planeControl.steer(steeringValue);
        } else if (name.equals("moveR") && !keyPressed) {
            if (keyPressed) {
                steeringValue += .2f;
            } else {
                steeringValue += -.2f;
            }
            planeControl.steer(steeringValue);
        }
    }
    
    public VehicleControl getPlaneControl() {
        return this.planeControl;
    }
    
    private void createPlane() {
        Spatial plane = assetManager.loadModel("Models/Airplane/MiG-21bis.obj");
        plane.setName("plane");
        Material mat_stl = new Material(assetManager, "MatDefs/Misc/Unshaded.j3md");
        Texture tex_ml = assetManager.loadTexture("Textures/Airplane/MiG-21bis.png");
        mat_stl.setTexture("ColorMap", tex_ml);
        plane.setMaterial(mat_stl);
        
        CollisionShape shape = CollisionShapeFactory.createDynamicMeshShape(plane);
        planeControl = new VehicleControl(shape, 400);
        this.addControl(planeControl);
        this.attachChild(plane);
        
        
        float stiffness = 60.0f;//200=f1 car
        float compValue = .3f; //(should be lower than damp)
        float dampValue = .4f;
        planeControl.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
        planeControl.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
        planeControl.setSuspensionStiffness(stiffness);
        planeControl.setMaxSuspensionForce(10000.0f);

        //Create four wheels and add them at their locations
        Material mat = new Material(assetManager, "MatDefs/Misc/Unshaded.j3md");
        mat.setTransparent(true);
        
        Vector3f wheelDirection = new Vector3f(0, -1, 0); // was 0, -1, 0
        Vector3f wheelAxle = new Vector3f(0, 0, -1); // was -1, 0, 0
        float radius = 0.3f;
        float restLength = 0.3f;
        float yOff = 0.5f;
        float xOff = 0.6f;
        float zOff = 1.5f;

        Cylinder wheelMesh = new Cylinder(16, 16, radius, radius * 0.6f, true);
        Cylinder wheelMeshBack = new Cylinder(16, 16, 0.4f, 0.4f * 0.6f, true);

        Node node1 = new Node("wheel 1 node");
        Geometry wheels1 = new Geometry("wheel 1", wheelMesh);
        node1.attachChild(wheels1);
        wheels1.rotate(0, FastMath.HALF_PI, 0);
        wheels1.setMaterial(mat);
        planeControl.addWheel(node1, new Vector3f(-4.2f, yOff, 0f),
                wheelDirection, wheelAxle, restLength, radius, true);

        Node node2 = new Node("wheel 2 node");
        Geometry wheels2 = new Geometry("wheel 2", wheelMeshBack);
        node2.attachChild(wheels2);
        wheels2.rotate(0, FastMath.HALF_PI, 0);
        wheels2.setMaterial(mat);
        planeControl.addWheel(node2, new Vector3f(xOff, 0.7f, zOff),
                wheelDirection, wheelAxle, restLength, 0.4f, false);

        Node node3 = new Node("wheel 3 node");
        Geometry wheels3 = new Geometry("wheel 3", wheelMeshBack);
        node3.attachChild(wheels3);
        wheels3.rotate(0, FastMath.HALF_PI, 0);
        wheels3.setMaterial(mat);
        planeControl.addWheel(node3, new Vector3f(xOff, 0.7f, -zOff),
                wheelDirection, wheelAxle, restLength, 0.4f, false);

        this.attachChild(node1);
        this.attachChild(node2);
        this.attachChild(node3);
        
        
        bulletAppState.getPhysicsSpace().add(planeControl);
    }

    private void makeEngine() {
        ParticleEmitter fire = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material mat_red = new Material(assetManager, "MatDefs/Misc/Particle.j3md");
        mat_red.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        fire.setMaterial(mat_red);
        fire.setImagesX(2);
        fire.setImagesY(2); // 2x2 texture animation
        fire.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f));   // red
        fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
        fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));

        fire.setStartSize(1.0f);
        fire.setEndSize(0.1f);
        fire.setGravity(-8, 0, 0);
        fire.setLowLife(0.5f);
        fire.setHighLife(1.2f);
        fire.getParticleInfluencer().setVelocityVariation(0.3f);
        fire.setLocalTranslation(6.4f, 1.2f, 0f);

        this.attachChild(fire);
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        if (event.getObjectA() instanceof RigidBodyControl) {
            System.out.println("Collision avec RigidBody");
        }
    }
    
    public void update() {
        
    }
    
//    public void makeCannonBall() {
//        /** Create a cannon ball geometry and attach to scene graph. */
//        Geometry ball_geo = new Geometry("cannon ball", sphere);
//        ball_geo.setMaterial(stone_mat);
//        rootNode.attachChild(ball_geo);
//        /** Position the cannon ball  */
//        ball_geo.setLocalTranslation(cam.getLocation());
//        /** Make the ball physcial with a mass > 0.0f */
//        RigidBodyControl ball_phy = new RigidBodyControl(1f);
//        /** Add physical ball to physics space. */
//        ball_geo.addControl(ball_phy);
//        bulletAppState.getPhysicsSpace().add(ball_phy);
//        /** Accelerate the physcial ball to shoot it. */
//        ball_phy.setLinearVelocity(cam.getDirection().mult(25));
//    }
}
