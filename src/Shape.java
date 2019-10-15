import org.lwjgl.system.CallbackI;

import java.beans.VetoableChangeListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Vector;

import static java.lang.Math.*;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.opengl.GL11.*;

public class Shape {
    long window = glfwCreateWindow(640, 640, "MyLWGL Program", 0, 0);
    private Triangle[] triangles;
    private Vector2[] vertices;
    private Vector2[] edges;
    private Vector2 position;
    private Vector2 pushVector;
    private Vector2 surfaceSlope;
    private Vector2 pointOfRotation;
    private Vector2 pointOfCollision;
    private double size;
    private double[] rotMatrix;
    private int numVertices;
    private int i =0;

    public static double rads = PI / 180;
    private boolean contact;
    private RigidBodies contactBody;
    private Color color;


//contains things that have to do with the geometry of the shape
    public Shape(int shape, Vector2 position, double size, Color color, float orientation, boolean isRod, Vector2 rodDimensions) {
        this.position = position;
        this.size = size;
        this.color = color;

        //ArrayList<Shape> shapes = new ArrayList<Shape>();
        numVertices = shape;
        vertices = new Vector2[numVertices];
        triangles = new Triangle[shape];
        if(!isRod) {
            for (int i = 0; i < vertices.length; i++) {
                double theta = (i * (360 / shape) * (PI / 180));
                vertices[i] = new Vector2(position.x + (size * cos(theta + (orientation * rads))), position.y + (size * sin(theta + (orientation * rads))));
            }
        }else{
            vertices[0] = new Vector2(position.x + (rodDimensions.x/2),position.y + (rodDimensions.y/2));
            vertices[1] = new Vector2(position.x - (rodDimensions.x/2),position.y + (rodDimensions.y/2));
            vertices[2] = new Vector2(position.x - (rodDimensions.x/2),position.y - (rodDimensions.y/2));
            vertices[3] = new Vector2(position.x + (rodDimensions.x/2),position.y - (rodDimensions.y/2));

        }
        for(int i = 0; i <= shape; i++){
            if(i == shape-1){
                triangles[i] = new Triangle(position, vertices[i], vertices[0]);
            }else if(i<shape){
                triangles[i] = new Triangle(position, vertices[i], vertices[i+1]);
            }
        }

    }




    public void Update(RigidBodies thisBody) {

        //for every triangle in thisbody Check if a point from any other body is inside thisBody's triangle
        for (int T = 0; T < triangles.length; T++) {
            for (RigidBodies body : Main.Shapes) {
                if (body != thisBody) {
                    contactBody = body;
                    for (int V = 0; V < body.getVertices().length; V++) {
                        //check for contact with any other bodies
                        Contact(T, V, thisBody, body);
                        if (contact) {
                            //if there is contact handle the collisions
                            handleCollision(thisBody);
                            //contact = false;
                        }
                    }
                }
            }
        }
        //for every Vertex of this body check if thisBodies vertex is inside another triangle
        for (int V = 0; V < vertices.length; V++){
            for(RigidBodies body : Main.Shapes) {
                if (body != thisBody) {
                    contactBody = body;
                    for (int T = 0; T < body.getTriangles().length; T++)
                        Contact(T, V, body, thisBody);
                    if (contact) {
                        //if there is contact handle the collisions
                        handleCollision(thisBody);
                        //contact = false;
                    }
                }
            }
        }
        //move shape adds the push vector and change and position to all the vertices and triangles
        moveShape(thisBody);
    }


    public void Contact(int T, int V, RigidBodies thisBody, RigidBodies body) {
        //want to check every shape except the shape your on
        Vector2 checkVertex;
        Triangle checkTri;
        double d00, d01, d02, d11, d12;
        double denom, u, v;

        //first loop
        // checks for if any shapes have hit you here
        //checks every vertice of every other shape if there is a vertex inside thisBodies's triangle

        checkVertex = body.getVertices()[V];
        checkTri = thisBody.getTriangles()[T];

        Vector2 v0 = checkTri.c.Subtract(checkTri.a);
        Vector2 v1 = checkTri.b.Subtract(checkTri.a);
        Vector2 v2 = checkVertex.Subtract(checkTri.a);

        d00 = v0.DotProduct(v0);
        d01 = v0.DotProduct(v1);
        d02 = v0.DotProduct(v2);
        d11 = v1.DotProduct(v1);
        d12 = v1.DotProduct(v2);
        denom = (d00 * d11) - (d01 * d01);
        u = ((d11 * d02) - (d01 * d12)) / denom;
        v = ((d00 * d12) - (d01 * d02)) / denom;
        contact = ((u > 0) && (v > 0) && (u + v < 1));

        if (contact) {
            //T is the triangle int this body that is being checked
            //check vertex is the vertex of another shape to see if the vertex is inside our triangle t
            setPushVector(thisBody, body, checkTri, checkVertex);
        }
    }



