import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class QueueApp extends JFrame {
    private List<Client> queue = new ArrayList<>();
    private QueuePanel queuePanel;
    private Random random = new Random();
    private int clientCounter = 1;
    private javax.swing.Timer simulationTimer;
    private javax.swing.Timer animationTimer;

    public QueueApp() {
        setTitle("Symulacja kolejki do restauracji (z animacją)");
        setSize(900, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        queuePanel = new QueuePanel(queue);
        add(queuePanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JButton addButton = new JButton("Dodaj klienta");
        JButton stepButton = new JButton("Symuluj krok");
        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");

        controlPanel.add(addButton);
        controlPanel.add(stepButton);
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        add(controlPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addClient());
        stepButton.addActionListener(e -> simulateStep());
        startButton.addActionListener(e -> startSimulation());
        stopButton.addActionListener(e -> stopSimulation());

        setVisible(true);

        animationTimer = new javax.swing.Timer(20, e -> animateClients());
        animationTimer.start();
    }

    private void addClient() {
        int targetX = 50 + queue.size() * 80;
        Client c = new Client(clientCounter++, 5 + random.nextInt(6), targetX, getHeight()/2);
        c.setX(900);
        c.setY(getHeight()/2);
        queue.add(c);
        queuePanel.repaint();
    }

    private void simulateStep() {
        if (!queue.isEmpty()) {
            int event = random.nextInt(3);
            Client first = queue.get(0);

            switch (event) {
                case 0 -> {
                    if (!first.isExiting()) {
                        first.setExiting(true);
                        first.setTargetX(getWidth() + 100);
                        first.setExitDirection(Client.ExitDirection.UP);
                    }
                }
                case 1 -> {
                    if (first != null && random.nextBoolean() && !first.isExiting()) {
                        first.setExiting(true);
                        first.setPatience(0);
                        first.setTargetX(getWidth() + 100);
                        first.setExitDirection(Client.ExitDirection.DOWN);
                    }
                }
                case 2 -> {
                    for (Client c : queue) {
                        c.decreasePatience();
                        if (c.getPatience() <= 0 && !c.isExiting()) {
                            c.setExiting(true);
                            c.setTargetX(getWidth() + 100);
                            c.setExitDirection(Client.ExitDirection.DOWN);
                        }
                    }
                }
            }
            updateTargets();
        }
        queuePanel.repaint();
    }

    private void updateTargets() {
        int targetX = 50;
        for (Client c : queue) {
            if (!c.isExiting()) c.setTargetX(targetX);
            targetX += 80;
        }
    }

    private void animateClients() {
        boolean needsRepaint = false;
        for (Client c : queue) {
            if (c.getX() != c.getTargetX()) {
                int dx = c.getTargetX() - c.getX();
                int stepX = dx / 50;
                if (stepX == 0) stepX = (dx > 0) ? 1 : -1;
                c.setX(c.getX() + stepX);
                needsRepaint = true;
            }

            if (c.isExiting()) {
                int stepY = (c.getExitDirection() == Client.ExitDirection.UP) ? -2 : 2;
                c.setY(c.getY() + stepY);
                needsRepaint = true;
            }
        }

        queue.removeIf(c -> c.isExiting() && (c.getX() > getWidth() || c.getY() < -50 || c.getY() > getHeight() + 50));
        if (needsRepaint) queuePanel.repaint();
    }

    private void startSimulation() {
        simulationTimer = new javax.swing.Timer(1500, e -> simulateStep());
        simulationTimer.start();
    }

    private void stopSimulation() {
        if (simulationTimer != null) simulationTimer.stop();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QueueApp::new);
    }
}

class Client {
    enum Mood { HAPPY, NEUTRAL, ANGRY }
    enum ExitDirection { UP, DOWN }

    private int id;
    private int patience;
    private int x, y;
    private int targetX;
    private boolean exiting = false;
    private ExitDirection exitDirection = ExitDirection.UP;

    public Client(int id, int patience, int targetX, int y) {
        this.id = id;
        this.patience = patience;
        this.targetX = targetX;
        this.x = targetX;
        this.y = y;
    }

    public int getId() { return id; }
    public int getPatience() { return patience; }
    public void setPatience(int p) { patience = p; }
    public void decreasePatience() { patience--; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getTargetX() { return targetX; }
    public void setTargetX(int targetX) { this.targetX = targetX; }

    public boolean isExiting() { return exiting; }
    public void setExiting(boolean b) { exiting = b; }

    public ExitDirection getExitDirection() { return exitDirection; }
    public void setExitDirection(ExitDirection dir) { exitDirection = dir; }

    public Mood getMood() {
        if (patience > 4) return Mood.HAPPY;
        else if (patience > 2) return Mood.NEUTRAL;
        else return Mood.ANGRY;
    }
}

class QueuePanel extends JPanel {
    private List<Client> queue;

    public QueuePanel(List<Client> queue) {
        this.queue = queue;
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (queue.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(new Font("Arial", Font.BOLD, 14));

        for (Client c : queue) {
            switch (c.getMood()) {
                case HAPPY -> g2.setColor(Color.GREEN);
                case NEUTRAL -> g2.setColor(Color.ORANGE);
                case ANGRY -> g2.setColor(Color.RED);
            }

            g2.fillOval(c.getX(), c.getY() - 30, 60, 60);
            g2.setColor(Color.BLACK);
            g2.drawOval(c.getX(), c.getY() - 30, 60, 60);
            g2.drawString("K" + c.getId(), c.getX() + 15, c.getY() - 5);
            g2.drawString("P:" + c.getPatience(), c.getX() + 10, c.getY() + 15);
        }
    }
}
