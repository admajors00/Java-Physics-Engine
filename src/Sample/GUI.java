//package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
public class GUI extends Application {
    Button button;
    Button button1;
    Button button2;
    Button backToScene1;
    Stage window;
    Scene scene;
    Scene scene2;
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        window = primaryStage;
        window.setTitle("JavaFX Tutorial");
        //When X is pressed closeProgram will be called before the program closes
        window.setOnCloseRequest(e-> {
            //.consume tells java that you will handle the request instead of javav closing the program
            e.consume();
            closeProgram();
        });


        button = new Button();
        button.setText("Click Me");
        button.setOnAction(event -> AlertBox.display("WARNING!", "Click now to win!"));


        //different way than the previous button to use buttons
        //anonymous inner class
        //this wa is nice because you don't have to check the event source
        button1 = new Button("No Me");
        button1.setOnAction(e->window.setScene(scene2));


        //lambda expression, java knows what the e -> means because of the conyext of the code
        button2 = new Button("Yes or No");
        button2.setOnAction(e -> {
            boolean result = CommunicatingBetweenWindows.display("Yes or No","Choose wisely");
            if (result){
                AlertBox.display("Choice ", "You chose... Poorly");
            }else{
                AlertBox.display("I am The senate!", "Not Yet!");
            }
        });


        backToScene1 = new Button("Go to scene 1");
        backToScene1.setOnAction(e->window.setScene(scene));


        VBox layout = new VBox(20);
        layout.getChildren().addAll(button, button1, button2);
        scene = new Scene(layout, 400, 400 );


        VBox layout2 = new VBox(20);
        layout2.getChildren().addAll(backToScene1);
        scene2 = new Scene(layout2, 300, 400);


        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public void closeProgram(){
        Boolean answer = CommunicatingBetweenWindows.display("Close Program", "Are you sure you wan to exit the program?");
        if(answer){
            window.close();
        }
    }
}