    public void setPushVector(RigidBodies thisBody, RigidBodies body, Triangle checkTri, Vector2 checkVertex){
        //the push vector method finds out how far a vertex has penetrated a triangle the calculates how far the vertex must be moved to get the vertex out of the triangle
        //thisbody can be thought of as the shape that you are ,and body can be though of any other shape

        //what is supposed to happen to find the push vector is
        //that you find an equation for the line that the checkVertex is moving along
        //and an equation for the line that the surface of the body that is hit
        //set the two equations equal to each other and solve for x, which is the x
        //coord that the two lines intesect at. Plug that x value back into the
        //equation for either line to get the y coord of intersection.
        //Then the position of the checkVertex and subtract the coords of the
        //intersection and that is the push vector, which will be added to position of
        //the shape that is inside the other

        //problems are that what happens when the two shapes are intersecting each other
        //list more here if you find any
        double m1, m2, y1, y2, x, y, x1, x2;
        double tri0;
/*        Drawpoint(checkVertex.x, checkVertex.y, body.getColor());
        DrawLine(checkVertex, checkVertex.Add(body.getVelocity().Multiply(100)), body.getColor());
        DrawLine(checkTri.c, checkTri.b,thisBody.getColor());*/
        //prevents program from dividing by zero
        if((body.getVelocity().x)==0){
            body.getVelocity().x += .0000001;
        }
        if(checkTri.c.x-checkTri.b.x == 0){
            tri0 = 0.00001;
        }else {
            tri0 = checkTri.c.x - checkTri.b.x;

        }
        //eq of a line y-y1 = m1(x-x1)
        //                y = m1(x-x1)+y1

        //(x,y) and (x1,y1) are coords for a point on the line
        //gets slope of the line that the shape is moving along
        m1 = ( body.getVelocity().y)/( body.getVelocity().y);
        //gets slope of the side of the shape that was contacted
        m2 = (checkTri.c.y-checkTri.b.y )/(tri0);

        y1 = checkVertex.y;
        x1 = checkVertex.x;
        y2 = checkTri.c.y;
        x2 = checkTri.c.x;

        //Calculates x-value for point of intersection
        x=(((m1*x1)-y1-(m2*x2)+y2))/(m1-m2);
        //plugs the x value back into the equation for the line to get the y coord
        y = (m2*(x-x2))+y2;
        pointOfCollision = new Vector2(x, y);
        pushVector = new Vector2(checkVertex.x -x, -y+checkVertex.y);
        surfaceSlope = new Vector2(checkTri.b.x-checkTri.c.x , checkTri.b.y-checkTri.c.y );
        //Drawpoint(x, y, thisBody.getColor());

    }


    public void handleCollision(RigidBodies thisBody){
        Vector2 pushVectorAgregator;
        double surfaceAngle;
        double surfaceOrthAngle;
        double movementAngleCB = 0;
        float bounceAngleCB;
       //this is where the pushvector is added to the position
       //when commented out it appears to work okay at first
       if(!thisBody.getWall()) {
            for (Vector2 vertex : vertices) {
                vertex.x += pushVector.x;
                vertex.y += pushVector.y;
            }
        }
        thisBody.MoveTo(pushVector.x, pushVector.y);

        surfaceAngle = surfaceSlope.Angle()*(180/PI);
        surfaceOrthAngle = surfaceAngle+90;
        movementAngleCB = contactBody.getVelocity().Angle()*(180/PI);
        bounceAngleCB = (float)((2*surfaceOrthAngle)-movementAngleCB);
        if(!thisBody.getWall()) {
            thisBody.setVelocity(thisBody.getVelocity().Add(contactBody.getVelocity()).Multiply(thisBody.getBounceCoef()));
           // contactBody.setVelocity(new Vector2(0,0));
        //thisBody.setVelocity(contactBody.getMomentum().Add(thisBody.getMomentum()).Divide(thisBody.getMass()+contactBody.getMass()).Subtract(contactBody.getVelocity()));
        //contactBody.setVelocity(contactBody.getMomentum().Add(thisBody.getMomentum()).Divide(thisBody.getMass()+contactBody.getMass()).Subtract(thisBody.getVelocity()));

        }
       if(!contactBody.getWall()) {

           contactBody.setVelocity(new Vector2(contactBody.getVelocity().Magnitude() * cos(bounceAngleCB), contactBody.getVelocity().Magnitude() * sin(bounceAngleCB)));
       }
    }


