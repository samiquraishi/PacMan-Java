import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        // Define grid dimensions
        int rows = 21;
        int cols = 19;
        int tileSize = 32;
        int windowWidth = cols * tileSize;
        int windowHeight = rows * tileSize;

        // Create and configure the game window
        JFrame gameWindow = new JFrame("Pac-Man Clone");
        gameWindow.setSize(windowWidth, windowHeight);
        gameWindow.setLocationRelativeTo(null);
        gameWindow.setResizable(false);
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize the game panel and add it to the window
        Game gamePanel = new Game();
        gameWindow.add(gamePanel);
        gameWindow.pack();
        gamePanel.requestFocusInWindow();
        gameWindow.setVisible(true);
    }
}

