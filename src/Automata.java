import tester.*; // The tester library
import javalib.worldimages.*; // images, like RectangleImage or OverlayImages
import javalib.impworld.*; // the abstract World class and the big-bang library for imperative 
// worlds
import java.awt.Color; // general colors (as triples of red,green,blue values)
// and predefined colors (Red, Green, Yellow, Blue, Black, White)
import java.util.ArrayList;

// a contract for all cells
interface ICell {
  // gets the state of this ICell
  // returns 0 for an off cell, and 1 for an on cell
  int getState();

  // render this ICell as an image of a rectangle with this width and height
  // an on cell is drawn as a black square, while an off cell is white
  WorldImage render(int width, int height);

  // produces the child cell of this ICell with the given left and right neighbors
  ICell childCell(ICell left, ICell right);
}

// a base cell
abstract class ACell implements ICell {
  // whether the current cell is on or off (if state is binary)
  int state;

  // what numerical rule this cell follows
  int rule;

  // constructor
  ACell(int state, int rule) {
    this.state = state;
    this.rule = rule;
  }

  // gets the current state of this cell
  public int getState() {
    return this.state;
  }

  // render this ICell as an image of a rectangle with this width and height
  // an on cell is drawn as a black square, while an off cell is white
  public WorldImage render(int width, int height) {
    Color color;
    if (this.state == 1) {
      color = Color.BLACK;
    } else { // this.state == 0
      color = Color.WHITE;
    }
    return new RectangleImage(width, height, OutlineMode.SOLID, color);
  }

  // create a child cell using this cell and the given left and right cells
  public ICell childCell(ICell left, ICell right) {
    ArrayList<Integer> ruleList = new ArrayList<>();
    int temprule = this.rule;

    // create an 8-bit backwards binary representation of this cell's rule
    while (ruleList.size() < 8) {
      if ((temprule > 0) && (temprule % 2 == 0)) {
        ruleList.add(0);
        temprule = temprule / 2;
      } else if ((temprule > 0) && (temprule % 2 == 1)) {
        ruleList.add(1);
        temprule = (temprule - 1) / 2;
      } else {
        ruleList.add(0);
      }
    }

    return this
        .produceCell(ruleList.get(left.getState() * 4 + this.getState() * 2 + right.getState()));
  }

  // creates a cell of this type with the given state
  abstract ICell produceCell(int i);
}

// a cell that is always off
class InertCell extends ACell {
  
  // constructor
  InertCell() {
    super(0, 0);
  }

  // creates a new InertCell
  ICell produceCell(int i) {
    return this;
  }
}

// a cell that follows the 60 numerical rule
class Rule60 extends ACell {
  
  // constructor
  Rule60(int state) {
    super(state, 60);
  }

  // creates a cell of the Rule60 type with the given state
  ICell produceCell(int i) {
    return new Rule60(i);
  }
}

// a cell that follows the 30 numerical rule
class Rule30 extends ACell {

  // constructor
  Rule30(int state) {
    super(state, 30);
  }

  // creates a cell of the Rule30 type with the given state
  ICell produceCell(int i) {
    return new Rule30(i);
  }
}

// a cell that follows the 182 numerical rule
class Rule182 extends ACell {
  
  // constructor
  Rule182(int state) {
    super(state, 182);
  }

  // creates a cell of the Rule82 type with the given state
  ICell produceCell(int i) {
    return new Rule182(i);
  }
}

// a cell that follows the 54 numerical rule
class Rule54 extends ACell {
  
  // constructor
  Rule54(int state) {
    super(state, 54);
  }

  // creates a cell of the Rule54 type with the given state
  ICell produceCell(int i) {
    return new Rule54(i);
  }
}

// represents a single row of cells, with the ability to find the next generation, and draw itself
class CellArray {

  // the current cells in this single row of cells
  ArrayList<ICell> cells;

  // constructor
  // ASSUME: that cells has a size of at least 1
  CellArray(ArrayList<ICell> cells) {
    this.cells = cells;
  }

  // returns a new CellArray containing the next generation of cells, based on
  // this CellArray's cells
  CellArray nextGen() {
    ArrayList<ICell> result = new ArrayList<ICell>();

    // add an inert cell at the beginning and end of the list of cells
    this.cells.add(0, new InertCell());
    this.cells.add(new InertCell());

    // loop though the cells of the original cells list (from before adding inert
    // cells at the
    // beginning and end), and add the children to the result list
    for (int i = 1; i < this.cells.size() - 1; i += 1) {
      result.add(this.cells.get(i).childCell(this.cells.get(i - 1), this.cells.get(i + 1)));
    }

    // prevent inadvertent mutation
    this.cells.remove(0);
    this.cells.remove(this.cells.size() - 1);

    return new CellArray(result);
  }

