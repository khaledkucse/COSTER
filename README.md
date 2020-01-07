# COSTER
Context-Sensitive Type Solver Tool for Finding the Fully Qualified Name of the API Element in Online Code Snippets

Code snippets available in community question answering sites (CQA) are a great source of learning how to use APIs. However, it is difficult to determine what APIs are discussed in those online code snippets because they often lack declaration and import statements. We introduce COSTER, a context sensitive type solver that can find Fully Qualified Names (FQN) of API elements used in those code snippets. The tool uses three different similarity measures to rank the potential FQNs of query API elements. Our quantitative evaluation and user study demonstrate that the tool can help developers to reuse API usage examples by suggesting required import statements to compile the code. Inferring types of API elements that can help researchers and tool developers focus on developing techniques to precisely locate API usage examples.

## Motivating Example

Consider the following example where a code snippet from a  Stack Overflow post (post id: 20157996) (on the top) is used by our tool to find the FQNs of the API elements in it (on the bottom).

![Code snippet from a Stack Overflow ](/images/motivating.png)

The  Stack Overflow  code snippet on the top suffers from  declaration, external reference and name ambiguities. For example, API elements such as dFact, build, and doc do not have  declaration statements that causes the declaration ambiguity. API elements, such as DocumentBuilderFactory, Result, and Elemen do not have the import statements that are necessary to compile the code, causing the external reference ambiguity. The name Element matches with five different Element classes in JDK, causing the name ambiguity. Thus learning API usages from such an ambiguous code snippet is hard for developers and researchers. Resolving the FQNs of API elements can help to understand the API usages and also help to reuse the code snippet. To mitigate the problem, we build the tool named COSTER that finds Fully Qualified Names (FQNs) of API elements in the online code snippets. Our tool infer three types (i.e., FQNs) of API elements: Class objects, Field calls, and  Method calls. For example, COSTER identifies the FQN of the class object dfact (see line 2) as javax.xml.parsers.DocumentBuilderFactory. COSTER also determines the FQNs of method calls add.append(…) and doc.createTextNode(…) (see line 12) as org.w3c.dom.Element and org.w3c.dom.Document,respectively. Once the FQNs of API elements are determined, COSTER can identify the missing import statements also.



## Feature of the Tool

Before running the code, you must have following packages installed in your computer:

COSTER  supports following four features:

1. Infer Types: Determine types or FQNs of API elements. We use the term type or FQN interchangeably throughout the discussion.
2. Complete Import Statements: Add missing import statements
3. Training COSTER: This enables the tool to support type inference of new APIs
4. Evaluation: Researchers and tool developers can also use the tool to evaluate the performance of COSTER. This enables to compare COSTER with other type inference techniques.

![Architecture of COSTER](/images/COSTERTOOL.png)

The tool consists of five major components (shown as orange colored rectangular box) and four features (shown as green colored diamond box). The model generator component extracts the API elements present in the subject systems at the codebase based on the jar files present at the Jar repository. It then collects the context of each API element and sends the context to the model manager. The model manager calculates the occurrence likelihood score for each Fully Qualified Name (FQN) and stores the context along with the score in an inverted index we called it as a model. Our tool supports the feature training and retraining the model using the model generator component.  By retraining we mean, the user can update Occurrence Likelihood Dictionary supporting new APIs and subject systems.


Query generator collects the API elements and associated contexts from the online forums’ code snippets. The context of each API element is passed into the candidate list processor that takes the trained model as input and generates the candidate list for each test case and refines the candidates based on the likelihood, context similarity, and name similarity scores.  The top-k recommendations returns a ranked list of top-k FQNs(s) for each case that provide three features: inferring types of API element, completing the import statements and evaluating the tool. Infer types feature annotate the API elements within the code snippet and output the code along with the annotation in a file (similar to the right side of the motivating example). Import statement completions is done through an Eclipse plugin that includes the import statements before the code snippet upon pasting it in the Eclipse IDE. Evaluation features enables the user to recreate our evaluation results and uses them for future studies.

###Download

The tool is available in two forms: a) Command line tool b) Eclipse plugin

Command line tool provides three features: Infer types, Training COSTER and Evaluation and Eclipse Plugin provides the three feature: Complete Import Statement, Infer types and Training COSTER.

