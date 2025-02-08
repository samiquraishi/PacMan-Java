import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class Game extends JPanel implements ActionListener, KeyListener {
    // Custom GameObject class for game entities
    class GameObject {
        int posX;
        int posY;
        int objectWidth;
        int objectHeight;
        Image sprite;

        int initialX;
        int initialY;
        char moveDirection = 'U'; // U D L R
        int speedX = 0;
        int speedY = 0;
        boolean isScared = false; // Added for ghost scared mode

        GameObject(Image sprite, int posX, int posY, int objectWidth, int objectHeight) {
            this.sprite = sprite;
            this.posX = posX;
            this.posY = posY;
            this.objectWidth = objectWidth;
            this.objectHeight = objectHeight;
            this.initialX = posX;
            this.initialY = posY;
        }

        void setDirection(char moveDirection) {
            char previousDirection = this.moveDirection;
            this.moveDirection = moveDirection;
            calculateSpeed();
            this.posX += this.speedX;
            this.posY += this.speedY;
            for (GameObject barrier : barriers) {
                if (detectCollision(this, barrier)) {
                    this.posX -= this.speedX;
                    this.posY -= this.speedY;
                    this.moveDirection = previousDirection;
                    calculateSpeed();
                }
            }
        }

        void calculateSpeed() {
            int speed = isScared ? gridSize/8 : gridSize/4; // Modified to account for scared mode
            if (this.moveDirection == 'U') {
                this.speedX = 0;
                this.speedY = -speed;
            }
            else if (this.moveDirection == 'D') {
                this.speedX = 0;
                this.speedY = speed;
            }
            else if (this.moveDirection == 'L') {
                this.speedX = -speed;
                this.speedY = 0;
            }
            else if (this.moveDirection == 'R') {
                this.speedX = speed;
                this.speedY = 0;
            }
        }

        void resetPosition() {
            this.posX = this.initialX;
            this.posY = this.initialY;
        }
    }

    private int gridRows = 21;
    private int gridCols = 19;
    private int gridSize = 32;
    private int gameWidth = gridCols * gridSize;
    private int gameHeight = gridRows * gridSize;

    private Image wall;
    private Image ghostBlue;
    private Image ghostOrange;
    private Image ghostPink;
    private Image ghostRed;
    private Image scaredGhost; // Added for scared mode
    private Image cherrySprite; // Added for power-up

    private Image pacUp;
    private Image pacDown;
    private Image pacLeft;
    private Image pacRight;

    private boolean isScaredMode = false; // Added for scared mode
    private Timer scaredTimer; // Added for scared mode duration
    private HashSet<GameObject> cherries; // Added for power-ups

    //X = barrier, O = empty, P = player, ' ' = pellet, C = cherry
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] gameMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X        C        X",
            "X XX X XXXXX X XX X",
            "X C  X       X  C X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X XXXX",
            "XC      bpo      CX",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   C   X    X",
            "X XXXXXX X XXXXXX X",
            "X   C         C   X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    HashSet<GameObject> barriers;
    HashSet<GameObject> pellets;
    HashSet<GameObject> enemies;
    GameObject player;

    Timer gameTimer;
    char[] moveDirections = {'U', 'D', 'L', 'R'}; //up down left right
    Random randomGenerator = new Random();
    int playerScore = 0;
    int playerLives = 3;
    boolean isGameOver = false;

    Game() {
        setPreferredSize(new Dimension(gameWidth, gameHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        loadSprites();
        initializeGame();

        for (GameObject enemy : enemies) {
            char newDirection = moveDirections[randomGenerator.nextInt(4)];
            enemy.setDirection(newDirection);
        }

        // Initialize scared timer
        scaredTimer = new Timer(5000, e -> disableScaredMode());

        gameTimer = new Timer(50, this);
        gameTimer.start();
    }

    private void loadSprites() {
        wall = new ImageIcon(getClass().getResource("/sprites/wall.png")).getImage();
        ghostBlue = new ImageIcon(getClass().getResource("/sprites/blueGhost.png")).getImage();
        ghostOrange = new ImageIcon(getClass().getResource("/sprites/orangeGhost.png")).getImage();
        ghostPink = new ImageIcon(getClass().getResource("/sprites/pinkGhost.png")).getImage();
        ghostRed = new ImageIcon(getClass().getResource("/sprites/redGhost.png")).getImage();
        scaredGhost = new ImageIcon(getClass().getResource("/sprites/scaredGhost.png")).getImage();
        cherrySprite = new ImageIcon(getClass().getResource("/sprites/cherry.png")).getImage();

        pacUp = new ImageIcon(getClass().getResource("/sprites/pacmanUp.png")).getImage();
        pacDown = new ImageIcon(getClass().getResource("/sprites/pacmanDown.png")).getImage();
        pacLeft = new ImageIcon(getClass().getResource("/sprites/pacmanLeft.png")).getImage();
        pacRight = new ImageIcon(getClass().getResource("/sprites/pacmanRight.png")).getImage();
    }

    public void initializeGame() {
        barriers = new HashSet<GameObject>();
        pellets = new HashSet<GameObject>();
        enemies = new HashSet<GameObject>();
        cherries = new HashSet<GameObject>();

        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                String currentRow = gameMap[row];
                char mapObject = currentRow.charAt(col);

                int xPos = col * gridSize;
                int yPos = row * gridSize;

                createGameObject(mapObject, xPos, yPos);
            }
        }
    }

    private void createGameObject(char mapObject, int xPos, int yPos) {
        switch(mapObject) {
            case 'X':
                GameObject barrier = new GameObject(wall, xPos, yPos, gridSize, gridSize);
                barriers.add(barrier);
                break;
            case 'b':
                GameObject blueGhost = new GameObject(ghostBlue, xPos, yPos, gridSize, gridSize);
                enemies.add(blueGhost);
                break;
            case 'o':
                GameObject orangeGhost = new GameObject(ghostOrange, xPos, yPos, gridSize, gridSize);
                enemies.add(orangeGhost);
                break;
            case 'p':
                GameObject pinkGhost = new GameObject(ghostPink, xPos, yPos, gridSize, gridSize);
                enemies.add(pinkGhost);
                break;
            case 'r':
                GameObject redGhost = new GameObject(ghostRed, xPos, yPos, gridSize, gridSize);
                enemies.add(redGhost);
                break;
            case 'P':
                player = new GameObject(pacRight, xPos, yPos, gridSize, gridSize);
                break;
            case ' ':
                GameObject pellet = new GameObject(null, xPos + 14, yPos + 14, 4, 4);
                pellets.add(pellet);
                break;
            case 'C':
                GameObject cherry = new GameObject(cherrySprite, xPos, yPos, gridSize, gridSize);
                cherries.add(cherry);
                break;
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderGame(g);
    }

    public void renderGame(Graphics g) {
        drawGameObjects(g);
        displayScore(g);
    }

    private void drawGameObjects(Graphics g) {
        g.drawImage(player.sprite, player.posX, player.posY, player.objectWidth, player.objectHeight, null);

        for (GameObject enemy : enemies) {
            g.drawImage(enemy.sprite, enemy.posX, enemy.posY, enemy.objectWidth, enemy.objectHeight, null);
        }

        for (GameObject barrier : barriers) {
            g.drawImage(barrier.sprite, barrier.posX, barrier.posY, barrier.objectWidth, barrier.objectHeight, null);
        }

        for (GameObject cherry : cherries) {
            g.drawImage(cherry.sprite, cherry.posX, cherry.posY, cherry.objectWidth, cherry.objectHeight, null);
        }

        g.setColor(Color.WHITE);
        for (GameObject pellet : pellets) {
            g.fillRect(pellet.posX, pellet.posY, pellet.objectWidth, pellet.objectHeight);
        }
    }

    private void displayScore(Graphics g) {
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (isGameOver) {
            g.drawString("Game Over: " + String.valueOf(playerScore), gridSize/2, gridSize/2);
        }
        else {
            g.drawString("Lives: " + String.valueOf(playerLives) + " Score: " + String.valueOf(playerScore),
                    gridSize/2, gridSize/2);
        }
    }

    public void updateGame() {
        movePlayer();
        moveEnemies();
        checkPelletCollection();
        checkCherryCollection();
    }

    private void movePlayer() {
        player.posX += player.speedX;
        player.posY += player.speedY;

        for (GameObject barrier : barriers) {
            if (detectCollision(player, barrier)) {
                player.posX -= player.speedX;
                player.posY -= player.speedY;
                break;
            }
        }
    }

    private void moveEnemies() {
        for (GameObject enemy : enemies) {
            if (detectCollision(enemy, player)) {
                if (isScaredMode) {
                    enemy.resetPosition();
                    playerScore += 200;
                } else {
                    playerLives -= 1;
                    if (playerLives == 0) {
                        isGameOver = true;
                        return;
                    }
                    resetPositions();
                }
            }

            handleEnemyMovement(enemy);
        }
    }

    private void handleEnemyMovement(GameObject enemy) {
        if (enemy.posY == gridSize*9 && enemy.moveDirection != 'U' && enemy.moveDirection != 'D') {
            enemy.setDirection('U');
        }

        enemy.posX += enemy.speedX;
        enemy.posY += enemy.speedY;

        boolean needsNewDirection = false;

        for (GameObject barrier : barriers) {
            if (detectCollision(enemy, barrier) || enemy.posX <= 0 ||
                    enemy.posX + enemy.objectWidth >= gameWidth) {
                enemy.posX -= enemy.speedX;
                enemy.posY -= enemy.speedY;
                needsNewDirection = true;
                break;
            }
        }

        if (needsNewDirection) {
            char newDirection = moveDirections[randomGenerator.nextInt(4)];
            enemy.setDirection(newDirection);
        }
    }

    private void checkPelletCollection() {
        GameObject collectedPellet = null;
        for (GameObject pellet : pellets) {
            if (detectCollision(player, pellet)) {
                collectedPellet = pellet;
                playerScore += 10;
            }
        }
        pellets.remove(collectedPellet);

        if (pellets.isEmpty()) {
            initializeGame();
            resetPositions();
        }
    }

    private void checkCherryCollection() {
        GameObject collectedCherry = null;
        for (GameObject cherry : cherries) {
            if (detectCollision(player, cherry)) {
                collectedCherry = cherry;
                playerScore += 50;
                enableScaredMode();
            }
        }
        cherries.remove(collectedCherry);
    }

    private void enableScaredMode() {
        isScaredMode = true;
        for (GameObject enemy : enemies) {
            enemy.isScared = true;
            enemy.sprite = scaredGhost;
            enemy.calculateSpeed();
        }
        scaredTimer.restart();
    }

    private void disableScaredMode() {
        isScaredMode = false;
        for (GameObject enemy : enemies) {
            enemy.isScared = false;
            // Restore original sprites
            for (GameObject ghost : enemies) {
                if (ghost == enemy) {
                    if (ghost.initialX == gridSize * 8 && ghost.initialY == gridSize * 9) {
                        ghost.sprite = ghostBlue;
                    } else if (ghost.initialX == gridSize * 9 && ghost.initialY == gridSize * 9) {
                        ghost.sprite = ghostOrange;
                    } else if (ghost.initialX == gridSize * 10 && ghost.initialY == gridSize * 9) {
                        ghost.sprite = ghostPink;
                    } else {
                        ghost.sprite = ghostRed;
                    }
                }
            }
            enemy.calculateSpeed();
        }
    }

    public boolean detectCollision(GameObject obj1, GameObject obj2) {
        return  obj1.posX < obj2.posX + obj2.objectWidth &&
                obj1.posX + obj1.objectWidth > obj2.posX &&
                obj1.posY < obj2.posY + obj2.objectHeight &&
                obj1.posY + obj1.objectHeight > obj2.posY;
    }

    public void resetPositions() {
        player.resetPosition();
        player.speedX = 0;
        player.speedY = 0;
        for (GameObject enemy : enemies) {
            enemy.resetPosition();
            char newDirection = moveDirections[randomGenerator.nextInt(4)];
            enemy.setDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
        if (isGameOver) {
            gameTimer.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (isGameOver) {
            initializeGame();
            resetPositions();
            playerLives = 3;
            playerScore = 0;
            isGameOver = false;
            gameTimer.start();
        }

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            player.setDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            player.setDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            player.setDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            player.setDirection('R');
        }

        if (player.moveDirection == 'U') {
            player.sprite = pacUp;
        }
        else if (player.moveDirection == 'D') {
            player.sprite = pacDown;
        }
        else if (player.moveDirection == 'L') {
            player.sprite = pacLeft;
        }
        else if (player.moveDirection == 'R') {
            player.sprite = pacRight;
        }
    }
}