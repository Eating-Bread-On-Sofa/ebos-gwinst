package cn.edu.bjtu.ebosgwinst.entity;


public class GwServState {
    private Boolean command;
    private Boolean gatewayInstance;
    private Boolean edgexCoreData;
    private Boolean edgexCoreMetadata;
    private Boolean edgexCoreCommand;

    public GwServState() {
        this.command = false;
        this.gatewayInstance = true;
        this.edgexCoreData = false;
        this.edgexCoreMetadata = false;
        this.edgexCoreCommand = false;
    }

    public Boolean getCommand() {
        return command;
    }

    public void setCommand(Boolean command) {
        this.command = command;
    }

    public Boolean getGatewayInstance() {
        return gatewayInstance;
    }

    public void setGatewayInstance(Boolean gatewayInstance) {
        this.gatewayInstance = gatewayInstance;
    }

    public Boolean getEdgexCoreData() {
        return edgexCoreData;
    }

    public void setEdgexCoreData(Boolean edgexCoreData) {
        this.edgexCoreData = edgexCoreData;
    }

    public Boolean getEdgexCoreMetadata() {
        return edgexCoreMetadata;
    }

    public void setEdgexCoreMetadata(Boolean edgexCoreMetadata) {
        this.edgexCoreMetadata = edgexCoreMetadata;
    }

    public Boolean getEdgexCoreCommand() {
        return edgexCoreCommand;
    }

    public void setEdgexCoreCommand(Boolean edgexCoreCommand) {
        this.edgexCoreCommand = edgexCoreCommand;
    }
}
