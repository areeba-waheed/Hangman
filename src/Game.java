


import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Game {

	private static String answer;
	private String tmpAnswer;
	private String[] letterAndPosArray;
	private ArrayList<String> words = new ArrayList<String>();
	private Scanner scan;
	private int moves;
	private int index;
	private final ReadOnlyObjectWrapper<GameStatus> gameStatus;
	private ObjectProperty<Boolean> gameState = new ReadOnlyObjectWrapper<Boolean>();
	public String correctGuesses = "";


	public Game() {
		gameStatus = new ReadOnlyObjectWrapper<GameStatus>(this, "gameStatus", GameStatus.OPEN);
		gameStatus.addListener(new ChangeListener<GameStatus>() {
			@Override
			public void changed(ObservableValue<? extends GameStatus> observable,
								GameStatus oldValue, GameStatus newValue) {
				if (gameStatus.get() != GameStatus.OPEN) {
					log("in Game: in changed");
					//currentPlayer.set(null);
				}
			}

		});
        fillArrayOfWords();
		setRandomWord();
		prepTmpAnswer();
		prepLetterAndPosArray();
		moves = 0;

		gameState.setValue(false); // initial state
		createGameStatusBinding();
	}

	private void createGameStatusBinding() {
		List<Observable> allObservableThings = new ArrayList<>();
		ObjectBinding<GameStatus> gameStatusBinding = new ObjectBinding<GameStatus>() {
			{
				super.bind(gameState);
			}
			@Override
			public GameStatus computeValue() {
				log("in computeValue");
				GameStatus check = checkForWinner(index);
				if(check != null ) {
					return check;
				}

				if(tmpAnswer.trim().length() == 0 && index != -1){
					log("new game");
					return GameStatus.OPEN;
				}
				else if (index != -1){
					log("good guess");
					return GameStatus.GOOD_GUESS;
				}
				else {
					moves++;
					log("bad guess");
					return GameStatus.BAD_GUESS;
					//printHangman();
				}
			}
		};
		gameStatus.bind(gameStatusBinding);
	}

	public ReadOnlyObjectProperty<GameStatus> gameStatusProperty() {
		return gameStatus.getReadOnlyProperty();
	}
	public GameStatus getGameStatus() {
		return gameStatus.get();
	}

	public String getAnswer() {
	    return answer;
    }

    public String[] getAnsArray() {
		return letterAndPosArray;
	}

	private void setRandomWord() {
        answer = words.get(((int)(Math.random() * words.size())));
        log(answer);
    }
    private void fillArrayOfWords(){
        try {
            scan = new Scanner(new File("words.txt"));
        } catch (FileNotFoundException e) {
            log("File not found");
        }

        while(scan.hasNext()){
            words.add(scan.nextLine());
        }
    }
	private void prepTmpAnswer() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < answer.length(); i++) {
			sb.append(" ");
		}
		tmpAnswer = sb.toString();
	}

	private void prepLetterAndPosArray() {
		letterAndPosArray = new String[answer.length()];
		for(int i = 0; i < answer.length(); i++) {
			letterAndPosArray[i] = answer.substring(i,i+1);
		}
	}

	private int getValidIndex(String input) {
		int index = -1;
		for(int i = 0; i < letterAndPosArray.length; i++) {
			if(letterAndPosArray[i].equalsIgnoreCase(input)) {
				index = i;
				letterAndPosArray[i] = "";
				correctGuesses = correctGuesses + input;
				break;
			}
			if(correctGuesses.contains(input)) {
				index = 1;
			}
		}
		return index;
	}

	private int update(String input) {
		int index = getValidIndex(input);
		if(index != -1) {
			StringBuilder sb = new StringBuilder(tmpAnswer);
			sb.setCharAt(index, input.charAt(0));
			tmpAnswer = sb.toString();
			//correctGuesses = correctGuesses + input;
		}
		/*if (correctGuesses.contains(input)) {
			index = 1;
		}*/
		return index;
	}

	private static void drawHangmanFrame() {}

	public void makeMove(String letter) {
		log("\nin makeMove: " + letter);
		index = update(letter);
		// this will toggle the state of the game
		gameState.setValue(!gameState.getValue());
	}

	public void reset() {
		index = 0;
		setRandomWord();
		prepTmpAnswer();
		prepLetterAndPosArray();
		moves = 0;

		correctGuesses = "";
		gameState.setValue(false);
		createGameStatusBinding();
	}

	private int numOfTries() {
		return 6; // TODO, fix me
	}

	public static void log(String s) {
		System.out.println(s);
	}

	private GameStatus checkForWinner(int status) {
		log("in checkForWinner");
		if(tmpAnswer.equalsIgnoreCase(answer)) {
			log("won");
			return GameStatus.WON;
		}
		else if(moves == numOfTries()) {
			log("game over");
			return GameStatus.GAME_OVER;
		}
		else {
			return null;
		}
	}
	public enum GameStatus {
		GAME_OVER {
			@Override
			public String toString() {
				String gameOver = "Game Over\nCorrect Answer: "+ answer;
				return gameOver;
			}
		},
		BAD_GUESS {
			@Override
			public String toString() { return "Bad guess..."; }
		},
		GOOD_GUESS {
			@Override
			public String toString() {
				return "Good guess!";
			}
		},
		WON {
			@Override
			public String toString() {
				return "You won!";
			}
		},
		OPEN {
			@Override
			public String toString() {
				return "Game on, let's go!";
			}
		}
	}

}