    public void moveShape(RigidBodies thisBody){
        float totalX = 0;
        float totalY = 0;
        for(Vector2 vertex: vertices) {
            vertex.x += thisBody.getVelocity().x;
            vertex.y += thisBody.getVelocity().y;
        }
        //moves center keeps from jumping outside of the shape

        for (Vector2 vertex: vertices){
            totalX += vertex.x;
            totalY += vertex.y;
        }
        double centX = totalX/numVertices;
        double centY = totalY/numVertices;
        thisBody.setPosition(new Vector2(centX, centY));
        for (Triangle triangle: triangles){
            triangle.a.x = centX;
            triangle.a.y = centY;
        }
    }


    public void rotate(double angularSpeed, RigidBodies body, Vector2 POR){
        //  newVertMatrix = rotMatrix(Vertices-porMatrix)+porMatrix
        rotMatrix = new double[4];
        rotMatrix[0] =  cos(angularSpeed);
        rotMatrix[1] =  sin(angularSpeed) * -1;
        rotMatrix[2] =  sin(angularSpeed);
        rotMatrix[3] =  cos(angularSpeed);
/*      rotMatrix = |0: cos(), 1:-sin()|
                    |2: sin(), 3: cos()|*/
        pointOfRotation = POR;
/*        porMatrix = new Vector2[numVertices];
        for(Vector2 por : porMatrix){
            por = position;
        }*/

        for(Vector2 vertex: vertices) {
            vertex.x -= pointOfRotation.x;
            vertex.y -= pointOfRotation.y;
        }
        for (Vector2 vertex: vertices){
            double temp = (rotMatrix[0]*vertex.x)+(rotMatrix[1]*vertex.y);
            vertex.y = (rotMatrix[2]*vertex.x)+(rotMatrix[3]*vertex.y);
            vertex.x = temp;
        }
        for(Vector2 vertex: vertices){
            vertex.x += pointOfRotation.x;
            vertex.y += pointOfRotation.y;
        }
    }


    public void checkPlanetCollision(RigidBodies thisBody, Vector2 distanceFrom){

        for(RigidBodies body: Main.Shapes){
            if(body.isPlanet()){
                if(body!=thisBody) {
                    if (distanceFrom.Magnitude() <= abs(body.getSize())) {

                        thisBody.MoveTo(0, body.getSize());
                    }
                }
            }
        }
    }


    public void CheckWallCollision(Vector2 velocity, Vector2 posit, RigidBodies thisBody) {
        ArrayList<Vector2> touchingWall = new ArrayList<Vector2>();
        Vector2 push = new Vector2(0, 0);
        Vector2 pushMaxFound = new Vector2(0,0);
        boolean foundVert = false;
        boolean hitLeft = false;
        boolean hitRight = false;
        boolean hitBottom = false;
        boolean hitTop = false;

        //checks to see if the coords of any vertex are greater than the boundries of the screen
        for (Vector2 vertex : vertices) {
            if (vertex.x > 1) {
                // velocity.x = velocity.x * -1;
                push = new Vector2(vertex.x - 1, 0);
                //  System.out.println(body.name + " : left : "+i + " : "+push.x + " ; " + push.y);
                foundVert = true;
                hitRight = true;
            }
            if (vertex.x < -1) {
                // velocity.x = velocity.x * -1;
                push = new Vector2(vertex.x + 1, 0);
                //  System.out.println(body.name + " : Right : "+i + " : "+push.x + " ; " + push.y);
                foundVert = true;
                hitLeft = true;

            }
            if (vertex.y < -1) {
                // velocity.y = velocity.y * -1;
                push = new Vector2(0, vertex.y + 1);
                //  System.out.println(body.name + " : bottom : "+ i + " : "+push.x + " ; " + push.y);
                foundVert = true;
                hitBottom = true;
            }
            if (vertex.y > 1) {
                // velocity.y = velocity.y * -1;
                push = new Vector2(0, vertex.y - 1);
                //System.out.println(body.name + " : Top : "+i + " : "+push.x + " ; " + push.y);
                foundVert = true;
                hitTop = true;

            }
            //finds which vertex is the farthes outside the boundries
            if (Math.abs(push.x) > Math.abs(pushMaxFound.x) || Math.abs(push.y) > Math.abs(pushMaxFound.y)) {
                pushMaxFound = push;
                touchingWall.add(vertex);
            }

        }

        if (thisBody.getShapeType() < 10 && hitBottom){
            thisBody.rotateOnWallContact(thisBody, touchingWall);
        }else{
            thisBody.setNetTorque(0);
        }

        if (foundVert) {
            // System.out.println(body.name+" :  pushMaxFound : "+ pushMaxFound.x+" : "+pushMaxFound.y);
            for (Vector2 vertex : vertices) {
                vertex.x -= pushMaxFound.x;
                vertex.y -= pushMaxFound.y;
            }
            thisBody.MoveTo(pushMaxFound.x*-1, pushMaxFound.y*-1);

            for (Triangle triangle : triangles) {
                triangle.a = posit.Subtract(pushMaxFound);
            }
            if ((hitTop && hitBottom) || (hitLeft && hitBottom) || (hitRight && hitBottom) || (hitLeft && hitTop) || (hitRight && hitTop)) {
                velocity.y *= -1*thisBody.getBounceCoef();
                velocity.x *= -1*thisBody.getBounceCoef();
            }else if(hitTop) {
                //foundVert = false;
                velocity.y *= -1*thisBody.getBounceCoef();
            }else if(hitBottom){
                velocity.y *= -1*thisBody.getBounceCoef();
            }else if(hitLeft){
                velocity.x *= -1*thisBody.getBounceCoef();
            }else if (hitRight) {
                velocity.x *= -1*thisBody.getBounceCoef();
            }
            foundVert = false;
        }
    }

