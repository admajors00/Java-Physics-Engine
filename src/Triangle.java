public class Triangle {
    public Vector2 a;
    public Vector2 b;
    public Vector2 c;
    public Triangle(Vector2 a, Vector2 b, Vector2 c){
            this.a = a;
            this.b = b;
            this.c = c;
    }
    public Triangle Add(Vector2 v) {
        return new Triangle(new Vector2(a.x + v.x,a.y + v.y),new Vector2(b.x + v.x,b.y + v.y), new Vector2(c.x + v.x,c.y + v.y));
    }
}
