import java.io.*;
import java.util.*;

public class CrackerBarrelPuzzle{
  public static void main(String[] args){
    int round = 1;
    for(int i = 0; i < 5; i++)
    {
      for(int j = 0; j <= i; j++)
      {
        System.out.println("=== " + round + " ===");
        round++;
        Game play = new Game(i, j);
        play.Start();
      }
    }
  }

  static class CrackerBarrelBoard{
    boolean[][] points = new boolean[5][5];

    public CrackerBarrelBoard(int row, int column){
      for(int i = 0; i < 5; i++)
      {
        for(int j = 0; j <= i; j++)
        {
          points[i][j] = true;
        }
      }
      points[row][column] = false;
    }

    public CrackerBarrelBoard(int gameBoard){
      for(int i = 4; i >= 0; i--)
      {
        for(int j = i; j >= 0; j--)
        {
          if((gameBoard & 1) == 1)
            points[i][j] = true;
          else
            points[i][j] = false;
          gameBoard /= 2;
        }
      }
    }

    public CrackerBarrelBoard(CrackerBarrelBoard prev) {
      for(int i = 0; i < 5; i++)
      {
        for(int j = 0; j <= i; j++)
        {
          points[i][j] = prev.points[i][j];
        }
      }
    }

    public List<CrackerBarrelBoard> resultBoards(){
      List<CrackerBarrelBoard> gameBoards = new ArrayList<CrackerBarrelBoard>();

      for(int i = 0; i < 5; i++)
      {
        for(int j = 0; j <= i; j++)
        {
          Slot start = new Slot(i, j);
          List<Chance> potentialChances = Chances.getChances(start);
          for(Chance chance : potentialChances) {
            if(validChance(chance))
              gameBoards.add(jump(chance));
          }
        }
      }
      return gameBoards;
    }

    public boolean validChance(Chance chance) {
      if(!points[chance.getStart().getRow()][chance.getStart().getCol()])
        return false;
      if(!points[chance.getJump().getRow()][chance.getJump().getCol()])
        return false;
      if(points[chance.getEnd().getRow()][chance.getEnd().getCol()])
        return false;

      return true;
    }

    public CrackerBarrelBoard jump(Chance chance){
      CrackerBarrelBoard bd = new CrackerBarrelBoard(this);

      bd.points[chance.getStart().getRow()][chance.getStart().getCol()] = false;
      bd.points[chance.getJump().getRow()][chance.getJump().getCol()] = false;
      bd.points[chance.getEnd().getRow()][chance.getEnd().getCol()] = true;

      return bd;
    }

    public boolean solvedBoard() {
      int remainingPegs = 0;

      for(int i = 0; i < 5; i++){
        for(int j = 0; j <= i; j++)
        {
          if(points[i][j]){
            remainingPegs++;
            if(remainingPegs > 1)
              return false;
          }
        }
      }
      return remainingPegs == 1;
    }

    public int toInt() {
      int ret = 0;
      for(int i = 0; i < 5; ++i)
  			for (int j = 0; j <= i; ++j) {
  				ret *= 2;
  				if (points[i][j]) {
  					ret |= 1;
  				}
  			}

  		return ret;
    }

    public String toString() {
      StringBuffer sb = new StringBuffer();

      for (int i = 0; i < 5; ++i) {
        for (int s = 4-i; s > 0; --s)
          sb.append(" ");
        for (int j = 0; j <= i; ++j) {
          sb.append(points[i][j] ? 'x' : '.').append(" ");
        }
        sb.append("\n");
      }
      return sb.toString();
    }
  }

  static class Game{
    CrackerBarrelBoard startingBoard;

    public Game(int row, int column){
      startingBoard = new CrackerBarrelBoard(row, column);
    }

    public void Start(){
      Sequence startingSlot = new Sequence(startingBoard);

      for(CrackerBarrelBoard nextBoard : startingBoard.resultBoards()){
        Sequence nextNode = new Sequence(nextBoard);
        if(play(nextBoard, nextNode))
          startingSlot.addChild(nextNode);
      }
      printSolution(startingSlot);
    }

