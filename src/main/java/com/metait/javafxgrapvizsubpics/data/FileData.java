package com.metait.javafxgrapvizsubpics.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FileData {

    private String fileName, visualLabel, label, strCreateTime, strModTime;
    private boolean isDirectory, isXsdFile, isDotFile, isJsonFile, isPngFile;
    private String msg = null;

    public FileData(String p_msg) {
        msg = p_msg;
    }

    public String toString() {
        if (msg != null)
            return msg;
        else
            return visualLabel;
    }

    public FileData(String p_fileName, String p_visualLabel, String p_label, boolean isDirectory, String strCreateTime, String strModTime) {
            this.fileName = p_fileName;
            this.visualLabel = p_visualLabel;
            this.label = p_label;
            this.isDirectory = isDirectory;
            this.strCreateTime  = strCreateTime;
            this.strModTime  = strModTime;
            this.isXsdFile = this.hasFileExtension(".xsd");
            this.isDotFile = this.hasFileExtension(".dot");
            this.isJsonFile = this.hasFileExtension(".json");
            this.isPngFile = this.hasFileExtension(".png");
    }

    public String getMsg()
    {
        return msg;
    }

    public static Calendar getCalendar(String strTime) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
        Date dateOld = sdf.parse(strTime);
        Calendar calendarOld = Calendar.getInstance();
        calendarOld.setTime(dateOld);
        return calendarOld;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getLabel() {
        return this.label;
    }

    public String getVisualLabel() {
        return this.visualLabel;
    }

    public boolean isDirectory() {
            return this.isDirectory;
        }

        public boolean isXsdFile() {
            return this.isXsdFile;
        }

        public boolean isDotFile() {
            return this.isDotFile;
        }

        public boolean isJsonFile() {
            return this.isJsonFile;
        }

        public boolean isPngFile() {
            return this.isPngFile;
        }

        private boolean hasFileExtension(String strExt)
        {
            boolean ret = false;
            final String lowerFN = this.fileName.toLowerCase();
            int ind = lowerFN.indexOf(strExt);
            if (ind > -1 && lowerFN.endsWith(strExt))
            {
                return true;
            }
            return ret;
        }
}
