package elkiTPL;

import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.distance.DistanceDBIDList;
import de.lmu.ifi.dbs.elki.database.query.distance.SpatialDistanceQuery;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.FileBasedDatabaseConnection;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancevalue.DoubleDistance;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.AbstractRTreeSettings;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar.RStarTreeIndex;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar.RStarTreeNode;
import de.lmu.ifi.dbs.elki.persistent.LRUCache;
import de.lmu.ifi.dbs.elki.persistent.MemoryPageFile;
import de.lmu.ifi.dbs.elki.persistent.PageFile;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class Simulation {
  private static RStarTreeIndex dbIndex;
  private static SpatialDistanceQuery distanceQuery;
  private static EuclideanDistanceFunction distanceFunction;
  private static LRUCache c1;



  private static Database[] createDatabase(String queryFile, int pagesize){

    distanceFunction = EuclideanDistanceFunction.STATIC;


    ListParameterization inputparamsQuery = new ListParameterization();
    inputparamsQuery.addParameter(FileBasedDatabaseConnection.Parameterizer.INPUT_ID, queryFile); //synthetic data
    //inputparamsQuery.addParameter(OptionID.DATABASE_CONNECTION, GeneratorXMLDatabaseConnection.class);

    Database dbQuery = ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, inputparamsQuery);
    inputparamsQuery.failOnErrors();
    dbQuery.initialize();

    Relation<DoubleVector> rQuery = dbQuery.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);


    System.out.println("Size of relation: "+rQuery.size());

    System.out.println("Building R*-Tree...");
    long time = System.nanoTime();
    time = System.nanoTime();
    AbstractRTreeSettings settings = new AbstractRTreeSettings();
    PageFile<RStarTreeNode> pageFile = new MemoryPageFile<RStarTreeNode>(pagesize);
    dbIndex = new RStarTreeIndex<DoubleVector>(rQuery, pageFile, settings);
    //dbIndex.insertAll(rQuery.getDBIDs());
    time = System.nanoTime()-time;
    System.out.println("R*-Tree built in "+time/1E6+"ms.");
    System.out.println("objects: "+dbIndex.getRoot().getNumEntries());


    distanceQuery = distanceFunction.instantiate(rQuery);


    return new Database[]{dbQuery};
  }



  public DistanceDBIDList<DoubleDistance> simulate(String file, int pagesize, int k, int dimension, boolean withClipping) {

    Database[] db = createDatabase(file, pagesize);
    Relation<DoubleVector> relation = db[0].getRelation(TypeUtil.NUMBER_VECTOR_FIELD);

    // Query
    double[] coordinates = new double[dimension];
    for (int i = 0; i < dimension; i++){
      coordinates[i] = Math.random();
    }
    DoubleVector q = new DoubleVector(coordinates);
    System.out.println("Query: "+q);

    long time = System.nanoTime();
    time = System.nanoTime();
    System.out.println("Performing RkNN-Query...");

    DistanceDBIDList<DoubleDistance> result = null;

    GenericTPLRkNNQuery gtpl = new GenericTPLRkNNQuery(dbIndex, distanceQuery, withClipping);
    result = gtpl.getRKNNForObject(relation.get(getDBObjectAsQueryObject(relation)), k);

    return result;
  }


    // Generiert CSV Datei
    // Beispiel-CSV:
    // 0.3;0.22;0.9;
  public void generate(int dimension, int numPoints, String file) throws IOException {
    FileWriter fw = new FileWriter(file);
    BufferedWriter out = new BufferedWriter(fw);

    for (int i = 0; i < numPoints; i++){
      String newline = "";
      for (int j = 0; j < dimension; j++){
        double number = Math.random();
        newline = newline + number + ";";
      }
      if (i != 0){
        newline = "\n" + newline;
      }
      out.write(newline);
    }

    out.close();
  }

  // Gets a random object from the db
  public DBID getDBObjectAsQueryObject(Relation<DoubleVector> r){
    DBIDIter iter = r.getDBIDs().iter();
    int random = (int) (Math.random() * r.size() + 1);
    for (int i = 0; i < random && iter.valid(); i++){
      iter.advance();
    }
    System.out.println("Query: "+iter);

    return DBIDUtil.deref(iter);
  }
}