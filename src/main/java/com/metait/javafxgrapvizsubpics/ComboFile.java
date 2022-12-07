package com.metait.javafxgrapvizsubpics;

import java.io.File;

public class ComboFile {
    private File file = null;

    public ComboFile(File p_file){
        file = p_file;
    }
    public File getFile() {
        return file;
    }
    public String toString() { return file.getName(); }

}
