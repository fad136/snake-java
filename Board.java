import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.awt.FontFormatException;
import java.io.IOException;

public class Board extends JPanel implements ActionListener {

    private final int SCORE_HEIGHT = 30;    // Height for score display
    private final int MARGIN = 20;          // Margin from window edges
    private final int BORDER_SIZE = 1;      // Border thickness
    private final int B_WIDTH = 300;        // Total panel width
    private final int B_HEIGHT = 280;       // Total panel height (including score area)
    
    // Game area dimensions (inside border, with margins)
    private final int GAME_AREA_X = MARGIN;
    private final int GAME_AREA_Y = SCORE_HEIGHT + MARGIN;
    private final int GAME_AREA_WIDTH = B_WIDTH - (2 * MARGIN);
    private final int GAME_AREA_HEIGHT = B_HEIGHT - SCORE_HEIGHT - (2 * MARGIN);
    
    private final int DOT_SIZE = 10;
    private final int ALL_DOTS = 900;
    private final int RAND_POS_X = (GAME_AREA_WIDTH / DOT_SIZE);
    private final int RAND_POS_Y = (GAME_AREA_HEIGHT / DOT_SIZE);
    private final int DELAY = 140;

    private int[] x;
    private int[] y;

    private int dots;
    private int apple_x;
    private int apple_y;

    private boolean leftDirection = false;
    private boolean rightDirection = true;
    private boolean upDirection = false;
    private boolean downDirection = false;
    private boolean inGame = true;

    private Timer timer;
    private Image ball;
    private Image apple;
    private Image head;

    private TAdapter adapter;

    private int score = 0;
    private Font customFont;
    private Font scoreFont;
    private Font gameOverFont;

    private static final int SCORE_PADDING = 20;

    private boolean isInitialized = false;

