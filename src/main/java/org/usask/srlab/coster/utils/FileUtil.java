package org.usask.srlab.coster.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import javafx.util.Pair;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;


import org.usask.srlab.coster.model.APIElement;


public class FileUtil {
    private static final FileUtil singleTonFileUtilInst = new FileUtil();

    private FileUtil() {
        super();
    }
    public static FileUtil getSingleTonFileUtilInst() {
        return singleTonFileUtilInst;
    }

    public synchronized String getFileContent(String fp) {
		String strResult = "";
		try {
			strResult = new String(Files.readAllBytes(Paths.get(fp)));
		} catch (Exception ex) {
			// ex.printStackTrace();
		}
		return strResult;

	}

	public synchronized ArrayList<String> getFileStringArray(String fp) {
		ArrayList<String> lstResults = new ArrayList<String>();
		try {
			try (BufferedReader br = new BufferedReader(new FileReader(fp))) {
				String line;
				while ((line = br.readLine()) != null) {
					// process the line.
					// strResult+=line+"\n";
					if (!line.trim().isEmpty()) {
						lstResults.add(line.trim());
					}
				}
			}
		} catch (Exception ex) {
			// ex.printStackTrace();
		}
		return lstResults;

	}

	public synchronized int countNumberOfLines(String fp) {
		int count = 0;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fp));
			String line;
			while ((line = br.readLine()) != null) {
				if (!line.trim().isEmpty())
					count++;
			}
			return count;
		} catch (IOException e) {
			return -1;
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
				}
		}
	}

	public synchronized void appendToFile(String fp, String line) {
		BufferedWriter bf = null;
		try {
			bf = new BufferedWriter(new FileWriter(new File(fp), true), 32768);
			bf.write(line);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (bf != null)
					bf.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public synchronized void appendLineToFile(String fp, String line) {
		BufferedWriter bf = null;
		try {
			bf = new BufferedWriter(new FileWriter(new File(fp), true));
			bf.write(line + "\n");

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (bf != null)
					bf.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
    public synchronized void appendToFile(String path, Collection<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (String l : lines)
            sb.append(l + "\n");
        appendToFile(path, sb.toString());
    }

	public synchronized void deleteFile(String fp) {
		try {
			File f = new File(fp);
			if (f.exists()) {
				f.delete();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public synchronized void writeToFile(String path, String content) {
		BufferedWriter bf = null;
		try {
			bf = new BufferedWriter(new FileWriter(new File(path), false));
			bf.write(content);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (bf != null)
					bf.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public synchronized void writeToFile(String path, Collection<String> lines) {
		StringBuilder sb = new StringBuilder();
		for (String l : lines)
			sb.append(l + "\n");
		writeToFile(path, sb.toString());
	}

	public synchronized ArrayList<File> getFiles(File file) {
		ArrayList<File> files = new ArrayList<>();
		if (file.isDirectory())
			for (File sub : file.listFiles())
				files.addAll(getFiles(sub));
		else  if (file.isFile()){
			files.add(file);
		}
		return files;
	}

	public synchronized void writeObjectToFile(Object object, String objectFile, boolean append) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(objectFile, append)));
			out.writeObject(object);
			out.flush();
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public synchronized Object readObjectFromFile(String objectFile) {
		try {
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(objectFile)));
			Object object = in.readObject();
			in.close();
			return object;
		}
		catch (Exception e) {
			//e.printStackTrace();
			return null;
		}
	}

	public synchronized void writeCOSTERProjectData(String path, Collection<APIElement> apiElements){
        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(path));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader("File Path", "Line Number", "API Element", "Local Context", "Global Context", "Combined Context", "FQN"));) {
            for(APIElement eachAPI:apiElements){

                csvPrinter.printRecord(eachAPI.getFileName().replaceAll(",",""),
                        eachAPI.getLineNumber()+"",
                        eachAPI.getName().replaceAll(",",""),
                        StringUtils.join(eachAPI.getLocalContext()," ").replaceAll(",",""),
                        StringUtils.join(eachAPI.getGlobalContext()," ").replaceAll(",",""),
                        StringUtils.join(eachAPI.getContext()," ").replaceAll(",",""),
                        eachAPI.getActualFQN().replaceAll(",",""));
            }
            csvPrinter.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized ArrayList<Pair<String,String>> readCSVFiles(File projectDatasetFile){
        ArrayList<Pair<String,String>> filecontent = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(Paths.get(projectDatasetFile.getAbsolutePath()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim());) {
            for (CSVRecord csvRecord : csvParser) {
                filecontent.add(new Pair<>(csvRecord.get("Combined Context"),csvRecord.get("FQN")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filecontent;
    }

	public synchronized ArrayList<APIElement> readTestCase(File file){
		ArrayList<APIElement> filecontent = new ArrayList<>();
		try (Reader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()));
			 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
					 .withFirstRecordAsHeader()
					 .withIgnoreHeaderCase()
					 .withTrim());) {
			for (CSVRecord csvRecord : csvParser) {
				String lc = csvRecord.get("Local Context");
				List<String> localContext = Arrays.asList(lc.trim().split(" "));
				String gc = csvRecord.get("Global Context");
				List<String> globalContext = Arrays.asList(gc.trim().split(" "));
				String cc = csvRecord.get("Combined Context");
				List<String> combinedContext = Arrays.asList(cc.trim().split(" "));

				APIElement apiElement = new APIElement(csvRecord.get("API Element"),csvRecord.get("File Path"),Integer.parseInt(csvRecord.get("Line Number")),localContext,globalContext,combinedContext,csvRecord.get("FQN"));
				filecontent.add(apiElement);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filecontent;
	}

	public synchronized HashMap<String, String> getFilesContentInDirectory(String fp) {
		ArrayList<File> allFiles = getFiles(new File(fp));
		HashMap<String, String> contents = new HashMap<>();
		for(File eachFile:allFiles)
			contents.putAll(getDictonaryStringMap(eachFile.getAbsolutePath()));
		return contents;
    }

	private synchronized HashMap<String,String> getDictonaryStringMap(String fp) {
		HashMap<String, String> lstResults = new HashMap<>();
		try {
			try (BufferedReader br = new BufferedReader(new FileReader(fp))) {
				String line;
				while ((line = br.readLine()) != null) {
					if(!line.trim().isEmpty()) {
						String[] tokens = line.trim().split("\\.");
						String lstToken = tokens[tokens.length-1];
						lstResults.put(line.trim(),lstToken);
					}
				}
			}
		} catch (Exception ex) {
			// ex.printStackTrace();
		}
		return lstResults;

	}

}