    public void checkMovement(String pointOfError, Vector2 prevPos, Vector2 newPos, RigidBodies body){

        if (abs( prevPos.Magnitude() -newPos.Magnitude())> body.getVelocity().Magnitude()){
            System.out.println(pointOfError + " " + i);
            i++;
        }
    }
    public boolean isInShape(Vector2 checkPosition) {
        for (int T = 0; T < triangles.length; T++) {

            Triangle checkTri;
            double d00, d01, d02, d11, d12;
            double denom, u, v;

            //check if the given point is inside a triangle

            checkTri = triangles[T];

            Vector2 v0 = checkTri.c.Subtract(checkTri.a);
            Vector2 v1 = checkTri.b.Subtract(checkTri.a);
            Vector2 v2 = checkPosition.Subtract(checkTri.a);

            d00 = v0.DotProduct(v0);
            d01 = v0.DotProduct(v1);
            d02 = v0.DotProduct(v2);
            d11 = v1.DotProduct(v1);
            d12 = v1.DotProduct(v2);
            denom = (d00 * d11) - (d01 * d01);
            u = ((d11 * d02) - (d01 * d12)) / denom;
            v = ((d00 * d12) - (d01 * d02)) / denom;
            contact = ((u > 0) && (v > 0) && (u + v < 1));
            if (contact) {
                return true;
            }

        }
        return false;
    }



    public Vector2[] getShapeVertices() {
        return vertices;
    }
    public boolean getContact(){
        return contact;
    }
    public Vector2 getPushVector() {
        return pushVector;
    }
    public Triangle[] getTriangles() {
        return triangles;
    }


    public void Draw() {
        glBegin(GL_LINE_LOOP);
        for (Vector2 vertex : vertices) {
            glColor3d(color.R, color.G, color.B);
            glVertex2d(vertex.x, vertex.y);
        }
        glEnd();
    }
    public void Drawpoint(double x, double y, Color c) {
        Vector2[] point = new Vector2[3];
        for (int i = 0; i < 3; i++) {
            double theta = (i * (360 / 3) * (PI / 180));
            point[i] = new Vector2(x + (.01 * cos(theta)), y + (.05 * sin(theta)));
        }
        glBegin(GL_TRIANGLES);
        for(int i=0; i< point.length; i++){
            glVertex2d(point[i].x, point[i].y);
            glColor3d(c.R, c.G, c.B);
        }
        glEnd();
    }
    public static void DrawLine(Vector2 a, Vector2 b, Color c){
        glBegin(GL_LINE_LOOP);
        glVertex2d(a.x, a.y);
        glVertex2d(b.x, b.y);
        glColor3d(c.R, c.G, c.B);
        glEnd();
    }



    public void fillTriangles() {
        glBegin(GL_TRIANGLES);
        for (Triangle triangle : triangles) {
            glColor3d(0, 0, 0);
            glVertex2d(triangle.b.x, triangle.b.y);
            glColor3d(0, 0, 0);
            glVertex2d(triangle.c.x, triangle.c.y);
            glColor3d(1, 1, 1);
            glVertex2d(triangle.a.x, triangle.a.y);
        }
        glEnd();
    }


    public void triangleLines() {
        glBegin(GL_LINE_LOOP);
        for (Triangle triangle : triangles) {
            glColor3d(1, 1, 1);
            glVertex2d(triangle.b.x, triangle.b.y);
            glColor3d(1, 1, 1);
            glVertex2d(triangle.c.x, triangle.c.y);
            glColor3d(1, 1, 1);
            glVertex2d(triangle.a.x, triangle.a.y);
        }
        glEnd();
    }


    public static void Triangle(double posX, double posY, double width, double height) {
        glBegin(GL_TRIANGLES);
        //top
        glVertex2d(posX, posY + height);
        //bottom left
        glVertex2d(posX - width, posY - height);
        //bottom right
        glVertex2d(posX + width, posY - height);
        glEnd();
    }
}