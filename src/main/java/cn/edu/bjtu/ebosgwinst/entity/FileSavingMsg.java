package cn.edu.bjtu.ebosgwinst.entity;

public class FileSavingMsg {
    private String FileName;
    private String msg;
    private boolean success;

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        this.FileName = fileName;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public FileSavingMsg(String fileName, String msg, boolean success) {
        FileName = fileName;
        this.msg = msg;
        this.success = success;
    }
}