    public Board() {
        setBackground(Color.BLACK);
        setOpaque(true);
        setDoubleBuffered(true);
        setFocusable(true);
        setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT));
        
        x = new int[ALL_DOTS];
        y = new int[ALL_DOTS];
        
        adapter = new TAdapter();
        addKeyListener(adapter);
        initFont();
        loadImages();
        
        initGame();
        
        timer = new Timer(DELAY, this);
        timer.setInitialDelay(0);
    }
    
    private void initFont() {
        try {
            scoreFont = new Font("Comic Sans MS", Font.PLAIN, 12);
            gameOverFont = new Font("Comic Sans MS", Font.PLAIN, 16);
        } catch (Exception e) {
            scoreFont = new Font("SansSerif", Font.PLAIN, 12);
            gameOverFont = new Font("SansSerif", Font.PLAIN, 16);
        }
    }

    private void loadImages() {
        try {
            MediaTracker tracker = new MediaTracker(this);
            
            String resourcePath = new File("resources").getAbsolutePath();
            ball = new ImageIcon(resourcePath + "/dot.png").getImage();
            apple = new ImageIcon(resourcePath + "/apple.png").getImage();
            head = new ImageIcon(resourcePath + "/head.png").getImage();
            
            tracker.addImage(ball, 0);
            tracker.addImage(apple, 1);
            tracker.addImage(head, 2);
            
            try {
                tracker.waitForAll();
            } catch (InterruptedException e) {
                System.err.println("Image loading interrupted");
            }
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initGame() {
        dots = 3;
        score = 0;
        
        // Initialize snake position, considering the score area
        for (int z = 0; z < dots; z++) {
            x[z] = 50 - z * DOT_SIZE;
            y[z] = 50;
        }
        
        locateApple();
        inGame = true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, B_WIDTH, B_HEIGHT);
        
        // Draw score
        g.setColor(Color.WHITE);
        g.setFont(scoreFont);
        String scoreText = "Score: " + score;
        FontMetrics fm = g.getFontMetrics();
        g.drawString(scoreText, (B_WIDTH - fm.stringWidth(scoreText)) / 2, SCORE_HEIGHT - 10);

        // Draw border with a darker green color
        g.setColor(new Color(0, 100, 0));  // Darker green
        g.drawRect(GAME_AREA_X, GAME_AREA_Y, GAME_AREA_WIDTH, GAME_AREA_HEIGHT);
        
        if (timer != null) {
            doDrawing(g);
        }
    }
    
    private void doDrawing(Graphics g) {
        if (inGame) {
            // Draw game elements inside the border
            g.drawImage(apple, apple_x + GAME_AREA_X, apple_y + GAME_AREA_Y, this);

            for (int z = 0; z < dots; z++) {
                if (z == 0) {
                    g.drawImage(head, x[z] + GAME_AREA_X, y[z] + GAME_AREA_Y, this);
                } else {
                    g.drawImage(ball, x[z] + GAME_AREA_X, y[z] + GAME_AREA_Y, this);
                }
            }
        } else {
            gameOver(g);
        }
    }

    private void gameOver(Graphics g) {
        String msg = "Game Over";
        String scoreMsg = "Final Score: " + score;
        String restartMsg = "Press SPACE to restart";
        String quitMsg = "Press Q to quit";

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(Color.WHITE);
        g2d.setFont(gameOverFont);
        FontMetrics fm = g2d.getFontMetrics();

        int centerY = B_HEIGHT / 2;
        int lineSpacing = 30;

        g2d.setColor(new Color(0, 0, 0, 128));
        int shadow = 2;
        
        int msgX = (B_WIDTH - fm.stringWidth(msg)) / 2;
        g2d.drawString(msg, msgX + shadow, centerY - lineSpacing * 2 + shadow);
        
        int scoreX = (B_WIDTH - fm.stringWidth(scoreMsg)) / 2;
        g2d.drawString(scoreMsg, scoreX + shadow, centerY - lineSpacing + shadow);
        
        int restartX = (B_WIDTH - fm.stringWidth(restartMsg)) / 2;
        g2d.drawString(restartMsg, restartX + shadow, centerY + lineSpacing + shadow);
        
        int quitX = (B_WIDTH - fm.stringWidth(quitMsg)) / 2;
        g2d.drawString(quitMsg, quitX + shadow, centerY + lineSpacing * 2 + shadow);

        g2d.setColor(Color.WHITE);
        g2d.drawString(msg, msgX, centerY - lineSpacing * 2);
        g2d.drawString(scoreMsg, scoreX, centerY - lineSpacing);
        g2d.drawString(restartMsg, restartX, centerY + lineSpacing);
        g2d.drawString(quitMsg, quitX, centerY + lineSpacing * 2);
    }

    private void checkApple() {
        if ((x[0] == apple_x) && (y[0] == apple_y)) {
            dots++;
            score += 10;
            locateApple();
        }
    }

    private void move() {
        // Only move if game is still running
        if (!inGame) {
            return;
        }

        for (int z = dots; z > 0; z--) {
            x[z] = x[(z - 1)];
            y[z] = y[(z - 1)];
        }

        if (adapter.queuedDirection != null) {
            adapter.applyDirectionChange(adapter.queuedDirection);
            adapter.queuedDirection = null;
        }

        if (leftDirection) {
            x[0] -= DOT_SIZE;
        }

        if (rightDirection) {
            x[0] += DOT_SIZE;
        }

        if (upDirection) {
            y[0] -= DOT_SIZE;
        }

        if (downDirection) {
            y[0] += DOT_SIZE;
        }
    }

    private void checkCollision() {
        // Check collision with body
        for (int z = 1; z < dots; z++) {
            if ((x[0] == x[z]) && (y[0] == y[z])) {
                inGame = false;
                break;
            }
        }

        // Check collision with borders of game area
        if (y[0] >= GAME_AREA_HEIGHT) {
            inGame = false;
        }

        if (y[0] < 0) {
            inGame = false;
        }

        if (x[0] >= GAME_AREA_WIDTH) {
            inGame = false;
        }

        if (x[0] < 0) {
            inGame = false;
        }
        
        if (!inGame) {
            timer.stop();
        }
    }

    private void locateApple() {
        boolean validLocation;
        do {
            validLocation = true;
            int r = (int) (Math.random() * RAND_POS_X);
            apple_x = r * DOT_SIZE;

            r = (int) (Math.random() * RAND_POS_Y);
            apple_y = r * DOT_SIZE;

            // Check if apple spawns on snake's body
            for (int z = 0; z < dots; z++) {
                if (x[z] == apple_x && y[z] == apple_y) {
                    validLocation = false;
                    break;
                }
            }
        } while (!validLocation);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame) {
            checkApple();
            checkCollision();
            move();
        }

        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        isInitialized = true;
    }

    private class TAdapter extends KeyAdapter {
        private Direction currentDirection;
        private Direction queuedDirection;
        private boolean canChangeDirection = true;
        private static final long KEY_DELAY = 50;

        private enum Direction {
            UP, DOWN, LEFT, RIGHT;
            
            boolean isOpposite(Direction other) {
                return (this == UP && other == DOWN) ||
                       (this == DOWN && other == UP) ||
                       (this == LEFT && other == RIGHT) ||
                       (this == RIGHT && other == LEFT);
            }
        }

        public TAdapter() {
            currentDirection = Direction.RIGHT;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (!inGame) {
                handleGameOverKeys(key);
                return;
            }

            Direction newDirection = getDirectionFromKey(key);
            if (newDirection != null) {
                // Only queue the new direction if it's not opposite to the current direction
                if (!newDirection.isOpposite(currentDirection)) {
                    queuedDirection = newDirection;
                }
            }
        }

        private Direction getDirectionFromKey(int key) {
            switch (key) {
                case KeyEvent.VK_LEFT: return Direction.LEFT;
                case KeyEvent.VK_RIGHT: return Direction.RIGHT;
                case KeyEvent.VK_UP: return Direction.UP;
                case KeyEvent.VK_DOWN: return Direction.DOWN;
                default: return null;
            }
        }

        private void applyDirectionChange(Direction newDirection) {
            // Only apply the direction change if it's not opposite to the current direction
            if (!newDirection.isOpposite(currentDirection)) {
                switch (newDirection) {
                    case LEFT:
                        if (!rightDirection) {
                            leftDirection = true;
                            upDirection = false;
                            downDirection = false;
                        }
                        break;
                    case RIGHT:
                        if (!leftDirection) {
                            rightDirection = true;
                            upDirection = false;
                            downDirection = false;
                        }
                        break;
                    case UP:
                        if (!downDirection) {
                            upDirection = true;
                            rightDirection = false;
                            leftDirection = false;
                        }
                        break;
                    case DOWN:
                        if (!upDirection) {
                            downDirection = true;
                            rightDirection = false;
                            leftDirection = false;
                        }
                        break;
                }
                currentDirection = newDirection;
            }
        }

        private void handleGameOverKeys(int key) {
            if (key == KeyEvent.VK_SPACE) {
                resetGameState();
                startGame();
            } else if (key == KeyEvent.VK_Q) {
                Window window = SwingUtilities.getWindowAncestor(Board.this);
                if (window != null) {
                    window.dispose();
                }
            }
        }
    }

    private void resetGameState() {
        dots = 3;
        score = 0;
        leftDirection = false;
        rightDirection = true;
        upDirection = false;
        downDirection = false;
        inGame = true;
        adapter = new TAdapter();
        addKeyListener(adapter);
        initGame();
    }

    public void startGame() {
        if (isInitialized) {
            timer.start();
        }
    }
} 