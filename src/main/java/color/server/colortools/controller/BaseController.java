package color.server.colortools.controller;

import color.server.colortools.entity.ResInfo;

public abstract class BaseController {
    ResInfo resErr(int status, String msg) {
        ResInfo resInfo = new ResInfo(true, status, msg);
        return resInfo;
    }

    ResInfo resSuccess(Object msg) {
        ResInfo resInfo = new ResInfo(false, ResInfo.STATUS_SUCCESS, msg);
        return resInfo;
    }


    /**
     * 校验字符串是否满足要求
     *
     * @param lenList
     * @param strList
     * @return
     */
    public boolean strlenValidate(int[][] lenList, String... strList) {
        for (int i = 0; i < lenList.length; i++) {
            int[] currRange = lenList[i];
            if(strList[i] == null){
                return false;
            }
            int strLen = strList[i].length();
            if (strList[i] == null || strLen < currRange[0] || strLen > currRange[1]) {
                return false;
            }

        }
        return true;
    }
}

