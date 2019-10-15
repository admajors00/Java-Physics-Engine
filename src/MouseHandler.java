import org.lwjgl.glfw.GLFWCursorPosCallback;

// Our MouseHandler class extends the abstract class
// abstract classes should never be instantiated so here
// we create a concrete that we can instantiate
public class MouseHandler extends GLFWCursorPosCallback {
    double prevXpos = 0;
    double prevYpos = 0;
    @Override
    public void invoke(long window, double xpos, double ypos) {
        // TODO Auto-generated method stub
        // this basically just prints out the X and Y coordinates
        // of our mouse whenever it is in our window

        xpos = (xpos-(Main.winSize/2))/(Main.winSize/2);
        ypos = (ypos-(Main.winSize/2))/(Main.winSize/2);
        Main.setMouseVelocity(xpos-prevXpos, -ypos+prevYpos);
        prevXpos = xpos;
        prevYpos = ypos;

        Main.setMousePos(xpos, -ypos);
    }
}
