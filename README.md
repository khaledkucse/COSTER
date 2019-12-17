# COSTER
COSTER stands for Context Sensitive Type Solver. The tool finds the fully qualified name of the API element from the source code snippet.

## Getting Started

The project takes the java projects stored at subject_systems folder as input and based on the configuration written in src/main/java/org/srlab/coster/config/Config.java, it collects local and global context for each API element using Eclipse JDT and create a dataset. Next using the dataset an user can do following four functionalities:
 
1. Train: Train the Occurance Likelihood Dictonary(OLD) and store all the contexts with scores in index files.
2. Retrain:  Retrive the trained models and retain the models with more subject systems or APIs
3. Intrinsic Evaluation: Calculate the precision, recall and f score for any testing subject system.
4. Extrinsic Evaluation: Calculate the precision, recall and f score for any compilable Stack Overflow code.
5. Infer Type: From a non compilable Stack Overflow code given with a ``.java`` file, it infers Type of each code element and generate a output file where the types are annotated over the API elements using bracket.


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


Before talking the step wise execution of each mechanism, let me describe the program arguments user need to know
run each functionalities.


#### Arguments:

-c,--contsim <Context Similarity Function [Optional]>   Similarity functions used for context similarity. User can choose one from: cosine (default), jaccard, lcs

-d,--dataset <Dataset Path [Optional]>                  Path of the Intermediate dataset created by COSTER during training, retraining and evaluation. Default location is in /data directory.

-e,--evaluationType <Evaluation Types>                  Types of evaluation. Option: intrinsic, extrinsic

-f,--functionality <Functionality>                      Types of functionalities the tool will run for. Option: train, retrain, infer, eval

-h,--help                                               Prints this usage information.

-i,--input <Input File>                                 Location of Input File

-j,--jarPath <Jar Path [Optional]>                      Path of the directory where jar files are stored. Used by all functionality. Default location is in /data directory

-m,--modelPath <Model Path [Optional]>                  Directory path where trained Occurance Likelihood Dictonary and index files are stored after training and retraining.This path is also required
                                                        during inference and evaluation.Default Localtion is is/model directory

-n,--namesim <Name Similarity Function [Optional]>      Similairty function used for name similarity.User can choose one from: levenshtein (default), hamming, lcs

-o,--output <Output File>                               Location of Output File

-q,--fqnThreshold <FQN Threshold[optional]>             Threshold value to select the number of context used to filter FQNs while Creating OLD.Default: 50

-r,--repositoryPath <Repository Path [Optional]>        The path of the repository where the subject system for training, retraining and evaluation are stored.Default location is in the /data
                                                        directory.
-t,--top <Top-K [Optional]>                             Number of suggestion the tool generates duting inference and evaluation. default: 1



#### Arguments for running the tool for each functionality:
Bellow we take about the arguments the user need to execute each of the five functionalities of COSTER.
If a user choose to run the tool using IDE, (s)he needs to put either mandatory or full argument in the run configuration.
If the user choose to run the jar file or the docker image, (s)he needs to put the argument after ``java - jar COSTER.jar`` command.
Mandatory version of the argument means the user must put that to execute the functionality with default values. 
Full version dictates the arguments the user can tweak to get different results.


##### Train:
Mandatory:
```
-f train
```
Full:
```
-f train -r <repository path> -j <jar repository path> -d <dataset path> -m <model path> -q <FQN threshold>
```

##### Retrain:
Mandatory:
```
-f retrain
```
Full:
```
-f retrain -r <repository path> -j <jar repository path> -d <dataset path> -m <model path> -q <FQN threshold>
```

##### Intrinsic Evaluation:
Mandatory:
```
-f eval -e intrinsic
```
Full:
```
-f eval -e intrinsic -r <repository path> -j <jar repository path> -d <dataset path> -m <model path> -t <top-k> -c <context similarity function> -n <name similarity function>
```

##### Extrinsic Evaluation:
Mandatory:
```
-f eval -e extrinsic
```
Full:
```
-f eval -e extrinsic -r <repository path> -j <jar repository path> -d <dataset path> -m <model path> -t <top-k> -c <context similarity function> -n <name similarity function>
```


##### File Infer:
Mandatory:
```
-f infer -i <input file path> -o <output file path>
```
Full:
```
-f infer -i <input file path> -o <output file path> -j <jar repository path> -m <model path> -t <top-k> -c <context similarity function> -n <name similarity function>
```

### Running from the code base:

A step by step instruction is given in the following

Step 1

```
Open src/main/java/org/srlab/coster/config/Config.java
Check the global variables.
Most important is:
private static final String ROOT_FOLDER = "path/to/your/project/folder";
```

Step 2: 
Download the data folder and model folder from the following Google Drive link:

[coster_data](http://bit.ly/2EqTiOk)

[coster_model](http://bit.ly/2PuqkmI)

Extract them into the data and model directory respectively.


Step 3: 
```
Open src/main/java/org/srlab/coster/COSTER.java
Run main function with appropriate program arguments
```

### Running the executable jar file
A step by step instruction is given in the following

Step 1

Same as the Step 2 of previous mechanism dowload the [coster_data](http://bit.ly/2EqTiOk)
and [coster_model](http://bit.ly/2PuqkmI)
and extract them on the folders

Step 2

Run the following command to exevute the jar file
```
java -jar COSTER.jar <program arguments>
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

after a while you will see the latest version of the image is pulled in your engine. If you want you can check using following commad:

```
docker images
```

Step 3

Run the following command to run the docker image. The following will delete the docker container after you exit. If you want to keep the docker container do neccessary changes
```
docker run -it --rm khaledkucse/coster
```

Step 4

Now you are at the khaledkucse/coster container. 
The images is built using alpine linux. SO any command such as ``ls`` can give you all flexibility to explore the container.
 
Now run the following command to run the executable jar inside the docker container

```
java -jar COSTER.jar <program arguments>
```



## Issues:

If Memory Limit Exception occurs it means your cache is full with the previous data. One solution can be remove the projects that are parsed already from the dcc_subject_systems folder and rerun the ModelEntryCollectionDriver.java file. Or you can run the project in a computer with more memory.

For other issue please create an [issue](https://github.com/khaledkucse/COSTER/issues). We will look forward to solve it.

## Built With

* [EclipseJDT](https://github.com/eclipse/eclipse.jdt.core)
* [Simple-JSON](https://mvnrepository.com/artifact/com.googlecode.json-simple/json-simple)
* [Apache Commons](https://commons.apache.org/)


## Authors

* **C M Khaled Saifullah** - *Initial work* - [khaledkucse](https://github.com/khaledkucse)

See also the list of [contributors](https://github.com/khaledkucse/COSTER/graphs/contributors) who participated in this project.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details


