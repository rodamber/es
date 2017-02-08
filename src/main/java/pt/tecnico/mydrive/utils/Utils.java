package pt.tecnico.mydrive.utils;

import org.jdom2.Element;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;

public class Utils {
    public static String elementDefaultValue(Element element, String str, String byDefault) {
        if (element.getChild(str) == null)
            return byDefault;
        else
            return element.getChild(str).getValue();
    }

    public static long generateDateID() {
        DateTime dt = DateTime.now();
        return Long.parseLong(dt.toString("yyyyMMddHHmmss"));
    }

    public static String fixPath(String path) {
        String[] dirs = path.split("/");
        String absolutePath = "";
        boolean ignore = false;
        for(int i = dirs.length-1; i >= 0; i--) {
            if(ignore && !dirs[i].equals("..")) {
                ignore = false;
                continue;
            } else if(dirs[i].equals("..")) {
                ignore = true;
                continue;
            } else if(dirs[i].equals(".") || (ignore && dirs[i].equals(".."))) {
                continue;
            }
            absolutePath = "/" + dirs[i] + absolutePath;
        }
        if(absolutePath.charAt(0) == '/' && absolutePath.charAt(1) == '/') {
            return absolutePath.substring(1);
        } else {
            return absolutePath;
        }
    }

}