Our tool can be downloaded/cloned through this [github repository](https://github.com/khaledkucse/COSTER).
 
The user needs to have data and model for executing all three features of command line tool. Those can be downloaded from the following links:

Data: [COSTER Data](http://bit.ly/costerData) 

Model: [COSTER MODEL](http://bit.ly/costerModel)


###Installing
To install the tool, following requirements need to be fulfilled.

```
Oracle java 8
maven
```
For installing the tool from GitHub repository run the following command to clone the repository:

```
git clone https://github.com/khaledkucse/COSTER.git
```

###Running the command line tool:
To run the command line tool from Code, the user needs to import all dependency of the tool using Maven and download the coster_data and coster_model. Next (s)he extracts the files into data and model directories respectively and runs the following class file:
```
src/main/java/org/srlab/coster/COSTER.java
```
N.B: User needs to provide some program arguments (discussed later).


To run the command line tool using executable jar file, user needs to download the above mentioned Data and Model files and extracts them in respective places and run the following command to run the jar file
```
java -jar COSTER.jar <program arguments>
```


Let us see some examples of executing all three features of command line tool.

1. Infer Types: A user wants to learn the FQNs of APIs used in a SO code. (s)He can puts the code into a file named input.java, place the file within the tool directory and run the following command:

```
java -jar COSTER.jar -f infer -i input.java -o output.java -j data/jars/so/ -m model/ -t 1 -c cosine -n levenshtein
```

Here functionality is chosen as infer, path to input file is input.java and path to output file is output.java, path to the jar files repository at data/jars/so/, path to the models model/, number of top-k suggestions 1, method for context similarity is Cosine and method for name similarity is Levenshtein distance. Execution trace of following figure will appear.

![Usages example of Infer Type feature](/images/infer.png)

First some info related to the tool are shown. Next, the tool collects jar files, trained model files and input code snippet form input.java. It then extracts the code, collects potential API elements and infers them. Finally the annotated API elements along with the full code snippet is generated in output.java file.


2. Train: A user can reproduce our model and add new APIs with our trained model using train feature. To do that user needs to run the following command:

```
java -jar COSTER.java -f train -r data/GitHubDataset/subjectSystem/ -j data/jars/github/ -d data/GitHubDataset/dataset/ -m model/ -q 50 -k 0
```

Following execution trace will appear once the command is executed successfully.

![Usages example of Training of COSTER model feature](/images/train.png)


Similar to previous feature it will show some information related to the tool at first. Next it extracts new subject systems, create training dataset, retrieve the trained model, and retrain the model.

3. Evaluation: Evaluation of the tool is done so that the user can reproduce the result we reported in our papers.  Two types of evaluation is supported. Intrinsic evaluation where users can evaluate the tool for the subject system and extrinsic evaluation where user can evaluate COSTER for StackOverflow code snippets. Let us consider the intrinsic evaluation and to do that user needs to use the following command:

```
java -jar COSTER.jar -f eval -e intrinsic -r data/TestDataset/subjectSystem/ -j data/jars/github/ -d data/TestDataset/dataset/ -t 1 -m model/ -c cosine -n levenshtein
```

After the successful run of the command following execution trace will appear.

![Usages example of Evaluation of COSTER feature](/images/eval.png)

The tool collects the model files, code snippets for evaluation and jar files. Next it extracts the code snippets and prepares a dataset of all test cases. Finally it infers test cases, calculates the performance metrics and shows them.



Option | Description | Default values
------------ | ------------- | ---------
-c|	Similarity Functions for Context Similarity|	cosine (default), jaccard, lcs
-d|	Path to the intermediate dataset|	data/GitHubDataset/dataset/
-e|	Types of evaluation|	intrinsic, extrinsic
-f|	Type of functionality|	train, retrain, infer, eval
-h|	Prints the usage information|	disabled
-i|	Input file path for infer functionality|	required
-j|	Pat to the Jar repository|	data/jars/github
-m|	Path to the Model directory|	model/
-n|	Similarity functions for Name similarity|	levenshtein (default), hamming, lcs
-o|	Output filepath for infer functionality|	required
-q|	A threshold value of the minimum number of contexts for an FQN to be trained|	50
-r|	Path to the repository/code base|	data/GIthybDataset/subjectSytem
-t|	Integer representing the k in Top-k recommendation|	1



The arguments are very straightforward to use. For example “-c” means the similarity method for context similarity. In our proposed technique, we use Cosine similarity that calculates the cosine distance between context of the test case and each candidate. However, the user can use other string similarity metrics such as Jaccard similarity index or Longest Common Subsequence (LCS) similarity methods. Similarly, user can use Hamming distance or LCS for name similarity rather than the default Levenshtein distance in case of name similarity using “-n” argument.




#### Docker Images
To ease the user hassles of creating similar environment, we also provide a docker image that has a similar environment will all dependencies and files. Docker image can be found in the following docker hub repository.

[Docker Image](https://hub.docker.com/r/khaledkucse/coster)

To run our docker image the user only need to install Docker Engine, a lightweight operating system over the host operating system to store docker images, create docker containers form the images and running the containers using operating system virtualization technology. Detailed documentation about Docker and how to install the Docker Engine can be found [here](https://docs.docker.com/).

For installing the docker image of COSTER, run the following command to pull the image from docker hub into the docker engine:

```
docker pull khaledkucse/coster:1.0.0
```

Next, use the following command to run the docker image

```
docker run -it --name coster khaledkucse/coster:1.0.0
```


The command creates a docker container named coster and run the container.  Inside the container, use the following command to execute the features of command line tool.

```
java -jar COSTER.jar <program arguments>
```


## Publication
Our paper on COSTER has been accepted as a full technical paper in ASE 2019, where we not only explain the technique in detail but also compare the technique with other state-of-the-art techniques and analyze why our technique is comparatively better the compared technique using five different analyses. The data, model, and code used in our experiment are available to download. In case you want to use those to replicate the study or in a different study please feel free to contact us.

You can find more details on our technique in the following link:

[COSTER Paper](http://bit.ly/costerPaper)

C M Khaled Saifullah, M. Asaduzzaman, Chanchal K. Roy. Learning from Examples to Find Fully Qualified Names of API Elements in Code Snippets, Accepted to be published in 34th IEEE/ACM International Conference on Automated Software Engineering, 243-254., 2019.

#### Citation
@inproceedings{coster,
  title={Learning from Examples to Find Fully Qualified Names of API Elements in Code Snippets},
  author={Saifullah, C M Khaled and Asaduzzaman, Muhammad and Roy, Chanchal K.},
  booktitle={Proceedings of the 34th International Conference on Automated Software Engineering (ASE)},
  pages={243--254},
  year={2019}
}


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


