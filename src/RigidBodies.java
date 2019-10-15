//import jdk.nashorn.internal.objects.Global;
//import sun.security.provider.SHA;

import org.lwjgl.system.CallbackI;

import javax.xml.crypto.Data;
import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;
import java.util.Iterator;

import static java.lang.Math.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
//import static jdk.nashorn.internal.objects.Global.print;
//import static org.lwjgl.opengl.GL11.*;


public class RigidBodies {
    public String name;

    public static Vector2 mousePos;
    //private char color;
    private Color color;
    private Shape shape;
    private int vertecies;
    private int state;
    private int shape_type;
    private Vector2 position;
    private Vector2 newPosition;
    private Vector2 velocity;
    private Vector2 acceleration;
    private Vector2 pointOfRotation;
    private Vector2 rodDimensions;
    private Vector2 momentum;
    private double orientation;
    private double angularSpeed;
    private double angularAcceleration;
    private double netTorque = 0;

    private double size;
    private double force;
    private double inetria;
    private double apothem;
    private double sideLeangth;
    private double mass;
    private double bounceCoef;
    private boolean keyControl;
    private boolean thisContact;
    private boolean wall;
    private boolean planet;
    private boolean isRod;


    private static double wallCoefFriction = 5;
    private static double gravitationalC;
    private static double G = 6.67 * Math.pow(10f, -11f);
    private static double g = -.00000981;

    //-.00000981;
    //long window = glfwCreateWindow(Main.winSize, Main.winSize, "MyLWGL Program", 0, 0);
//size is the diameter
//contains things that have to do with the physics of the bodies
    public RigidBodies(int shape_type, double size, Vector2 position, Vector2 velocity, String name, float mass, Color color, double angularspeed, float orientation, boolean wall, boolean planet, boolean keyControl, double bounceCoef, boolean isRod, Vector2 rodDimensions, int state) {
        this.position = position;
        this.velocity = velocity;
        this.shape_type = shape_type;
        this.acceleration = new Vector2(0, 0);
        this.name = name;
        this.color = color;
        this.mass = mass;
        this.momentum = new Vector2(mass * velocity.x, mass * velocity.y);
        this.bounceCoef = bounceCoef;
        this.orientation = orientation;
        this.angularSpeed = angularspeed;
        this.angularAcceleration = angularAcceleration;
        this.pointOfRotation = position;
        this.size = size;
        this.wall = wall;
        this.state = state;
        this.isRod = isRod;
        this.rodDimensions = rodDimensions;
        this.planet = planet;
        this.sideLeangth = size * sin(180 / shape_type);
        this.apothem = (size * .5) * cos(180 / shape_type);
        if (isRod) {
            //gets the longest side of the rod as the length
            double length;
            if (Math.abs(rodDimensions.x) >= Math.abs(rodDimensions.y)) {
                length = Math.abs(2 * rodDimensions.x);
            } else {
                length = Math.abs(2 * rodDimensions.y);
            }
            inetria = (mass * length * length) / 12;
        } else {
            inetria = pow(18, -1) * pow(mass, 2) * (pow((size * .5), 3) - pow(apothem, 3)) * (2 * shape_type);
        }

        shape = new Shape(shape_type, position, size, color, orientation, isRod, rodDimensions);
        System.out.println(this.name + "'s inertia: " + this.inetria);
    }