    private void printSolution(Sequence parent){
      System.out.println(parent.getBoard());

      if(parent.numChildren() > 0) {
  			Sequence nextNode = parent.getFirstChild();
  			printSolution(nextNode);				// recurse
  			if (nextNode.numChildren() == 0)
  				parent.removeFirstChild();
        }
    }

    private boolean play(CrackerBarrelBoard bd, Sequence parent){
      if(bd.solvedBoard())
        return true;

      List<CrackerBarrelBoard> nextBoards = bd.resultBoards();

      boolean found = false;

      for(CrackerBarrelBoard nextBoard : nextBoards) {
        Sequence nextNode = new Sequence(nextBoard);
        if(play(nextBoard, nextNode)){
          found = true;
          parent.addChild(nextNode);
        }
      }

      return found;
    }
  }

  static class Sequence {
    Sequence level;
    CrackerBarrelBoard bd;
    List<Sequence> children = new ArrayList<Sequence>();

    public Sequence(CrackerBarrelBoard bd){
      this.bd = bd;
    }

    public void addChild(Sequence child){
      children.add(child);
    }

    public CrackerBarrelBoard getBoard(){
      return bd;
    }

    public boolean hasChildren(){
      return children.size() > 0;
    }

    public Sequence getFirstChild() {
      return children.get(0);
    }

    public void removeFirstChild(){
      children.remove(0);
    }

    public int numChildren() {
      return children.size();
    }
  }

  static class Slot{
    int row;
    int column;

    public Slot(int row, int column){
      this.row = row;
      this.column = column;
    }

    public int getRow(){
      return row;
    }

    public int getCol(){
      return column;
    }

    public String toString(){
      return "[" + row + "," + column + "]";
    }

    public int hashCode(){
      int result = 17;
      result = 37*result+row;
      result = 37*result+column;

      return result;
    }

    public boolean equals(Object other){
      if(!(other instanceof Slot))
        return false;

      Slot that = (Slot) other;

      if(this.row != that.row)
        return false;

      return this.column == that.column;
    }
  }

  static class Chance{
    private Slot start;
    private Slot jump;
    private Slot end;

    public Chance(Slot start, Slot jump, Slot end){
      this.start = start;
      this.jump = jump;
      this.end = end;
    }

    public Slot getStart() {
      return start;
    }

    public Slot getJump() {
      return jump;
    }

    public Slot getEnd() {
      return end;
    }

    public String toString() {
      StringBuffer sb = new StringBuffer();

      sb.append("{"+start);
      sb.append(","+jump);
      sb.append(","+end+ "}");

      return sb.toString();
    }
  }

  static class Chances {
  	private static Map<Slot,List<Chance>> validMoves = new HashMap<Slot,List<Chance>>();