  // draws this row of cells
  // takes in two numbers representing the width and the height of an individual
  // cell
  WorldImage draw(int width, int height) {
    WorldImage result = new EmptyImage();

    for (ICell cell : this.cells) {
      // TODO: is this modifying properly???? it seems like functional programming
      result = new BesideImage(result, cell.render(width, height));
    }
    return result;
  }
}

// a world representing the progressive generations of a CellArray
class CAWorld extends World {

  // constants
  static final int CELL_WIDTH = 10;
  static final int CELL_HEIGHT = 10;
  static final int INITIAL_OFF_CELLS = 20;
  static final int TOTAL_CELLS = INITIAL_OFF_CELLS * 2 + 1;
  static final int NUM_HISTORY = 41;
  static final int TOTAL_WIDTH = TOTAL_CELLS * CELL_WIDTH;
  static final int TOTAL_HEIGHT = NUM_HISTORY * CELL_HEIGHT;

  // the current generation of cells
  CellArray curGen;
  // the history of previous generations (earliest state at the start of the list)
  ArrayList<CellArray> history;

  // Constructs a CAWorld with INITIAL_OFF_CELLS of off cells on the left,
  // then one on cell, then INITIAL_OFF_CELLS of off cells on the right
  CAWorld(ICell off, ICell on) {
    ArrayList<ICell> cells = new ArrayList<>();

    cells.add(on);
    for (int i = 0; i < INITIAL_OFF_CELLS; i += 1) {
      cells.add(0, off);
      cells.add(off);
    }

    this.curGen = new CellArray(cells);
    this.history = new ArrayList<CellArray>();
  }

  // Modifies this CAWorld by adding the current generation to the history
  // and setting the current generation to the next one
  public void onTick() {

    // remove the oldest generation from history if history is about to be larger
    // than the screen
    if (this.history.size() == NUM_HISTORY) {
      this.history.remove(0);
    }
    this.history.add(this.curGen);
    this.curGen = this.curGen.nextGen();
  }

  // Draws the current world, ``scrolling up'' from the bottom of the image
  public WorldImage makeImage() {
    // make a light-gray background image big enough to hold 41 generations of 41
    // cells each
    WorldImage bg = new RectangleImage(TOTAL_WIDTH, TOTAL_HEIGHT, OutlineMode.SOLID,
        new Color(240, 240, 240));

    // build up the image containing the past and current cells
    WorldImage cells = new EmptyImage();
    for (CellArray array : this.history) {
      cells = new AboveImage(cells, array.draw(CELL_WIDTH, CELL_HEIGHT));
    }
    cells = new AboveImage(cells, this.curGen.draw(CELL_WIDTH, CELL_HEIGHT));

    // draw all the cells onto the background
    return new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, cells, 0, 0, bg);
  }

  // creates a scene representing the game every tick
  public WorldScene makeScene() {
    WorldScene canvas = new WorldScene(TOTAL_WIDTH, TOTAL_HEIGHT);
    canvas.placeImageXY(this.makeImage(), TOTAL_WIDTH / 2, TOTAL_HEIGHT / 2);
    return canvas;
  }
}

// example class for testing
class ExamplesAutomata {
  ACell inert;
  ACell offRule30;
  ACell onRule30;
  ACell offRule60;
  ACell onRule60;

  CellArray array30;
  CellArray array60;

  // initializes the constants
  void initData() {
    this.inert = new InertCell();
    this.offRule30 = new Rule30(0);
    this.onRule30 = new Rule30(1);
    this.offRule60 = new Rule60(0);
    this.onRule60 = new Rule60(1);

    this.array30 = new CellArray(new ArrayList<ICell>());
    this.array30.cells.add(this.onRule30);
    this.array30.cells.add(this.onRule30);
    this.array30.cells.add(this.onRule30);
    this.array30.cells.add(this.offRule30);
    this.array30.cells.add(this.offRule30);

    this.array60 = new CellArray(new ArrayList<ICell>());
    this.array60.cells.add(this.offRule60);
    this.array60.cells.add(this.onRule60);
    this.array60.cells.add(this.offRule60);
    this.array60.cells.add(this.offRule60);
    this.array60.cells.add(this.onRule60);
  }

