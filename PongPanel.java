package pong;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import leaderboard.LeaderboardClient;

@SuppressWarnings("serial")
public class PongPanel extends JPanel implements ActionListener, KeyListener{

    //private boolean showTitleScreen = true;
    private boolean playing = true;
    private boolean gameOver = false;

    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean wPressed = false;
    private boolean sPressed = false;

    private int ballX = 250;
    private int ballY = 250;
    private int diameter = 20;
    private int speed = 2;
    private int ballDeltaX = (-1*speed);
    private int ballDeltaY = (3*speed);

    private int playerX = 25;
    private int playerY = 250;
    private int playerWidth = 10;
    private int playerHeight = 50;

    private int computerX = 465;
    private int computerY = 250;
    private int computerWidth = 10;
    private int computerHeight = 50;

    private int paddleSpeed = 5;
    private double computerSpeed = 1.2;
    
    private int playerScore = 0;
    private int computerScore = 0;

    //construct a PongPanel
    public PongPanel(){
        setBackground(Color.BLACK);

        //listen to key presses
        setFocusable(true);
        addKeyListener(this);

        //call step() 60 fps
        Timer timer = new Timer(1000/60, this);
        timer.start();
    }


    public void actionPerformed(ActionEvent e){
        step();
    }

    public void step(){
        if(playing){
            //move player 1
            if (upPressed || wPressed) {
                if (playerY-paddleSpeed > 0) {
                    playerY -= paddleSpeed;
                }
            }
            if (downPressed || sPressed) {
                if (playerY + paddleSpeed + playerHeight < getHeight()) {
                    playerY += paddleSpeed;
                }
            }

            //move computer [player 2]
            if(ballY < computerY) {
            	computerY -= paddleSpeed*computerSpeed;
            } else if(ballY > (computerY + computerHeight)) {
            	computerY += paddleSpeed*computerSpeed;
            }

            //where will the ball be after it moves?
            int nextBallLeft = ballX + ballDeltaX;
            int nextBallRight = ballX + diameter + ballDeltaX;
            int nextBallTop = ballY + ballDeltaY;
            int nextBallBottom = ballY + diameter + ballDeltaY;

            int playerRight = playerX + playerWidth;
            int playerTop = playerY;
            int playerBottom = playerY + playerHeight;

            float computerLeft = computerX;
            float computerTop = computerY;
            float computerBottom = computerY + computerHeight;


            //ball bounces off top and bottom of screen
            if (nextBallTop < 0 || nextBallBottom > getHeight()) {
                ballDeltaY *= -1;
            }

            //will the ball go off the left side?
            if (nextBallLeft < playerRight) { 
                //is it going to miss the paddle?
                if (nextBallTop > playerBottom || nextBallBottom < playerTop) {
                    computerScore ++;
                    if (computerScore >= 3 && playerScore > 0) {
                        playing = false;
                        gameOver = true;
                    }
                    ballX = 250;
                    ballY = 250;
                } else {
                    ballDeltaX *= -1;
                }
            }

            //will the ball go off the right side?
            if (nextBallRight > computerLeft) {
                //is it going to miss the paddle?
                if (nextBallTop > computerBottom || nextBallBottom < computerTop) {
                    playerScore ++;
                    if (playerScore >= 3 && computerScore > 0) {
                        playing = false;
                        gameOver = true;
                    }
                    ballX = 250;
                    ballY = 250;
                }
                else {
                    ballDeltaX *= -1;
                }
            }

            //move the ball
            ballX += ballDeltaX;
            ballY += ballDeltaY;
        }

        //stuff has moved, tell this JPanel to repaint itself
        repaint();
    }

    //paint the game screen
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.setColor(Color.WHITE);

        if (playing) {
            int playerRight = playerX + playerWidth;
            int computerLeft =  computerX;

            //draw dashed line down center
            for (int lineY = 0; lineY < getHeight(); lineY += 50) {
                g.drawLine(250, lineY, 250, lineY+25);
            }

            //draw "goal lines" on each side
            g.drawLine(playerRight, 0, playerRight, getHeight());
            g.drawLine(computerLeft, 0, computerLeft, getHeight());

            //draw the scores
            g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
            g.drawString(String.valueOf(playerScore), 100, 100);
            g.drawString(String.valueOf(computerScore), 400, 100);

            //draw the ball
            g.fillOval(ballX, ballY, diameter, diameter);

            //draw the paddles
            g.fillRect(playerX, playerY, playerWidth, playerHeight);
            g.fillRect(computerX, computerY, computerWidth, computerHeight);
        } else if (gameOver) {
            g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
            g.drawString(String.valueOf(playerScore), 100, 100);
            g.drawString(String.valueOf(computerScore), 400, 100);

            g.setFont(new Font(Font.DIALOG, Font.BOLD, 36));
            if (playerScore > computerScore && computerScore > 0) {
                g.drawString("Player 1 Wins!", 165, 200);
            } else if(playerScore < computerScore && playerScore > 0) {
                g.drawString("Computer Wins!", 165, 200);
            }

            g.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
            g.drawString("Press space to restart.", 150, 400);
            
            sendScore();
        }
    }

    private boolean scoreSubmitted = false;
    private void sendScore() {
		if(!scoreSubmitted) {
			scoreSubmitted = true;
			int submit = JOptionPane.showConfirmDialog(null, "Would you like to submit your score to the server?");
			if(submit == 0) {
				String username = JOptionPane.showInputDialog("What would you like your username to be? (maximum 3 letters, and uppercase is forced)");
				if(username.length() > 3) {
					username = username.substring(0, 3);
				}
				username = username.toUpperCase();
				JOptionPane.showConfirmDialog(null, Arrays.toString(LeaderboardClient.getInstance().pongRequestLeaderboards(username + " " + playerScore)));
			}
		}
	}


	public void keyTyped(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {
        if(playing) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                upPressed = true;
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                downPressed = true;
            } else if (e.getKeyCode() == KeyEvent.VK_W) {
                wPressed = true;
            } else if (e.getKeyCode() == KeyEvent.VK_S) {
                sPressed = true;
            } else {} //make sure this else
        } else if (gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                gameOver = false;
                playerY = 250;
                computerY = 250;
                ballX = 250;
                ballY = 250;
                playerScore = 0;
                computerScore = 0;
                scoreSubmitted = false;
            }
        }
    }


    public void keyReleased(KeyEvent e) {
        if (playing) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                upPressed = false;
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                downPressed = false;
            } else if (e.getKeyCode() == KeyEvent.VK_W) {
                wPressed = false;
            } else if (e.getKeyCode() == KeyEvent.VK_S) {
                sPressed = false;
            }
        }
    }

}