  	static {
  		Slot start;

  		start = new Slot(0,0);
  		validMoves.put(start, new ArrayList<Chance>());
  		validMoves.get(start).add(new Chance(start, new Slot(1,0), new Slot(2,0)));
  		validMoves.get(start).add(new Chance(start, new Slot(1,1), new Slot(2,2)));

  		start = new Slot(1,0);
  		validMoves.put(start, new ArrayList<Chance>());
  		validMoves.get(start).add(new Chance(start, new Slot(2,0), new Slot(3,0)));
  		validMoves.get(start).add(new Chance(start, new Slot(2,1), new Slot(3,2)));

  		start = new Slot(1,1);
  		validMoves.put(start, new ArrayList<Chance>());
  		validMoves.get(start).add(new Chance(start, new Slot(2,1), new Slot(3,1)));
  		validMoves.get(start).add(new Chance(start, new Slot(2,2), new Slot(3,3)));

  		start = new Slot(2,0);
  		validMoves.put(start, new ArrayList<Chance>());
  		validMoves.get(start).add(new Chance(start, new Slot(1,0), new Slot(0,0)));
  		validMoves.get(start).add(new Chance(start, new Slot(2,1), new Slot(2,2)));
  		validMoves.get(start).add(new Chance(start, new Slot(3,0), new Slot(4,0)));
  		validMoves.get(start).add(new Chance(start, new Slot(3,1), new Slot(4,2)));

  		start = new Slot(2,1);
  		validMoves.put(start, new ArrayList<Chance>());
  		validMoves.get(start).add(new Chance(start, new Slot(3,1), new Slot(4,1)));
  		validMoves.get(start).add(new Chance(start, new Slot(3,2), new Slot(4,3)));

  		start = new Slot(2,2);
  		validMoves.put(start, new ArrayList<Chance>());
  		validMoves.get(start).add(new Chance(start, new Slot(1,1), new Slot(0,0)));
  		validMoves.get(start).add(new Chance(start, new Slot(2,1), new Slot(2,0)));
  		validMoves.get(start).add(new Chance(start, new Slot(3,2), new Slot(4,2)));
  		validMoves.get(start).add(new Chance(start, new Slot(3,3), new Slot(4,4)));

  		start = new Slot(3,0);
  		validMoves.put(start, new ArrayList<Chance>());
  		validMoves.get(start).add(new Chance(start, new Slot(2,0), new Slot(1,0)));
  		validMoves.get(start).add(new Chance(start, new Slot(3,1), new Slot(3,2)));

  		start = new Slot(3,1);
  		validMoves.put(start, new ArrayList<Chance>());
  		validMoves.get(start).add(new Chance(start, new Slot(2,1), new Slot(1,1)));
  		validMoves.get(start).add(new Chance(start, new Slot(3,2), new Slot(3,3)));

  		start = new Slot(3,2);
  		validMoves.put(start, new ArrayList<Chance>());
  		validMoves.get(start).add(new Chance(start, new Slot(2,1), new Slot(1,0)));
  		validMoves.get(start).add(new Chance(start, new Slot(3,1), new Slot(3,0)));

  		start = new Slot(3,3);
  		validMoves.put(start, new ArrayList<Chance>());
  		validMoves.get(start).add(new Chance(start, new Slot(2,2), new Slot(1,1)));
  		validMoves.get(start).add(new Chance(start, new Slot(3,2), new Slot(3,1)));

  		start = new Slot(4,0);
  		validMoves.put(start, new ArrayList<Chance>());
  		validMoves.get(start).add(new Chance(start, new Slot(3,0), new Slot(2,0)));
  		validMoves.get(start).add(new Chance(start, new Slot(4,1), new Slot(4,2)));

  		start = new Slot(4,1);
  		validMoves.put(start, new ArrayList<Chance>());
  		validMoves.get(start).add(new Chance(start, new Slot(3,1), new Slot(2,1)));
  		validMoves.get(start).add(new Chance(start, new Slot(4,2), new Slot(4,3)));

  		start = new Slot(4,2);
  		validMoves.put(start, new ArrayList<Chance>());
  		validMoves.get(start).add(new Chance(start, new Slot(3,1), new Slot(2,0)));
  		validMoves.get(start).add(new Chance(start, new Slot(3,2), new Slot(2,2)));
  		validMoves.get(start).add(new Chance(start, new Slot(4,1), new Slot(4,0)));
  		validMoves.get(start).add(new Chance(start, new Slot(4,3), new Slot(4,4)));

  		start = new Slot(4,3);
  		validMoves.put(start, new ArrayList<Chance>());
  		validMoves.get(start).add(new Chance(start, new Slot(3,2), new Slot(2,1)));
  		validMoves.get(start).add(new Chance(start, new Slot(4,2), new Slot(4,1)));

  		start = new Slot(4,4);
  		validMoves.put(start, new ArrayList<Chance>());
  		validMoves.get(start).add(new Chance(start, new Slot(3,3), new Slot(2,2)));
  		validMoves.get(start).add(new Chance(start, new Slot(4,3), new Slot(4,2)));
  	}


  	public static List<Chance> getChances(Slot position) {
  		if (!validMoves.containsKey(position))
  			throw new RuntimeException("Invalid position: " + position);

  		return validMoves.get(position);
  	}


  	public String toString() {
  		return validMoves.toString();
  	}
  }

}