package com.metait.javafxgrapvizsubpics.tasks;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

/**
 * This class is generating a dot main file from a (main) xsd file. It run this task as java process.
 */
public class XsdFileIntoDotFile extends AbstractBaseService {
    private String baseCmd = "java -cp ./lib/log4j-1.2.15.jar:./lib/xsom.jar:./lib/relaxngDatatype.jar -jar ./XmlSchemaToGraph.jar -o ";
    private String xsdFilePath = null;
    public void setXsdPath(String outputDir, String strPath) {
        xsdFilePath = strPath;
        if (xsdFilePath != null && xsdFilePath.trim().length()>0) {
            File currentDir = new File(".");
            // java -jar XmlSchemaToGraph.jar -o ${absolutputdir}${getSeparator()}${outfilename} file:${absolutputdir}/${xsdfile}`
            String fileUrl = xsdFilePath;
            URI uri = null;
//            try {
                File outputFile = new File(xsdFilePath.replace(".xsd",".dot"));
                uri = Path.of(xsdFilePath).toUri();
                fileUrl = uri.toString();
                String strOutputFile = outputFile.getAbsolutePath();
                // fileUrl = ("file:///" + xsdFilePath).to
                setExecutionData(currentDir.getAbsolutePath(), baseCmd +" " +strOutputFile + " " + fileUrl);
  //          }catch (Exception e){
    //            e.printStackTrace();
      //      }
        }
    }

    public XsdFileIntoDotFile() {
        super();
    }
}
