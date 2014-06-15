package elkiTPL;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.FileBasedDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.filter.FixedDBIDsFilter;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.AbstractRTreeSettings;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar.RStarTreeIndex;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar.RStarTreeNode;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.strategies.bulk.SortTileRecursiveBulkSplit;
import de.lmu.ifi.dbs.elki.persistent.MemoryPageFile;
import de.lmu.ifi.dbs.elki.persistent.PageFile;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

public class Utils {

  /**
   * Creates a file based database at given dbFilePath
   * @param dbFilePath Path where file based database should be stored
   * @return The database created
   */
  public static Database[] createDatabase(String dbFilePath){

    // create config
    ListParameterization parametrizationConfig = new ListParameterization();
    parametrizationConfig.addParameter(FileBasedDatabaseConnection.Parameterizer.INPUT_ID, dbFilePath); //synthetic data
    List<Class<?>> filterlist = new ArrayList<>();
    filterlist.add(FixedDBIDsFilter.class);
    parametrizationConfig.addParameter(FileBasedDatabaseConnection.Parameterizer.FILTERS_ID, filterlist);
    parametrizationConfig.addParameter(FixedDBIDsFilter.Parameterizer.IDSTART_ID, 0);

    // create and initialize database
    Database db = ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, parametrizationConfig);
    parametrizationConfig.failOnErrors();
    db.initialize();

    return new Database[]{db};
  }

  /**
   * Creates and returns an R*Tree index for a given relation
   * @param relation Relation which should be indexed by the R*-Tree
   * @param pageSize Page size of the R*-Tree as number of bytes (e.g. 1024 bytes)
   * @return
   */
  public static RStarTreeIndex<DoubleVector> createRStarTree(Relation<DoubleVector> relation, int pageSize){
    // Print out nodes and distances. This is also to check if the DBIDs correspond to the line numbers in the CSV file TODO: Write test instead of this!
    /*DBIDIter iter = relation.getDBIDs().iter();
    while (iter.valid()){
      System.out.println(iter.internalGetIndex() + " : " + relation.get(iter));
      iter.advance();
    }*/

    PageFile<RStarTreeNode> memoryPageFile      = new MemoryPageFile<RStarTreeNode>(pageSize);
    AbstractRTreeSettings settings              = new AbstractRTreeSettings();
    settings.setBulkStrategy(SortTileRecursiveBulkSplit.STATIC);
    RStarTreeIndex<DoubleVector> rStarTreeIndex = new RStarTreeIndex<DoubleVector>(relation, memoryPageFile, settings);
    rStarTreeIndex.initialize();

    return rStarTreeIndex;
  }


  /**
   * Generates a CSV file with "numPoints" random vectors in "dimensions" dimensions.
   * Example CSV: 0.3;0.22;0.9;
   * @param dimensions
   * @param numPoints
   * @param csvPath
   * @throws IOException
   */
  public static void generateRandomCSVFile(int dimensions, int numPoints, String csvPath) throws IOException {
    // create directories and file if non-existent
    Path pathToFile = Paths.get(csvPath);
    Files.createDirectories(pathToFile.getParent());
    if (!Files.exists(pathToFile))
        Files.createFile(pathToFile);
    FileWriter fw = new FileWriter(csvPath, false); // false = overwrite current file content
    BufferedWriter out = new BufferedWriter(fw);

    NumberFormat doubleUKformatter = new DecimalFormat("#0.00", new DecimalFormatSymbols(Locale.UK));

    for (int i = 0; i < numPoints; i++){
      String newline = "";
      for (int j = 0; j < dimensions; j++){
//        newline = newline + Math.random() + ";"; // original line, replaced by the following two lines, which create only two digit numbers:
        newline = newline + doubleUKformatter.format(Math.random()) + ";";
      }
      if (i != 0){
        newline = "\n" + newline;
      }
      out.write(newline);
    }

    out.close();
  }

  /**
   * Gets a random object's DBID from the DB
   * @param relation The database relation
   * @return the DBID from the random object received from the DB
   */
  public static DBID getRandomDBObject(Relation<DoubleVector> relation){
    DBIDIter iter = relation.getDBIDs().iter();
    int randomNumber = (int) (Math.random() * relation.size() + 1);
    for (int i = 0; i < randomNumber && iter.valid(); i++){
      iter.advance();
    }

    return DBIDUtil.deref(iter);
  }
}