    public void Update(RigidBodies thisBody, long window) {


        // System.out.println(thisBody.name + " "+thisBody.acceleration.Magnitude());
        //acceleration and velocity are mutiplied first bc it makes it easier to scale speeds and then by deltaT so that speeds can be in units per seconds
        //deltaT is the time between frames

        angularAcceleration = (netTorque / inetria) * 10000;
        angularSpeed += angularAcceleration * Main.deltaT;
        orientation += angularSpeed * Main.deltaT;
        velocity = velocity.Add(acceleration.Multiply(Main.deltaT * .0000012));
        position = position.Add(velocity.Multiply(Main.deltaT * 1));


        shape.Update(thisBody);
/*        Shape.DrawLine(new Vector2(1,0 ), new Vector2(-1, 0), new Color(1,1,1));
        Shape.DrawLine(new Vector2(0,1 ), new Vector2(0, -1), new Color(1,1,1));*/

        Gravity(thisBody, Main.inspace, Main.onPlanet);
        if (Main.onPlanet) {
            Gravity(thisBody, Main.inspace, Main.onPlanet);
            shape.rotate(angularSpeed, thisBody, position);
        }
        if (!Main.inspace) {
            shape.CheckWallCollision(velocity, position, thisBody);
            shape.triangleLines();


        } else {
            removeTooFarAway(thisBody);
            shape.CheckWallCollision(velocity, position, thisBody);
        }

        //Shape.DrawLine(position, position.Add(velocity.Multiply(200)), color);

        //ifClickInBody(window);
        shape.Draw();
    }

    //if an object gets too far away from the center of the screen it gets added to the list of objects that will
// be remover from the shapes list
    private void removeTooFarAway(RigidBodies thisBody) {
        if (position.DistanceFrom(new Vector2(0, 0)) > 1.5) {
            Main.Remove.add(thisBody);
        }
    }


    Vector2 rocketSpeed = new Vector2(0, 0);

    public static void setKeyControl(RigidBodies rocket1) {
        Vector2 rocketSpeed = new Vector2(1000, 1000);
        //control a rocket with arrow keys
        if (Main.onPlanet) {
            rocketSpeed = new Vector2(.0001, .001);
        } else {
            rocketSpeed = new Vector2(.0000001, .0000001);
        }
        if (Input.keys[GLFW_KEY_W]) {
            rocket1.setVelocity(new Vector2(rocket1.getVelocity().x, rocket1.getVelocity().y + rocketSpeed.y));
        }
        if (Input.keys[GLFW_KEY_S]) {
            rocket1.setVelocity(new Vector2(rocket1.getVelocity().x, rocket1.getVelocity().y - rocketSpeed.y));
        }
        if (Input.keys[GLFW_KEY_A]) {
            rocket1.setVelocity(new Vector2(rocket1.getVelocity().x - rocketSpeed.x, rocket1.getVelocity().y));
        }
        if (Input.keys[GLFW_KEY_D]) {
            rocket1.setVelocity(new Vector2(rocket1.getVelocity().x + rocketSpeed.x, rocket1.getVelocity().y));
        }
        if (Input.keys[GLFW_KEY_Q]) {
            rocket1.setAngularSpeed((rocket1.getAngularSpeed() + .00001));
        }
        if (Input.keys[GLFW_KEY_E]) {
            rocket1.setAngularSpeed((rocket1.getAngularSpeed() - .00001));
        }

    }


    public void Gravity(RigidBodies thisBody, boolean inSpace, boolean onPlanet) {
        if (inSpace) {
            //variables are created so that the sum of the acceleration due to gravity form all the planets can be added together
            //if i were to add the acceleration due to gravity directly to thisbodies acceleration the the acceleration would be accelerating
            double tempAx = 0;
            double tempAy = 0;
            for (RigidBodies body : Main.Shapes) {
                if (body != thisBody) {
                    if (position.DistanceFrom(body.position) > body.size) {
                        if (body.planet) {
                            Vector2 distanceFrom = new Vector2(position.x - body.position.x, position.y - body.position.y);
                            shape.checkPlanetCollision(thisBody, distanceFrom);
                            tempAx += cos(distanceFrom.Angle()) * (getGravitationalC(distanceFrom.Magnitude(), body.mass));
                            tempAy += sin(distanceFrom.Angle()) * (getGravitationalC(distanceFrom.Magnitude(), body.mass));
                        }
                    }
                }
            }

            acceleration.x = tempAx;
            acceleration.y = tempAy;
        }
        if (onPlanet) {

            if (thisBody.wall == false) {
                velocity.y += g;
            }
        }
    }


