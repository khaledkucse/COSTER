# DAMCA Context Colelctor
DAMCA Context Collector(DCC) branch is a java program coded using symbol solver api of java parser system. 
The code generate AST information for DAMCA, SLP and SLAMC systems for any java project.


## Getting Started

The project takes the java projects stored at subject_systems folder as input and based on the configuration written in src/main/java/org/srlab/damca/config/Config.java, it collects AST information for DAMCA, SLP and SLAMC system. The program is based on symbol solver api of java parser tool. 

### Prerequisites

Before running the code, you must have following packages installed in your computer:

```
java8 or later
maven
```

### Installing

A step by step instruvtion is given in the following

Step 1

```
Open src/main/java/org/srlab/damca/config/Config.java
change the global variables
Most important is:
private static final String ROOT_FOLDER = "path/to/your/project/folder";
```
Besides to enble the data collection for any system(DAMCA, SLP or SLAMC) yo need to trigger following parameters:
 ```
 public static final Boolean IS_DAMCA_COLLECT = Boolean.FALSE;
 public static final Boolean IS_SLP_COLLECT = Boolean.FALSE;
 public static final Boolean IS_SLAMC_COLLECT = Boolean.TRUE;
 ```

Step 2: 
Delete(or store at another place) the dcc_log.txt file, all files in dcc_models, dcc_damca_dataset,dcc_slp_dataset and dcc_slamc_dataset files.

Put the java projects you want to parse at dcc_subject_systems folder. It is reccomended to put the whole projects in a folder.
You need not built the project.

Step 3: 
```
Open src/main/java/org/srlab/damca/completioner/ModelEntryCollectionDriver.java
Run main function
```

Based on the controlling parameters(IS_DAMCA_COLLECT,IS_SLP_COLLECT,IS_SLAMC_COLLECT) the program will return the dataset randomly organized in 10 folds at damca_dataset, slp_dataset and slamc_dataset. Moreover for every project individual dataset will be stored at models folder.  

## Issues:

If Memory Limit Exception occurs it means your cache is full with the previous data. One solution can be remove the projects that are parsed already from the dcc_subject_systems folder and rerun the ModelEntryCollectionDriver.java file. Or you can run the project in a computer with more memory.

For other issue please create an issue. We will look forward to solve it.

## Built With

* [JavaParser](https://github.com/javaparser/javaparser)


## Authors

* **C M Khaled Saifullah** - *Initial work* - [khaledkucse](https://github.com/khaledkucse)

See also the list of [contributors](https://github.com/khaledkucse/DeepAPIMethodCallReccomendation/graphs/contributorss) who participated in this project.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details


