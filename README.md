RkNN-embedding
==============

A comparison between RkNN-retrieval on graphs (Eager algorithm) and its embedded version in R*-Trees with materialized reference points (modified TPL algorithm)


#### by [FlorianLiebhart](https://github.com/FlorianLiebhart/)
--------


## Coverage of this project
- Java/Scala implementation of naive RkNN retrieval in graphs
- Scala implementation of the Eager algorithm for efficient RkNN retrieval in large graphs, as introduced [in the publication by Yiu, Mamoulis, Papadias and Tao](http://www4.comp.polyu.edu.hk/~csmlyiu/conf/ICDE05-RNN.pdf)
- Java implementation of the TPL algoirthm for efficient RkNN retrieval in R-Trees (implemented not by me, but by another student of LMU)
- GUI for starting these (and several other graph-) algorithms (implemented not by me, but by another student of LMU)
- Scala implementation of a performance comparison between 
    - RkNN-retrieval on graphs using the Eager algorithm
    and 
    - RkNN-retrieval on graphs embedded in an R*-Tree, with materialized reference points, using a modification of the TPL algorithm
    

## Development and Usage

### Getting started

1. Check out this git respository: 
```git checkout git@github.com:FlorianLiebhart/RkNN-embedding.git```
2. Download elki.jar and put it into the lib folder of this project
3. Run ```sbt update``` to download all necessary sbt dependencies (except ELKI, which needed to be downloaded manually).
4. Run ```sbt compile``` from the command line to compile this project.
5. Then, run the main method in src/main/scala/app/RkNNComparator.scala to start the comparison; Or start the GUI in src/main/java/app/GUIStarter.java

Note: Step 4. needs a little know how to do from the command line.
Best using IntelliJ Idea v. 13.1: Create IDEA project files with the [sbt-idea plugin](https://github.com/mpeltonen/sbt-idea) using the command ```sbt gen-idea```.
You should also install the sbt plugin within IntelliJ Idea.


### Running Tests

Run ```sbt test``` from the command line to run the tests. (some eager tests might still fail, because they may not be up to date)



## Technology stack:

### Build Tool
- [SBT v. 0.13.0](http://www.scala-sbt.org/0.13.0/docs/home.html) [`open source`](https://github.com/sbt/sbt)
  * easy to use build tool for Scala, Java, and more. 

### DBS & Datastructures
- [ELKI v. 0.6.0](elki.dbs.ifi.lmu.de) `open source`
  * data mining software written in Java. The focus of ELKI is research in algorithms, with an emphasis on unsupervised methods in cluster analysis and outlier detection. 

### Implementations
- [Scala v. 2.10.3](http://www.scala-lang.org/) [`open source`](https://github.com/scala/scala)
  * modern object-oriented, functional programming language.
   * [API Documenation](http://www.scala-lang.org/api/2.10.3/#package)
- [Java v. 7](http://java.com/de/) `open source`
