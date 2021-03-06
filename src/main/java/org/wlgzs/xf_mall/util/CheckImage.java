package org.wlgzs.xf_mall.util;

/**
 * @author:胡亚星
 * @createTime 2018-06-03 14:19
 * @description:
 **/
public class CheckImage {

    public static boolean verifyImage(String fileName){
        String reg="(?i).+?\\.(jpg|gif|bmp|png|jpeg)";
        return fileName.matches(reg);
    }

    public static boolean verifyImages(String[] fileNames){
        String reg="(?i).+?\\.(jpg|gif|bmp|png|jpeg)";
        for (String fileName : fileNames) {
            if (!fileName.matches(reg)) {
                return false;
            }
        }
        return true;
    }
}
