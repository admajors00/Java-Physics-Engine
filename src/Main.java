//package sample;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;


import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
/*
* initialize shapes positions by iterating through an arrray ag get start pos
* then draw the shapes
*
* graphics loop starts
* then call get new pos
* then check collision
* then draw
*
*
* */
import static java.lang.Math.PI;
import static java.lang.Math.pow;
import static java.lang.Math.random;
import static java.lang.Thread.sleep;
import static org.lwjgl.glfw.GLFW.*;
//gives functions
import static org.lwjgl.opengl.GL11.*;


public class Main {
    public static boolean inspace = false;
    public static boolean onPlanet = true;
    public static boolean controlShip = true;
    public static boolean mousecontroll = true;

    public static int winSize = 700;
    public static ArrayList<RigidBodies> Remove;
    public static ArrayList<RigidBodies> Que;
    public static ArrayList<RigidBodies> Shapes;
    public static double startTime = System.currentTimeMillis();
    public static double rads = PI / 180;
    public static double time1 = Main.startTime;
    public static double time2 = getTime()-Main.startTime;
    public static double deltaT;
    public static double getTime(){
        return System.currentTimeMillis();
    }
    public static Vector2 mousePos;
    public static Vector2 mouseVelocity;
    static int i = 0;
    private static GLFWKeyCallback keyCallback;
    private static GLFWCursorPosCallback mouseCallback;

    public static void setMousePos(double a, double b){
        mousePos = new Vector2(a,b);
    }
    public static void setMouseVelocity(double a, double b){
        mouseVelocity = new Vector2(a,b).Multiply(deltaT*110 );
    }

