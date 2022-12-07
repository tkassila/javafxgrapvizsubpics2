package com.metait.javafxgrapvizsubpics.tasks;

import java.io.File;

    /**
    * This class is generating dot files from a main dot file. It run this task as java process.
    */
    public class GenerateDotFilesFromOneDotfile extends AbstractBaseService {
        private String baseCmd = "java -cp ./javafxsubgraphviz.jar:./groovy-4.0.6.jar com.metait.javafxsubgraphviz.GenerateDotFilesFromOneDotFile ";

        public void setParams(String inputDotfile, String outputDotDir, String deepLevel) {
            if (inputDotfile != null && inputDotfile.trim().length()>0) {
                // `... com.metait.javafxsubgraphviz.GenerateDotFilesFromOneDotFile ${inputdotfile} ${outputdotdir} deeplevel=${deeplevel}`;
                String strExec = baseCmd +" " +inputDotfile +" " +outputDotDir +" deeplevel=" +deepLevel;
                setExecutionData(new File(".").getAbsolutePath(), strExec);
            }
        }

        public GenerateDotFilesFromOneDotfile() {
            super();
        }
    }

