package Snake;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    //A snake body part is 50x50
    private final Double snakeSize = 50.;
    //The head of the snake is created, at position (250,250)
    private Rectangle snakeHead;
    //First snake tail created behind the head of the snake
    private Rectangle snakeTail_1;
    //x and y position of the snake head different from starting position
    double xPos;
    double yPos;

    //Food
    Food food;

    //Direction snake is moving at start
    private Direction direction;

    //List of all position of thew snake head
    private final List<Position> positions = new ArrayList<>();

    //List of all snake body parts
    private final ArrayList<Rectangle> snakeBody = new ArrayList<>();

    //Game ticks is how many times the snake have moved
    private int gameTicks;

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Button startButton;

    Timeline timeline;

    private boolean canChangeDirection;

    @FXML
    void start(MouseEvent event) {

        for (Rectangle snake : snakeBody) {
            anchorPane.getChildren().remove(snake);
        }

        gameTicks = 0;
        positions.clear();
        snakeBody.clear();
        snakeHead = new Rectangle(250, 250, snakeSize, snakeSize);
        snakeTail_1 = new Rectangle(snakeHead.getX() - snakeSize, snakeHead.getY(), snakeSize, snakeSize);
        xPos = snakeHead.getLayoutX();
        yPos = snakeHead.getLayoutY();
        direction = Direction.RIGHT;
        canChangeDirection = true;
        food.moveFood();

        snakeBody.add(snakeHead);
        snakeHead.setFill(Color.RED);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        snakeBody.add(snakeTail_1);

        anchorPane.getChildren().addAll(snakeHead, snakeTail_1);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        timeline = new Timeline(new KeyFrame(Duration.seconds(0.3), e -> {
            positions.add(new Position(snakeHead.getX() + xPos, snakeHead.getY() + yPos));
            moveSnakeHead(snakeHead);
            for (int i = 1; i < snakeBody.size(); i++) {
                moveSnakeTail(snakeBody.get(i), i);
            }
            canChangeDirection = true;
            //System.out.println((xPos + snakeHead.getX()) + "-----" + (yPos + snakeHead.getY()));
            eatFood();
            gameTicks++;
            if(checkIfGameIsOver(snakeHead)){
                timeline.stop();
            }
        }));
        food = new Food(-50,-50,anchorPane,snakeSize);
    }

    //Change position with key pressed
    @FXML
    void moveSquareKeyPressed(KeyEvent event) {
        if(canChangeDirection){
            if (event.getCode().equals(KeyCode.W) && direction != Direction.DOWN) {
                direction = Direction.UP;
            } else if (event.getCode().equals(KeyCode.S) && direction != Direction.UP) {
                direction = Direction.DOWN;
            } else if (event.getCode().equals(KeyCode.A) && direction != Direction.RIGHT) {
                direction = Direction.LEFT;
            } else if (event.getCode().equals(KeyCode.D) && direction != Direction.LEFT) {
                direction = Direction.RIGHT;
            }
            canChangeDirection = false;
        }
    }

    //Create another snake body part
    @FXML
    void addBodyPart(ActionEvent event) {
        addSnakeTail();
    }

    //Snake head is moved in the direction specified
    private void moveSnakeHead(Rectangle snakeHead) {
        if (direction.equals(Direction.RIGHT)) {
            xPos = xPos + snakeSize;
            snakeHead.setTranslateX(xPos);
        } else if (direction.equals(Direction.LEFT)) {
            xPos = xPos - snakeSize;
            snakeHead.setTranslateX(xPos);
        } else if (direction.equals(Direction.UP)) {
            yPos = yPos - snakeSize;
            snakeHead.setTranslateY(yPos);
        } else if (direction.equals(Direction.DOWN)) {
            yPos = yPos + snakeSize;
            snakeHead.setTranslateY(yPos);
        }
    }

    //A specific tail is moved to the position of the head x game ticks after the head was there
    private void moveSnakeTail(Rectangle snakeTail, int tailNumber) {
        double yPos = positions.get(gameTicks - tailNumber + 1).getYPos() - snakeTail.getY();
        double xPos = positions.get(gameTicks - tailNumber + 1).getXPos() - snakeTail.getX();
        snakeTail.setTranslateX(xPos);
        snakeTail.setTranslateY(yPos);
    }

    //New snake tail is created and added to the snake and the anchor pane
    private void addSnakeTail() {
        Rectangle rectangle = snakeBody.get(snakeBody.size() - 1);
        Rectangle snakeTail = new Rectangle(
                snakeBody.get(1).getX() + xPos + snakeSize,
                snakeBody.get(1).getY() + yPos,
                snakeSize, snakeSize);
        snakeBody.add(snakeTail);
        anchorPane.getChildren().add(snakeTail);
    }

    public boolean checkIfGameIsOver(Rectangle snakeHead) {
        if (xPos > 340 || xPos < -340 ||yPos < -340 || yPos > 340) {
            System.out.println("Game_over");
            return true;
        } else if(snakeHitItSelf()){
            return true;
        }
        return false;
    }

    public boolean snakeHitItSelf(){
        int size = positions.size() - 1;
        if(size > 2){
            for (int i = size - snakeBody.size(); i < size; i++) {
                if(positions.get(size).getXPos() == (positions.get(i).getXPos())
                        && positions.get(size).getYPos() == (positions.get(i).getYPos())){
                    System.out.println("Hit");
                    return true;
                }
            }
        }
        return false;
    }

    private void eatFood(){
        if(xPos + snakeHead.getX() == food.getPosition().getXPos() && yPos + snakeHead.getY() == food.getPosition().getYPos()){
            System.out.println("Eat food");
            foodCantSpawnInsideSnake();
            addSnakeTail();
        }
    }

    private void foodCantSpawnInsideSnake(){
        food.moveFood();
        while (isFoodInsideSnake()){
            food.moveFood();
        }


    }

    private boolean isFoodInsideSnake(){
        int size = positions.size();
        if(size > 2){
            for (int i = size - snakeBody.size(); i < size; i++) {
                if(food.getPosition().getXPos() == (positions.get(i).getXPos())
                        && food.getPosition().getYPos() == (positions.get(i).getYPos())){
                    System.out.println("Inside");
                    return true;
                }
            }
        }
        return false;
    }


}