import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.io.File;

public class Snake extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int GAME_WIDTH = 300;
    private static final int GAME_HEIGHT = 300;
    private Board board;

    public Snake() {
        initializeFrame();
    }

    private void initializeFrame() {
        // Pre-load the Comic Sans font to prevent delay
        new Font("Comic Sans MS", Font.PLAIN, 20).getFamily();
        
        // Basic frame setup with exact size
        setUndecorated(true);
        setBackground(Color.BLACK);
        getContentPane().setBackground(Color.BLACK);
        
        // Create and add loading panel with exact size
        LoadingPanel loadingPanel = new LoadingPanel();
        add(loadingPanel);
        
        // Set exact size and position
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setSize(GAME_WIDTH, GAME_HEIGHT);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private void startLoading() {
        SwingWorker<Void, Integer> loader = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    publish(0);
                    Thread.sleep(50);
                    publish(30);
                    
                    // Create board
                    board = new Board();
                    publish(60);
                    Thread.sleep(50);
                    
                    publish(100);
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int progress = chunks.get(chunks.size() - 1);
                Component component = getContentPane().getComponent(0);
                if (component instanceof LoadingPanel) {
                    ((LoadingPanel) component).updateProgress(progress);
                }
            }

            @Override
            protected void done() {
                getContentPane().removeAll();
                add(board);
                
                // Switch to decorated window
                dispose();
                setUndecorated(false);
                
                // Calculate the frame size to maintain the exact game area
                Insets insets = getInsets();
                int frameWidth = GAME_WIDTH + insets.left + insets.right;
                int frameHeight = GAME_HEIGHT + insets.top + insets.bottom;
                setSize(frameWidth, frameHeight);
                setPreferredSize(new Dimension(frameWidth, frameHeight));
                setMinimumSize(new Dimension(frameWidth, frameHeight));
                setMaximumSize(new Dimension(frameWidth, frameHeight));
                
                setLocationRelativeTo(null);
                setVisible(true);
                
                board.requestFocusInWindow();
                board.startGame();
            }
        };
        loader.execute();
    }

    private class LoadingPanel extends JPanel {
        private int progress = 0;
        private final Font loadingFont = new Font("Comic Sans MS", Font.PLAIN, 16);
        private final Color loadingColor = new Color(50, 205, 50);

        public LoadingPanel() {
            setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
            setBackground(Color.BLACK);
        }

        public void updateProgress(int progress) {
            this.progress = progress;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw snake title
            g2d.setFont(new Font("Comic Sans MS", Font.BOLD, 24));
            g2d.setColor(loadingColor);
            String title = "Snake Game";
            FontMetrics titleFm = g2d.getFontMetrics();
            g2d.drawString(title, 
                          (getWidth() - titleFm.stringWidth(title)) / 2,
                          getHeight() / 3);

            // Draw loading text
            g2d.setFont(loadingFont);
            String loadingText = "Loading... " + progress + "%";
            FontMetrics fm = g2d.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(loadingText)) / 2;
            g2d.drawString(loadingText, textX, getHeight() / 2 - 20);

            // Draw progress bar
            int barWidth = 200;
            int barHeight = 15;
            int barX = (getWidth() - barWidth) / 2;
            int barY = getHeight() / 2;
            
            // Draw border
            g2d.setColor(Color.WHITE);
            g2d.drawRect(barX - 1, barY - 1, barWidth + 1, barHeight + 1);
            
            // Draw progress
            g2d.setColor(loadingColor);
            int progressWidth = (int) (barWidth * (progress / 100.0));
            g2d.fillRect(barX, barY, progressWidth, barHeight);
        }
    }

    public static void main(String[] args) {
        // Pre-load system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create and show loading screen immediately
        SwingUtilities.invokeLater(() -> {
            Snake game = new Snake();
            // Show frame in next event dispatch cycle
            SwingUtilities.invokeLater(() -> {
                game.setVisible(true);
                game.startLoading();
            });
        });
    }
} 