  // tests the method childCell in the Cell class
  void testChildCell(Tester t) {
    this.initData();

    t.checkExpect(this.offRule30.childCell(this.inert, this.offRule30), new Rule30(0)); // 0
    t.checkExpect(this.offRule30.childCell(this.offRule30, this.onRule30), new Rule30(1));// 1
    t.checkExpect(this.offRule30.childCell(this.onRule30, this.inert), new Rule30(1));// 4
    t.checkExpect(this.offRule30.childCell(this.onRule30, this.onRule30), new Rule30(0));// 5

    t.checkExpect(this.onRule30.childCell(this.inert, this.offRule30), new Rule30(1)); // 2
    t.checkExpect(this.onRule30.childCell(this.offRule30, this.onRule30), new Rule30(1));// 3
    t.checkExpect(this.onRule30.childCell(this.onRule30, this.inert), new Rule30(0));// 6
    t.checkExpect(this.onRule30.childCell(this.onRule30, this.onRule30), new Rule30(0));// 7

    t.checkExpect(this.offRule60.childCell(this.inert, this.offRule60), new Rule60(0)); // 0
    t.checkExpect(this.offRule60.childCell(this.offRule60, this.onRule60), new Rule60(0));// 1
    t.checkExpect(this.offRule60.childCell(this.onRule60, this.inert), new Rule60(1));// 4
    t.checkExpect(this.offRule60.childCell(this.onRule60, this.onRule60), new Rule60(1));// 5

    t.checkExpect(this.onRule60.childCell(this.inert, this.offRule60), new Rule60(1)); // 2
    t.checkExpect(this.onRule60.childCell(this.offRule60, this.onRule60), new Rule60(1));// 3
    t.checkExpect(this.onRule60.childCell(this.onRule60, this.inert), new Rule60(0));// 6
    t.checkExpect(this.onRule60.childCell(this.onRule60, this.onRule60), new Rule60(0));// 7

  }

  // tests method childCell in Rule30 class
  void testRule30ChildCell(Tester t) {
    this.initData();

    t.checkExpect(this.onRule30.childCell(this.onRule30, this.onRule30), new Rule30(0));
    t.checkExpect(this.onRule30.childCell(this.onRule30, this.offRule30), new Rule30(0));
    t.checkExpect(this.offRule30.childCell(this.onRule30, this.onRule30), new Rule30(0));
    t.checkExpect(this.offRule30.childCell(this.onRule30, this.offRule30), new Rule30(1));
    t.checkExpect(this.onRule30.childCell(this.offRule30, this.onRule30), new Rule30(1));
    t.checkExpect(this.onRule30.childCell(this.offRule30, this.offRule30), new Rule30(1));
    t.checkExpect(this.offRule30.childCell(this.offRule30, this.onRule30), new Rule30(1));
    t.checkExpect(this.offRule30.childCell(this.offRule30, this.offRule30), new Rule30(0));
  }

  // tests method childCell in Rule60 class
  void testRule60ChildCell(Tester t) {
    this.initData();

    t.checkExpect(this.onRule60.childCell(this.onRule60, this.onRule60), new Rule60(0));
    t.checkExpect(this.onRule60.childCell(this.onRule60, this.offRule60), new Rule60(0));
    t.checkExpect(this.offRule60.childCell(this.onRule60, this.onRule60), new Rule60(1));
    t.checkExpect(this.offRule60.childCell(this.onRule60, this.offRule60), new Rule60(1));
    t.checkExpect(this.onRule60.childCell(this.offRule60, this.onRule60), new Rule60(1));
    t.checkExpect(this.onRule60.childCell(this.offRule60, this.offRule60), new Rule60(1));
    t.checkExpect(this.offRule60.childCell(this.offRule60, this.onRule60), new Rule60(0));
    t.checkExpect(this.offRule60.childCell(this.offRule60, this.offRule60), new Rule60(0));
  }

  // tests method childCell in InertCell class
  void testInertCellChildCell(Tester t) {
    this.initData();

    t.checkExpect(this.inert.childCell(this.onRule60, this.onRule60), new InertCell());
    t.checkExpect(this.inert.childCell(this.onRule60, this.offRule60), new InertCell());
    t.checkExpect(this.inert.childCell(this.offRule60, this.onRule60), new InertCell());
    t.checkExpect(this.inert.childCell(this.offRule60, this.offRule60), new InertCell());
  }

  // tests the getState method in the Cell class
  void testGetState(Tester t) {
    this.initData();

    t.checkExpect(this.inert.getState(), 0);
    t.checkExpect(this.offRule30.getState(), 0);
    t.checkExpect(this.onRule30.getState(), 1);
    t.checkExpect(this.onRule60.getState(), 1);
    t.checkExpect(this.offRule60.getState(), 0);
  }

  // tests the render method in the Cell class
  void testRender(Tester t) {
    this.initData();

    t.checkExpect(this.inert.render(0, 0),
        new RectangleImage(0, 0, OutlineMode.SOLID, Color.WHITE));
    t.checkExpect(this.offRule30.render(10, 10),
        new RectangleImage(10, 10, OutlineMode.SOLID, Color.WHITE));
    t.checkExpect(this.onRule60.render(10, 10),
        new RectangleImage(10, 10, OutlineMode.SOLID, Color.BLACK));
  }