    public void rotateOnWallContact(RigidBodies thisBody, ArrayList<Vector2> touchingWall) {
        double alpha = 0;
        double theta = 0;
        double t = 0;
        for (Vector2 vertex : touchingWall) {
            theta = (vertex.AngleBetween(position));
            t = theta * (180 / PI);
            if (vertex.x > position.x) {
                netTorque -= (mass * g * (size / 2) * cos(theta));
            } else if (vertex.x < position.x) {
                netTorque += (mass * g * (size / 2) * cos(theta));
            }
        }

        // angularAcceleration = alpha*10000;
    }


    public void ifClickInBody(long window) {
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == 1) {
            if (shape.isInShape(Main.mousePos)) {
                System.out.println("yo");
                state = 1;
                setVelocity(new Vector2(0, 0));
                //setPosition(Main.mousePos);
                setVelocity(Main.mouseVelocity);


/*                pointOfRotation =Main.mousePos;
                double inertiaT = 0;
                double[] distanceToVertices = new double[shape_type];
                for (int i = 0; i < getVertices().length; i++) {
                    double newApothem = (size / 2) * cos(180 / shape_type);
                    distanceToVertices[i] = position.DistanceFrom(getVertices()[i]);
                    inertiaT += (pow(18, -1) * pow((mass / 2 * shape_type), 2) * (pow((distanceToVertices[i] / 2), 3) - pow(newApothem, 3))) * 2;
                }
                inetria = inertiaT;*/
            } else {
                state = 0;

               /* pointOfRotation =position;
                inetria = (pow(18, -1) * pow((mass / 2 * shape_type), 2) * (pow((size / 2), 3) - pow(apothem, 3))) * 2 * shape_type;*/
            }

        } else {


        }
    }


    //getters and setters
    public double getGravitationalC(double distanceFrom, double mass) {
        if (distanceFrom == 0) {
            distanceFrom = .0000000001;
        }
        return ((G * mass) / (Math.pow(distanceFrom * 400000, 2) * 1000)) * -1;
    }


    public Vector2 getMomentum() {

        return momentum;
    }

    public double getForce() {
        force = mass * acceleration.Magnitude();
        return force;
    }

    public void MoveTo(double x, double y) {

        position.x += x;
        position.y += y;
    }

    public double getOrientation() {
        return orientation;
    }

    public void setOrientation(double a) {
        orientation = a;
    }

    public double getAngularSpeed() {
        return angularSpeed;
    }

    public void setAngularSpeed(double a) {
        angularSpeed = a;
    }

    public double getMass() {
        return mass;
    }

    public double getAngularAcceleration() {
        return angularAcceleration;
    }

    public void setAngularAcceleration(double a) {
        angularAcceleration = a;
    }

    public double getNetTorque() {
        return netTorque;
    }

    public void setNetTorque(double a) {
        netTorque = a;
    }

    public boolean getKeyControl() {
        return keyControl;
    }

    public Vector2[] getVertices() {
        return shape.getShapeVertices();
    }

    public Triangle[] getTriangles() {
        return shape.getTriangles();
    }

    public static long getTime() {
        return System.currentTimeMillis();
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 a) {
        position = a;
    }

    public Vector2 getNewPosition() {
        return newPosition;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2 a) {
        velocity = a;
    }

    public boolean getWall() {
        return wall;
    }

    public boolean getIsWall() {
        return isRod;
    }

    public boolean isPlanet() {
        return planet;
    }

    public double getSize() {
        return size;
    }

    public int getShapeType() {
        return shape_type;
    }

    public double getBounceCoef() {
        return bounceCoef;
    }

    public void setBounceCoef(double a) {
        bounceCoef = a;
    }

    public Color getColor() {
        return color;
    }
}
