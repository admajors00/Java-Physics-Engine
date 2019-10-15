public class Vector2 {
    public double x = 0.0;
    public double y = 0.0;

    //holds the x and y for position, velocity, etc
    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2() {
        this.x = 0.0;
        this.y = 0.0;
    }

    public double Magnitude() {
        //magnitude eq/ replaces speed
        return Math.sqrt(x * x + y * y);
    }

    public double Angle() {
        //get the angle of the vector
        return Math.atan2(y, x);
    }

    public Vector2 Add(Vector2 b) {
        return new Vector2(x + b.x, y + b.y);
    }

    public Vector2 Subtract(Vector2 b) {
        return new Vector2(x - b.x, y - b.y);
    }

    public Vector2 Multiply(double a) {
        return new Vector2(x * a, y * a);
    }
    public Vector2 Divide(double a) {
        return new Vector2(x / a, y / a);
    }

    public double DotProduct(Vector2 b) {
        return (x*b.x + y*b.y);
    }

    public double AngleBetween(Vector2 b){
        return Math.acos(DotProduct(b)/( Magnitude() * b.Magnitude()));
    }
    public double DistanceFrom(Vector2 b){
        return Math.sqrt(Math.pow(x-b.x, 2) + Math.pow(y-b.y, 2));
    }
    //static bc it is not dependent on an instance of a vector class
    public static Vector2 CreateVector2(double angle, double magnitude) {
        return new Vector2(magnitude * Math.cos(angle), magnitude * Math.sin(angle));
    }
}
