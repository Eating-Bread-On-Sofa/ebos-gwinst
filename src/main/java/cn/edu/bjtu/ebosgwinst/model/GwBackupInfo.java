package cn.edu.bjtu.ebosgwinst.model;

import com.alibaba.fastjson.JSONArray;

public class GwBackupInfo {
    private JSONArray command;
    private JSONArray edgeXDevice;
    private JSONArray edgeXProfile;
    private JSONArray edgeXService;
    private JSONArray edgeXExport;

    public JSONArray getCommand() {
        return command;
    }

<<<<<<< HEAD
    public void setCommand(JSONArray command) { this.command = command; }
=======
    public void setCommand(JSONArray command) {
        this.command = command;
    }
>>>>>>> 9ad2d6641e6e197fd89cfe25a2f1fb25345e4258

    public JSONArray getEdgeXDevice() {
        return edgeXDevice;
    }

    public void setEdgeXDevice(JSONArray edgeXDevice) {
        this.edgeXDevice = edgeXDevice;
    }

    public JSONArray getEdgeXProfile() {
        return edgeXProfile;
    }

    public void setEdgeXProfile(JSONArray edgeXProfile) {
        this.edgeXProfile = edgeXProfile;
    }

    public JSONArray getEdgeXService() {
        return edgeXService;
    }

    public void setEdgeXService(JSONArray edgeXService) {
        this.edgeXService = edgeXService;
    }

    public JSONArray getEdgeXExport() {
        return edgeXExport;
    }

    public void setEdgeXExport(JSONArray edgeXExport) {
        this.edgeXExport = edgeXExport;
    }
}
