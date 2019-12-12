# COSTER
COSTER stands for Context Sensitive Type Solver. The tool finds the fully qualified name of the API element from the source code snippet.

## Getting Started

The project takes the java projects stored at subject_systems folder as input and based on the configuration written in src/main/java/org/srlab/coster/config/Config.java, it collects local and global context for each API element using Eclipse JDT and create a dataset. Next using the dataset an user can do following four dunctionalities:
 
1. Train COSTER models
2. Retrain the models with more subject systems
3. Intrinsic Evaluation
4. Extrinsic Evaluation 

### Prerequisites

Before running the code, you must have following packages installed in your computer:

```
java 8
maven
```

### Installing
We support three types of execution mechanism. These are:
1. Running from the code base
2. Running the executable jar file
3. Running the docker file


### Running from the code base:

A step by step instruction is given in the following

Step 1

```
Open src/main/java/org/srlab/coster/config/Config.java
change the global variables
Most important is:
private static final String ROOT_FOLDER = "path/to/your/project/folder";
```

Step 2: 
Download the data folder and model folder from the following Google Drive link:

[coster_data](https://drive.google.com/open?id=1Nbei0Y0aURAyc7AgVo4V8wW8kv-6baF5)

[coster_model](https://drive.google.com/open?id=1bVjk5BgX9Hcc5BBeyxjok0a5yH7lqxnQ)

Extract them into the project folder.


Step 3: 
```
Open src/main/java/org/srlab/coster/COSTER.java
Run main function
```
It will run through all four functionalities in the console

### Running the executable jar file
A step by step instruction is given in the following

Step 1

Same as the Step 2 of previous mechanism dowload the [coster_data](https://drive.google.com/open?id=1Nbei0Y0aURAyc7AgVo4V8wW8kv-6baF5)
and [coster_model](https://drive.google.com/open?id=1bVjk5BgX9Hcc5BBeyxjok0a5yH7lqxnQ)
and extract them on the project folder

Step 2

Run the following command to exevute the jar file
```
java -jar COSTER.jar
```



### Running the docker file
A docker image is created for the user to avoid all the complexity of downloading the folders and all.

Docker image is located at [khaledkucse/coster](https://hub.docker.com/r/khaledkucse/coster)

A step by step instruction is given in the following

Step 1

Install docker engine in your machine. Its very simple and brief documentation is present at [Docker documentation](https://docs.docker.com/v17.09/engine/installation/).

Step 2

Run the following command to pull COSTER's docker image into your docker engine
```
docker pull khaledkucse/coster
```

after a while you will see the image is pulled in your engine. If you want you can check using following commad:

```
docker images
```

Step 3

Run the following command to run the docker image. The following will delete the docker container after you exit. If you want to keep the docker container do neccessary changes
```
docker run -it --rm khaledkucse/coster
```

Step 4

Now you are at the khaledkucse/coster container. just "ls" to see the content inside of it. Now run the following command to run the executable jar inside the docker container

```
java -jar COSTER.jar
```



## Issues:

If Memory Limit Exception occurs it means your cache is full with the previous data. One solution can be remove the projects that are parsed already from the dcc_subject_systems folder and rerun the ModelEntryCollectionDriver.java file. Or you can run the project in a computer with more memory.

For other issue please create an issue. We will look forward to solve it.

## Built With

* [EclipseJDT](https://github.com/eclipse/eclipse.jdt.core)
* [Simple-JSON](https://mvnrepository.com/artifact/com.googlecode.json-simple/json-simple)
* [Apache Lucene](https://lucene.apache.org/)


## Authors

* **C M Khaled Saifullah** - *Initial work* - [khaledkucse](https://github.com/khaledkucse)

See also the list of [contributors](https://github.com/khaledkucse/COSTER/graphs/contributorss) who participated in this project.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details