  // tests the nextGen method in class CellArray
  void testNextGen(Tester t) {
    this.initData();

    ArrayList<ICell> array30NextCells = new ArrayList<ICell>();
    ArrayList<ICell> array30Next2Cells = new ArrayList<ICell>();
    ArrayList<ICell> array60NextCells = new ArrayList<ICell>();

    array30NextCells.add(new Rule30(1));
    array30NextCells.add(new Rule30(0));
    array30NextCells.add(new Rule30(0));
    array30NextCells.add(new Rule30(1));
    array30NextCells.add(new Rule30(0));

    array30Next2Cells.add(new Rule30(1));
    array30Next2Cells.add(new Rule30(1));
    array30Next2Cells.add(new Rule30(1));
    array30Next2Cells.add(new Rule30(1));
    array30Next2Cells.add(new Rule30(1));

    array60NextCells.add(new Rule60(0));
    array60NextCells.add(new Rule60(1));
    array60NextCells.add(new Rule60(1));
    array60NextCells.add(new Rule60(0));
    array60NextCells.add(new Rule60(1));

    CellArray array30Next = new CellArray(array30NextCells);
    CellArray array30Next2 = new CellArray(array30Next2Cells);
    CellArray array60Next = new CellArray(array60NextCells);

    t.checkExpect(this.array30.nextGen(), array30Next);
    t.checkExpect(array30Next.nextGen(), array30Next2);
    t.checkExpect(this.array60.nextGen(), array60Next);

  }

  // test the draw method in class CellArray
  void testDraw(Tester t) {
    WorldImage img = new EmptyImage();

    for (ICell cell : this.array30.cells) {
      img = new BesideImage(img, cell.render(10, 10));
    }
    t.checkExpect(this.array30.draw(10, 10), img);

    WorldImage img2 = new EmptyImage();

    for (ICell cell : this.array60.cells) {
      img2 = new BesideImage(img2, cell.render(10, 10));
    }
    t.checkExpect(this.array60.draw(10, 10), img2);
  }

  // tests the constructor for class CAWorld
  void testCAWorldConstructor(Tester t) {
    CAWorld test30World = new CAWorld(new Rule30(0), new Rule30(1));

    t.checkExpect(test30World.history.size(), 0);

    ArrayList<ICell> cells = new ArrayList<>();

    cells.add(new Rule30(1));
    for (int i = 0; i < 20; i += 1) {
      cells.add(0, new Rule30(0));
      cells.add(new Rule30(0));
    }

    t.checkExpect(test30World.curGen, new CellArray(cells));
  }

  // test the onTick method in class CAWorld
  void testCAWorldOnTick(Tester t) {

    CAWorld inertWorld = new CAWorld(new InertCell(), new InertCell());

    ArrayList<ICell> inertWorldGen = new ArrayList<>();
    for (int i = 0; i < 41; i += 1) {
      inertWorldGen.add(new InertCell());
    }

    t.checkExpect(inertWorld.history.size(), 0);
    t.checkExpect(inertWorld.curGen, new CellArray(inertWorldGen));

    inertWorld.onTick();

    t.checkExpect(inertWorld.history.size(), 1);
    t.checkExpect(inertWorld.curGen, new CellArray(inertWorldGen));
    t.checkExpect(inertWorld.history.get(0), new CellArray(inertWorldGen));

    inertWorld.onTick();

    t.checkExpect(inertWorld.history.size(), 2);
    t.checkExpect(inertWorld.curGen, new CellArray(inertWorldGen));
    t.checkExpect(inertWorld.history.get(0), new CellArray(inertWorldGen));
    t.checkExpect(inertWorld.history.get(1), new CellArray(inertWorldGen));

    for (int i = 0; i < 40; i += 1) {
      inertWorld.onTick();
    }

    t.checkExpect(inertWorld.history.size(), 41);

    inertWorld.onTick();

    t.checkExpect(inertWorld.history.size(), 41);
  }

  // test method produceCell in ACell class
  void testProduceCell(Tester t) {
    t.checkExpect(this.onRule30.produceCell(0), this.offRule30);
    t.checkExpect(this.onRule30.produceCell(1), this.onRule30);
    t.checkExpect(this.onRule60.produceCell(0), this.offRule60);
    t.checkExpect(this.onRule60.produceCell(1), this.onRule60);
    t.checkExpect(this.inert.produceCell(0), this.inert);
    t.checkExpect(this.inert.produceCell(1), this.inert);

  }

  void testBigBang(Tester t) {
    CAWorld testWorld = new CAWorld(new Rule182(0), new Rule182(1));

    testWorld.bigBang(CAWorld.TOTAL_WIDTH, CAWorld.TOTAL_HEIGHT, 0.1);
  }
}
