package cn.edu.bjtu.ebosgwinst.entity;

public class FileDescriptor {
    private String name;
    private String extension;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public FileDescriptor(String name, String extension) {
        this.name = name;
        this.extension = extension;
    }
}
