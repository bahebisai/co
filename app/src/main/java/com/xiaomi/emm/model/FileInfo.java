package com.xiaomi.emm.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/5.
 */
public class FileInfo  implements Serializable {
    String file_name;
    String file_size;
    String file_time;
    String file_path;
    public void setFileName(String file_name) {
        this.file_name = file_name;
    }

    public String getFileName() {
        return file_name;
    }

    public void setFileSize(String file_size) {
        this.file_size = file_size;
    }

    public String getFileSize() {
        return file_size;
    }

    public void setFileTime(String file_time) {
        this.file_time = file_time;
    }

    public String getFileTime() {
        return file_time;
    }

    public void setFilePath(String file_path) {
        this.file_path = file_path;
    }

    public String getFilePath() {
        return file_path;
    }
}