    public static void main(String[] args) throws InterruptedException {




//glfwInit returns an intiger 1 if it is sucseful and 0 if not
        if (!glfwInit()) {
            throw new IllegalStateException("Failed to init GLFW!");
        }

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

//creates window object
//moniter controllls if winow is full scren
        long window = glfwCreateWindow(winSize, winSize, "MyLWGL Program", 0, 0);
        if (window == 0) {
            throw new IllegalStateException("Failed to create window!");
        }

        glfwSetKeyCallback(window, keyCallback = new Input());
        glfwSetCursorPosCallback(window, mouseCallback = new MouseHandler());

//says hey I need a context so I can display graphics
//creates context opens up the graphics card and alloos open gl to draw to it
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

//centers/shows window
// glfwSetWindowPos(window, (videoMode.width()+10), (videoMode.height()+10));
        glfwShowWindow(window);

        //make list of shapes
        //name shapes list or rigidbodies list
        Shapes = new ArrayList<RigidBodies>();
        Que = new ArrayList<RigidBodies>();
        Remove = new ArrayList<RigidBodies>();
//size is diameter
//generate bodies at random
        /*for(int i = 0; i<10;i++) {
            Shapes.add( new RigidBodies(4, .01, new Vector2((random()*2)-1, (random()*2)-1)
                    , Vector2.CreateVector2(((random()*2)-1)*(360) * rads, ((random()*2)-1)*.005), Integer.toString(i),
                    500 , new Color(1, 1, 1), 0, -20, true, true, false, .0, false, null, 0));
        }*/
//uncomment region for 3 shapes
        if(!inspace) {
            RigidBodies Shape1 = new RigidBodies(4, .01, new Vector2(-.95, -.75),
                    Vector2.CreateVector2(30 * rads, .005), "Yellow",
                    500, new Color(1, 1, 0), 0.000, 80, false, true, true, .99, false, new Vector2(.5, .1), 0);


            RigidBodies Shape2 = new RigidBodies(4, .1, new Vector2(.95, -.75),
                    Vector2.CreateVector2(150 * rads, .005), "Green",
                    500, new Color(0, 1, 0), .00, 45, false, true, true, .8, false, null, 0);

            RigidBodies Shape3 = new RigidBodies(200, .1, new Vector2(.75, .5),
                    Vector2.CreateVector2(180 * rads, .001), "Blue",
                    500, new Color(0, 0, 1), 0.00, -20, false, true, true, .81, false, null, 0);


            RigidBodies mouse = new RigidBodies(12, .1, new Vector2(.5, .5),
                    Vector2.CreateVector2(180 * rads, 00.001), "Blue",
                    500, new Color(0, 0, 1), 0.00, 5, false, true, true, .81, false, null, 0);
            Shape.DrawLine(new Vector2(0,0), new Vector2(1,0), new Color(0,0,1));
            Shapes.add(mouse);
            Shapes.add(Shape1);
            Shapes.add(Shape2);
            Shapes.add(Shape3);
       }

//uncomment reagion for planets and spaceship,
// to control the spaceship the line 'RigidBodies.setKeyControl(rocket1);'
// must also be uncommented which is inside the while loop towawrds the bottom of this class
//if you want things to be able to fly off the screen 'shape.CheckWallCollision(velocity, position, thisBody);'
//in the RigidBodies class must be commented out
        RigidBodies rocket1 = new RigidBodies(4, .005, new Vector2(-.80, .8),
            Vector2.CreateVector2(0 * rads, .0), "Red",
            500f, new Color(1, 0, 0), 0, 0, false, false, true, .9, false, null, 0);



        if(inspace && !onPlanet) {
           
            RigidBodies planet1 = new RigidBodies(20, .015925, new Vector2(0, 0),
                    Vector2.CreateVector2(0 * rads, .00), "Green",
                    5.927f * (float) Math.pow(10, 24), new Color(0, 1, 0), 0, 0, true, true, false, .1, false, null, 0);

            RigidBodies planet2 = new RigidBodies(4, .0043425, new Vector2(-.0, .961005),
                    Vector2.CreateVector2(0 * rads, .00141000 * .14), "White",
                    7.3477f * (float) Math.pow(10, 22), new Color(1, 1, 1), 0, 0, true, true, false, .1, false, null, 0);
            Shapes.add(planet2);
            Shapes.add(planet1);
            Shapes.add(rocket1);
        }
        if(inspace){
            RigidBodies blackhole = new RigidBodies(6, .05, new Vector2(0, 0),
                    Vector2.CreateVector2(0 * rads, 0), "Blackhole",
                    7.3477f * (float) Math.pow(10, 25), new Color(1, 1, 1), 0, 0, false, true, false, .1, false, null, 0);
            //Shapes.add(blackhole);
        }


// Simulate Arrow shot downhill at an angle parralel to the ground vs arrow shot straight forward parralell to the ground
/*        RigidBodies circ2 = new RigidBodies(10, .01, new Vector2(1,.7282),
                Vector2.CreateVector2(40 * rads, -.08), "Blue", 5, new Color(0,0,1), 0, 0, false, false, false);
        Shapes.add(circ2);

        RigidBodies rect3 = new RigidBodies(2, 1.305, new Vector2(0,-.1609),
                Vector2.CreateVector2(0 * rads, .00), "Blue",
                 5, new Color(1,1,1), 0, 40, true, false, false);
        Shapes.add(rect3);

        RigidBodies circ1 = new RigidBodies(10, .01, new Vector2(1,0.65),
                Vector2.CreateVector2(0 * rads, -.08), "Red",
                 5, new Color(1,0,0), 0, 0, false, false, false);
        Shapes.add(circ1);

        RigidBodies rect5 = new RigidBodies(2, 1, new Vector2(.0,.6),
                Vector2.CreateVector2(0 * rads, .00), "Blue",
                 5, new Color(1,1,1), 0, 0, true, false, false);
        Shapes.add(rect5);*/
//to draw a line at 20 degrees from the bottom corner to the opposite side (0,-0.6306)




        //keeps window from instantly closing
        while (!glfwWindowShouldClose(window)) {
            //deta time is the time between each frame
            time2=System.currentTimeMillis();
            deltaT = (time2-time1)/1000;

            time1 = time2;

            if(inspace||controlShip){
                RigidBodies.setKeyControl(rocket1);
            }


//            System.out.println(mousePos.x + " " + mousePos.y);
            //continuously update untill window wants to close

            glfwPollEvents();
            glClear(GL_COLOR_BUFFER_BIT);
            if (!(Shapes.size() >= 200)) {
                if (mousecontroll) {
                    if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == 1) {
                        System.out.println(mouseVelocity + " -- " + mouseVelocity.Angle());
                        i++;
                        if (i > 16) {
                            i=0;
                            Shapes.add(new RigidBodies(3, .01, new Vector2(mousePos.x, mousePos.y)
                                    , Vector2.CreateVector2(mouseVelocity.Angle(), mouseVelocity.Magnitude()*.01), "white",
                                    500 * (float) Math.pow(10, 20), new Color(1, 1, 1), 0, -20, false, true, false, .80, false, null, 0));
                        }
                    }
                }
            }


            glEnd();
            sleep(1);
            //for each shape in the rigid bodies list
            for (RigidBodies thisBody : Shapes) {
                thisBody.Update(thisBody, window);
            }

            Shapes.addAll(Que);
            Que.clear();
            Shapes.removeAll(Remove);
            Remove.clear();
            glfwSwapBuffers(window);
        }
        //closes out glfw
        glfwTerminate();
    }

}
